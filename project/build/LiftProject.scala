import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) {
  val liftVersion = "2.3"

  // uncomment the following if you want to use the snapshot repo
  // val scalatoolsSnapshot = ScalaToolsSnapshots

  // If you're using JRebel for Lift development, uncomment
  // this line
  override def scanDirectories = Nil

  val lift_postgresql = "postgresql" % "postgresql" % "8.4-701.jdbc4"
  val widgets = "net.liftweb" %% "lift-widgets" % liftVersion 

  override def libraryDependencies = Set(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default",
    "junit" % "junit" % "4.5" % "test->default",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"
  ) ++ super.libraryDependencies

  val jettyWebPath = "src" / "main" / "webapp" / "WEB-INF" / "jetty-web.xml"

  lazy val installProductionRunMode = task {
    FileUtilities.copyFile("project" / "jetty-web.xml",
                           jettyWebPath,
                           log)
    log.info("Copied jetty-web.xml into place")
    None
  } describedAs("Install a jetty-web.xml that sets the run mode to production")

  lazy val superPackage = super.packageAction dependsOn(installProductionRunMode)

  lazy val removeProductionRunMode = task {
    FileUtilities.clean(jettyWebPath, log)
    None
  } describedAs("Remove jetty-web.xml and hence set run mode back to testing")

  override def packageAction = removeProductionRunMode dependsOn(superPackage) describedAs BasicWebScalaProject.PackageWarDescription

}
