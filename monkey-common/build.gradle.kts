import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm() {
        java {
            version = "21"
        }
    }
    js() {
        nodejs()
        binaries.executable()
        compilerOptions {
            target = "es2015"
        }
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

