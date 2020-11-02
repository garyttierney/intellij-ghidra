import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.intellij.tasks.PatchPluginXmlTask

plugins {
    id("org.jetbrains.intellij") version "0.6.1"
    id("net.saliman.properties") version "1.5.1"

    kotlin("jvm") version "1.4.10"
}

group = "com.codingmates.ghidra"
version = "0.1.0-SNAPSHOT"

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

intellij {
    version = "2020.2"
    pluginName = "intellij-ghidra"

    setPlugins("java")
}

tasks.getByName<PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
      Add change notes here.<br>
      <em>most HTML tags may be used</em>"""
    )
}