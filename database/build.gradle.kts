val exposedVersion = "0.30.2"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.zaxxer:HikariCP:4.0.3")
    runtimeOnly("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.7")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
}
