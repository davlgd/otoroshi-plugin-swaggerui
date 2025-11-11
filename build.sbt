name := "otoroshi-swagger-ui-plugin"
organization := "com.davlgd"
scalaVersion := "2.12.20"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "fr.maif" %% "otoroshi" % "17.8.0" % "provided"
    )
  )
