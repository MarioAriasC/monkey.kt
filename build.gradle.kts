import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    application
    id("org.mikeneck.graalvm-native-image") version "v1.4.0"
}

group = "org.marioarias"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.test {
    useTestNG()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
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

nativeImage {
    graalVmHome = System.getenv("GRAALVM_HOME")
    buildType { build ->
        build.executable("org.marioarias.monkey.MainKt")
    }
    executableName = "monkey-grl"
    outputDirectory = file(".")
/*    arguments(
        "--no-fallback"
//      this option is equivalent to --no-fallback
//      "-H:ReflectionConfigurationFiles=./graal-reflect.json"
    )*/
}
