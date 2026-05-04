plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    kotlin("jvm")
    id("jacoco")
}

group = "org.shippin"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("org.shippin.app.Launcher")
}

tasks.named<JavaExec>("run") {
    systemProperties(System.getProperties().mapKeys {
        it.key.toString() })
}

repositories {
    mavenCentral()
}

javafx {
    version = "26"
    modules("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing")
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

    //jqwik
    implementation("net.jqwik:jqwik:1.9.3")

    //pdf
    implementation("org.apache.pdfbox:pdfbox:3.0.2")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
kotlin {
    jvmToolchain(25)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
}