package ru.godl1ght.clicker

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.*

object WindowManager {
    private lateinit var frame: JFrame
    private lateinit var cpsField: JTextField
    private lateinit var windowTitleField: JTextField
    private lateinit var bindButton: JButton
    private lateinit var statusLabel: JLabel

    private lateinit var activationModeBox: JComboBox<String>
    private lateinit var mouseButtonBox: JComboBox<String>

    private var waitingForBind = false

    fun launch() {

        SwingUtilities.invokeLater {
            frame = JFrame("Globus Clicker")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.minimumSize = Dimension(420, 460)
            frame.setSize(420, 460)
            frame.layout = BorderLayout()

            val configPanel = JPanel()
            configPanel.layout = BoxLayout(configPanel, BoxLayout.Y_AXIS)

            val cpsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            cpsPanel.border = BorderFactory.createTitledBorder("Clicks Per Second")
            cpsPanel.add(JLabel("CPS:"))
            cpsField = JTextField(5)
            cpsPanel.add(cpsField)
            val incButton = JButton("+").apply {
                addActionListener {
                    cpsField.text = ((cpsField.text.toIntOrNull() ?: 0) + 1).coerceAtMost(100).toString()
                }
            }
            val decButton = JButton("-").apply {
                addActionListener {
                    cpsField.text = ((cpsField.text.toIntOrNull() ?: 0) - 1).coerceAtLeast(1).toString()
                }
            }
            cpsPanel.add(incButton)
            cpsPanel.add(decButton)

            val windowPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            windowPanel.border = BorderFactory.createTitledBorder("Target Window")
            windowPanel.add(JLabel("Title:"))
            windowTitleField = JTextField(15)
            windowPanel.add(windowTitleField)

            val bindPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            bindPanel.border = BorderFactory.createTitledBorder("Activation Key")
            bindButton = JButton()
            bindPanel.add(bindButton)

            val modePanel = JPanel(FlowLayout(FlowLayout.LEFT))
            modePanel.border = BorderFactory.createTitledBorder("Activation Mode")
            activationModeBox = JComboBox(arrayOf("toggle", "hold"))
            modePanel.add(activationModeBox)

            val mousePanel = JPanel(FlowLayout(FlowLayout.LEFT))
            mousePanel.border = BorderFactory.createTitledBorder("Mouse Button")
            mouseButtonBox = JComboBox(arrayOf("LMB", "RMB"))
            mousePanel.add(mouseButtonBox)

            val resetPanel = JPanel(FlowLayout(FlowLayout.CENTER))
            val resetButton = JButton("Reset Settings").apply {
                addActionListener {
                    cpsField.text = "15"
                    windowTitleField.text = "Example"
                    activationModeBox.selectedItem = "toggle"
                    mouseButtonBox.selectedItem = "LMB"
                    Config.reset()
                    bindButton.text = "Assign a key or mouse button"
                }
            }
            resetPanel.add(resetButton)

            configPanel.add(cpsPanel)
            configPanel.add(windowPanel)
            configPanel.add(bindPanel)
            configPanel.add(modePanel)
            configPanel.add(mousePanel)
            configPanel.add(resetPanel)

            val bottomPanel = JPanel(BorderLayout())
            val toggleButton = JButton("Toggle Clicker")

            bottomPanel.add(toggleButton, BorderLayout.CENTER)
            statusLabel = JLabel("Status: OFF", SwingConstants.CENTER)
            statusLabel.preferredSize = Dimension(400, 40)
            statusLabel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), 40)
            bottomPanel.add(statusLabel)

            val linkPanel = JPanel(FlowLayout(FlowLayout.CENTER))
            val github = createLinkLabel("GitHub", "https://github.com/GoDL1ghT")
            val telegram = createLinkLabel("Telegram", "https://t.me/god1ik")

            linkPanel.add(github)
            linkPanel.add(telegram)

            val southPanel = JPanel()
            southPanel.layout = BoxLayout(southPanel, BoxLayout.Y_AXIS)
            southPanel.add(bottomPanel)
            southPanel.add(linkPanel)

            //////////////////////////////////////////////////////////

            Config.load()
            cpsField.text = Config.cps.toString()
            windowTitleField.text = Config.windowTitle
            activationModeBox.selectedItem = Config.activationMode
            mouseButtonBox.selectedItem = Config.mouseButton
            bindButton.text = when {
                Config.bindKeyCode != -1 -> "Bind Key: ${KeyEvent.getKeyText(Config.bindKeyCode)}"
                Config.bindMouseButton != null -> "Bind Mouse: Mouse ${Config.bindMouseButton}"
                else -> "Assign a key or mouse button"
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
            HotkeyManager.registerHotkey(
                toggle = { toggleClicker() },
                holdStart = {
                    if (activationModeBox.selectedItem == "hold") {
                        Clicker.startClicking()
                        statusLabel.text = "Status: ON"
                    } else
                        toggleClicker()
                },
                holdStop = {
                    if (activationModeBox.selectedItem == "hold") {
                        Clicker.stopClicking()
                        statusLabel.text = "Status: OFF"
                    }
                }
            )

            frame.add(configPanel, BorderLayout.CENTER)
            frame.add(southPanel, BorderLayout.SOUTH)
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
        val mode = activationModeBox.selectedItem.toString()
        val mouseButton = mouseButtonBox.selectedItem.toString()

        if (cps != null && cps in 1..100 && title.isNotEmpty()) {
            Config.cps = cps
            Config.windowTitle = title
            Config.activationMode = mode
            Config.mouseButton = mouseButton
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

    private fun createLinkLabel(text: String, url: String): JLabel {
        val label = JLabel("<html><a href=''>$text</a></html>")

        label.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        label.toolTipText = url

        label.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                try {
                    Desktop.getDesktop().browse(URI(url))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        })
        return label
    }

}
