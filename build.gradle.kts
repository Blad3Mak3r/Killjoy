import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"

    application
    java
    idea
}

val exposedVersion = "0.32.1"

allprojects {
    group = rootProject.group
    version = rootProject.version

    ext.set("exposedVersion", exposedVersion)

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
        implementation("net.dv8tion:JDA:4.2.1_274") { exclude(module = "opus-java") }

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

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
}

application {
    mainClass.set("dev.killjoy.bot.Launcher")
}

group = "dev.killjoy"
version = "0.10.2"

tasks {
    named("clean") {
        subprojects.forEach {
            dependsOn(it.tasks.clean)
        }
    }

    register<ShadowJar>("buildBot") {
        dependsOn(":bot:shadowJar")
    }
}
