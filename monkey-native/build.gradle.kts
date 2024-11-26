plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

dependencies {
    commonMainImplementation(project(":monkey-common"))
}

kotlin {

    when (val hostOp = System.getProperty("os.name")) {
        "Mac OS X" -> this.macosX64("native")
        "Linux" -> this.linuxX64("native")
        else -> throw GradleException("Host OS $hostOp is not supported")
    }.apply {
        binaries {
            executable()
        }
    }
}