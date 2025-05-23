package ru.godl1ght.clicker

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object Config {
    private val configDir = File("C:\\GlobusClicker").apply { mkdirs() }
    private val configFile = File(configDir, "clicker.properties")

    var cps = 15
    var windowTitle = "Example"
    var activationMode = "toggle"
    var mouseButton = "LMB"
    var bindKeyCode = -1
    var bindMouseButton: Int? = null

    fun load() {
        if (!configFile.exists()) return

        val props = Properties().apply {
            load(FileInputStream(configFile))
        }

        cps = props.getProperty("cps")?.toIntOrNull() ?: cps
        windowTitle = props.getProperty("windowTitle") ?: windowTitle
        activationMode = props.getProperty("activationMode") ?: activationMode
        mouseButton = props.getProperty("mouseButton") ?: mouseButton
        bindKeyCode = props.getProperty("bindKeyCode")?.toIntOrNull() ?: -1
        bindMouseButton = props.getProperty("bindMouseButton")?.toIntOrNull()
    }

    fun save() {
        val props = Properties().apply {
            setProperty("cps", cps.toString())
            setProperty("windowTitle", windowTitle)
            setProperty("activationMode", activationMode)
            setProperty("mouseButton", mouseButton)
            setProperty("bindKeyCode", bindKeyCode.toString())
            setProperty("bindMouseButton", bindMouseButton?.toString() ?: "")
        }
        FileOutputStream(configFile).use { props.store(it, null) }
    }

    fun reset() {
        cps = 15
        windowTitle = "Example"
        activationMode = "toggle"
        mouseButton = "LMB"
        bindKeyCode = -1
        bindMouseButton = null
        save()
    }

}
