import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    application
}

val compileKotlin: KotlinCompile by tasks

group = "org.marioarias"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.test {
    useTestNG()
}


dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.testng:testng:7.3.0")
}

application {
    mainClassName = "org.marioarias.monkey.MainKt"
}
