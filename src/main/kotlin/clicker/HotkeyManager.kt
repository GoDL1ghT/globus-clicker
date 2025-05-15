package ru.godl1ght.clicker

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import ru.godl1ght.clicker.Config.bindKeyCode
import ru.godl1ght.clicker.Config.bindMouseButton
import java.awt.KeyboardFocusManager
import java.util.logging.Level
import java.util.logging.Logger

object HotkeyManager {
    private var toggleCallback: (() -> Unit)? = null

    fun registerHotkey(callback: () -> Unit) {
        this.toggleCallback = callback
        Logger.getLogger(GlobalScreen::class.java.`package`.name).level = Level.OFF
        GlobalScreen.registerNativeHook()

        GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
            override fun nativeKeyPressed(e: NativeKeyEvent) {
                if (bindKeyCode != -1 && e.keyCode == bindKeyCode && !isActiveWindow())
                    toggleCallback?.invoke()
            }

            override fun nativeKeyReleased(e: NativeKeyEvent) {}
            override fun nativeKeyTyped(e: NativeKeyEvent) {}
        })

        GlobalScreen.addNativeMouseListener(object : NativeMouseListener {
            override fun nativeMousePressed(e: NativeMouseEvent) {
                if (WindowManager.isWaitingForBind()) {
                    Config.bindKeyCode = -1
                    Config.bindMouseButton = e.button

                    setBindKeyCode(-1)
                    setBindMouseButton(e.button)

                    WindowManager.applyMouseBind(e.button)

                    Config.save()

                    return
                }

                if (bindMouseButton != null && e.button == bindMouseButton && !isActiveWindow())
                    toggleCallback?.invoke()
            }

            override fun nativeMouseReleased(e: NativeMouseEvent) {}
            override fun nativeMouseClicked(e: NativeMouseEvent) {}
        })
    }

    private fun isActiveWindow(): Boolean {
        val activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager()?.activeWindow
        return activeWindow != null && activeWindow.isActive
    }

    fun setBindKeyCode(code: Int) {
        bindKeyCode = code
    }

    fun setBindMouseButton(button: Int?) {
        bindMouseButton = button
    }

    fun getBindKeyText(): Int = bindKeyCode
    fun getBindMouseText(): Int? = bindMouseButton

}
