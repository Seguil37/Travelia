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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // 🔽 Repositorio de CardinalCommerce necesario para PayPal
        maven {
            url = uri("https://cardinalcommerceprod.jfrog.io/artifactory/android")
            credentials {
                // Estas credenciales son públicas y NO son de tu cuenta,
                // pero GitHub las detecta como secretas, así que las quitamos.
                // username = "braintree_team_sdk"
                // password = "AKCp8jQcoDy2hxSWhvDAUQKXKLDDP0X6NYRkqrgFLRc3qDrayg0rrCbJpsKKyMwaykVL8FWusJpp"
            }
        }
    }
}

rootProject.name = "Travelia"
include(":app")
 