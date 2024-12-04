plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

dependencies {
    commonMainImplementation(project(":monkey-common"))
}

kotlin {
    js {
        nodejs()
        binaries.executable()
        compilerOptions {
            target = "es2015"
        }
    }
}