// Konfiguracija upravljanja vtičnikov (pluginManagement)
pluginManagement {
    repositories {
        // Doda gradlePluginPortal kot repozitorij za iskanje vtičnikov
        gradlePluginPortal()
        // Doda Google Maven repozitorij kot repozitorij za iskanje vtičnikov
        google()
        // Doda Maven Central repozitorij kot repozitorij za iskanje vtičnikov
        mavenCentral()
    }
}

// Upravljanje reševanja odvisnosti (dependencyResolutionManagement)
dependencyResolutionManagement {
    // Nastavi način ravnanja z repozitoriji na "FAIL_ON_PROJECT_REPOS"
    // To pomeni, da se uporaba repozitorijev, definiranih znotraj projektov, ne dovoli
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Doda Google Maven repozitorij kot vir odvisnosti
        google()
        // Doda Maven Central repozitorij kot vir odvisnosti
        mavenCentral()
    }
}

// Nastavi ime korenskega projekta
rootProject.name = "My Application"

// Vključi modul ":app" v gradnjo projekta
include(":app")
