import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
    id("org.mikeneck.graalvm-native-image") version "v1.4.0"
}

group = "org.marioarias"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":monkey-common"))
}

tasks.withType<KotlinCompile>{
    kotlinOptions {
        jvmTarget = "19"
    }
}

application{
    mainClass.set("org.marioarias.monkey.MainKt")
}

nativeImage {
    graalVmHome = System.getenv("GRAALVM_HOME")
    buildType { build ->
        build.executable("org.marioarias.monkey.MainKt")
    }
    executableName = "monkey-graal"
    outputDirectory = file(".")
    arguments(
        "--no-fallback"
    )
}