# Otoroshi Swagger UI Plugin

Need to display OpenAPI/Swagger documentation for your APIs behind Otoroshi? This plugin transforms a route into a beautiful interactive documentation page. No backend to configure, just a URL to your spec.

## Tech Stack

- Otoroshi 17.8
- Scala 2.12 with SBT
- Swagger UI 5.30 (CDN)

## Quick Start

### Build

```bash
sbt clean compile package
# → target/scala-2.12/otoroshi-swagger-ui-plugin_2.12-0.1.0.jar
```

### Check for dependency updates

```bash
sbt dependencyUpdates
```

### Running Otoroshi

**Local**:
```bash
java -cp "otoroshi.jar:target/scala-2.12/otoroshi-swagger-ui-plugin_2.12-0.1.0.jar" play.core.server.ProdServerStart
```

**With Docker**:
```bash
docker run -p 8080:8080 -v $(pwd)/target/scala-2.12:/opt/plugins \
  -e OTOROSHI_INITIAL_ADMIN_LOGIN=admin -e OTOROSHI_INITIAL_ADMIN_PASSWORD=password \
  --entrypoint /bin/sh maif/otoroshi:latest \
  -c 'java -Dhttp.port=8080 -cp "/usr/app/otoroshi.jar:/opt/plugins/*" play.core.server.ProdServerStart'
```

Default credentials: `admin` / `password`
Access: http://otoroshi.oto.tools:8080 (add `127.0.0.1 otoroshi.oto.tools` to `/etc/hosts`)

### Configuration

In Otoroshi UI:
1. Create a route (e.g. `/docs`)
2. Add the **"Swagger UI Plugin"**
3. Configure:
   - **swagger_url**: URL to your OpenAPI spec (JSON or YAML)
   - **title**: Browser tab title

```json
{
  "swagger_url": "https://api.example.com/openapi.json",
  "title": "My API Docs"
}
```

## How it works

Inspired by Otoroshi's **StaticResponse** plugin:
- Extends `NgBackendCall`
- Intercepts requests at `CallBackend` step
- Returns HTML directly with Swagger UI (no backend)
- Swagger UI 5.30.2 loaded from CDN

| | StaticResponse | SwaggerUI Plugin |
|---|---|---|
| Content | Custom body | Fixed HTML (Swagger UI) |
| Config | body, status, headers | swagger_url, title |
| Usage | Mock/test | API documentation |

## License

Apache License 2.0 - Copyright 2025 davlgd
