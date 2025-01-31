import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "xyz.xenondevs.nova"
version = "0.2.2"

val mojangMapped = System.getProperty("mojang-mapped") != null

plugins {
    kotlin("jvm") version "1.7.10"
    id("xyz.xenondevs.specialsource-gradle-plugin") version "1.0.0"
    id("xyz.xenondevs.string-remapper-gradle-plugin") version "1.0.0"
    id("xyz.xenondevs.nova.nova-gradle-plugin") version "0.12"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation(deps.nova)
    implementation(variantOf(deps.spigot) { classifier("remapped-mojang") })
}

addon {
    id.set(project.name)
    name.set(project.name.capitalized())
    version.set(project.version.toString())
    novaVersion.set(deps.versions.nova)
    main.set("xyz.xenondevs.nova.logistics.Logistics")
    authors.set(listOf("StudioCode", "ByteZ", "Javahase"))
    spigotResourceId.set(102713)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    
    register<Copy>("addonJar") {
        group = "build"
        dependsOn("addon", if (mojangMapped) "jar" else "remapObfToSpigot")
        
        from(File(File(project.buildDir, "libs"), "${project.name}-${project.version}.jar"))
        into(System.getProperty("outDir")?.let(::File) ?: project.buildDir)
    }
}

spigotRemap {
    spigotVersion.set(deps.versions.spigot.get().substringBefore('-'))
    sourceJarTask.set(tasks.jar)
    spigotJarClassifier.set("")
}

remapStrings {
    remapGoal.set(if (mojangMapped) "mojang" else "spigot")
    spigotVersion.set(deps.versions.spigot.get())
    classes.set(emptyList())
}

generateWailaTextures {
    filter.set { !it.name.contains(Regex("\\d")) }
}