package com.heima.admin.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  //开启注册中心
public class AdminGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminGatewayApplication.class,args);
    }
}