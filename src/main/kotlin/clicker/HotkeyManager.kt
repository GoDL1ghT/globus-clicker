package ru.godl1ght.clicker

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import java.awt.event.KeyEvent
import java.util.logging.Level
import java.util.logging.Logger

object HotkeyManager {
    private var bindKeyCode: Int = NativeKeyEvent.VC_R
    private var bindMouseButton: Int? = null
    private var toggleCallback: (() -> Unit)? = null

    fun registerHotkey(callback: () -> Unit) {
        toggleCallback = callback
        Logger.getLogger(GlobalScreen::class.java.`package`.name).level = Level.OFF
        GlobalScreen.registerNativeHook()

        GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
            override fun nativeKeyPressed(e: NativeKeyEvent) {
                if (e.keyCode == bindKeyCode) {
                    toggleCallback?.invoke()
                }
            }
            override fun nativeKeyReleased(e: NativeKeyEvent) {}
            override fun nativeKeyTyped(e: NativeKeyEvent) {}
        })

        GlobalScreen.addNativeMouseListener(object : NativeMouseListener {
            override fun nativeMousePressed(e: NativeMouseEvent) {
                if (e.button == NativeMouseEvent.BUTTON4 || e.button == NativeMouseEvent.BUTTON5 ||e.button == bindMouseButton) {
                    toggleCallback?.invoke()
                }
            }
            override fun nativeMouseReleased(e: NativeMouseEvent) {}
            override fun nativeMouseClicked(e: NativeMouseEvent) {}
        })
    }

    fun setBindKeyCode(code: Int) {
        bindKeyCode = code
    }

    fun setBindMouseButton(button: Int) {
        bindMouseButton = button
    }

    fun getBindKeyText(): String {
        return KeyEvent.getKeyText(bindKeyCode)
    }

    fun getBindMouseText(): String? {
        return bindMouseButton?.let { "Mouse Button $it" }
    }
}
