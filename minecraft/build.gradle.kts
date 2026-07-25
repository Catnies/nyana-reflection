repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    api(project(":"))

    implementation(libs.mapping.io)
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
}

tasks {
    shadowJar {
        relocate("net.fabricmc.mappingio", "net.nyana.reflection.lib.mappingio")
    }
}