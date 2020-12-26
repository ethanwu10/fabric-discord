import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    java
    kotlin("jvm") version "1.4.21"
    id("fabric-loom") version "0.5-SNAPSHOT"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

group = "dev.ethanwu.mc"
version = project.property("mod.version")!!

repositories {
    mavenCentral()
}

configurations {
    create("bundledImplementation") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    implementation {
        extendsFrom(configurations["bundledImplementation"])
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("fabric.minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("fabric.yarn_mappings")}")
    modImplementation("net.fabricmc:fabric-loader:${project.property("fabric.loader_version")}")

    "bundledImplementation"(kotlin("stdlib"))
    "bundledImplementation"("com.vladsch.flexmark:flexmark:0.62.2")
    "bundledImplementation"("com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.62.2")
}

tasks {
    processResources {
        val version: String = project.property("mod.version").let {
            if (it !is String) {
                throw IllegalArgumentException("Missing/malformed mod version in gradle.properties!")
            }
            if (it.contains("-SNAPSHOT")) {
                it.replace(
                    "SNAPSHOT",
                    DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").format(ZonedDateTime.now(ZoneOffset.UTC))
                )
            } else {
                it
            }
        }

        inputs.property("mod_version", version)

        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("mod_version" to version))
        }
    }

    remapJar {
        // Pack bundled implementation dependencies
        from(
            configurations["bundledImplementation"].map { if (it.isDirectory) it else zipTree(it) }
        )
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")

        dependsOn("classes")
        from(sourceSets.main.get().allSource)
    }
}

