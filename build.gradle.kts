plugins {
    java
}

group = "me.pauleff"
version = "26.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("commons-cli:commons-cli:1.6.0")
    implementation("org.json:json:20251224")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.32")
    implementation("com.github.Querz:NBT:6.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.jar {
    archiveFileName.set("MinecraftOfflineOnlineConverter-${project.version}.jar")
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "me.pauleff.Main",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version.toString(),
                "Specification-Title" to project.name,
                "Specification-Version" to project.version.toString(),
            ),
        )
    }
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Builds a fat JAR with all dependencies"
    archiveFileName.set("MinecraftOfflineOnlineConverter-${project.version}-jar-with-dependencies.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes("Main-Class" to "me.pauleff.Main")
    }
    from(sourceSets.main.get().output)
    dependsOn(tasks.classes)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}
