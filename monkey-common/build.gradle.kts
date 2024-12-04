plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm {
        java {
            version = "21"
        }
    }
    js{
        nodejs()
    }
    macosX64()
    linuxX64()
    sourceSets {
        val commonMain by getting {}
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

