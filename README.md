# Double-D 2D Management — Android

Android WebView shell with local SQLite persistence for agents, betting entries, and session results.

## Build
GitHub Actions builds a debug APK on every push to `main`, and can also be started manually from Actions → Build Double-D Android APK → Run workflow.

## Important
The app's web UI must be placed at `app/src/main/assets/index.html`. The APK build workflow will fail until that asset is added. The original UI source was prepared in the ChatGPT workspace; this repository setup is the build pipeline.
