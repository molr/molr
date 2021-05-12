import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.ReportRenderer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    extra["runsOnCernVm"] = hasProperty("CERN_TECHNET_VM")
    extra["applyLicense"] = !hasProperty("CERN_TECHNET_VM")
    extra["performDeployment"] = findProperty("DEPLOYMENT") == "true"

    repositories {
        if (findProperty("runsOnCernVm") as Boolean) {
            maven { url = uri("http://artifactory.cern.ch/development") }
            maven { url = uri("http://artifactory.cern.ch/gradle-plugins") }
        } else {
            gradlePluginPortal()
        }
    }
}

plugins {
    val applyLicence: Boolean = findProperty("applyLicense") as Boolean
    val performDeployment: Boolean = findProperty("performDeployment") as Boolean
    id("java")
    id("jacoco")
    id("eclipse")
    id("idea")
    id("org.springframework.boot") version "${findProperty("springBootVersion")}" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0" apply performDeployment
    id("com.github.jk1.dependency-license-report") version "1.5" apply applyLicence
}

if (findProperty("performDeployment") as Boolean) {
    println("Applying deployment plugins")
    apply(plugin = "maven")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "io.github.gradle-nexus.publish-plugin")
}

if (findProperty("applyLicense") as Boolean) {
    println("Applying licensing report plugin")
    apply(plugin = "com.github.jk1.dependency-license-report")

    licenseReport {
        renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("report.html", "Backend"))
        filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer())
//        renderers = [this.class. classLoader . loadClass ('com.github.jk1.license.render.InventoryHtmlReportRenderer').newInstance()]
//        filters = [this.class. classLoader . loadClass ('com.github.jk1.license.filter.LicenseBundleNormalizer').newInstance()]
    }
}

group = findProperty("POM.groupId") as String

tasks.wrapper {
    gradleVersion = "5.6.4"
    distributionType = Wrapper.DistributionType.ALL
}

project(":molr-commons") {
    if (findProperty("performDeployment") as Boolean) {
        println("Applying deployment scripts")
        apply(from = "/home/andrea/Projects/github/gradle-scripts/deployment/deploy-to-maven-central-v4.gradle")
    }

    dependencies {
        val guavaVersion: String by project
        compile(group = "com.google.guava", name = "guava", version = guavaVersion)
    }
}

project(":molr-core") {
    if (findProperty("performDeployment") as Boolean) {
        println("Applying deployment scripts")
        apply(from = "/home/andrea/Projects/github/gradle-scripts/deployment/deploy-to-maven-central-v4.gradle")
    }

    dependencies {
        val jUnitVersion: String by project
        val assertJVersion: String by project
        val reactorVersion: String by project
        val springFrameworkVersion: String by project
        val slf4jVersion: String by project

        compile(project(":molr-commons"))
        /* For support test classes */
        /* NOTE: In generated pom, they have to e set to compile */
        compileOnly(group = "unit", name = "junit", version = jUnitVersion)
        compileOnly(group = "org.assertj", name = "assertj-core", version = assertJVersion)

        compile(group = "io.projectreactor", name = "reactor-core", version = reactorVersion)
        compile(group = "org.springframework", name = "spring-context", version = springFrameworkVersion)

        testCompile(group = "org.slf4j", name = "slf4j-simple", version = slf4jVersion)
        testCompile(group = "io.projectreactor", name = "reactor-test", version = reactorVersion)
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "eclipse")
    apply(plugin = "idea")
//    apply(plugin = "org.springframework.boot")

    dependencies {
        val slf4jVersion: String by project
        val jUnitVersion: String by project
        val assertJVersion: String by project

        compile(group = "org.slf4j", name = "slf4j-api", version = slf4jVersion)
        compile(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.2")

        testCompile(group = "junit", name = "junit", version = jUnitVersion)
        testCompile(group = "org.assertj", name = "assertj-core", version = assertJVersion)
    }

    repositories {
        if (findProperty("runsOnCernVm") as Boolean) {
            maven { url = uri("http://artifactory.cern.ch/development") }
        } else {
            mavenCentral()
        }
    }

    if (project.tasks.findByName("javadocJar") == null) {
        tasks.create<Jar>("javadocJar") {
            archiveClassifier.set("javadoc")
            from("javadoc")
        }
    }

    if (project.tasks.findByName("sourcesJar") == null) {
        tasks.create<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    eclipse {
        classpath {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    jacoco {
        toolVersion = "0.8.4"
    }

    tasks.test {
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    tasks.javadoc { options.encoding = "UTF-8" }

    tasks.jacocoTestReport {
        reports {
            html.isEnabled = false
            xml.isEnabled = true
            xml.destination = file("${buildDir}/reports/jacoco/report.xml")
            csv.isEnabled = false
        }
    }
}

//
//    task updatePom {
//        doLast {
//            def pomInfo = pom {
//                project {
//                    parent {
//                        groupId 'io.molr'
//                        artifactId 'molr'
//                        version '1.0-SNAPSHOT'
//                    }
//                    inceptionYear '2019'
//                    licenses {
//                        license {
//                            name 'The Apache Software License, Version 2.0'
//                            url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
//                            distribution 'repo'
//                        }
//                    }
//                }
//            }
//
//            if (project.name == 'demo-commons') {
//                pomInfo = pomInfo.withXml {
//                    def pluginNode = asNode().appendNode("build").appendNode("plugins").appendNode("plugin");
//                    pluginNode.appendNode("groupId", 'org.xolstice.maven.plugins');
//                    pluginNode.appendNode("artifactId", 'protobuf-maven-plugin');
//                    pluginNode.appendNode("version", '0.5.1');
//
//                    def configurationNode = pluginNode.appendNode("configuration");
//                    configurationNode.appendNode("protocArtifact", 'com.google.protobuf:protoc:3.7.1:exe:${os.detected.classifier}')
//                    configurationNode.appendNode("pluginId", 'grpc-java');
//                    configurationNode.appendNode("pluginArtifact", 'io.grpc:protoc-gen-grpc-java:1.21.0:exe:${os.detected.classifier}');
//
//                    def goalsNode = pluginNode.appendNode("executions").appendNode("execution").appendNode("goals");
//                    goalsNode.appendNode("goal", 'compile');
//                    goalsNode.appendNode("goal", 'compile-custom');
//                }
//            }
//            pomInfo.writeTo("pom-generated.xml")
//        }
//    }
//    }
