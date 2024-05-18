import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.2"
    kotlin("plugin.serialization") version "1.9.10"
}

buildscript {
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.4.0")
    }
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("io.ktor:ktor-client-core:2.3.9")
    implementation("io.ktor:ktor-client-cio:2.3.9")
    implementation("media.kamel:kamel-image:0.9.4")

    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("net.java.dev.jna:jna-platform:4.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

val obfuscate by tasks.registering(proguard.gradle.ProGuardTask::class)

fun mapObfuscatedJarFile(file: File) =
    File("${project.buildDir}/tmp/obfuscated/${file.nameWithoutExtension}.min.jar")

compose.desktop {
    application {
        mainClass = "ua.besf0r.cubauncher.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Cubauncher"
            packageVersion = "1.0.0"

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
        }
        disableDefaultConfiguration()
        fromFiles(obfuscate.get().outputs.files.asFileTree)
        mainJar.set(tasks.jar.map { RegularFile { mapObfuscatedJarFile(it.archiveFile.get().asFile) } })
    }
}

obfuscate.configure {
    dependsOn(tasks.jar.get())

    val allJars = tasks.jar.get().outputs.files + sourceSets.main.get().runtimeClasspath.filter { it.path.endsWith(".jar") }

    for (file in allJars) {
        injars(file)
        outjars(mapObfuscatedJarFile(file))
    }

    libraryjars("${compose.desktop.application.javaHome ?: System.getProperty("java.home")}/jmods")

    configuration("proguard-rules.pro")
}