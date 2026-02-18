pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {

    // ⭐ VERY IMPORTANT (prevents dependency conflicts)
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()

        // ⭐ Required for MPAndroidChart
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Quizz"
include(":app")
