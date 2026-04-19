# CamerGo Driver Dispatch Service

This microservice handles the dispatching of drivers to ride requests in the CamerGo ride-hailing platform. It follows Hexagonal Architecture and is built with Spring Boot 3.5, Java 21, Redis, Kafka, and H3 geospatial indexing.

## Features

- Receives ride requested events via Kafka
- Finds nearby available drivers using H3 hexagonal grid
- Ranks drivers based on distance, rating, acceptance rate, and direction
- Sends ride requests to drivers via WebSocket
- Handles driver accept/reject events
- Publishes ride assignment events

## Architecture

The service is structured into layers:
- **Domain**: Core business logic (entities, value objects, domain services, ports)
- **Application**: Use case orchestration, DTOs
- **Infrastructure**: Adapters for Redis, Kafka, WebSocket
- **Interfaces**: REST controllers, mappers

## Technology Stack

- Java 21
- Spring Boot 3.5
- Spring Data Redis
- Spring Kafka
- H3 (Uber's hexagonal grid)
- Testcontainers for integration tests

## Configuration

Key properties in `application.yml`:

```yaml
dispatch:
  h3.resolution: 9               # H3 cell resolution
  max-search-radius: 5            # Maximum k-ring radius
  driver-timeout: 10000           # Timeout for driver response (ms)