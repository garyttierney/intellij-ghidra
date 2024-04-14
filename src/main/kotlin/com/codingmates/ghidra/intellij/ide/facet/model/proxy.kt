package com.codingmates.ghidra.intellij.ide.facet.model

import java.io.File

class GhidraGModuleProxy(private val instance: Any) {
    private val moduleRootGetter = instance.javaClass.getMethod("getModuleRoot")

    val moduleRoot: GhidraResourceFileProxy
        get() = GhidraResourceFileProxy(moduleRootGetter.invoke(instance))
}

class GhidraResourceFileProxy(private val instance: Any) {
    private val canonicalPathGetter = instance.javaClass.getMethod("getCanonicalPath")

    val canonicalPath: String
        get() = canonicalPathGetter.invoke(instance) as String
}

class GhidraApplicationPropertiesProxy(private val instance: Any) {
    private val applicationVersionGetter = instance.javaClass.getMethod("getApplicationVersion")

    val applicationVersion: String
        get() = applicationVersionGetter.invoke(instance) as String
}

class GhidraApplicationLayoutProxy(private val instance: Any) {
    private val extensionInstallationDirsGetter = instance.javaClass.getMethod("getExtensionInstallationDirs")
    private val settingsDirGetter = instance.javaClass.getMethod("getUserSettingsDir")
    private val applicationPropertiesGetter = instance.javaClass.getMethod("getApplicationProperties")
    private val modulesGetter = instance.javaClass.getMethod("getModules")

    val applicationProperties: GhidraApplicationPropertiesProxy
        get() = GhidraApplicationPropertiesProxy(applicationPropertiesGetter.invoke(instance))

    val extensionInstallationDirs: List<GhidraResourceFileProxy>
        get() = (extensionInstallationDirsGetter.invoke(instance) as List<Any>).map(::GhidraResourceFileProxy)

    val modules: Map<String, GhidraGModuleProxy>
        get() = (modulesGetter.invoke(instance) as Map<String, Any>).mapValues { GhidraGModuleProxy(it.value) }

    val settingsDir: File
        get() = settingsDirGetter.invoke(instance) as File
}

fun createApplicationLayoutProxy(classLoader: ClassLoader, applicationDir: File): GhidraApplicationLayoutProxy {
    val layoutClass = classLoader.loadClass("ghidra.GhidraApplicationLayout")
    val layoutConstructor = layoutClass.getConstructor(File::class.java)
    val proxy = layoutConstructor.newInstance(applicationDir)

    return GhidraApplicationLayoutProxy(proxy)
}
