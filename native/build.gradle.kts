plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

dependencies {
    commonMainImplementation(project(":monkey"))
}

kotlin {
    macosX64("native") {
        binaries {
            executable()
        }
    }
}