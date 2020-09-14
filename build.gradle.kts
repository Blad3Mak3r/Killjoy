import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.10"

    id("com.github.johnrengelman.shadow") version "5.1.0"
    application
}

val jdaVersion = "4.2.0_204"

application {
    mainClassName = "tv.blademaker.killjoy.Launcher"
}

group = "tv.blademaker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))

    //  HugeBot dependencies
    implementation("net.hugebot:RateLimiter:v1.0.0")

    //  Discord Required
    implementation("net.dv8tion:JDA:$jdaVersion") { exclude(module = "opus-java") }
    implementation("com.jagrosh:jda-utilities:3.0.4")

    //  Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("io.sentry:sentry:1.7.30")
    implementation("com.google.guava:guava:29.0-jre")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "tv.blademaker.killjoy.Launcher"
    }
    archiveBaseName.set("KilljoyAI")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "tv.blademaker.killjoy.Launcher"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.register("stage") {
    dependsOn("shadowJar")
    copy {
        from("build/libs/KilljoyAI.jar")
        into("target/")
    }
    dependsOn("clean")
}