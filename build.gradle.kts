import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.language.jvm.tasks.ProcessResources
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    kotlin("jvm") version "1.5.20"
    id("com.github.johnrengelman.shadow") version "7.0.0"

    application
    java
    idea
}

val jdaVersion = "4.3.0_296"
val exposedVersion = "0.32.1"
val ktorVersion = "1.6.1"
val coroutinesVersion = "1.5.0"
val logbackVersion = "1.2.3"
val prometheusVersion = "0.11.0"
val sentryVersion = "5.0.1"
val commonsLang = "3.12.0"

group = "dev.killjoy"
val versionObj = Version(0, 14, 5)
version = versionObj.build()

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://jitpack.io")
}

dependencies {
    //Kotlin
    implementation(kotlin("stdlib", "1.5.20"))
    implementation(kotlin("serialization", "1.5.20"))
    implementation(kotlin("reflect", "1.5.20"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    //Reflections
    implementation("org.reflections:reflections:0.9.12")

    //JDA
    implementation("net.dv8tion:JDA:$jdaVersion") { exclude(module = "opus-java") }
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.sentry:sentry:$sentryVersion")
    implementation("com.github.minndevelopment:jda-ktx:d460e2a")

    //Database
    implementation("com.zaxxer:HikariCP:4.0.3")
    runtimeOnly("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    //Webhooks
    implementation("club.minnced:discord-webhooks:0.5.7")

    //HugeBot dependencies
    implementation("net.hugebot:RateLimiter:1.1")
    implementation("com.github.killjoybot:Valorant.API:0.1")

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
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")

    testImplementation("junit:junit:4.13.2")
}

abstract class GenerateAgentsContent : DefaultTask() {

    @TaskAction
    fun execute() {
        println("Generating agents contents...")
    }

}

tasks {
    named<ShadowJar>("shadowJar") {
        println("Building version ${project.version}")
        manifest {
            attributes["Main-Class"] = "dev.killjoy.Launcher"
        }
        archiveBaseName.set("Killjoy")
        archiveClassifier.set("")
        archiveVersion.set("")
        dependsOn("processResources")
    }

    named<ProcessResources>("processResources") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        val tokens = mapOf(
            "project.version"       to project.version,
            "project.revision"      to (gitRevision() ?: "NO COMMIT"),
            "project.build_number"  to (getBuildCI() ?: "NO BUILD NUMBER")
        )

        from("src/main/resources") {
            include("app.properties")
            filter<ReplaceTokens>("tokens" to tokens)
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    register<GenerateAgentsContent>("generateAgentsContent")
}

class Version(
    private val major: Int,
    private val minor: Int,
    private val revision: Int? = null
) {

    private fun getVersion(): String {
        return if (revision == null) "$major.$minor"
        else "$major.$minor.$revision"
    }

    fun build(): String = getVersion()
}

fun getBuildCI(): String? {
    return System.getenv("BUILD_NUMBER") ?: System.getProperty("BUILD_NUMBER") ?: null
}

fun gitRevision(): String? {
    return try {
        val gitVersion = org.apache.commons.io.output.ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = gitVersion
        }
        gitVersion.toString(Charsets.UTF_8).trim()
    } catch (e: java.lang.Exception) {
        return System.getenv("GITHUB_SHA")?.trim() ?: return null
    }
}

application {
    mainClass.set("dev.killjoy.Launcher")
}
