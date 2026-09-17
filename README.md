# Pict-Libs

An Android picture-word game. Play six rounds, choosing **one word from twelve
illustrated options in each round**. After the sixth pick, your silly sentence
appears automatically.

The game selects a six-blank sentence template before the first round. Each blank
specifies a category and themed subcategory; its twelve random options all match
that requirement. Themes include animal and food nouns, looks/textures and mood
adjectives, and movement and sound verbs. Each theme has sixteen illustrated words.
All six picks fill their exact slots, and verbs use the form required by the template.
**Undo last pick** lets you revisit a round; rotation preserves your picks and pool.

## Run in Android Studio

1. Open `/home/mcgrain/Projects/Pict-Libs` (the folder containing `settings.gradle.kts`).
2. Choose **File → Sync Project with Gradle Files** and let sync finish.
3. Select **Pict-Libs** in the run-configuration dropdown. The shared configuration
   is in `.run/Pict-Libs.run.xml` and launches the `app` module's default activity.
4. In **Tools → Device Manager**, start **Pixel_9_Pro**, or select a connected
   Android device running Android 7.0 / API 24 or newer with USB debugging enabled.
5. Click **Run ▶** to build, install, and launch. Click **Debug** to use breakpoints.

If the new configuration is not listed yet, reopen the project or create an
**Android App** configuration under **Run → Edit Configurations**, choose module
`Pict-Libs.app`, and set Launch to **Default Activity**.

The project uses Android SDK 37, Android Gradle Plugin 9.4.0, and the included
Gradle 9.6.0 wrapper. Use an Android Studio version compatible with that plugin.
Install SDK Platform 37 via SDK Manager if prompted. Gradle's daemon JVM is
configured in `gradle/gradle-daemon-jvm.properties` (Java 25); Java source/target
compatibility is separately set to Java 11.

## Automated tests

With an emulator or device running, open
`app/src/androidTest/java/com/example/pict_libs/GameFlowTest.kt` and click the
green gutter arrow beside the test class. The tests check six separate twelve-card
pools, round prompts, rotation, undo, automatic sentence display, and fresh replay.
`GameSessionTest.kt` under `app/src/test` also checks category constraints, all
templates, invalid picks, word-pool variety, and session restoration.

From the project root:

```sh
./gradlew assembleDebug testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

Device-test reports: `app/build/reports/androidTests/connected/debug/index.html`.
Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Manual check

- Verify the prompt shows **Pick 1 of 6**, a category, and its theme.
- Tap one of twelve cards; confirm **Pick 2 of 6** and a new matching pool.
- Use **Undo last pick** to revisit the previous blank.
- Continue through six rounds; confirm the sentence opens automatically and uses
  all six words.
- Use **Change last word** to revisit round six, or **Play Again** to start a new game.

## Step 2: Piper read-aloud

The app bundles the two full-quality Piper voices requested from
[Piper TTS Tool](https://piper.ttstool.com):

- **Lessac High · English (US)** — `en_US-lessac-high`
- **Cori High · English (UK)** — `en_GB-cori-high`

Both produce 22,050 Hz audio. The Android native runtime is **sherpa-onnx 1.13.8**,
which executes these Piper VITS/ONNX models and performs eSpeak phonemization.
Models are the full-quality Piper conversions distributed by sherpa-onnx, not the
smaller quantized variants. No website, Android system TTS engine, API key, or
network connection is used during speech generation.

On the sentence screen, choose a voice and tap **Read my sentence**. The first
use copies bundled model assets into app-private storage; loading a voice and
generating speech run on a background worker. The button becomes **Stop reading**
during generation and playback. Voice selection is remembered. Playback stops
when you leave or rotate the screen, and cancelled synthesis results are discarded.
Use device media volume to control loudness. **Reload Piper voices** retries setup.

### Build assets

The first Android Studio build automatically downloads approximately **281 MB**:
two model archives and the Android runtime AAR. Downloads have pinned SHA-256
checksums in `app/build.gradle.kts` and are cached under `app/build/piperDownloads`.
Extracted assets are generated under `app/build/generated/piperAssets`; large
binary files are not stored in Git. `clean` removes these build caches.

To prepare them explicitly before the first build:

```sh
./gradlew preparePiperAssets downloadPiperRuntime
```

Both models are included in the APK, making installation larger than the original
game. First-use app storage also contains a copy of the models and phonemizer data.
The supplied runtime includes ARM and x86 Android libraries, including the x86_64
emulator used for testing. Model cards remain bundled alongside the models;
see `THIRD_PARTY_NOTICES.md` for upstream sources.

### Speech tests

`SpeechControllerTest` covers playback state, preferences, errors, stale callbacks,
and cleanup. `StorySpeechTest` checks the two voice options, stop, and rotation.
`PiperSynthesisTest` generates non-silent audio with **both actual models**, checks
the sample rate, and verifies synthesis-to-playback completion for both voices.
Run with `./gradlew connectedDebugAndroidTest` on a running emulator or device.

### Future sample voices

These are pretrained Piper voices. Creating a new voice from your own samples is
not implemented; that requires preparing/training a model or integrating a
sample-based synthesis provider through `speech/SpeechEngine.kt`.
