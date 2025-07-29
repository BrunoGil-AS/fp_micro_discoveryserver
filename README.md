# Discovery Server

The Discovery Server is a critical infrastructure microservice that implements Netflix Eureka Server for service discovery in the FP microservices ecosystem. It acts as a registry where all microservices register themselves and discover other services, enabling dynamic service location and load balancing without hard-coded endpoints.

This service is essential for the microservices architecture and must be the first service to start, as all other services depend on it for registration and discovery.

## Table of Contents

- [Main Technologies](#main-technologies)
- [Key Components](#key-components)
  - [Eureka Server Configuration](#1-eureka-server-configuration)
  - [Application Structure](#2-application-structure)
  - [Configuration Management](#3-configuration-management)
- [Architecture Role](#architecture-role)
  - [Service Discovery Pattern](#service-discovery-pattern)
  - [Registration and Health Checks](#registration-and-health-checks)
  - [Load Balancing Support](#load-balancing-support)
- [Development Configuration](#development-configuration)
  - [Prerequisites](#prerequisites)
  - [Environment Setup](#environment-setup)
  - [Configuration Properties](#configuration-properties)
- [Service Registration](#service-registration)
  - [Client Registration](#client-registration)
  - [Service Health Monitoring](#service-health-monitoring)
  - [Instance Management](#instance-management)
- [Web Dashboard](#web-dashboard)
  - [Eureka Console Access](#eureka-console-access)
  - [Service Monitoring](#service-monitoring)
  - [Instance Status](#instance-status)

---

## Main Technologies

- **Spring Boot 3.3.12** – Core framework
- **Spring Cloud 2023.0.5** – Cloud-native patterns
- **Netflix Eureka Server** – Service discovery implementation
- **Spring Cloud Config Client** – Centralized configuration
- **Logback** – Comprehensive logging system
- **Maven** – Build and dependency management

## Key Components

### 1. Eureka Server Configuration

**[FpMicroDiscoveryserverApplication.java](src/main/java/com/aspiresys/fp_micro_discoveryserver/FpMicroDiscoveryserverApplication.java)** – Main application with Eureka Server enabled

```java
@SpringBootApplication
@EnableEurekaServer
public class FpMicroDiscoveryserverApplication {
    public static void main(String[] args) {
        SpringApplication.run(FpMicroDiscoveryserverApplication.class, args);
    }
}
```

- **@EnableEurekaServer** enables Netflix Eureka Server functionality
- **Service Registry** for microservice registration and discovery
- **Health Check Monitoring** for registered services
- **Load Balancing Support** through service instance management

### 2. Application Structure

**[ServletInitializer.java](src/main/java/com/aspiresys/fp_micro_discoveryserver/ServletInitializer.java)** – WAR deployment support

```java
public class ServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(FpMicroDiscoveryserverApplication.class);
    }
}
```

- **WAR Packaging** support for traditional application servers
- **Servlet Container** integration for enterprise deployments
- **Production Deployment** flexibility

### 3. Configuration Management

The Discovery Server uses a multi-layered configuration approach:

- **Local Configuration** (application.properties)
- **Environment-specific** (application-dev.properties)
- **Centralized Configuration** via Config Server
- **External Properties** for production deployment

## Architecture Role

### Service Discovery Pattern

The Discovery Server implements the **Service Discovery Pattern** in microservices architecture:

```text
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Auth Service  │    │ Product Service │    │  Order Service  │
│   Port: 8081    │    │   Port: 9002    │    │   Port: 9003    │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          │ Register             │ Register             │ Register
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼───────────┐
                    │   Discovery Server      │
                    │   Eureka Server         │
                    │   Port: 8761           │
                    └─────────────────────────┘
                                 │
                    ┌─────────────▼───────────┐
                    │      Gateway           │
                    │   Route Resolution     │
                    │   Port: 8080          │
                    └─────────────────────────┘
```

### Registration and Health Checks

- **Service Registration**: Automatic registration of microservice instances
- **Health Monitoring**: Periodic health checks for registered services
- **Instance Management**: Automatic removal of unhealthy instances
- **Metadata Support**: Service metadata for routing and configuration

### Load Balancing Support

- **Client-side Load Balancing**: Service instance information for load balancers
- **Round-Robin Distribution**: Default load balancing strategy
- **Failover Support**: Automatic failover to healthy instances
- **Zone Awareness**: Support for multi-zone deployments

## Development Configuration

### Prerequisites

1. **Java 17+**
2. **Maven 3.6+**
3. **Config Server** (fp_micro_configserver) running (optional but recommended)
4. **Port 8761** available for Eureka Server

### Environment Setup

The Discovery Server is designed to be the **first service** to start in the ecosystem:

```bash
# Start order for FP microservices:
1. Config Server (optional - for centralized config)
2. Discovery Server (this service) - Port 8761
3. Auth Service - Port 8081
4. Gateway - Port 8080
5. Business Services (Product, Order, User) - Ports 9002, 9003, 9004
```

### Configuration Properties

**Development Configuration** (application-dev.properties):

```properties
# Eureka Server Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false

# Config Server Integration
spring.config.import=optional:configserver:http://localhost:8888
```

**Production Configuration** (from Config Server):

```properties
# Server Configuration
server.port=8761
spring.application.name=discovery-service

# Eureka Server Settings
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.server.enable-self-preservation=false

# Performance Tuning
eureka.server.eviction-interval-timer-in-ms=5000
eureka.server.renewal-percent-threshold=0.85
```

## Service Registration

### Client Registration

Other services register with the Discovery Server using Eureka Client:

```java
// Example from other microservices
@SpringBootApplication
@EnableEurekaClient  // or @EnableDiscoveryClient
public class MicroserviceApplication {
    // Service automatically registers with Discovery Server
}
```

**Client Configuration** (in other services):

```properties
# Eureka Client Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true

# Instance Configuration
eureka.instance.prefer-ip-address=true
eureka.instance.lease-renewal-interval-in-seconds=10
eureka.instance.lease-expiration-duration-in-seconds=30
```

### Service Health Monitoring

The Discovery Server monitors service health through:

- **Heartbeat Mechanism**: Services send periodic heartbeats (default: 30 seconds)
- **Health Check Endpoints**: Integration with Spring Boot Actuator
- **Instance Status**: UP, DOWN, OUT_OF_SERVICE, UNKNOWN
- **Automatic Deregistration**: Removal of failed instances

### Instance Management

```text
Service Lifecycle in Discovery Server:
1. Service Startup → Registration Request
2. Registration → Instance Added to Registry
3. Heartbeat → Instance Status: UP
4. Health Check → Continuous Monitoring
5. Service Shutdown → Graceful Deregistration
6. Heartbeat Failure → Instance Status: DOWN
7. Expiration → Instance Removed from Registry
```

## Web Dashboard

### Eureka Console Access

The Discovery Server provides a web-based dashboard:

```text
URL: http://localhost:8761
```

**Dashboard Features:**

- **Registered Services**: List of all registered microservices
- **Instance Information**: Service instances with health status
- **System Status**: Eureka server health and statistics
- **Configuration**: Server configuration details

### Service Monitoring

Dashboard sections include:

- **System Status**: Server uptime, environment, data center
- **DS Replicas**: Discovery Server replicas (for HA setups)
- **Instances Currently Registered**: Active service instances
- **General Info**: Memory usage, instance statistics
- **Instance Info**: Detailed instance metadata

### Instance Status

Instance status indicators:

- **🟢 UP**: Service is healthy and available
- **🔴 DOWN**: Service is not responding to health checks
- **🟡 OUT_OF_SERVICE**: Service is temporarily unavailable
- **⚪ UNKNOWN**: Status cannot be determined

---
