pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net")
    }
}

rootProject.name = "OmniFix"

include("omnifix-kernel")
// omnifix-compat-valkyrien-portals: sources live under that directory; compiled via omnifix-forge
include("omnifix-forge")