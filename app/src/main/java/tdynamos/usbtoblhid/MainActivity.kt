package tdynamos.usbtoblhid

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.hardware.input.InputManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import tdynamos.usbtoblhid.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import kotlin.math.roundToInt

import android.util.Log;



@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity(), HidUtils.ConnectionStateChangeListener {
    private lateinit var binding: ActivityMainBinding
    private var lastMouseX: Float? = null
    private var lastMouseY: Float? = null
    private var pointerCaptured = false
    private val scrollScale = 3
    private lateinit var inputManager: InputManager

    var bluetoothPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (isEnableBluetooth()) {
                showToast(R.string.toast_bluetooth_on)
            } else {
                showToast(R.string.toast_bluetooth_off)
            }
        }
    }

    var discoverPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == 120) {
            start()
        }
    }
    var connectPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            showToast(R.string.toast_permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inputManager = getSystemService(InputManager::class.java)
        inputManager.registerInputDeviceListener(inputDeviceListener, null)

        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        

        if (Build.VERSION.SDK_INT >= 31 && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            connectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else if (!isEnableBluetooth()) {
            bluetoothPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            discoverPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.root.setOnCapturedPointerListener { _, event ->
                handleCapturedPointer(event)
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inputManager.registerInputDeviceListener(inputDeviceListener, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasMouseDevice()) {
            binding.root.post { binding.root.requestPointerCapture() }
        }
    }

    override fun onPause() {
        super.onPause()
        inputManager.unregisterInputDeviceListener(inputDeviceListener)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasFocus && hasMouseDevice()) {
            binding.root.requestPointerCapture()
        }
    }

    private fun start() {
        HidUtils.registerApp(applicationContext)
        HidConsts.reporters(applicationContext)
        HidUtils.connectionStateChangeListener = this
    }


    override fun onConnecting() {
    }

    override fun onConnected() {
        if (Build.VERSION.SDK_INT >= 31 && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            binding.tvConnectStatus.text = "${getString(R.string.connected)} : ${HidUtils.mDevice!!.name}"
        }

    }

    override fun onDisConnected() {
        GlobalScope.launch(Dispatchers.Main) {
            binding.tvConnectStatus.text = getString(R.string.ununited)
        }

    }


    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!HidUtils.isConnected()) {
            return super.dispatchKeyEvent(event)
        }

        // Send to HID keyboard handler
        if (handleKeyboardEvent(event)) {
            return true
        }

        return super.dispatchKeyEvent(event)
    }

    private fun handleKeyboardEvent(event: KeyEvent): Boolean {
        val device = event.device ?: return false
        if (device.isVirtual) return false

        // Ignore repeats; multi-key presses will be tracked in the buffer
        if (event.repeatCount > 0) return true

        // Handle modifier keys first
        val modifierMask = InputHidMapper.keyCodeToModifierMask(event.keyCode)
        if (modifierMask != null) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                HidConsts.kbdKeyDown("M$modifierMask")
            } else if (event.action == KeyEvent.ACTION_UP) {
                HidConsts.kbdKeyUp("M$modifierMask")
            }
            return true
        }

        // Handle regular keys
        val usage = InputHidMapper.keyCodeToHidUsage(event.keyCode) ?: return false
        if (event.action == KeyEvent.ACTION_DOWN) {
            HidConsts.kbdKeyDown(usage.toString())  // Adds key to KeyBuffer
        } else if (event.action == KeyEvent.ACTION_UP) {
            HidConsts.kbdKeyUp(usage.toString())    // Removes key from KeyBuffer
        }

        return true
    }

    private fun handleCapturedPointer(event: MotionEvent): Boolean {

        // Current button states
        val bs = event.buttonState
        val left   = bs and MotionEvent.BUTTON_PRIMARY   != 0
        val right  = bs and MotionEvent.BUTTON_SECONDARY != 0
        val middle = bs and MotionEvent.BUTTON_TERTIARY  != 0
        
        when (event.actionMasked) {

            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_HOVER_MOVE -> {
                // Use relative motion for smooth fractional accumulation
                val dx = event.getAxisValue(MotionEvent.AXIS_RELATIVE_X)
                val dy = event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)
                // Send via optimized mouseMove function
                if (!HidUtils.isConnected()) return true
                HidConsts.mouseMove(dx, dy, 0f, left, right, middle)
            }

            MotionEvent.ACTION_SCROLL -> {
                // Vertical and horizontal scroll (round to int)
                val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                val hScroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL)
                val wheel = if (vScroll != 0f) vScroll else hScroll

                if (wheel != 0f) {
                    if (!HidUtils.isConnected()) return true
                    HidConsts.mouseMove(0f, 0f, wheel, left, right, middle)
                }
            }

            MotionEvent.ACTION_BUTTON_PRESS,
            MotionEvent.ACTION_BUTTON_RELEASE,
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_UP -> {
                // Only button state changed
                if (!HidUtils.isConnected()) return true
                HidConsts.mouseMove(0f, 0f, 0f, left, right, middle)
            }
        }

        return true
    }
    
    // mouse requestPointerCapture
    private val inputDeviceListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            Log.i("MDEBUG", "MOUSE_ID: " + deviceId.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isMouseDevice(deviceId)) {
                    binding.root.post { binding.root.requestPointerCapture() }
                }
            }
        }
        override fun onInputDeviceRemoved(deviceId: Int) {
        }
        override fun onInputDeviceChanged(deviceId: Int) {
        }
    }

    private fun hasMouseDevice(): Boolean {
        return inputManager.inputDeviceIds.any { isMouseDevice(it) }
    }

    private fun isMouseDevice(deviceId: Int): Boolean {
        val device = inputManager.getInputDevice(deviceId) ?: return false
        val isMouse = device.sources and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE
        return isMouse && !device.isVirtual
    }
}
