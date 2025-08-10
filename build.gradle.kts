plugins {
    id("java")
}

group = "me.fertiz.netflux"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.alphacephei:vosk:0.3.45")
}

tasks.test {
    useJUnitPlatform()
}