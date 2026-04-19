# Documentation Aggregator

The `documentations-aggregator` service centralizes the OpenAPI documentation of TicketFlow - Boaz SI microservices and provides a single entry point for consulting technical references.

## Objectives

- aggregate OpenAPI specifications from multiple microservices;
- provide a unified interface via Swagger UI and Scalar;
- standardize access to documentation in local, dev, and production environments;
- facilitate dynamic service discovery using Eureka.

## Available Interfaces

| Interface | URL | Usage |
| --- | --- | --- |
| Swagger UI | `/docs`, `/swagger-ui.html`, `/documentation/swagger-ui/index.html` | Multi-service browsing with Swagger UI configuration |
| Scalar | `/documentation/scalar` | Modern navigation between aggregated specifications |
| Service OpenAPI | `/api/v3/api-docs` | OpenAPI specification of the `documentation-aggregator` service |
| Raw Markdown Guide | `/documentation/guide.md` | Text version of this documentation |

## Main Endpoints

### `GET /documentation/services`

Returns the list of available services for aggregation.

### `GET /documentation/api-docs/{serviceId}`

Retrieves the OpenAPI specification of a target service and rewrites the `servers` section to route calls through the documentation gateway.

### `GET /documentation/swagger-config`

Dynamically builds the Swagger UI configuration with all available sources.

### `GET /documentation/scalar/sources`

Dynamically builds the list of sources displayed in Scalar.

### `GET /documentation/guide.md`

Exposes this technical guide in Markdown format.

## How It Works

1. The service retrieves available services via Eureka when `eureka.client.enabled=true`.
2. In static mode, it can also use the `aggregator.services` configuration.
3. Each remote specification is fetched from `aggregator.dynamic.api-docs-path`.
4. Excluded services are filtered via `documentation.excluded-services`.
5. The `servers` URLs of retrieved specs are normalized to provide a consistent entry point.

## Useful Configuration

```yaml
aggregator:
  dynamic:
    api-docs-path: /api/v3/api-docs

documentation:
  excluded-services:
    - config-server
    - api-gateway
    - discovery-service

docs:
  enabled: true

springdoc:
  api-docs:
    path: /api/v3/api-docs
```

## Best Practices

- ensure each microservice publishes its OpenAPI on the configured path;
- exclude technical services that should not appear in the navigation;
- use /documentation/guide.md as a versioned source to enrich the functional documentation of the gateway;
- keep this page as the human entry point, and OpenAPI specs as the machine‑readable API contract.