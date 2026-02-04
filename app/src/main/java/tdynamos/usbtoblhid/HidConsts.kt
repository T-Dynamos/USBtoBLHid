package tdynamos.usbtoblhid

import android.bluetooth.BluetoothHidDevice
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

import android.util.Log;

object HidConsts {
    const val TAG = "u-HidConsts"
    const val NAME = "BS-HID-Peripheral"
    const val DESCRIPTION = "fac"
    const val PROVIDER = "funny"

    @JvmField
    var HidDevice: BluetoothHidDevice? = null

    private var handler: Handler? = null
    private val inputReportQueue: Queue<HidReport> = ConcurrentLinkedQueue()

    private fun addInputReport(inputReport: HidReport?) {
        if (inputReport != null) {
            inputReportQueue.offer(inputReport)
        }
    }

    var scheperoid: Long = 5
    fun reporters(context: Context) {
        handler = Handler(context.mainLooper)
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val report = inputReportQueue.poll()
                if (report != null && HidUtils.isConnected()) {
                    postReport(report)
                }

            }
        }, 0, scheperoid)
    }

    private fun postReport(report: HidReport) {
        HidReport.SendState = HidReport.State.Sending
        val ret = HidDevice!!.sendReport(HidUtils.mDevice, report.ReportId.toInt(), report.ReportData)
        if (!ret) {
            HidReport.SendState = HidReport.State.Failded
        } else {
            HidReport.SendState = HidReport.State.Sended
        }
    }

    fun sendMouseReport(reportData: ByteArray?) {
        val report = HidReport(HidReport.DeviceType.Mouse, 0x01.toByte(), reportData!!)
        addInputReport(report)
    }

    private val MouseReport = HidReport(HidReport.DeviceType.Mouse, 0x01.toByte(), byteArrayOf(0, 0, 0, 0))
    fun mouseMove(dx: Float, dy: Float, wheel: Float, leftButton: Boolean, rightButton: Boolean, middleButton: Boolean) {
        var dx = dx.toInt()
        var dy = dy.toInt()
        var wheel = wheel.toInt()

        val b1 = leftButton
        val b2 = rightButton
        val b3 = middleButton

        // Log.i("MDEBUG", "$dx, $dy, $wheel $b1 $b2 $b3")


        if (dx > 127) dx = 127
        if (dx < -127) dx = -127
        if (dy > 127) dy = 127
        if (dy < -127) dy = -127
        if (wheel > 127) wheel = 127
        if (wheel < -127) wheel = -127
        if (leftButton) {
            MouseReport.ReportData[0] = MouseReport.ReportData[0] or 1
        } else {
            MouseReport.ReportData[0] = (MouseReport.ReportData[0] and 1.inv()).toByte()
        }
        if (rightButton) {
            MouseReport.ReportData[0] = MouseReport.ReportData[0] or 2
        } else {
            MouseReport.ReportData[0] = (MouseReport.ReportData[0] and 2.inv()).toByte()
        }
        if (middleButton) {
            MouseReport.ReportData[0] = MouseReport.ReportData[0] or 4
        } else {
            MouseReport.ReportData[0] = (MouseReport.ReportData[0] and 4.inv()).toByte()
        }
        MouseReport.ReportData[1] = dx.toByte()
        MouseReport.ReportData[2] = dy.toByte()
        MouseReport.ReportData[3] = wheel.toByte()
        addInputReport(MouseReport)
    }

    private var ModifierByte: Byte = 0
    // 6-key rollover buffer
    private val keyBuffer = ByteArray(6) { 0 }


    private fun addKey(key: Byte) {
        for (i in keyBuffer.indices) {
            if (keyBuffer[i] == 0.toByte()) {
                keyBuffer[i] = key
                return
            }
        }
    }

    private fun removeKey(key: Byte) {
        for (i in keyBuffer.indices) {
            if (keyBuffer[i] == key) {
                keyBuffer[i] = 0
            }
        }
    }


    fun modifierDown(usageId: Byte): Byte {
        synchronized(HidConsts::class.java) {
            ModifierByte = ModifierByte or usageId
        }
        return ModifierByte
    }

    fun modifierUp(usageId: Byte): Byte {
        val inv = usageId.inv().toByte()
        synchronized(HidConsts::class.java) {
            ModifierByte = (ModifierByte and inv).toByte()
        }
        return ModifierByte
    }

    fun kbdKeyDown(usageStr: String) {
        if (usageStr.isEmpty()) return

        synchronized(HidConsts::class.java) {

            if (usageStr.startsWith("M")) {
                val mod = modifierDown(
                    usageStr.removePrefix("M").toInt().toByte()
                )
                sendFullKeyReport()
            } else {
                val key = usageStr.toInt().toByte()
                addKey(key)
                sendFullKeyReport()
            }
        }
    }


    fun kbdKeyUp(usageStr: String) {
        if (usageStr.isEmpty()) return

        synchronized(HidConsts::class.java) {

            if (usageStr.startsWith("M")) {
                modifierUp(
                    usageStr.removePrefix("M").toInt().toByte()
                )
            } else {
                val key = usageStr.toInt().toByte()
                removeKey(key)
            }

            sendFullKeyReport()
        }
    }

    private fun sendFullKeyReport() {

        val reportData = ByteArray(8)

        reportData[0] = ModifierByte
        reportData[1] = 0 // reserved
        
        for (i in 0 until 6) {
            reportData[2 + i] = keyBuffer[i]
        }

        val report = HidReport(
            HidReport.DeviceType.Keyboard,
            0x02.toByte(),
            reportData
        )

        addInputReport(report)
    }


    fun intArrayToByteArray(vararg values: Int): ByteArray = ByteArray(values.size) { i -> values[i].toByte() }

    @JvmField
    val Descriptor = intArrayToByteArray(
        // ----- MOUSE -----
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x02,       // Usage (Mouse)
        0xA1, 0x01,       // Collection (Application)
        0x09, 0x01,       //   Usage (Pointer)
        0xA1, 0x00,       //   Collection (Physical)
        0x85, 0x01,       //   Report ID = 1

        // Mouse buttons
        0x05, 0x09,       //     Usage Page (Button)
        0x19, 0x01,       //     Usage Minimum (Button 1)
        0x29, 0x03,       //     Usage Maximum (Button 3)
        0x15, 0x00,       //     Logical Minimum 0
        0x25, 0x01,       //     Logical Maximum 1
        0x95, 0x03,       //     Report Count = 3 (3 buttons)
        0x75, 0x01,       //     Report Size = 1 bit
        0x81, 0x02,       //     Input (Data, Variable, Absolute)

        // Padding for buttons (to fill 1 byte)
        0x95, 0x01,       //     Report Count = 1
        0x75, 0x05,       //     Report Size = 5 bits
        0x81, 0x03,       //     Input (Constant, Variable, Absolute)

        // X/Y/Wheel movement
        0x05, 0x01,       //     Usage Page (Generic Desktop)
        0x09, 0x30,       //     Usage X
        0x09, 0x31,       //     Usage Y
        0x09, 0x38,       //     Usage Wheel
        0x15, 0x81,       //     Logical Minimum -127
        0x25, 0x7F,       //     Logical Maximum 127
        0x75, 0x08,       //     Report Size = 8 bits
        0x95, 0x03,       //     Report Count = 3
        0x81, 0x06,       //     Input (Data, Variable, Relative)

        0xC0,             //   End Physical Collection
        0xC0,             // End Application Collection

        // ----- KEYBOARD -----
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x06,       // Usage (Keyboard)
        0xA1, 0x01,       // Collection (Application)
        0x85, 0x02,       // Report ID = 2

        // Modifiers (Ctrl/Shift/Alt/GUI)
        0x05, 0x07,       // Usage Page (Keyboard/Keypad)
        0x19, 0xE0,       // Usage Minimum (Left Ctrl)
        0x29, 0xE7,       // Usage Maximum (Right GUI)
        0x15, 0x00,       // Logical Minimum 0
        0x25, 0x01,       // Logical Maximum 1
        0x75, 0x01,       // Report Size = 1 bit per modifier
        0x95, 0x08,       // Report Count = 8 bits (all modifiers)
        0x81, 0x02,       // Input (Data, Variable, Absolute)

        // Reserved byte
        0x75, 0x08,       // Report Size = 8 bits
        0x95, 0x01,       // Report Count = 1
        0x81, 0x01,       // Input (Constant) – reserved for alignment

        // Keycodes (6-key rollover)
        0x05, 0x07,       // Usage Page (Keyboard/Keypad)
        0x19, 0x00,       // Usage Minimum = 0
        0x29, 0x65,       // Usage Maximum = 101
        0x15, 0x00,       // Logical Minimum = 0
        0x25, 0x65,       // Logical Maximum = 101
        0x75, 0x08,       // Report Size = 8 bits per key
        0x95, 0x06,       // Report Count = 6 keys
        0x81, 0x00,       // Input (Data, Array, Absolute)

        // LED Output (CapsLock/NumLock/etc)
        0x05, 0x08,       // Usage Page (LEDs)
        0x19, 0x01,       // Usage Minimum = Num Lock
        0x29, 0x05,       // Usage Maximum = Kana
        0x75, 0x01,       // Report Size = 1 bit per LED
        0x95, 0x05,       // Report Count = 5 LEDs
        0x91, 0x02,       // Output (Data, Variable, Absolute)

        // Padding for LEDs
        0x75, 0x03,       // Report Size = 3 bits
        0x95, 0x01,       // Report Count = 1
        0x91, 0x03,       // Output (Constant, Variable, Absolute)
        0xC0              // End Keyboard Collection
    )

}
