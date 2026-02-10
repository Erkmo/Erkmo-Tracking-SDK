plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = "com.erkmo"
version = "0.1.0"

android {
    namespace = "com.erkmo.analytics"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = group.toString()
            artifactId = "analytics"
            version = version.toString()
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
