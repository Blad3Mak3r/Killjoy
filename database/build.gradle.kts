val exposedVersion = ext.get("exposedVersion")

dependencies {
    implementation(kotlin("stdlib"))

    api("com.zaxxer:HikariCP:4.0.3")
    api("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9")
    api("org.jetbrains.exposed:exposed-core:$exposedVersion")
    api("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    api("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    api("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
}
