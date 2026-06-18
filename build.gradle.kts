plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.4.1"
}

group = "net.nyana"
version = "1.0.0"

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
    compileOnly("org.ow2.asm:asm:9.9.1")
    implementation(libs.mapping.io)
    compileOnly(libs.jetbrains.annotations)

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.ow2.asm:asm:9.9.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveClassifier = ""
        archiveFileName = "nyana-reflection-${project.version}.jar"
        destinationDirectory.set(file("$rootDir/target"))
        relocate("net.fabricmc.mappingio", "net.nyana.reflection.lib.mappingio")
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    repositories {
        maven {
            name = "Catnies"
            url = uri("https://repo.catnies.top/releases")
            credentials(PasswordCredentials::class)
            authentication { create<BasicAuthentication>("basic") }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.nyana"
            artifactId = "nyana-reflection"
            version = version
            from(components["java"])
            pom {
                name = "Nyana Reflection"
                url = "https://github.com/Catnies/nyana-serialization"
            }
        }
    }
}