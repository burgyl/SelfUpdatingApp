# Self Updating App
An Android Application that can update itself if new releases in GitHub are available.

Based on [*Implement an in-app update function*](https://medium.com/grandcentrix/implement-an-in-app-updater-1f50fbc38416) by Tom Seifert.

## Requirements
Your releases' tags must be the code version of the app and an APK must be in the binaries.

## What it does
It shows a dialog with the changelog and the name of the last release.

If the user clicks OK :
- Android 7 and above: Downloads and installs the APK
- Below Android 7 : Opens the release page on a web browser

## Installation
