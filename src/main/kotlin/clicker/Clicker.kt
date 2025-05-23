package ru.godl1ght.clicker

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import ru.godl1ght.clicker.WindowManager.findWindowByTitle
import kotlin.concurrent.thread

object Clicker {
    @Volatile
    var running = false

    @Volatile
    var cps = Config.cps

    @Volatile
    var windowTitle: String = Config.windowTitle

    private const val clickX = 250
    private const val clickY = 325

    private fun getMouseButtonFlag(): Int {
        return when (Config.mouseButton) {
            "RMB" -> 0x0002 // RIGHTDOWN/UP
            "LMB" -> 0x0001 // LEFTDOWN/UP
            else -> 0x0001
        }
    }

    fun startClicking() {
        if (running) return

        running = true

        thread {
            val hwnd = findWindowByTitle(windowTitle)
            val buttonFlag = getMouseButtonFlag()

            while (running) {
                hwnd?.let {
                    User32.INSTANCE.ShowWindow(it, WinUser.SW_SHOW)
                    User32.INSTANCE.SetForegroundWindow(it)

                    val lParam = (clickY shl 16) or (clickX and 0xFFFF)
                    val downMessage = if (buttonFlag == 0x0002) 0x0204 else 0x0201
                    val upMessage = if (buttonFlag == 0x0002) 0x0205 else 0x0202

                    User32.INSTANCE.PostMessage(it, downMessage, WinDef.WPARAM(0), WinDef.LPARAM(lParam.toLong()))
                    User32.INSTANCE.PostMessage(it, upMessage, WinDef.WPARAM(0), WinDef.LPARAM(lParam.toLong()))
                }

                Thread.sleep((1000 / cps).toLong())
            }
        }
    }

    fun stopClicking() {
        running = false
    }

}
