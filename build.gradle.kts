import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("org.jetbrains.compose") version "1.6.20-dev1667"
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

tasks.withType<Wrapper> {
    gradleVersion = "8.5"
    distributionType = Wrapper.DistributionType.BIN
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("io.ktor:ktor-client-core:2.3.9")
    implementation("io.ktor:ktor-client-java:2.3.9")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.9")

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
        mainClass = "ua.besf0r.kovadlo.MainKt"
        nativeDistributions {
            packageName = "Kovadlo"
            packageVersion = "1.0.0"

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
        }
        disableDefaultConfiguration()
        fromFiles(obfuscate.get().outputs.files.asFileTree)
        mainJar.set(tasks.jar.map { RegularFile { mapObfuscatedJarFile(it.archiveFile.get().asFile) } })
    }
}
tasks.withType<KotlinCompile>{
    kotlinOptions.jvmTarget = "11"
}
tasks.withType<JavaCompile>{
    options.release = 11
}

obfuscate.configure {
    dependsOn(tasks.jar.get())

    val allJars = tasks.jar.get().outputs.files + sourceSets.main.get().runtimeClasspath.filter { it.path.endsWith(".jar") }

    for (file in allJars) {
        injars(file)
        outjars(mapObfuscatedJarFile(file))
    }

    libraryjars("${compose.desktop.application.javaHome}/jmods")

    configuration("proguard-rules.pro")
}


