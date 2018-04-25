package org.springframework.grpc.config;

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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.grpc.GrpcClient;
import org.springframework.grpc.GrpcServerRunner;
import org.springframework.grpc.annotation.ClassPathGrpcServiceScanner;
import org.springframework.grpc.annotation.GrpcService;
import org.springframework.grpc.binding.GrpcServiceProxy;
import org.springframework.grpc.service.CommonService;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Configuration
    @Import({AutoConfiguredGrpcServiceScannerRegistrar.class})
    public static class GrpcServiceScannerAutoConfiguration {

    }

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
            ClassPathGrpcServiceScanner scanner = new ClassPathGrpcServiceScanner(registry);
            try {
                if (this.resourceLoader != null) {
                    scanner.setResourceLoader(this.resourceLoader);
                }
                List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
                scanner.setAnnotationClass(GrpcService.class);
                scanner.registerFilters();
                Set<BeanDefinition> beanDefinitions = new HashSet<>();
                packages.forEach(pack ->
                    beanDefinitions.addAll(scanner.findCandidateComponents(pack))
                );

                for (BeanDefinition beanDefinition : beanDefinitions) {
                    if (beanDefinition instanceof AnnotatedBeanDefinition) {
                        String className = beanDefinition.getBeanClassName();
                        if (StringUtils.isEmpty(className)){
                            continue;
                        }
                        try {
                            Class<?> target = Class.forName(className);
                            InvocationHandler invocationHandler = new GrpcServiceProxy<>(target);
                            String[] path = className.split("\\.");
                            String beanName = path[path.length - 1];
                            beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
                            ((DefaultListableBeanFactory) this.beanFactory).registerSingleton(beanName, Proxy.newProxyInstance(GrpcService.class.getClassLoader(), new Class[]{target}, invocationHandler));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IllegalStateException ex) {
                log.warning("Could not determine auto-configuration package, automatic mapper scanning disabled.");
            }
        }
    }

}