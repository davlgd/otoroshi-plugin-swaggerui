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
  swaggerUIVersion: String
) extends NgPluginConfig {
  def json: JsValue = SwaggerUIConfig.format.writes(this)
}

object SwaggerUIConfig {
  val format = new Format[SwaggerUIConfig] {
    override def writes(o: SwaggerUIConfig): JsValue = Json.obj(
      "swagger_url" -> o.swaggerUrl,
      "title" -> o.title,
      "swagger_ui_version" -> o.swaggerUIVersion
    )
    override def reads(json: JsValue): JsResult[SwaggerUIConfig] = Try {
      SwaggerUIConfig(
        swaggerUrl = (json \ "swagger_url").as[String],
        title = (json \ "title").as[String],
        swaggerUIVersion = (json \ "swagger_ui_version").asOpt[String].getOrElse("5.30.2")
      )
    } match {
      case Failure(e) => JsError(e.getMessage)
      case Success(c) => JsSuccess(c)
    }
  }

  val configFlow: Seq[String] = Seq("swagger_url", "title", "swagger_ui_version")

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
      "label" -> "Swagger UI Version",
      "placeholder" -> "5.30.2",
      "help" -> "Swagger UI version to load from unpkg.com CDN (default: 5.30.2)"
    )
  ))
}

class SwaggerUIPlugin extends NgBackendCall {

  override def useDelegates: Boolean = false
  override def multiInstance: Boolean = true
  override def core: Boolean = true
  override def name: String = "Swagger UI Plugin"
  override def description: Option[String] = "Serves a Swagger UI page from a configurable OpenAPI specification URL".some
  override def defaultConfigObject: Option[NgPluginConfig] = None
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
        val htmlContent = generateSwaggerHTML(config.swaggerUrl, config.title, config.swaggerUIVersion)
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

  private def generateSwaggerHTML(swaggerUrl: String, title: String, version: String): String = {
    s"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@$version/swagger-ui.css">
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
    <script src="https://unpkg.com/swagger-ui-dist@$version/swagger-ui-bundle.js"></script>
    <script src="https://unpkg.com/swagger-ui-dist@$version/swagger-ui-standalone-preset.js"></script>
    <script>
        window.onload = function() {
            window.ui = SwaggerUIBundle({
                url: "$swaggerUrl",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
            });
        };
    </script>
</body>
</html>"""
  }
}
