plugins {
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.4.1"
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    group = "net.nyana"
    version = rootProject.libs.versions.project.version.get()

    base {
        archivesName.set("${rootProject.name}-${project.name}")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }

    dependencies {
        compileOnly(rootProject.libs.gson)
        compileOnly(rootProject.libs.jetbrains.annotations)
    }

    tasks {
        build {
            dependsOn(shadowJar)
        }

        shadowJar {
            archiveClassifier = ""
            archiveFileName = "${rootProject.name}-${project.name}-${project.version}.jar"
            destinationDirectory.set(rootProject.file("target"))
        }
    }
}

tasks {
    // 根项目不产出 jar, 避免生成 nyana-reflection-nyana-reflection 构件
    named("jar") { enabled = false }
    named("shadowJar") { enabled = false }
    named("sourcesJar") { enabled = false }

    clean {
        delete("target")
    }
}
