EAGLE-EDUCATION-e
├── app
├── .github
├── build.gradle
├── settings.gradle
├── gradle.properties
├── firestore.rules
└── storage.rules
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EagleEducation"
include(":app")
plugins {
    id 'com.android.application' version '8.7.3' apply false
    id 'org.jetbrains.kotlin.android' version '2.0.21' apply false
    id 'com.google.gms.google-services' version '4.4.2' apply false
}
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.gms.google-services'
}

android {
    namespace 'com.eagleeducation'
    compileSdk 35

    defaultConfig {
        applicationId 'com.eagleeducation'
        minSdk 23
        targetSdk 35
        versionCode 1
        versionName '1.0'
    }
}

dependencies {

}
