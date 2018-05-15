resolvers ++= Seq(
  "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com",
  Resolver.jcenterRepo
)

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.10.0")
