pluginManagement {
    repositories {
        mavenLocal()
        maven(url = "https://repo.binom.pw")
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        maven(url = "https://repo.binom.pw")
        google()
        mavenCentral()
    }
}
rootProject.name = "Device-Nats"
//include(":app")
include(":device-shared")
include(":server-shared")
//include(":deviceClient")
include(":server")
//include(":deviceShared")
//include(":agent")
//include(":glasses")
//include(":frontend")
//include(":serverShared")
