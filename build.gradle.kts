import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("edu.sc.seis.launch4j") version "4.0.0"
}

group = "me.fertiz"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.alphacephei:vosk:0.3.45")
}

project.setProperty("mainClassName", "me.fertiz.spotifyvoice.SpotifyVoiceApp")

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to "me.fertiz.spotifyvoice.SpotifyVoiceApp"
        )
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

sourceSets {
    named("main") {
        resources {
            srcDirs("src/main/resources")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

launch4j {
    outfile = "SpotifyVoiceApp.exe"
    mainClassName = project.property("mainClassName") as String
    headerType = "console"
    stayAlive = false
    setJarTask(project.tasks.shadowJar.get() as Jar)
}