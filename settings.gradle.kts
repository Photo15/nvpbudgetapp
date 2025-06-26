pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        jcenter() // Optional, use only if needed
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "NVPBudgetApp"
include(":app")
 