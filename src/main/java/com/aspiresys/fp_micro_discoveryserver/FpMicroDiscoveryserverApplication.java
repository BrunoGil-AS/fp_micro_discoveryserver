package com.aspiresys.fp_micro_discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class FpMicroDiscoveryserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(FpMicroDiscoveryserverApplication.class, args);
	}

}
