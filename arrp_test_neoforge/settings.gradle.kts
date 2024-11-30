pluginManagement {
    repositories {
        maven {
            name = "FabricMC Maven"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Architectury Maven"
            url = uri("https://maven.architectury.dev/")
        }
        maven {
            name = "MinecraftForge Maven"
            url = uri("https://files.minecraftforge.net/maven/")
        }
        maven {
            name = "NeoForged Maven"
            url = uri("https://maven.neoforged.net/releases/")
        }
        gradlePluginPortal()
    }
}

include("common")
include("neoforge")
include("arrp_test_common")