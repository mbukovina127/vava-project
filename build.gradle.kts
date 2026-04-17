plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    kotlin("jvm")

}

group = "org.shippin"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("org.shippin.app.Launcher")
}

repositories {
    mavenCentral()
}

javafx {
    version = "25.0.2"
    modules("javafx.controls", "javafx.fxml")
}

dependencies {


    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    // Logging
    implementation("org.apache.logging.log4j:log4j-api:2.25.3")
    implementation("org.apache.logging.log4j:log4j-core:2.25.3")

    //JDBC
    implementation("org.postgresql:postgresql:42.7.10")

    // Google JSON
    implementation("com.google.code.gson:gson:2.10.1")
    // Map viewer
    implementation("org.jxmapviewer:jxmapviewer2:2.6")

    //JUnit
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))

    // TEST
    implementation("net.jqwik:jqwik:1.9.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}