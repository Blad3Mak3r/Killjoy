import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.10"

    id("com.github.johnrengelman.shadow") version "5.1.0"
    application
}

application {
    mainClassName = "tv.blademaker.killjoy.Launcher"
}

group = "tv.blademaker"
version = "0.3.3"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

val ktorVersion = "1.4.2"
val coroutinesVersion = "1.4.1"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    //  HugeBot dependencies
    implementation("net.hugebot:RateLimiter:v1.0.0")

    //  Discord Required
    implementation("net.dv8tion:JDA:4.2.0_+") { exclude(module = "opus-java") }
    implementation("com.jagrosh:jda-utilities:3.0.5")

    //  Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("io.sentry:sentry:1.7.30")
    implementation("com.google.guava:guava:30.0-jre")

    //  Ktor HTTP Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    named<ShadowJar>("shadowJar") {
        manifest {
            attributes["Main-Class"] = "tv.blademaker.killjoy.Launcher"
        }
        archiveBaseName.set("KilljoyAI")
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    named("build") {
        dependsOn("shadowJar")
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "11"
    }

    register("stage") {
        dependsOn("clean")
        dependsOn("shadowJar")

    }
}