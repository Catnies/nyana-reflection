dependencies {
    compileOnly(rootProject.libs.asm)
    implementation(rootProject.libs.mapping.io)

    // ASM 对产物是 compileOnly (由运行环境提供), 但测试运行期需要它加载 ASM writer
    testRuntimeOnly(rootProject.libs.asm)

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    shadowJar {
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
            artifactId = "nyana-reflection-core"
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