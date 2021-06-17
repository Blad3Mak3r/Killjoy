import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.language.jvm.tasks.ProcessResources
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"

    application
    java
    idea
}

val jdaVersion = "4.3.0_279"
val exposedVersion = "0.32.1"
val ktorVersion = "1.6.0"
val coroutinesVersion = "1.5.0-native-mt"
val logbackVersion = "1.2.3"
val prometheusVersion = "0.11.0"
val sentryVersion = "5.0.1"

group = "killjoy"
val versionObj = Version(0, 10, 4)
version = versionObj.build()

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    jcenter()
    maven("https://jitpack.io")
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

    //JDA
    implementation("net.dv8tion:JDA:$jdaVersion") { exclude(module = "opus-java") }
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.sentry:sentry:$sentryVersion")

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
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.2")

    testImplementation("junit:junit:4.13.2")
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
            "project.version"   to project.version,
            "project.revision"  to gitRevision()
        )

        from("src/main/resources") {
            include("app.properties")
            filter<ReplaceTokens>("tokens" to tokens)
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

class Version(
    private val major: Int,
    private val minor: Int,
    private val revision: Int
) {
    private val pattern = "%d.%d.%d_%s"

    fun build() = pattern.format(major, minor, revision, getBuild())
}

fun gitRevision(): String {
    val gitVersion = org.apache.commons.io.output.ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = gitVersion
    }
    return gitVersion.toString(Charsets.UTF_8).trim()
}

fun getBuild(): String {
    val buildNumber = System.getenv("BUILD_NUMBER")
        ?: System.getProperty("BUILD_NUMBER")
        ?: System.getenv("github.run_number")
        ?: System.getProperty("github.run_number")
        ?: null
    val revision = gitRevision()

    return if (buildNumber == null) revision
    else "$buildNumber+$revision"
}

application {
    mainClass.set("dev.killjoy.Launcher")
}
