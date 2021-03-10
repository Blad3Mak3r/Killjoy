import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktorVersion = "1.5.1"
val coroutinesVersion = "1.4.2-native-mt"
val prometheusVersion = "0.10.0"

plugins {
    kotlin("jvm") version "1.4.31"
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

group = "tv.blademaker.killjoy"
version = "0.8.2"

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://jitpack.io")
}

dependencies {
    //Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("serialization"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    //HugeBot dependencies
    implementation("net.hugebot:RateLimiter:1.1")
    implementation("com.github.killjoybot:Valorant.API:0.1")

    //Common
    implementation("net.dv8tion:JDA:4.2.0_240") { exclude(module = "opus-java") }
    implementation("com.jagrosh:jda-utilities:3.0.5")

    //Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("io.sentry:sentry:4.3.0")
    implementation("com.google.guava:guava:30.1-jre")

    //HTTP Clients
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.konghq:unirest-java:3.11.11")

    //Prometheus
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.prometheus:simpleclient:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    //Config
    implementation("com.typesafe:config:1.4.1")
    implementation("org.json:json:20201115")

    implementation("org.jsoup:jsoup:1.13.1")

    //Cache
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")
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

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.useIR = true
    }

    register("stage") {
        dependsOn("clean")
        dependsOn("shadowJar")

    }
}
