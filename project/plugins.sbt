resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.7.5")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")


addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
