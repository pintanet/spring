package bdi.ist.sisna.admindemo.clientdisc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient 
public class ClientdiscApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientdiscApplication.class, args);
    }
}