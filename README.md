<table>
  <tr>
    <td width="96">
      <img src="cursor.png" alt="USBtoBLHid icon" width="96" />
    </td>
    <td>
      <h1>USBtoBLHid</h1>
    </td>
  </tr>
</table>

<p>
  Here is APK download link:
  <a href="https://github.com/T-Dynamos/USBtoBLHid/releases/download/v1.0/app-release.apk">Download APK</a>
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

## Test footage

Playing minecraft bedrock (PE) with this project.

HUB, mouse and keyboard are connected to another device nearby.



https://github.com/user-attachments/assets/ec3bac91-e6c7-4435-839c-e7046f9011a0



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
