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
    
    // Apache Commons Compress for tar.gz extraction
    implementation("org.apache.commons:commons-compress:1.24.0")
    
    // Tor control library (используем исходный код из ресурсов)
    // Библиотека jtorctl не доступна в Maven Central, поэтому добавляем её из JAR или используем альтернативу
    // Временно комментируем до получения правильного JAR
    // implementation("net.freehaven.tor.control:jtorctl:0.4")
}

tasks.test {
    useJUnitPlatform()
}