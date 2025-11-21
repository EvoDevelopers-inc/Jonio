plugins {
    id("java")
}

group = "evo.developers.ru"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.javalin:javalin:6.7.0")
    implementation("org.apache.commons:commons-compress:1.24.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("com.google.crypto.tink:tink:1.15.0")
    implementation("com.nimbusds:nimbus-jose-jwt:10.5")

}

tasks.test {
    useJUnitPlatform()
}


tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = "Main"
    }
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

