import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktorVersion = "1.5.3"
val coroutinesVersion = "1.4.3-native-mt"
val prometheusVersion = "0.10.0"

plugins {
    kotlin("jvm") version "1.5.0"
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
    mainClassName = "dev.killjoy.discord.Launcher"
}

group = "dev.killjoy"
version = "0.9.2"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://jitpack.io")

    jcenter()
}

dependencies {
    //Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("serialization"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    //Reflections
    implementation("org.reflections:reflections:0.9.12")

    //HugeBot dependencies
    implementation("net.hugebot:RateLimiter:1.1")
    implementation("com.github.killjoybot:Valorant.API:0.1")

    //Common
    //implementation("net.dv8tion:JDA:4.2.0_255") { exclude(module = "opus-java") }
    implementation("com.github.DV8FromTheWorld:JDA:feature~slash-commands-SNAPSHOT") { exclude(module = "opus-java") }

    //Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("io.sentry:sentry:4.3.0")
    implementation("com.google.guava:guava:30.1.1-jre")

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
    implementation("org.json:json:20210307")

    implementation("org.jsoup:jsoup:1.13.1")

    //Cache
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")

    testImplementation("junit:junit:4.13.2")
}

tasks {
    named<ShadowJar>("shadowJar") {
        manifest {
            attributes["Main-Class"] = "tv.blademaker.killjoy.Launcher"
        }
        archiveBaseName.set("Killjoy")
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
