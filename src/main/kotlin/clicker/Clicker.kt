package ru.godl1ght.clicker

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser
import ru.godl1ght.clicker.WindowManager.clickAt
import ru.godl1ght.clicker.WindowManager.findWindowByTitle
import kotlin.concurrent.thread

object Clicker {
    @Volatile
    var running = false
    @Volatile
    var cps = 10
    @Volatile
    var windowTitle: String = "VimeWorld"
    private const val clickX = 250
    private const val clickY = 325

    fun startClicking() {
        if (running) return

        running = true

        thread {
            val hwnd = findWindowByTitle(windowTitle)
            while (running) {
                User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_SHOW)
                User32.INSTANCE.SetForegroundWindow(hwnd)

                Thread.sleep((1000 / cps).toLong())
                clickAt(hwnd, clickX, clickY)
            }
        }
    }

    fun stopClicking() {
        running = false
    }

}
