plugins {
    kotlin("jvm") 
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