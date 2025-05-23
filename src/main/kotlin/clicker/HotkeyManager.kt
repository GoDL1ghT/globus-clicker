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
    private var holdCallbackStart: (() -> Unit)? = null
    private var holdCallbackStop: (() -> Unit)? = null

    fun registerHotkey(toggle: () -> Unit, holdStart: () -> Unit, holdStop: () -> Unit) {
        toggleCallback = toggle
        holdCallbackStart = holdStart
        holdCallbackStop = holdStop

        Logger.getLogger(GlobalScreen::class.java.`package`.name).level = Level.OFF
        GlobalScreen.registerNativeHook()

        GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
            override fun nativeKeyPressed(e: NativeKeyEvent) {
                if (bindKeyCode != -1 && e.keyCode == bindKeyCode && !isActiveWindow())
                    if (Config.activationMode == "hold") holdCallbackStart?.invoke()
                    else toggleCallback?.invoke()
            }

            override fun nativeKeyReleased(e: NativeKeyEvent) {
                if (bindKeyCode != -1 && e.keyCode == bindKeyCode && !isActiveWindow())
                    if (Config.activationMode == "hold") holdCallbackStop?.invoke()
            }

            override fun nativeKeyTyped(e: NativeKeyEvent) {}
        })

        GlobalScreen.addNativeMouseListener(object : NativeMouseListener {
            override fun nativeMousePressed(e: NativeMouseEvent) {
                if (WindowManager.isWaitingForBind()) {
                    bindKeyCode = -1
                    bindMouseButton = e.button

                    setBindKeyCode(-1)
                    setBindMouseButton(e.button)

                    WindowManager.applyMouseBind(e.button)

                    Config.save()

                    return
                }

                if (bindMouseButton != null && e.button == bindMouseButton && !isActiveWindow())
                    if (Config.activationMode == "hold") holdCallbackStart?.invoke()
                    else toggleCallback?.invoke()
            }

            override fun nativeMouseReleased(e: NativeMouseEvent) {
                if (bindMouseButton != null && e.button == bindMouseButton && !isActiveWindow())
                    if (Config.activationMode == "hold") holdCallbackStop?.invoke()
            }

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

}
