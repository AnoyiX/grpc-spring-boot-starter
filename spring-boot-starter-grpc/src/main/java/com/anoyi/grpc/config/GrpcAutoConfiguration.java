package com.anoyi.grpc.config;

import com.anoyi.grpc.GrpcClient;
import com.anoyi.grpc.GrpcServerRunner;
import com.anoyi.grpc.annotation.GrpcService;
import com.anoyi.grpc.annotation.GrpcServiceScan;
import com.anoyi.grpc.binding.GrpcServiceProxy;
import com.anoyi.grpc.service.CommonService;
import com.anoyi.grpc.util.ClassNameUtils;
import io.grpc.ServerBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
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

import java.util.*;
import java.util.logging.Logger;

@Configuration
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcAutoConfiguration {

    private static final Logger log = Logger.getLogger(GrpcAutoConfiguration.class.getName());

    private final AbstractApplicationContext applicationContext;

    private final GrpcProperties grpcProperties;

    public GrpcAutoConfiguration(AbstractApplicationContext applicationContext, GrpcProperties grpcProperties) {
        this.applicationContext = applicationContext;
        this.grpcProperties = grpcProperties;
    }

    @Bean
    public CommonService commonService() {
        return new CommonService(applicationContext);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.grpc.enable", havingValue = "true")
    public GrpcServerRunner grpcServerRunner() {
        return new GrpcServerRunner(ServerBuilder.forPort(grpcProperties.getPort()), commonService());
    }

    @Bean
    public GrpcClient grpcClient() {
        return new GrpcClient(grpcProperties);
    }

    /**
     * 扫描 @GrpcService 注解的接口，生成动态代理类，注入的 Spring 容器
     */
    public static class AutoConfiguredGrpcServiceScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

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

            for (BeanDefinition beanDefinition : beanDefinitions) {
                String className = beanDefinition.getBeanClassName();
                if (StringUtils.isEmpty(className)) {
                    continue;
                }
                try {
                    // 创建代理类
                    Class<?> target = Class.forName(className);
                    InvocationHandler invocationHandler = new GrpcServiceProxy<>(target);
                    Object proxy = Proxy.newProxyInstance(GrpcService.class.getClassLoader(), new Class[]{target}, invocationHandler);

                    // 注册到 Spring 容器
                    String beanName = ClassNameUtils.beanName(className);
                    ((DefaultListableBeanFactory) this.beanFactory).registerSingleton(beanName, proxy);
                } catch (ClassNotFoundException e) {
                    log.warning("class not found : " + className);
                }
            }
        }

        /**
         * 包扫描
         */
        private Set<BeanDefinition> scanPackages(AnnotationMetadata importingClassMetadata, ClassPathBeanDefinitionScanner scanner) {
            List<String> packages = AutoConfigurationPackages.get(beanFactory);
            Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(GrpcServiceScan.class.getCanonicalName());
            if (annotationAttributes != null) {
                String[] basePackages = (String[]) annotationAttributes.get("basePackages");
                if (basePackages.length > 0){
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

        protected class ClassPathGrpcServiceScanner extends ClassPathBeanDefinitionScanner {

            ClassPathGrpcServiceScanner(BeanDefinitionRegistry registry) {
                super(registry, false);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
            }

        }

    }

}