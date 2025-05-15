package ru.godl1ght.clicker

import java.io.File
import java.util.*

object Config {
    private val configFile = File("clicker.properties")
    var cps = 15
    var windowTitle = "VimeWorld"
    var bindKeyCode = -1
    var bindMouseButton: Int? = null

    fun load() {
        if (!configFile.exists()) return

        val props = Properties()

        props.load(configFile.inputStream())

        cps = props.getProperty("cps")?.toIntOrNull() ?: 15
        windowTitle = props.getProperty("windowTitle") ?: "VimeWorld"
        bindKeyCode = props.getProperty("bindKeyCode")?.toIntOrNull() ?: -1
        bindMouseButton = props.getProperty("bindMouseCode")?.toIntOrNull()
    }

    fun save() {
        val props = Properties().apply {
            setProperty("cps", cps.toString())
            setProperty("windowTitle", windowTitle)
            setProperty("bindKeyCode", bindKeyCode.toString())
            bindMouseButton?.let { setProperty("bindMouseCode", it.toString()) }
        }
        props.store(configFile.outputStream(), "Clicker settings")
    }

}
