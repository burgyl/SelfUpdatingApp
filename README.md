# Self Updating App
An Android Application that can update itself if new releases in GitHub are available.

Based on [*Implement an in-app update function*](https://medium.com/grandcentrix/implement-an-in-app-updater-1f50fbc38416) by Tom Seifert.

## Requirements
Your releases' tags must be the code version of the app and an APK must be in the binaries.

## What it does
It shows a dialog with the changelog and the name of the last release if there is a new release available.

If the user clicks OK :
- Android 7 and above: Downloads and installs the APK
- Below Android 7 : Opens the release page on a web browser

## Installation

### Gradle dependencies

Add this in your gradle dependencies :
```
implementation group: 'org.apache.httpcomponents', name: 'httpclient-android', version: '4.3.5.1'
implementation 'com.squareup.okhttp3:okhttp:4.6.0'
implementation 'com.squareup.moshi:moshi:1.9.3'
implementation 'com.squareup.okio:okio:2.8.0'
```

### Manifest

In the Manifest, add these permissions in the manifest tag :
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```
And add this provider in the application tag :
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_provider_paths" />
</provider>
```

### Java

Add the entirety of the package `selfupdate`.

In the onCreate of your activity, add this to check if there is an update at the start of the application :
```java
if (savedInstanceState == null) SelfUpdate.checkUpdate(this);
```

### Resources

Add the layouts for the dialogs :
```
layout/content_dialog_download.xml
layout/content_dialog_show_update.xml
```

Add the values :
```
values/dimens.xml
values/strings.xml
```

And add the file provider paths :
```
xml/file_provider_paths.xml
```
