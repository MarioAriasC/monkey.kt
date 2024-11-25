plugins {
    application
}

dependencies {
    implementation(project(":monkey-common"))
}

application {
   mainClass.set("org.marioarias.monkey.MainKt")
}