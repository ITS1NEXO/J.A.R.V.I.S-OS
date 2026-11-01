# ZORIN-M4 Native Android Edition

A native Kotlin Android app, separate from the J.A.R.V.I.S-OS launcher source.

## Features
- Native dark navy / electric-blue chat UI.
- OpenAI-compatible API settings (Groq, Gemini endpoint, OpenAI).
- SQLite chat history and explicit `remember <fact>` local memories.
- Android speech recognition input and Text-to-Speech output.
- Android 8.0+ (API 26).

## Build
The repository GitHub Actions workflow builds this project and uploads `app-debug.apk` as an artifact. You can also open this folder in Android Studio and build the debug APK.

## Setup
Open Settings in the app and enter a provider base URL, model name, and API key. Example endpoints:
- Groq: `https://api.groq.com/openai/v1`
- Gemini: `https://generativelanguage.googleapis.com/v1beta/openai`
- OpenAI: `https://api.openai.com/v1`

Cloud AI needs internet and a valid API key. The key is stored in app-private preferences on the device. Keep it private.

## Scope
This is a clean native Android rewrite of the supported chat, provider, memory, and voice features. Windows automation, PC-only model servers, screen scraping, and silent control of other apps are not included. The original Windows Python source archive had flattened package paths and could not be reused as an Android backend.
