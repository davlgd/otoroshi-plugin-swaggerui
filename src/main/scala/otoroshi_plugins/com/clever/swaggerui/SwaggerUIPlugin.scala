package otoroshi_plugins.com.clever.swaggerui

import akka.stream.Materializer
import otoroshi.env.Env
import otoroshi.next.plugins.api._
import otoroshi.next.proxy.NgProxyEngineError
import otoroshi.utils.syntax.implicits._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class SwaggerUIConfig(
  swaggerUrl: String,
  title: String,
  swaggerUIVersion: String,
  filter: Boolean,
  showModels: Boolean,
  displayOperationId: Boolean,
  showExtensions: Boolean,
  layout: String,
  sortTags: String,
  sortOps: String,
  theme: String
) extends NgPluginConfig {
  def json: JsValue = SwaggerUIConfig.format.writes(this)
}

object SwaggerUIConfig {
  val DefaultSwaggerUIVersion = "5.30.2"

  val default: SwaggerUIConfig = SwaggerUIConfig(
    swaggerUrl = "",
    title = "",
    swaggerUIVersion = DefaultSwaggerUIVersion,
    filter = true,
    showModels = false,
    displayOperationId = false,
    showExtensions = false,
    layout = "BaseLayout",
    sortTags = "alpha",
    sortOps = "alpha",
    theme = "default"
  )

  val format = new Format[SwaggerUIConfig] {
    override def writes(o: SwaggerUIConfig): JsValue = Json.obj(
      "swagger_url" -> o.swaggerUrl,
      "title" -> o.title,
      "swagger_ui_version" -> o.swaggerUIVersion,
      "filter" -> o.filter,
      "show_models" -> o.showModels,
      "display_operation_id" -> o.displayOperationId,
      "show_extensions" -> o.showExtensions,
      "layout" -> o.layout,
      "sort_tags" -> o.sortTags,
      "sort_ops" -> o.sortOps,
      "theme" -> o.theme
    )
    override def reads(json: JsValue): JsResult[SwaggerUIConfig] = Try {
      val version = (json \ "swagger_ui_version").asOpt[String].filter(_.nonEmpty).getOrElse(DefaultSwaggerUIVersion)
      SwaggerUIConfig(
        swaggerUrl = (json \ "swagger_url").as[String],
        title = (json \ "title").as[String],
        swaggerUIVersion = version,
        filter = (json \ "filter").asOpt[Boolean].getOrElse(true),
        showModels = (json \ "show_models").asOpt[Boolean].getOrElse(false),
        displayOperationId = (json \ "display_operation_id").asOpt[Boolean].getOrElse(false),
        showExtensions = (json \ "show_extensions").asOpt[Boolean].getOrElse(false),
        layout = (json \ "layout").asOpt[String].getOrElse("BaseLayout"),
        sortTags = (json \ "sort_tags").asOpt[String].getOrElse("alpha"),
        sortOps = (json \ "sort_ops").asOpt[String].getOrElse("alpha"),
        theme = (json \ "theme").asOpt[String].getOrElse("default")
      )
    } match {
      case Failure(e) => JsError(e.getMessage)
      case Success(c) => JsSuccess(c)
    }
  }

  val configFlow: Seq[String] = Seq("swagger_url", "title", "swagger_ui_version", "theme", "layout", "sort_ops", "sort_tags", "show_extensions", "filter", "show_models", "display_operation_id")

  val configSchema: Option[JsObject] = Some(Json.obj(
    "swagger_url" -> Json.obj(
      "type" -> "string",
      "label" -> "OpenAPI URL",
      "placeholder" -> "https://your-api.example.com/openapi.json",
      "help" -> "URL pointing to your OpenAPI/Swagger JSON or YAML specification"
    ),
    "title" -> Json.obj(
      "type" -> "string",
      "label" -> "Page Title",
      "placeholder" -> "API Docs",
      "help" -> "Title displayed in the browser tab"
    ),
    "swagger_ui_version" -> Json.obj(
      "type" -> "string",
      "label" -> "Swagger UI",
      "placeholder" -> DefaultSwaggerUIVersion,
      "help" -> s"Swagger UI version to load from unpkg.com CDN (default: $DefaultSwaggerUIVersion)"
    ),
    "filter" -> Json.obj(
      "type" -> "bool",
      "label" -> "Filter",
      "help" -> "Enable search/filter bar for operations (default: true)"
    ),
    "show_models" -> Json.obj(
      "type" -> "bool",
      "label" -> "Models",
      "help" -> "Show model schemas by default (default: false = hide models)"
    ),
    "display_operation_id" -> Json.obj(
      "type" -> "bool",
      "label" -> "Operation ID",
      "help" -> "Show operation IDs in the UI (default: false)"
    ),
    "show_extensions" -> Json.obj(
      "type" -> "bool",
      "label" -> "Extensions",
      "help" -> "Show vendor extension fields (x-*) (default: false)"
    ),
    "layout" -> Json.obj(
      "type" -> "string",
      "label" -> "Layout",
      "placeholder" -> "BaseLayout",
      "help" -> "UI layout style: BaseLayout or StandaloneLayout (default: BaseLayout)"
    ),
    "sort_tags" -> Json.obj(
      "type" -> "string",
      "label" -> "Sort Tags",
      "placeholder" -> "alpha",
      "help" -> "Sort tags alphabetically: alpha or none (default: alpha)"
    ),
    "sort_ops" -> Json.obj(
      "type" -> "string",
      "label" -> "Sort Ops",
      "placeholder" -> "alpha",
      "help" -> "Sort operations: alpha, method, or none (default: alpha)"
    ),
    "theme" -> Json.obj(
      "type" -> "string",
      "label" -> "Theme",
      "placeholder" -> "default",
      "help" -> "Optional swagger-ui-themes theme: default, feeling-blue, flattop, material, monokai, muted, newspaper, or outline"
    )
  ))
}

