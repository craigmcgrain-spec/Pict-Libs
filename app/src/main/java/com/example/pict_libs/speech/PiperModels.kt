package com.example.pict_libs.speech

import android.content.Context
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import java.io.File

object PiperModels {
    val voices = listOf(
        SpeechVoice("en_US-lessac-high", "Lessac High · English (US)"),
        SpeechVoice("en_GB-cori-high", "Cori High · English (UK)")
    )

    /** Called on the synthesis worker, never on the UI thread. */
    @Synchronized
    fun prepare(context: Context): File {
        val root = File(context.noBackupFilesDir, "piper-v1")
        if (File(root, ".ready").isFile) return root
        root.mkdirs()
        copyAssets(context, "piper", root)
        File(root, ".ready").writeText("sherpa-onnx-1.13.8/full-quality")
        return root
    }

    fun create(root: File, voiceId: String): OfflineTts {
        require(voices.any { it.id == voiceId })
        val directory = File(root, "vits-piper-$voiceId")
        return OfflineTts(config = OfflineTtsConfig(
            model = OfflineTtsModelConfig(
                vits = OfflineTtsVitsModelConfig(
                    model = File(directory, "$voiceId.onnx").absolutePath,
                    tokens = File(directory, "tokens.txt").absolutePath,
                    dataDir = File(directory, "espeak-ng-data").absolutePath
                ),
                numThreads = 2,
                provider = "cpu"
            )
        ))
    }

    private fun copyAssets(context: Context, source: String, destination: File) {
        val children = context.assets.list(source).orEmpty()
        if (children.isEmpty()) {
            context.assets.open(source).use { input ->
                destination.outputStream().use { input.copyTo(it) }
            }
        } else {
            destination.mkdirs()
            children.forEach { copyAssets(context, "$source/$it", File(destination, it)) }
        }
    }
}
