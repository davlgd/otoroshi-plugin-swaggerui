# Otoroshi Swagger UI Plugin

Need to display OpenAPI/Swagger documentation for your APIs through Otoroshi? This plugin transforms a route into a beautiful interactive documentation page. No backend to configure, just set an URL to your spec.

## Tech Stack

- Otoroshi 17.8
- Scala 2.12 with SBT
- Swagger UI 5.30 (CDN)
- Swagger UI Themes 3.0 (CDN)

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
./test-local.sh
```

Default credentials: `admin` / `password`
Access: http://otoroshi.oto.tools:8080 (add `127.0.0.1 otoroshi.oto.tools` to `/etc/hosts`)

### Configuration

In Otoroshi UI:
1. Create a route (e.g. `/docs`)
2. Add the **"Swagger UI Plugin"**
3. Configure (all optional except `swagger_url` and `title`):

**Required:**
- **swagger_url**: URL to your OpenAPI spec (JSON or YAML)
- **title**: Browser tab title

**Optional UI Customization:**
- **swagger_ui_version**: Swagger UI version (default: `5.30.2`)
- **filter**: Enable search/filter bar (default: `true`)
- **show_models**: Show model schemas (default: `false`)
- **display_operation_id**: Show operation IDs (default: `false`)
- **show_extensions**: Show vendor extensions (x-*) (default: `false`)
- **layout**: UI layout (`BaseLayout` or `StandaloneLayout`, default: `BaseLayout`)
- **sort_tags**: Sort tags (`alpha` or `none`, default: `alpha`)
- **sort_ops**: Sort operations (`alpha`, `method`, or `none`, default: `alpha`)
- **theme**: Optional swagger-ui-themes (`default`, `feeling-blue`, `flattop`, `material`, `monokai`, `muted`, `newspaper`, or `outline`, default: `default`)

**Minimal configuration:**
```json
{
  "swagger_url": "https://api.example.com/openapi.json",
  "title": "My API Docs"
}
```

**Full configuration example:**
```json
{
  "swagger_url": "https://api.example.com/openapi.json",
  "title": "My API Docs",
  "swagger_ui_version": "5.30.2",
  "filter": true,
  "show_models": true,
  "display_operation_id": false,
  "show_extensions": false,
  "layout": "BaseLayout",
  "sort_tags": "alpha",
  "sort_ops": "method",
  "theme": "monokai"
}
```

## License

Apache License 2.0 - Copyright 2025 davlgd
