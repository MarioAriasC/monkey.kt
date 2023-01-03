import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    kotlin("js")
}

/*plugins.withType<YarnPlugin> {
    rootProject.the<YarnRootExtension>().lockFileDirectory = project.rootDir.resolve("k")
}*/

group = "org.marioarias"
version = "1.0-SNAPSHOT"

kotlin {
    js(IR) {
        nodejs()
        binaries.executable()
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":monkey-common"))
}
