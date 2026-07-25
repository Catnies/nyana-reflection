plugins {
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.4.1"
}

val snapshot = true

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    group = "net.nyana"
    version = "1.0.4${if (snapshot) "-snapshot" else ""}"

    val moduleArtifactId = "${rootProject.name}${if (this != rootProject) "-${project.name}" else ""}"

    base {
        archivesName.set(moduleArtifactId)
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
    }

    dependencies {
        compileOnly(rootProject.libs.jetbrains.annotations)

        testImplementation(platform("org.junit:junit-bom:6.0.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    publishing {
        repositories {
            maven {
                name = "Catnies"
                url = uri("https://repo.catnies.top/${if (snapshot) "snapshots" else "releases"}")
                credentials(PasswordCredentials::class)
                authentication { create<BasicAuthentication>("basic") }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                groupId = "net.nyana"
                artifactId = moduleArtifactId
                version = version
                from(components["shadow"])
                artifact(tasks.named("sourcesJar"))
                pom {
                    name = "Nyana Reflection"
                    url = "https://github.com/Catnies/nyana-reflection"
                }
            }
        }
    }

    tasks {
        build {
            dependsOn(shadowJar)
        }

        shadowJar {
            archiveClassifier = ""
            archiveFileName = "$moduleArtifactId-${project.version}.jar"
            destinationDirectory.set(rootProject.file("target"))
        }

        test {
            useJUnitPlatform()
        }
    }
}

dependencies {
    compileOnly("org.ow2.asm:asm:9.9.1")
    implementation(libs.mapping.io)

    testRuntimeOnly("org.ow2.asm:asm:9.9.1")
}

tasks {
    clean {
        delete("target")
    }

    shadowJar {
        relocate("net.fabricmc.mappingio", "net.nyana.reflection.lib.mappingio")
    }
}
