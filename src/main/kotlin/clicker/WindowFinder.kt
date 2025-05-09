package ru.godl1ght.clicker

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser

object WindowFinder {

    fun findWindowByTitle(title: String): WinDef.HWND? {
        return User32.INSTANCE.FindWindow(null, title)
    }

    fun clickAt(hwnd: WinDef.HWND?, x: Int, y: Int) {
            val lParam = (y shl 16) or (x and 0xFFFF)

            User32.INSTANCE.PostMessage(hwnd, 0x0201, WinDef.WPARAM(1), WinDef.LPARAM(lParam.toLong()))
            User32.INSTANCE.PostMessage(hwnd, 0x0202, WinDef.WPARAM(0), WinDef.LPARAM(lParam.toLong()))
    }

}