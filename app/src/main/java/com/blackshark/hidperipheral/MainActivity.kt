package com.blackshark.hidperipheral

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
import com.blackshark.hidperipheral.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import kotlin.math.roundToInt


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

        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()

        if (Build.VERSION.SDK_INT >= 31 && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            connectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else if (!isEnableBluetooth()) {
            bluetoothPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            discoverPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.root.requestPointerCapture()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.root.setOnCapturedPointerListener { _, event ->
                if (!HidUtils.isConnected()) return@setOnCapturedPointerListener false
                handleCapturedPointer(event)
            }

            binding.root.setOnLongClickListener {
                if (pointerCaptured) {
                    binding.root.releasePointerCapture()
                } else {
                    binding.root.requestPointerCapture()
                }
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

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
        pointerCaptured = hasCapture
        if (hasCapture) {
            lastMouseX = null
            lastMouseY = null
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

        if (handleKeyboardEvent(event)) {
            return true
        }

        return super.dispatchKeyEvent(event)
    }

    private fun handleCapturedPointer(event: MotionEvent): Boolean {
        val buttonState = event.buttonState
        val left = buttonState and MotionEvent.BUTTON_PRIMARY != 0
        val right = buttonState and MotionEvent.BUTTON_SECONDARY != 0
        val middle = buttonState and MotionEvent.BUTTON_TERTIARY != 0

        when (event.actionMasked) {
            MotionEvent.ACTION_HOVER_MOVE,
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x.roundToInt()
                val dy = event.y.roundToInt()
                if (dx != 0 || dy != 0) {
                    HidConsts.mouseMove(dx, dy, 0, left, right, middle)
                }
            }
            MotionEvent.ACTION_BUTTON_PRESS,
            MotionEvent.ACTION_BUTTON_RELEASE,
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_UP -> {
                HidConsts.mouseMove(0, 0, 0, left, right, middle)
            }
            MotionEvent.ACTION_SCROLL -> {
                val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL).roundToInt() * scrollScale
                val hScroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL).roundToInt() * scrollScale
                val wheel = if (vScroll != 0) vScroll else hScroll
                if (wheel != 0) {
                    HidConsts.mouseMove(0, 0, wheel, left, right, middle)
                }
            }
        }

        return true
    }

    private fun handleKeyboardEvent(event: KeyEvent): Boolean {
        if (event.device?.isVirtual == true) {
            return false
        }

        if (event.repeatCount > 0) {
            return true
        }

        val modifierMask = InputHidMapper.keyCodeToModifierMask(event.keyCode)
        if (modifierMask != null) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                HidConsts.kbdKeyDown("M$modifierMask")
            } else if (event.action == KeyEvent.ACTION_UP) {
                HidConsts.kbdKeyUp("M$modifierMask")
            }
            return true
        }

        val usage = InputHidMapper.keyCodeToHidUsage(event.keyCode) ?: return false
        if (event.action == KeyEvent.ACTION_DOWN) {
            HidConsts.kbdKeyDown(usage.toString())
        } else if (event.action == KeyEvent.ACTION_UP) {
            HidConsts.kbdKeyUp(usage.toString())
        }
        return true
    }


    private val inputDeviceListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isMouseDevice(deviceId)) {
                    binding.root.post { binding.root.requestPointerCapture() }
                }
            }
        }

        override fun onInputDeviceRemoved(deviceId: Int) {}

        override fun onInputDeviceChanged(deviceId: Int) {}
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
