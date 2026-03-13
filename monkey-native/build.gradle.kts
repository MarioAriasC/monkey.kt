plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

dependencies {
    commonMainImplementation(project(":monkey-common"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xbinary=preCodegenInlineThreshold=40")
    }

    when (val hostOp = System.getProperty("os.name")) {
        "Mac OS X" -> this.macosX64("monkey-native")
        "Linux" -> this.linuxX64("monkey-native")
        else -> throw GradleException("Host OS $hostOp is not supported")
    }.apply {
        binaries {
            executable()
        }
    }
}