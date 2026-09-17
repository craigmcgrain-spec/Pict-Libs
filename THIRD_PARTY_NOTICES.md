# Speech components and model sources

## Piper voices

Voice selection reference: <https://piper.ttstool.com>

- Lessac High: <https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_US/lessac/high>
  - Model card: <https://huggingface.co/rhasspy/piper-voices/blob/main/en/en_US/lessac/high/MODEL_CARD>
  - Dataset terms linked by that card: <https://www.cstr.ed.ac.uk/projects/blizzard/2013/lessac_blizzard2013/license.html>
- Cori High: <https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_GB/cori/high>
  - Model card: <https://huggingface.co/rhasspy/piper-voices/blob/main/en/en_GB/cori/high/MODEL_CARD>
  - Dataset: public-domain LibriVox recordings, assembled by Bryce Beattie.

The full-quality runtime-compatible Piper model archives are obtained from
<https://github.com/k2-fsa/sherpa-onnx/releases/tag/tts-models>.
Their `MODEL_CARD`, configuration, and phoneme token files are preserved in the APK
under `assets/piper/vits-piper-<voice-id>/`.

## Native runtime

- sherpa-onnx 1.13.8 (Apache-2.0): <https://github.com/k2-fsa/sherpa-onnx/tree/v1.13.8>
- ONNX Runtime (MIT): <https://github.com/microsoft/onnxruntime>
- eSpeak NG phonemizer/data (GPL-3.0): <https://github.com/espeak-ng/espeak-ng>

The downloaded sherpa-onnx AAR supplies the Android inference/phonemization
implementation. Component and model terms are separate from this application's
source; upstream sources above contain their license texts and notices.
