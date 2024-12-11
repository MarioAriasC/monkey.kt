dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "monkey.kt"
include(":monkey-common")
include(":monkey-jvm")
include(":monkey-native")
include(":monkey-js")
include(":monkey-wasm")
