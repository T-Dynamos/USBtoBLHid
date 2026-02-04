package tdynamos.usbtoblhid

import android.view.KeyEvent

object InputHidMapper {
    fun keyCodeToHidUsage(keyCode: Int): Int? {
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> 4
            KeyEvent.KEYCODE_B -> 5
            KeyEvent.KEYCODE_C -> 6
            KeyEvent.KEYCODE_D -> 7
            KeyEvent.KEYCODE_E -> 8
            KeyEvent.KEYCODE_F -> 9
            KeyEvent.KEYCODE_G -> 10
            KeyEvent.KEYCODE_H -> 11
            KeyEvent.KEYCODE_I -> 12
            KeyEvent.KEYCODE_J -> 13
            KeyEvent.KEYCODE_K -> 14
            KeyEvent.KEYCODE_L -> 15
            KeyEvent.KEYCODE_M -> 16
            KeyEvent.KEYCODE_N -> 17
            KeyEvent.KEYCODE_O -> 18
            KeyEvent.KEYCODE_P -> 19
            KeyEvent.KEYCODE_Q -> 20
            KeyEvent.KEYCODE_R -> 21
            KeyEvent.KEYCODE_S -> 22
            KeyEvent.KEYCODE_T -> 23
            KeyEvent.KEYCODE_U -> 24
            KeyEvent.KEYCODE_V -> 25
            KeyEvent.KEYCODE_W -> 26
            KeyEvent.KEYCODE_X -> 27
            KeyEvent.KEYCODE_Y -> 28
            KeyEvent.KEYCODE_Z -> 29
            KeyEvent.KEYCODE_1 -> 30
            KeyEvent.KEYCODE_2 -> 31
            KeyEvent.KEYCODE_3 -> 32
            KeyEvent.KEYCODE_4 -> 33
            KeyEvent.KEYCODE_5 -> 34
            KeyEvent.KEYCODE_6 -> 35
            KeyEvent.KEYCODE_7 -> 36
            KeyEvent.KEYCODE_8 -> 37
            KeyEvent.KEYCODE_9 -> 38
            KeyEvent.KEYCODE_0 -> 39
            KeyEvent.KEYCODE_ENTER -> 40
            KeyEvent.KEYCODE_ESCAPE -> 41
            KeyEvent.KEYCODE_DEL -> 42
            KeyEvent.KEYCODE_TAB -> 43
            KeyEvent.KEYCODE_SPACE -> 44
            KeyEvent.KEYCODE_MINUS -> 45
            KeyEvent.KEYCODE_EQUALS -> 46
            KeyEvent.KEYCODE_LEFT_BRACKET -> 47
            KeyEvent.KEYCODE_RIGHT_BRACKET -> 48
            KeyEvent.KEYCODE_BACKSLASH -> 49
            KeyEvent.KEYCODE_SEMICOLON -> 51
            KeyEvent.KEYCODE_APOSTROPHE -> 52
            KeyEvent.KEYCODE_GRAVE -> 53
            KeyEvent.KEYCODE_COMMA -> 54
            KeyEvent.KEYCODE_PERIOD -> 55
            KeyEvent.KEYCODE_SLASH -> 56
            KeyEvent.KEYCODE_CAPS_LOCK -> 57
            KeyEvent.KEYCODE_F1 -> 58
            KeyEvent.KEYCODE_F2 -> 59
            KeyEvent.KEYCODE_F3 -> 60
            KeyEvent.KEYCODE_F4 -> 61
            KeyEvent.KEYCODE_F5 -> 62
            KeyEvent.KEYCODE_F6 -> 63
            KeyEvent.KEYCODE_F7 -> 64
            KeyEvent.KEYCODE_F8 -> 65
            KeyEvent.KEYCODE_F9 -> 66
            KeyEvent.KEYCODE_F10 -> 67
            KeyEvent.KEYCODE_F11 -> 68
            KeyEvent.KEYCODE_F12 -> 69
            KeyEvent.KEYCODE_SYSRQ -> 70
            KeyEvent.KEYCODE_SCROLL_LOCK -> 71
            KeyEvent.KEYCODE_BREAK -> 72
            KeyEvent.KEYCODE_INSERT -> 73
            KeyEvent.KEYCODE_MOVE_HOME -> 74
            KeyEvent.KEYCODE_PAGE_UP -> 75
            KeyEvent.KEYCODE_FORWARD_DEL -> 76
            KeyEvent.KEYCODE_MOVE_END -> 77
            KeyEvent.KEYCODE_PAGE_DOWN -> 78
            KeyEvent.KEYCODE_DPAD_RIGHT -> 79
            KeyEvent.KEYCODE_DPAD_LEFT -> 80
            KeyEvent.KEYCODE_DPAD_DOWN -> 81
            KeyEvent.KEYCODE_DPAD_UP -> 82
            KeyEvent.KEYCODE_NUM_LOCK -> 83
            KeyEvent.KEYCODE_NUMPAD_DIVIDE -> 84
            KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> 85
            KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> 86
            KeyEvent.KEYCODE_NUMPAD_ADD -> 87
            KeyEvent.KEYCODE_NUMPAD_ENTER -> 88
            KeyEvent.KEYCODE_NUMPAD_1 -> 89
            KeyEvent.KEYCODE_NUMPAD_2 -> 90
            KeyEvent.KEYCODE_NUMPAD_3 -> 91
            KeyEvent.KEYCODE_NUMPAD_4 -> 92
            KeyEvent.KEYCODE_NUMPAD_5 -> 93
            KeyEvent.KEYCODE_NUMPAD_6 -> 94
            KeyEvent.KEYCODE_NUMPAD_7 -> 95
            KeyEvent.KEYCODE_NUMPAD_8 -> 96
            KeyEvent.KEYCODE_NUMPAD_9 -> 97
            KeyEvent.KEYCODE_NUMPAD_0 -> 98
            KeyEvent.KEYCODE_NUMPAD_DOT -> 99
            KeyEvent.KEYCODE_MENU -> 101
            else -> null
        }
    }

    fun keyCodeToModifierMask(keyCode: Int): Int? {
        return when (keyCode) {
            KeyEvent.KEYCODE_CTRL_LEFT -> 0x01
            KeyEvent.KEYCODE_SHIFT_LEFT -> 0x02
            KeyEvent.KEYCODE_ALT_LEFT -> 0x04
            KeyEvent.KEYCODE_META_LEFT -> 0x08
            KeyEvent.KEYCODE_CTRL_RIGHT -> 0x10
            KeyEvent.KEYCODE_SHIFT_RIGHT -> 0x20
            KeyEvent.KEYCODE_ALT_RIGHT -> 0x40
            KeyEvent.KEYCODE_META_RIGHT -> 0x80
            else -> null
        }
    }
}
