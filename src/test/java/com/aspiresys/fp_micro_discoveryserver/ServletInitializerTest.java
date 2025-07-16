package com.aspiresys.fp_micro_discoveryserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import static org.junit.jupiter.api.Assertions.*;

class ServletInitializerTest {

    @Test
    void configure() {
        ServletInitializer initializer = new ServletInitializer();
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        
        SpringApplicationBuilder result = initializer.configure(builder);
        
        assertNotNull(result, "Should return a configured SpringApplicationBuilder");
    }
}
