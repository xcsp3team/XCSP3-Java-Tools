
lazy val root = (project in file(".")).
  settings(
    organization := "xcsp3",
    name := "xcsp3",
    version := "1.0.0-SNAPSHOT",
    crossPaths := false,    
    scalaVersion := "2.11.4",
    javacOptions in (Compile,doc) += "-Xdoclint:none",
    libraryDependencies += "junit" % "junit" % "4.12" % "test",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    publishTo := {
            val artifactoryName = "Artifactory Realm"
            val artifactoryUrl = "http://130.104.230.89/artifactory/"
            if (isSnapshot.value)
              Some(artifactoryName at artifactoryUrl + "libs-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
            else
              Some(artifactoryName at artifactoryUrl + "libs-release-local")
          },
   credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
    
  )
