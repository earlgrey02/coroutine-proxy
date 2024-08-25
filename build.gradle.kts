group = "com.kotlinx"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
