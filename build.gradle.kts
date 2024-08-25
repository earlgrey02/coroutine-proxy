plugins {
    alias(libs.plugins.kotlin)
    id("java-test-fixtures")
}

group = "com.kotlinx"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.coroutine)
    implementation(libs.cglib)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