class SwaggerUIPlugin extends NgBackendCall {

  override def useDelegates: Boolean = false
  override def multiInstance: Boolean = true
  override def core: Boolean = true
  override def name: String = "Swagger UI Plugin"
  override def description: Option[String] = "Serves a Swagger UI page from a configurable OpenAPI specification URL".some
  override def defaultConfigObject: Option[NgPluginConfig] = SwaggerUIConfig.default.some
  override def noJsForm: Boolean = true

  override def visibility: NgPluginVisibility = NgPluginVisibility.NgUserLand
  override def categories: Seq[NgPluginCategory] = Seq(NgPluginCategory.Custom("Documentation"))
  override def steps: Seq[NgStep] = Seq(NgStep.CallBackend)

  override def configFlow: Seq[String] = SwaggerUIConfig.configFlow
  override def configSchema: Option[JsObject] = SwaggerUIConfig.configSchema

  override def start(env: Env): Future[Unit] = {
    env.logger.info("[Swagger UI Plugin] plugin is now available!")
    ().vfuture
  }

  override def callBackend(
    ctx: NgbBackendCallContext,
    delegates: () => Future[Either[NgProxyEngineError, BackendCallResponse]]
  )(implicit env: Env, ec: ExecutionContext, mat: Materializer): Future[Either[NgProxyEngineError, BackendCallResponse]] = {

    ctx.cachedConfig(internalName)(SwaggerUIConfig.format) match {
      case Some(config) =>
        val htmlContent = generateSwaggerHTML(config)
        inMemoryBodyResponse(
          200,
          Map(
            "Content-Type" -> "text/html; charset=utf-8",
            "Cache-Control" -> "no-cache, no-store, must-revalidate"
          ),
          htmlContent.byteString
        ).future
      case None =>
        inMemoryBodyResponse(
          500,
          Map("Content-Type" -> "text/plain"),
          "Plugin configuration is missing. Please configure swagger_url and title.".byteString
        ).future
    }
  }

  private def generateSwaggerHTML(config: SwaggerUIConfig): String = {
    val modelsDepth = if (config.showModels) 1 else -1

    val operationsSorter = config.sortOps match {
      case "alpha" => """"alpha""""
      case "method" => """"method""""
      case _ => "undefined"
    }

    val tagsSorter = config.sortTags match {
      case "alpha" => """"alpha""""
      case _ => "undefined"
    }

    val themeLink = if (config.theme.nonEmpty && config.theme != "default") {
      s"""    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/swagger-ui-themes/themes/3.x/theme-${config.theme}.css">"""
    } else {
      ""
    }

    s"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${config.title}</title>
    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@${config.swaggerUIVersion}/swagger-ui.css">
$themeLink
    <style>
        html {
            box-sizing: border-box;
            overflow: -moz-scrollbars-vertical;
            overflow-y: scroll;
        }
        *, *:before, *:after {
            box-sizing: inherit;
        }
        body {
            margin: 0;
            padding: 0;
        }
    </style>
</head>
<body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@${config.swaggerUIVersion}/swagger-ui-bundle.js"></script>
    <script src="https://unpkg.com/swagger-ui-dist@${config.swaggerUIVersion}/swagger-ui-standalone-preset.js"></script>
    <script>
        window.onload = function() {
            window.ui = SwaggerUIBundle({
                url: "${config.swaggerUrl}",
                dom_id: '#swagger-ui',
                deepLinking: true,
                filter: ${config.filter},
                defaultModelsExpandDepth: $modelsDepth,
                displayOperationId: ${config.displayOperationId},
                showExtensions: ${config.showExtensions},
                operationsSorter: $operationsSorter,
                tagsSorter: $tagsSorter,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "${config.layout}"
            });
        };
    </script>
</body>
</html>"""
  }
}
