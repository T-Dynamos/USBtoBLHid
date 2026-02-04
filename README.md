# USBtoBLHid
<p align="center">
  <img src="cursor.png" alt="USBtoBLHid icon" width="160" />
</p>

USB HID to Bluetooth HID converter app for Android. 

Turn your wired USB keyboard and mouse into a Bluetooth keyboard and mouse by using an Android device as a bridge.

## What this project does
- Take input from USB keyboard or USB mouse.
- Send it as Bluetooth HID (keyboard + mouse) to other device.
- So you can type or move on other device without cable.

## How it works
- Android register as Bluetooth HID Device profile.
- App listen for input events from connected USB devices.
- App send HID reports over Bluetooth to the paired device.

## Requirements
- Android 9+ (API 28+).
- Bluetooth need be enabled and device set discoverable to pair.
- USB OTG and HUB needed for keyboard or mouse.

## Build

Use Gradle version 7.3.3.  
Download link: [Gradle 7.3.3 binary](https://services.gradle.org/distributions/gradle-7.3.3-bin.zip) (unzip to get `./gradle/` folder).
Example build and install command:

```bash
ANDROID_HOME=~/.buildozer/android/platform/android-sdk ./gradle/bin/gradle assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk
```

## Note about input movement

Reference for motion and input: https://developer.android.com/develop/ui/views/touch-and-input/gestures/movement

## Original author and attribution
This project is based on original work: https://github.com/LiangLuDev/HidPeripheral


App icon credit: <a href="https://www.flaticon.com/free-icons/pointer" title="pointer icons">Pointer icons created by meaicon - Flaticon</a>
