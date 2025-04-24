# 1. **Setting Up the Emulator (Without Android Studio)**
ChatGPT guide to emulating a camera and running knightvision.

## a. **Create an AVD (Android Virtual Device)**

You can use the `avdmanager` tool to create an Android Virtual Device (AVD) for testing.

1. Open your terminal and navigate to the **SDK** directory (`$ANDROID_HOME/tools/bin`).
2. Run the following command to list available devices:
   ```bash
   ./avdmanager list device
   ```
3. Create an AVD by specifying a device and a system image (you can choose an image with camera support, like `google_apis` or `x86_64`):
   ```bash
   ./avdmanager create avd -n MyEmulator -k "system-images;android-30;google_apis;x86_64"
   ```
   This creates a new virtual device named `MyEmulator`.

## b. **Start the Emulator**

Once the AVD is created, you can start it using the `emulator` command:

```bash
$ANDROID_HOME/emulator/emulator -avd MyEmulator
```

This will start the emulator in a new window. Make sure the camera is enabled in the emulator settings (for back/front cameras).

---

# 2. **Running Your App on the Emulator (Without Android Studio)**

You can build and install your app on the emulator using the **Gradle** and **ADB** (Android Debug Bridge) tools.

## a. **Build the APK**

First, ensure you have the **Gradle** tools set up to build your Android project.

Run the following command from the root of your project to build the APK:

```bash
./gradlew assembleDebug
```

This will create the APK in the `build/outputs/apk/debug/` directory.

## b. **Install the APK on the Emulator**

Once the APK is built, use `adb` to install it on the emulator:

1. Ensure the emulator is running.
2. Run the following command to install the APK:
   ```bash
   adb -s emulator-5554 install path/to/your/app-debug.apk
   ```
   Replace `path/to/your/app-debug.apk` with the actual path to your generated APK.

   You can find the device ID (e.g., `emulator-5554`) by running:

   ```bash
   adb devices
   ```

   This will list all connected devices/emulators.

---

# 3. **Running Instrumented Tests (Camera Preview)**

Once the emulator is running and the app is installed, you can run **instrumented tests** on the emulator from the command line using **Gradle**.

## a. **Running Instrumented Tests**

Use the following command to run instrumented tests on the connected emulator:

```bash
./gradlew connectedAndroidTest
```

This command will:
- Build the app.
- Install it on the emulator.
- Run your tests (e.g., UI tests, camera tests, etc.).

You can verify camera previews and other UI interactions by asserting the visibility and behavior of the UI elements in the tests.

## b. **Testing Camera Preview (UI Tests)**

For UI tests, you can use the **Compose Test Framework** or **Espresso** to assert that the camera preview (`PreviewView`) is rendered and that any UI elements like buttons or interactions work as expected.

Example using Compose Test Framework:

```kotlin
@Composable
fun testCameraPreview() {
    composeTestRule.setContent {
        CameraPreviewView(cameraProvider = mockCameraProvider())  // Mock or real provider
    }

    composeTestRule.onNodeWithTag("previewView")
        .assertExists()  // Ensure the PreviewView is rendered
}
```

Run the test:

```bash
./gradlew connectedAndroidTest
```

---

# 4. **Simulating Camera Input in the Emulator**

You can simulate the camera feed using **Extended Controls** in the emulator. While you can't use Android Studio’s GUI, you can still use the command line to access extended controls.

1. **Simulate the Camera Input**:
   - You can inject video or image files as a camera input through `adb` commands.
   - Run the following to start the camera feed with a file:
     ```bash
     adb -s emulator-5554 shell am broadcast -a android.intent.action.VIEW -d file:///path/to/video_or_image_file
     ```

2. **Check for Camera Functionality**:
   - If your app uses the camera for taking photos, recording video, or displaying the preview, ensure that you have set up the **camera permissions** in the emulator’s settings (Settings → Apps → Your app → Permissions).

---

# 5. **Monitoring Logs for Camera Tests**

Use `adb logcat` to view logs related to your camera tests and app behavior.

Run:

```bash
adb logcat
```

This will print logs from the emulator. You can filter out camera-related logs by searching for keywords like `Camera`, `Preview`, etc.

You can also filter logs from your app specifically:

```bash
adb logcat | grep "YourAppTag"
```

---

# Summary of Steps:
1. **Set up the Emulator**: Use `avdmanager` to create and start the emulator.
2. **Build and Install the App**: Use `gradlew` to build the app and `adb` to install it.
3. **Run Instrumented Tests**: Use `gradlew connectedAndroidTest` to run UI or camera tests.
4. **Simulate Camera Input**: Use the emulator's extended controls to simulate camera input.
5. **Monitor Logs**: Use `adb logcat` to view logs for debugging.
