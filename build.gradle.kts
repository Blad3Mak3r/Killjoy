import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.language.jvm.tasks.ProcessResources
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"

    application
    java
    idea
}

val jdaVersion = "4.3.0_323"
val exposedVersion = "0.38.2"
val ktorVersion = "2.0.3"
val coroutinesVersion = "1.6.2"
val logbackVersion = "1.2.11"
val prometheusVersion = "0.14.1"
val sentryVersion = "6.0.0"
val commonsLang = "3.12.0"

group = "dev.killjoy"
val versionObj = Version(0, 15, 10)
version = versionObj.build()

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://jitpack.io")
}

dependencies {
    //Kotlin
    implementation(kotlin("stdlib", "1.7.0"))
    implementation(kotlin("serialization", "1.7.0"))
    implementation(kotlin("reflect", "1.7.0"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    //Reflections
    implementation("org.reflections:reflections:0.10.2")

    //JDA
    implementation("net.dv8tion:JDA:$jdaVersion") { exclude(module = "opus-java") }
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.sentry:sentry:$sentryVersion")
    implementation("com.github.minndevelopment:jda-ktx:d460e2a")

    //Database
    implementation("com.zaxxer:HikariCP:5.0.0")
    runtimeOnly("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    //Webhooks
    implementation("club.minnced:discord-webhooks:0.8.0")

    //HugeBot dependencies
    implementation("net.hugebot:RateLimiter:1.1")

    //HTTP Clients
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Interactions
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("dev.kord:kord-rest:0.8.0-M15")

    implementation("io.prometheus:simpleclient:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    //Config
    implementation("com.typesafe:config:1.4.2")
    implementation("org.json:json:20220320")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("org.jsoup:jsoup:1.14.3")

    //Cache
    implementation("org.redisson:redisson:3.17.3") {
        exclude(module = "byte-buddy")
        exclude(module = "jodd-bean")
        exclude(module = "cache-api")
        exclude(module = "reactor-core")
        exclude(module = "rxjava")
    }

    //Test
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
        kotlinOptions.jvmTarget = "18"
    }

    register<GenerateAgentsContent>("generateAgentsContent")
}

class Version(
    private val major: Int,
    private val minor: Int,
    private val patch: Int? = null
) {

    private fun getVersion(): String {
        return if (patch == null) "$major.$minor"
        else "$major.$minor.$patch"
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
