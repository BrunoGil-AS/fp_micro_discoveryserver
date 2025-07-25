package com.aspiresys.fp_micro_discoveryserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, 
                properties = {"spring.profiles.active=test"})
class FpMicroDiscoveryserverApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EurekaClientConfigBean eurekaClientConfig;

    @Autowired
    private EurekaInstanceConfigBean eurekaInstanceConfig;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Override any remaining config that might not be set in application-test.properties
        registry.add("spring.config.import", () -> "optional:configserver:");
    }

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
        assertTrue(port > 0, "Application should start with a valid port");
    }

    @Test
    void eurekaServerConfigurationIsCorrect() {
        assertNotNull(eurekaClientConfig);
        assertNotNull(eurekaInstanceConfig);
        assertFalse(eurekaClientConfig.shouldRegisterWithEureka(), "Server should not register with itself");
        assertFalse(eurekaClientConfig.shouldFetchRegistry(), "Server should not fetch registry");
    }

    @Test
    void applicationStartsSuccessfully() {
        assertNotNull(applicationContext.getBean(FpMicroDiscoveryserverApplication.class));
    }
}
