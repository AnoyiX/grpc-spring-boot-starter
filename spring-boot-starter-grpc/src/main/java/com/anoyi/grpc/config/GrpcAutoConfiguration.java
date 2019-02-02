package com.anoyi.grpc.config;

import com.anoyi.grpc.GrpcClient;
import com.anoyi.grpc.GrpcServer;
import com.anoyi.grpc.annotation.GrpcService;
import com.anoyi.grpc.annotation.GrpcServiceScan;
import com.anoyi.grpc.binding.GrpcServiceProxy;
import com.anoyi.grpc.service.CommonService;
import com.anoyi.grpc.service.SerializeService;
import com.anoyi.grpc.service.impl.SofaHessianSerializeService;
import com.anoyi.grpc.util.ClassNameUtils;
import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Configuration
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcAutoConfiguration {

    private static final Attributes NAME_RESOLVER_PARAMS = Attributes.newBuilder().set(NameResolver.Factory.PARAMS_DEFAULT_PORT, GrpcUtil.DEFAULT_PORT_PLAINTEXT).build();

    private static final Pattern URI_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9+.-]*:/.*");

    private final AbstractApplicationContext applicationContext;

    private final GrpcProperties grpcProperties;

    public GrpcAutoConfiguration(AbstractApplicationContext applicationContext, GrpcProperties grpcProperties) {
        this.applicationContext = applicationContext;
        this.grpcProperties = grpcProperties;
    }

    /**
     * 全局 RPC 序列化/反序列化
     */
    @Bean
    @ConditionalOnMissingBean(SerializeService.class)
    public SerializeService serializeService() {
        return new SofaHessianSerializeService();
    }

    /**
     * PRC 服务调用
     */
    @Bean
    public CommonService commonService(SerializeService serializeService) {
        return new CommonService(applicationContext, serializeService);
    }

    /**
     * RPC 服务端
     */
    @Bean
    @ConditionalOnMissingBean(GrpcServer.class)
    @ConditionalOnProperty(value = "spring.grpc.enable", havingValue = "true")
    public GrpcServer grpcServer(CommonService commonService) throws Exception {
        GrpcServer server = new GrpcServer(grpcProperties, commonService);
        server.start();
        return server;
    }

    /**
     * RPC 客户端
     */
    @Bean
    @ConditionalOnMissingBean(GrpcClient.class)
    public GrpcClient grpcClient(SerializeService serializeService) {
        GrpcClient client = new GrpcClient(grpcProperties, serializeService);
        client.init();
        return client;
    }

    /**
     * NameResolver Refresher
     */
    @Bean
    @ConditionalOnBean(GrpcClient.class)
    @ConditionalOnProperty(value = "spring.grpc.enableNameResolverRefresh", havingValue = "true")
    public ScheduledExecutorService refreshDNSRecordService() {
        final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleAtFixedRate(() ->
                grpcProperties.getRemoteServers().forEach(remoteServer -> {
                    String target = remoteServer.getHost() + ":" + remoteServer.getPort();
                    NameResolver nameResolver = getNameResolver(NameResolverProvider.asFactory(), target);
                    nameResolver.refresh();
                }), grpcProperties.getNameResolverInitialDelay(), grpcProperties.getNameResolverInitialDelay(), TimeUnit.SECONDS);
        return scheduledExecutorService;
    }

    /**
     * 手动扫描 @GrpcService 注解的接口，生成动态代理类，注入到 Spring 容器
     */
    public static class ExternalGrpcServiceScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            ClassPathBeanDefinitionScanner scanner = new ClassPathGrpcServiceScanner(registry);
            scanner.setResourceLoader(this.resourceLoader);
            scanner.addIncludeFilter(new AnnotationTypeFilter(GrpcService.class));
            Set<BeanDefinition> beanDefinitions = scanPackages(importingClassMetadata, scanner);
            ProxyUtil.registerBeans(beanFactory, beanDefinitions);
        }

        /**
         * 包扫描
         */
        private Set<BeanDefinition> scanPackages(AnnotationMetadata importingClassMetadata, ClassPathBeanDefinitionScanner scanner) {
            List<String> packages = new ArrayList<>();
            Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(GrpcServiceScan.class.getCanonicalName());
            if (annotationAttributes != null) {
                String[] basePackages = (String[]) annotationAttributes.get("packages");
                if (basePackages.length > 0) {
                    packages.addAll(Arrays.asList(basePackages));
                }
            }
            Set<BeanDefinition> beanDefinitions = new HashSet<>();
            if (CollectionUtils.isEmpty(packages)) {
                return beanDefinitions;
            }
            packages.forEach(pack -> beanDefinitions.addAll(scanner.findCandidateComponents(pack)));
            return beanDefinitions;
        }

    }

    protected static class ClassPathGrpcServiceScanner extends ClassPathBeanDefinitionScanner {

        ClassPathGrpcServiceScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
        }

    }

    protected static class ProxyUtil {
        static void registerBeans(BeanFactory beanFactory, Set<BeanDefinition> beanDefinitions) {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                String className = beanDefinition.getBeanClassName();
                if (StringUtils.isEmpty(className)) {
                    continue;
                }
                try {
                    // 创建代理类
                    Class<?> target = Class.forName(className);
                    Object invoker = new Object();
                    InvocationHandler invocationHandler = new GrpcServiceProxy<>(target, invoker);
                    Object proxy = Proxy.newProxyInstance(GrpcService.class.getClassLoader(), new Class[]{target}, invocationHandler);

                    // 注册到 Spring 容器
                    String beanName = ClassNameUtils.beanName(className);
                    ((DefaultListableBeanFactory) beanFactory).registerSingleton(beanName, proxy);
                } catch (ClassNotFoundException e) {
                    log.warn("class not found : " + className);
                }
            }
        }
    }

    /**
     * 获取 NameResolver
     */
    private NameResolver getNameResolver(NameResolver.Factory nameResolverFactory, String target) {
        URI targetUri = null;
        StringBuilder uriSyntaxErrors = new StringBuilder();
        try {
            targetUri = new URI(target);
        } catch (URISyntaxException e) {
            uriSyntaxErrors.append(e.getMessage());
        }
        if (targetUri != null) {
            NameResolver resolver = nameResolverFactory.newNameResolver(targetUri, NAME_RESOLVER_PARAMS);
            if (resolver != null) {
                return resolver;
            }
        }
        if (!URI_PATTERN.matcher(target).matches()) {
            try {
                targetUri = new URI(nameResolverFactory.getDefaultScheme(), "", "/" + target, null);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
            NameResolver resolver = nameResolverFactory.newNameResolver(targetUri, NAME_RESOLVER_PARAMS);
            if (resolver != null) {
                return resolver;
            }
        }
        throw new IllegalArgumentException(String.format(
                "cannot find a NameResolver for %s%s",
                target, uriSyntaxErrors.length() > 0 ? " (" + uriSyntaxErrors + ")" : ""));
    }

}