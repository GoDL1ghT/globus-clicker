package ru.godl1ght.clicker

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import java.awt.GridLayout
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

object WindowManager {
    private lateinit var frame: JFrame
    private lateinit var cpsField: JTextField
    private lateinit var windowTitleField: JTextField
    private lateinit var bindButton: JButton
    private lateinit var statusLabel: JLabel
    private var waitingForBind = false

    fun launch() {

        SwingUtilities.invokeLater {
            frame = JFrame("Globus Clicker")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(320, 220)
            frame.layout = GridLayout(5, 1)

            val cpsPanel = JPanel()
            cpsPanel.add(JLabel("CPS:"))
            cpsField = JTextField(5)
            cpsPanel.add(cpsField)
            frame.add(cpsPanel)

            val windowPanel = JPanel()
            windowPanel.add(JLabel("Window Title:"))
            windowTitleField = JTextField(10)
            windowPanel.add(windowTitleField)
            frame.add(windowPanel)

            bindButton = JButton()
            frame.add(bindButton)

            val toggleButton = JButton("Toggle Clicker")
            frame.add(toggleButton)

            statusLabel = JLabel("Status: OFF", SwingConstants.CENTER)
            frame.add(statusLabel)

            //////////////////////////////////////////////////////////

            Config.load()
            cpsField.text = Config.cps.toString()
            windowTitleField.text = Config.windowTitle
            bindButton.text = when {
                Config.bindKeyCode != -1 -> "Bind Key: ${KeyEvent.getKeyText(Config.bindKeyCode)}"
                Config.bindMouseButton != null -> "Bind Mouse: Mouse ${Config.bindMouseButton}"
                else -> "Bind Key (Default: R)"
            }

            bindButton.addActionListener {
                waitingForBind = true
                bindButton.text = "Press a key or mouse button..."
            }

            toggleButton.addActionListener {
                toggleClicker()
            }

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher { e ->
                if (waitingForBind && e.id == KeyEvent.KEY_PRESSED) {
                    val nativeCode = awtToNativeKeyCode(e.keyCode)

                    Config.bindKeyCode = nativeCode
                    Config.bindMouseButton = null

                    HotkeyManager.setBindKeyCode(nativeCode)
                    HotkeyManager.setBindMouseButton(null)

                    bindButton.text = "Bind Key: ${KeyEvent.getKeyText(e.keyCode)}"
                    waitingForBind = false

                    Config.save()
                }
                false
            }

            frame.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (waitingForBind) {
                        Config.bindMouseButton = e.button
                        Config.bindKeyCode = -1

                        HotkeyManager.setBindKeyCode(-1)
                        HotkeyManager.setBindMouseButton(e.button)

                        bindButton.text = "Bind Mouse: Mouse ${e.button}"
                        waitingForBind = false

                        Config.save()
                    }
                }
            })

            HotkeyManager.setBindKeyCode(Config.bindKeyCode)
            HotkeyManager.setBindMouseButton(Config.bindMouseButton)
            HotkeyManager.registerHotkey { toggleClicker() }

            frame.isVisible = true
        }
    }

    fun awtToNativeKeyCode(awtCode: Int): Int {
        return when (awtCode) {
            KeyEvent.VK_A -> NativeKeyEvent.VC_A
            KeyEvent.VK_B -> NativeKeyEvent.VC_B
            KeyEvent.VK_C -> NativeKeyEvent.VC_C
            KeyEvent.VK_D -> NativeKeyEvent.VC_D
            KeyEvent.VK_E -> NativeKeyEvent.VC_E
            KeyEvent.VK_F -> NativeKeyEvent.VC_F
            KeyEvent.VK_G -> NativeKeyEvent.VC_G
            KeyEvent.VK_H -> NativeKeyEvent.VC_H
            KeyEvent.VK_I -> NativeKeyEvent.VC_I
            KeyEvent.VK_J -> NativeKeyEvent.VC_J
            KeyEvent.VK_K -> NativeKeyEvent.VC_K
            KeyEvent.VK_L -> NativeKeyEvent.VC_L
            KeyEvent.VK_M -> NativeKeyEvent.VC_M
            KeyEvent.VK_N -> NativeKeyEvent.VC_N
            KeyEvent.VK_O -> NativeKeyEvent.VC_O
            KeyEvent.VK_P -> NativeKeyEvent.VC_P
            KeyEvent.VK_Q -> NativeKeyEvent.VC_Q
            KeyEvent.VK_R -> NativeKeyEvent.VC_R
            KeyEvent.VK_S -> NativeKeyEvent.VC_S
            KeyEvent.VK_T -> NativeKeyEvent.VC_T
            KeyEvent.VK_U -> NativeKeyEvent.VC_U
            KeyEvent.VK_V -> NativeKeyEvent.VC_V
            KeyEvent.VK_W -> NativeKeyEvent.VC_W
            KeyEvent.VK_X -> NativeKeyEvent.VC_X
            KeyEvent.VK_Y -> NativeKeyEvent.VC_Y
            KeyEvent.VK_Z -> NativeKeyEvent.VC_Z
            else -> -1
        }
    }


    private fun toggleClicker() {
        val cps = cpsField.text.toIntOrNull()
        val title = windowTitleField.text.trim()

        if (cps != null && cps in 1..100 && title.isNotEmpty()) {
            Config.cps = cps
            Config.windowTitle = title
            Config.save()

            Clicker.cps = cps
            Clicker.windowTitle = title

            if (Clicker.running) {
                Clicker.stopClicking()
                statusLabel.text = "Status: OFF"
            } else {
                Clicker.startClicking()
                statusLabel.text = "Status: ON"
            }

        } else
            JOptionPane.showMessageDialog(
                frame,
                "Enter valid CPS (1–100) and/or window title",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
    }

    fun findWindowByTitle(title: String): WinDef.HWND? {
        return User32.INSTANCE.FindWindow(null, title)
    }

    fun clickAt(hwnd: WinDef.HWND?, x: Int, y: Int) {
        val lParam = (y shl 16) or (x and 0xFFFF)

        User32.INSTANCE.PostMessage(hwnd, 0x0201, WinDef.WPARAM(1), WinDef.LPARAM(lParam.toLong()))
        User32.INSTANCE.PostMessage(hwnd, 0x0202, WinDef.WPARAM(0), WinDef.LPARAM(lParam.toLong()))
    }

    fun isWaitingForBind(): Boolean = waitingForBind

    fun applyMouseBind(button: Int) {
        bindButton.text = "Bind Mouse: Mouse $button"
        waitingForBind = false
    }

}
