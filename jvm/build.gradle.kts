plugins {
    application
}

dependencies {
    implementation(project(":monkey"))
}

application {
   mainClass.set("org.marioarias.monkey.MainKt")
}