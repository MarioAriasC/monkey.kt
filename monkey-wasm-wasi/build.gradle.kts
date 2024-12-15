@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

dependencies {
    commonMainImplementation(project(":monkey-common"))
}

kotlin {
    wasmWasi{
        nodejs()
        binaries.executable()
    }
}