package ru.godl1ght.clicker

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
            cpsField = JTextField("10", 5)
            cpsPanel.add(cpsField)
            frame.add(cpsPanel)

            val windowPanel = JPanel()
            windowPanel.add(JLabel("Window Title:"))
            windowTitleField = JTextField("VimeWorld", 10)
            windowPanel.add(windowTitleField)
            frame.add(windowPanel)

            bindButton = JButton("Bind Key (Default: R)")
            frame.add(bindButton)

            val toggleButton = JButton("Toggle Clicker")
            frame.add(toggleButton)

            statusLabel = JLabel("Status: OFF", SwingConstants.CENTER)
            frame.add(statusLabel)

            bindButton.addActionListener {
                waitingForBind = true
                bindButton.text = "Press a key or mouse button..."
            }

            toggleButton.addActionListener {
                toggleClicker()
            }

            frame.isVisible = true

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher { e ->
                if (waitingForBind && e.id == KeyEvent.KEY_PRESSED) {
                    HotkeyManager.setBindKeyCode(e.keyCode)
                    bindButton.text = "Bind Key: ${KeyEvent.getKeyText(e.keyCode)}"
                    waitingForBind = false
                }
                false
            }

            frame.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (waitingForBind) {
                        HotkeyManager.setBindMouseButton(e.button)
                        bindButton.text = "Bind Mouse: M${e.button}"
                        waitingForBind = false
                    }
                }
            })

            HotkeyManager.registerHotkey { toggleClicker() }
        }
    }

    private fun toggleClicker() {
        val cps = cpsField.text.toIntOrNull()
        val title = windowTitleField.text.trim()

        if (cps != null && cps in 1..100 && title.isNotEmpty()) {
            Clicker.cps = cps
            Clicker.windowTitle = title

            if (Clicker.running) {
                Clicker.stopClicking()
                statusLabel.text = "Status: OFF"
            } else {
                Clicker.startClicking()
                statusLabel.text = "Status: ON"
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Enter valid CPS (1–100) and/or window title", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }
}