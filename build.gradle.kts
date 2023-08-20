plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
}
base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}
val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup
repositories {
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Ladysnake Libs"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
    }
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
    flatDir {
        dirs("E:\\Documents\\Mod Libraries\\ac\\build\\libs")
    }
    flatDir {
        dirs("E:\\Documents\\Mod Libraries\\fc\\build\\libs")
    }

}
dependencies {
    val minecraftVersion: String by project
    minecraft("com.mojang:minecraft:$minecraftVersion")
    val yarnMappings: String by project
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    val loaderVersion: String by project
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    val fabricKotlinVersion: String by project
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    val emiVersion: String by project
    modCompileOnly ("dev.emi:emi-fabric:${emiVersion}:api")
    modLocalRuntime ("dev.emi:emi-fabric:${emiVersion}")

    val trinketsVersion: String by project
    modImplementation("dev.emi:trinkets:$trinketsVersion"){
        exclude("net.fabricmc.fabric-api")
    }

    val fcVersion: String by project
    modImplementation(":fzzy_core:$fcVersion"){
        exclude("net.fabricmc.fabric-api")
    }

    /*modImplementation(":amethyst_core:1.0.0+1.19.3"){
        exclude("net.fabricmc.fabric-api")
    }*/

    val meVersion: String by project
    implementation("com.github.llamalad7.mixinextras:mixinextras-fabric:$meVersion")
    annotationProcessor("com.github.llamalad7.mixinextras:mixinextras-fabric:$meVersion")

}
tasks {
    val javaVersion = JavaVersion.VERSION_17
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }
    jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }
    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}