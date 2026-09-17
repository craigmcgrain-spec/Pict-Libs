import java.net.URI
import java.security.MessageDigest
import java.security.DigestInputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    alias(libs.plugins.android.application)
}

abstract class DownloadPiperArtifact : DefaultTask() {
    @get:Input abstract val sourceUrl: Property<String>
    @get:Input abstract val sha256: Property<String>
    @get:OutputFile abstract val destination: RegularFileProperty

    @TaskAction fun download() {
        val target = destination.get().asFile
        target.parentFile.mkdirs()
        val temporary = target.resolveSibling("${target.name}.part")
        try {
            logger.lifecycle("Downloading ${target.name}")
            val connection = URI(sourceUrl.get()).toURL().openConnection().apply {
                connectTimeout = 30_000
                readTimeout = 120_000
            }
            val digest = MessageDigest.getInstance("SHA-256")
            connection.getInputStream().use { input ->
                DigestInputStream(input, digest).use { verified ->
                    temporary.outputStream().use { verified.copyTo(it) }
                }
            }
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            check(actual == sha256.get()) { "Checksum mismatch for ${target.name}" }
            Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            temporary.delete()
        }
    }
}

val piperRuntime = tasks.register<DownloadPiperArtifact>("downloadPiperRuntime") {
    sourceUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.13.8/sherpa-onnx-1.13.8.aar"
    sha256 = "633c24321e06b1fe79feafa03ea16cbc0f8a286641e2da3559bac91bdb13bd96"
    destination = layout.buildDirectory.file("piperDownloads/sherpa-onnx-1.13.8.aar")
}

val piperModels = listOf(
    "en_US-lessac-high" to "8619d204c7005866fe4f420181dfa79622af6a6222389f0b0818d2af31e0db0e",
    "en_GB-cori-high" to "42922f07738fcde2e49eed4e959635692f73b933de35a6b7c1010162ff566292"
).map { (voice, hash) ->
    tasks.register<DownloadPiperArtifact>("downloadPiper_${voice.replace('-', '_')}") {
        sourceUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-$voice.tar.bz2"
        sha256 = hash
        destination = layout.buildDirectory.file("piperDownloads/vits-piper-$voice.tar.bz2")
    }
}

abstract class PiperAssets : Sync() {
    @get:OutputDirectory abstract val assetRoot: DirectoryProperty
}

val preparePiperAssets = tasks.register<PiperAssets>("preparePiperAssets") {
    dependsOn(piperModels)
    piperModels.forEach { download ->
        from(tarTree(resources.bzip2(download.get().destination.get().asFile)))
    }
    assetRoot = layout.buildDirectory.dir("generated/piperAssets")
    into(assetRoot.dir("piper"))
}

android {
    namespace = "com.example.pict_libs"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pict_libs"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    androidResources.noCompress += "onnx"
}

androidComponents.onVariants { variant ->
    variant.sources.assets?.addGeneratedSourceDirectory(preparePiperAssets) { it.assetRoot }
}

dependencies {
    implementation(files(piperRuntime.flatMap { it.destination }).builtBy(piperRuntime))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.androidx.junit)
}
