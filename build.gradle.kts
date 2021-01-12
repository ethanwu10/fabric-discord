import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

buildscript {
    dependencies {
        classpath("net.fabricmc:tiny-remapper") {
            version {
                strictly("0.3.2")
                because("Older versions use ASM 8.0, which is binary-incompatible")
                // Gradle resolves asm to 9.0 due to shadow
            }
        }
    }
}

plugins {
    java
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("fabric-loom") version "0.6-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "6.1.0"
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

    create("bundledRuntimeOnly") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    runtimeOnly {
        extendsFrom(configurations["bundledRuntimeOnly"])
    }

    minecraftLibraries {
        // HACK: remove netty from libraries to resolve runtimeClasspath conflict with
        // reactor-netty's netty dep (this is resolved in release by shading netty).
        exclude("io.netty", "netty-all")
        // HACK: remove patchy too since it overrides netty Bootstrap
        exclude("com.mojang", "patchy")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("fabric.minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("fabric.yarn_mappings")}")
    modImplementation("net.fabricmc:fabric-loader:${project.property("fabric.loader_version")}")

    val flexmarkVersion = "0.62.2"

    "bundledImplementation"(kotlin("stdlib"))
    "bundledImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    "bundledImplementation"("com.vladsch.flexmark:flexmark:${flexmarkVersion}")
    "bundledImplementation"("com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:${flexmarkVersion}")
    "bundledImplementation"("com.vladsch.flexmark:flexmark-ext-autolink:${flexmarkVersion}")
    "bundledImplementation"("com.discord4j:discord4j-core:3.2.0-M1")
    "bundledImplementation"("io.projectreactor:reactor-core")
    "bundledImplementation"("io.projectreactor.kotlin:reactor-kotlin-extensions")
    "bundledImplementation"("org.slf4j:slf4j-api:1.7.30")
    // version matches minecraft's log4j version
    "bundledRuntimeOnly"("org.apache.logging.log4j:log4j-slf4j-impl:2.8")
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

    shadowJar {
        configurations = listOf(
            project.configurations["bundledImplementation"],
            project.configurations["bundledRuntimeOnly"]
        )

        val shadowPrefix = "dev.ethanwu.mc.fabricdiscord.shadow"
        relocate("io.netty", "$shadowPrefix.io.netty")
    }

    remapJar {
        dependsOn(shadowJar)
        input.set(shadowJar.get().archiveFile)
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")

        dependsOn("classes")
        from(sourceSets.main.get().allSource)
    }
}

