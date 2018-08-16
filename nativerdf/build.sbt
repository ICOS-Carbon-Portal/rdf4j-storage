val mainVersion = "2.4-SNAPSHOT"

val nativeRdfSelfDeps = Seq("sail-base", "sail-inferencer","queryalgebra-evaluation","queryalgebra-model", "query", "model", "util")
val complianceSelfDeps = Seq("store-testsuite", "serql-testsuite", "sparql-testsuite", "repository-sail", "sail-memory", "sail-federation", "repository-manager")

def rdfSelfDeptoToModule(dep: String) = "org.eclipse.rdf4j" % ("rdf4j-" + dep) % mainVersion

//testOptions.in(ThisBuild, Test) += Tests.Argument(TestFrameworks.JUnit, "-a")

ThisBuild / Test / parallelExecution := false

version in ThisBuild := mainVersion

libraryDependencies in ThisBuild ++= Seq(
	"junit"         %  "junit"           % "4.12"  % "test",
	"com.novocode"  %  "junit-interface" % "0.11"  % "test"
)

resolvers in ThisBuild += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val nativerdf = (project in file("."))
	.settings(
		libraryDependencies ++= nativeRdfSelfDeps.map(rdfSelfDeptoToModule),
		libraryDependencies ++= Seq(
			"org.slf4j"    % "slf4j-api"  % "1.7.10"
		)
	)

lazy val compliance = (project in file("compliance"))
	.dependsOn(nativerdf)
	.settings(
		libraryDependencies ++= complianceSelfDeps.map(rdfSelfDeptoToModule),
		libraryDependencies ++= Seq(
			"org.assertj"     % "assertj-core"     % "3.9.1" % "test",
			"ch.qos.logback"  % "logback-classic"  % "1.1.2" % "test"
		)
	)

