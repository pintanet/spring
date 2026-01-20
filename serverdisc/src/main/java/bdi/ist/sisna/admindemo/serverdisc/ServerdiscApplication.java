package bdi.ist.sisna.admindemo.serverdisc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

@SpringBootApplication
@EnableDiscoveryClient
public class ServerdiscApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ServerdiscApplication.class, args);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Configuration
    @EnableEurekaServer
    @ConditionalOnProperty(name = "eureka.server.enabled", havingValue = "true", matchIfMissing = true)
    public static class EurekaServerConfig { }

    @Configuration
    @EnableAdminServer
    @ConditionalOnProperty(name = "spring.boot.admin.server.enabled", havingValue = "true", matchIfMissing = true)
    public static class AdminServerConfig { }
}