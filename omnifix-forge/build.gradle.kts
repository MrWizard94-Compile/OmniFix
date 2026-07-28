plugins {
    id("eclipse")
    id("idea")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.spongepowered.mixin") version "0.7.38"
}

fun req(name: String): String = findProperty(name) as String

val modId = req("mod_id")
val modName = req("mod_name")
val modVersion = req("mod_version")
val modGroupId = req("mod_group_id")
val modAuthors = req("mod_authors")
val modLicense = req("mod_license")
val modDescription = req("mod_description")
val minecraftVersion = req("minecraft_version")
val minecraftVersionRange = req("minecraft_version_range")
val forgeVersion = req("forge_version")
val forgeVersionRange = req("forge_version_range")
val loaderVersionRange = req("loader_version_range")
val mojmapChannel = req("mapping_channel")
val mojmapVersion = req("mapping_version")
val baseWarsModsDir = req("base_wars_mods_dir")

val mixinExtrasVersion = "0.4.1"
val mixinSquaredVersion = "0.1.1"

version = modVersion
group = modGroupId

base {
    archivesName = modId
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

sourceSets {
    main {
        java {
            // Kernel sources compiled into this jar (not shipped as a separate mod).
            srcDir("../omnifix-kernel/src/main/java")
            srcDir("../omnifix-compat-valkyrien-portals/src/main/java")
            srcDir("../omnifix-compat-create-portals/src/main/java")
        }
    }
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
    config("${modId}.bugfix.mixins.json")
    config("${modId}.vanilla.mixins.json")
    config("${modId}.leak.mixins.json")
    config("${modId}.net.mixins.json")
    config("${modId}.perf.mixins.json")
    config("${modId}.diagnostics.mixins.json")
    config("${modId}.create.mixins.json")
    config("${modId}.feature.mixins.json")
}

minecraft {
    mappings(mojmapChannel, mojmapVersion)
    copyIdeResources = true
    // Widen vanilla/Forge fields for world-leak + ender-dragon leak mixins (also shipped at META-INF for production).
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        configureEach {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("client") {
            property("forge.enabledGameTestNamespaces", modId)
        }
    }
}

repositories {
    mavenCentral()
    // MixinSquared releases
    maven {
        name = "Bawnorton"
        url = uri("https://maven.bawnorton.com/releases")
    }
    // Fallback for some MixinSquared / community artifacts
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
    flatDir { dirs(baseWarsModsDir) }
    flatDir { dirs("${rootProject.projectDir}/libs") }
}

dependencies {
    minecraft("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")

    compileOnly(files("${rootProject.projectDir}/libs/vs-api-1.1.0.jar"))
    compileOnly(files("$baseWarsModsDir/valkyrienskies-120-2.4.11.jar"))
    compileOnly(files("$baseWarsModsDir/immersive-portals-3.0.8-all.jar"))
    compileOnly(files("$baseWarsModsDir/embeddium-0.3.31+mc1.20.1.jar"))
    compileOnly(files("$baseWarsModsDir/kotlinforforge-4.12.0-all.jar"))
    // Create's PortalTrackProvider API; catnip (BlockFace) ships inside Create's nested Ponder jar,
    // vendored to libs/ because nested jars are invisible to the compile classpath.
    compileOnly(files("$baseWarsModsDir/create-1.20.1-6.0.8.jar"))
    compileOnly(files("${rootProject.projectDir}/libs/Ponder-Forge-1.20.1-1.0.91.jar"))

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // Compile against vendored libs; jar-in-jar embeds them into the final mod jar so packs do not
    // need Moonlight/Supplementaries (or a standalone install) for MixinSquared / MixinExtras.
    val libsDir = "${rootProject.projectDir}/libs"
    compileOnly(files("$libsDir/mixinextras-0.4.1.jar"))
    annotationProcessor(files("$libsDir/mixinextras-0.4.1.jar"))
    compileOnly(files("$libsDir/MixinSquared-0.1.1.jar"))

    // Also resolve Maven artifacts when available (IDE / future upgrades).
    compileOnly("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
    annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")

    runtimeOnly(files("$baseWarsModsDir/valkyrienskies-120-2.4.11.jar"))
    runtimeOnly(files("$baseWarsModsDir/immersive-portals-3.0.8-all.jar"))
}

// Shade MixinExtras + MixinSquared library jars into OmniFix. These vendored artifacts are not
// full Forge mods (no mods.toml); nesting them as JiJ would not load them. Shading puts classes
// on OmniFix's classpath; OmniFixMixinPlugin bootstraps both libraries at mixin-config load.
val embedMixinExtras = file("${rootProject.projectDir}/libs/mixinextras-0.4.1.jar")
val embedMixinSquared = file("${rootProject.projectDir}/libs/MixinSquared-0.1.1.jar")

tasks.named<ProcessResources>("processResources") {
    val replaceProperties = mapOf(
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "forge_version" to forgeVersion,
        "forge_version_range" to forgeVersionRange,
        "loader_version_range" to loaderVersionRange,
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to modLicense,
        "mod_version" to modVersion,
        "mod_authors" to modAuthors,
        "mod_description" to modDescription,
    )
    inputs.properties(replaceProperties)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties + mapOf("project" to project))
    }
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // Embed library classes (exclude signatures / foreign manifests / annotation-processor SPI).
    from(zipTree(embedMixinExtras)) {
        exclude(
            "META-INF/MANIFEST.MF",
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",
            "META-INF/*.EC",
            "META-INF/services/javax.annotation.processing.Processor",
        )
    }
    from(zipTree(embedMixinSquared)) {
        exclude(
            "META-INF/MANIFEST.MF",
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",
            "META-INF/*.EC",
            "META-INF/services/javax.annotation.processing.Processor",
        )
    }
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modId,
                "Specification-Vendor" to modAuthors,
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor" to modAuthors,
            )
        )
    }
    finalizedBy("reobfJar")
}
