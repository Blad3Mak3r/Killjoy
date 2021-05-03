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

    dependencies {
        //Common
        //implementation("net.dv8tion:JDA:4.2.0_255") { exclude(module = "opus-java") }
        implementation("com.github.DV8FromTheWorld:JDA:feature~slash-commands-SNAPSHOT") { exclude(module = "opus-java") }

        //Logging
        implementation("ch.qos.logback:logback-classic:1.2.3")

        implementation("io.sentry:sentry:4.3.0")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

application {
    mainClassName = "dev.killjoy.bot.Launcher"
}

group = "dev.killjoy"
version = "0.9.2"
