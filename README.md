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
- [Production Considerations](#production-considerations)
  - [High Availability Setup](#high-availability-setup)
  - [Security Configuration](#security-configuration)
  - [Performance Tuning](#performance-tuning)
- [Logging Configuration](#logging-configuration)
  - [Service Discovery Logs](#service-discovery-logs)
  - [Registration Events](#registration-events)
- [Running and Development](#running-and-development)
  - [Build and Run](#build-and-run)
  - [Health Checks](#health-checks)
  - [Troubleshooting](#troubleshooting)

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

## Production Considerations

### High Availability Setup

For production environments, consider multiple Discovery Server instances:

```properties
# Eureka Server Cluster Configuration
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://eureka-1:8761/eureka,http://eureka-2:8762/eureka

# Peer Awareness
eureka.server.enable-self-preservation=true
eureka.server.peer-eureka-nodes-update-interval-ms=10000
```

### Security Configuration

Production security considerations:

```properties
# Security (when enabled)
spring.security.user.name=admin
spring.security.user.password=${EUREKA_PASSWORD}
eureka.server.auth.enabled=true

# Network Security
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
```

### Performance Tuning

Optimize for production workloads:

```properties
# Response Cache
eureka.server.response-cache-update-interval-ms=30000
eureka.server.response-cache-auto-expiration-in-seconds=180

# Registry Fetch
eureka.client.registry-fetch-interval-seconds=30
eureka.client.instance-info-replication-interval-seconds=30

# Heartbeat and Expiration
eureka.instance.lease-renewal-interval-in-seconds=10
eureka.instance.lease-expiration-duration-in-seconds=30
```

## Logging Configuration

### Service Discovery Logs

**[logback-spring.xml](src/main/resources/logback-spring.xml)** – Comprehensive logging configuration

```xml
<configuration>
    <property name="LOG_PATH" value="./logs/discovery-server"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{36}] - %msg%n"/>

    <appender name="FILE_DISCOVERY" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/discovery-server.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/archived/discovery-server.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>40MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
</configuration>
```

### Registration Events

Log categories for monitoring:

- **Service Registration**: New service instances joining the registry
- **Service Deregistration**: Services leaving the registry
- **Health Check Events**: Service health status changes
- **Eureka Server Events**: Server startup, configuration changes
- **Network Events**: Communication issues, timeouts

**Example Log Output:**

```text
2025-07-24 10:30:15.123 [main] INFO [DISCOVERY-SERVER] [EurekaServerInitializerConfiguration] - Started Eureka Server
2025-07-24 10:30:45.456 [eureka-server] INFO [com.netflix.eureka.registry.AbstractInstanceRegistry] - Registered instance AUTH-SERVICE/192.168.1.100:auth-service:8081 with status UP
2025-07-24 10:31:00.789 [eureka-server] INFO [com.netflix.eureka.registry.AbstractInstanceRegistry] - Registered instance PRODUCT-SERVICE/192.168.1.100:product-service:9002 with status UP
```

## Running and Development

### Build and Run

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Start Discovery Server
mvn spring-boot:run

# Alternative: Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Health Checks

Monitor Discovery Server health:

```http
# Application health
GET http://localhost:8761/actuator/health

# Eureka specific endpoints
GET http://localhost:8761/eureka/apps
GET http://localhost:8761/eureka/status
```

**Response Example:**

```json
{
  "status": "UP",
  "components": {
    "eureka": {
      "status": "UP",
      "details": {
        "applications": 3
      }
    }
  }
}
```

### Troubleshooting

Common issues and solutions:

**1. Port Already in Use:**

```bash
# Check if port 8761 is in use
netstat -an | findstr 8761

# Kill process using the port (Windows)
taskkill /F /PID <process_id>
```

**2. Services Not Registering:**

- Verify Discovery Server is running first
- Check client configuration: `eureka.client.service-url.defaultZone`
- Ensure network connectivity between services
- Check firewall settings for port 8761

**3. Services Showing as DOWN:**

- Verify service health endpoints are accessible
- Check heartbeat configuration
- Review network latency and timeouts
- Examine service logs for health check failures

**4. Config Server Connection Issues:**

```properties
# Make config server optional for startup
spring.config.import=optional:configserver:http://localhost:8888
```

**Startup Verification:**

1. **Discovery Server Console**: <http://localhost:8761>
2. **Registered Services**: Should see services appearing as they start
3. **Health Status**: All services should show UP status
4. **Log Output**: No ERROR messages in discovery-server.log

---

**Next Steps:**

1. Start Config Server (if using centralized configuration)
2. Start Discovery Server (this service)
3. Start Auth Service
4. Start Gateway
5. Start business microservices (Product, Order, User)

The Discovery Server is the backbone of the FP microservices architecture, providing essential service registry and discovery capabilities for a robust, scalable microservices ecosystem.
