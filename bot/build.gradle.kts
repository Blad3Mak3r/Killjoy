import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktorVersion = "1.5.3"
val coroutinesVersion = "1.4.3-native-mt"
val prometheusVersion = "0.10.0"

val exposedVersion = ext.get("exposedVersion")

dependencies {
    //Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("serialization"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    //Database
    implementation(project(":database"))

    //Reflections
    implementation("org.reflections:reflections:0.9.12")

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
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")

    testImplementation("junit:junit:4.13.2")

    implementation("com.zaxxer:HikariCP:4.0.3")
    runtimeOnly("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.7")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
}

tasks {
    named<ShadowJar>("shadowJar") {
        manifest {
            attributes["Main-Class"] = "dev.killjoy.bot.Launcher"
        }
        archiveBaseName.set("Killjoy")
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "11"
    }
}
