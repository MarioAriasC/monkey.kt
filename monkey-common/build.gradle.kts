@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)

}

kotlin {
    jvm {
        compilerOptions {
//            freeCompilerArgs.add("-Xwhen-expressions=indy")
        }
        java {
            version = "25"
        }
    }
    js {
        nodejs()
    }
    wasmWasi {
        nodejs()
    }
    wasmJs() {
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

