import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktorVersion = "1.5.0"
val coroutinesVersion = "1.4.2-native-mt"

plugins {
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"

    application
    java
    idea
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClassName = "tv.blademaker.killjoy.Launcher"
}

group = "tv.blademaker"
version = "0.5"

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("serialization"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    //  HugeBot dependencies
    implementation("net.hugebot:RateLimiter:v1.0.0")

    //  Discord Required
    implementation("net.dv8tion:JDA:4.2.0_227") { exclude(module = "opus-java") }
    implementation("com.jagrosh:jda-utilities:3.0.5")

    //  Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("io.sentry:sentry:3.2.0")
    implementation("com.google.guava:guava:30.0-jre")

    //  HTTP Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")

    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    implementation("com.konghq:unirest-java:3.11.10")

    // Config
    implementation("com.typesafe:config:1.4.1")
    implementation("org.json:json:20201115")

    implementation("org.jsoup:jsoup:1.13.1")
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