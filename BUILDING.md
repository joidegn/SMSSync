## How To Build SMSSync app

This is a Gradle based project. You can build it using Android Studio or from the command line. To 
do so follow the steps below:

1. Install the following software:
       - Android SDK:
         http://developer.android.com/sdk/index.html
       - Gradle:
         http://www.gradle.org/downloads
       - Android Studio - Optional: 
         http://developer.android.com/sdk/installing/studio.html

2. Run the Android SDK Manager by pressing the SDK Manager toolbar button
   in Android Studio or by running the 'android' command in a terminal
   window.

3. In the Android SDK Manager, ensure that the following are installed,
   and are updated to the latest available version:
       - Tools -> Android SDK Platform-tools (rev 20 or above)
       - Tools -> Android SDK Tools (rev 23.0.2 or above)
       - Tools -> Android SDK Build-tools version 20
       - Tools -> Android SDK Build-tools version 19.1
       - Android 4.4 -> SDK Platform (API 8) at least
       - Android L (API 22)
       - Extras -> Android Support Repository
       - Extras -> Android Support Library
       - Extras -> Google Play services
       - Extras -> Google Repository

4. Create a file in the root of the project called local.properties. Enter the path to your Android SDK.
    Eg. `sdk.dir=/opt/android-studio/sdk`

5. Import the project in Android Studio:

    1. Press File > Import Project
    2. Navigate to and choose the settings.gradle file in this project
    3. Press OK


## How To Build SMSSync website

### Preview website

The SMSSync website hosted on github pages is generated using [hugo](http://gohugo.io/).
Please refer to the [hugo's installation](http://gohugo.io/overview/installing/) guide on how to get it running.

```
$ cd website-src

$ hugo server -w

```

You then preview the site at [http://localhost:1313]( http://localhost:1313)

### Compile HTML files

Compile the ruhoh site to HTML so it can be published to a hosting server. We use github pages to host SMSSync's website.

```
$ cd website-src

$ bundle exec ruhoh  compile '<path_to_a_folder_to_compile_the_html_into>'

```

**Note:** The folder to compile the html files into has to be empty as the `ruhoh compile` command will delete any files in it.

### Publish compiled HTML files

Publish the compiled HTML files to [github pages](http://ushahidi.github.io/SMSSync/)

```
$ cd website-src/public

$ git init .

$ git add .

$ git commit -m "<update_message>"

$ git push git@github.com:ushahidi/SMSSync.git master:gh-pages --force

```
## Release Build

To make a release make sure you have `gradle.properties` in the root of the `app` module with the
following content.

**gradle.properties**
```
releaseKeyStore=<key_store_file>
releaseKeyPassword=<key_password>
releaseKeyStorePassword=<key_store_password>
releaseKeyAlias=key_alias
googleDocsForm=<link_to_google_docs_feedback_form>
gPlaystoreServiceAccountEmailAddress=<playstore_service_account_email>
gPlaystorePKFile=<google-playstore-pk-file.p12>
inAppPurchasePubKey=<In-app_purchase_public_key>
googleAnalyticsCode=<google-analytics-code>
```

A typical `gradle.properties` content should look like this:
```
releaseKeyStore=/home/username/.android/debug.keystore
releaseKeyStorePassword=android
releaseKeyAlias=androiddebugkey
releaseKeyPassword=android
googleDocsForm=https://docs.google.com/forms/d/1lL/formResponse
gPlaystoreServiceAccountEmailAddress=9323892392132-842jajdkdadummy@developer.gserviceaccount.com
gPlaystorePKFile=/home/username/pdummy-pk-file.p12
inAppPurchasePubKey=MIDKDLSkdakidkkse8283-23jjkasdk
googleAnalyticsCode=UA-1234567890
```

Then in the project's root directory, issue:

`./release major milestone alpha`

This should build the app, version it, create a tag and push it to the remote repo and publish
to the Google Playstore's alpha track.
