plugins {
    application
    id("org.graalvm.buildtools.native") version "0.11.1"
}

dependencies {
    implementation(project(":monkey-common"))
}

application {
    mainClass.set("org.marioarias.monkey.MainKt")
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("monkey-graal")
            mainClass.set("org.marioarias.monkey.MainKt")
            resources.autodetect()
        }
    }
}