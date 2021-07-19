import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
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
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(kotlin("test-testng"))
}

application {
    mainClassName = "org.marioarias.monkey.MainKt"
}
