plugins {
    kotlin("jvm") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"

    application
    java
    idea
}

allprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        maven("https://m2.dv8tion.net/releases")
        maven("https://dl.bintray.com/kotlin/kotlinx")
        maven("https://jitpack.io")

        jcenter()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

application {
    mainClassName = "dev.killjoy.bot.Launcher"
}

group = "dev.killjoy"
version = "0.9.2"
