# Pasteque Android
> http://www.pasteque.coop

Presentation
============
Pasteque Android is a POS (point of sale) project under the [GNU General Public License v3][gnu].
Our environment is the combinaison of [Gradle][gradle] and the [Android SDK][android] 

Quick Start
===========
Download the project.
```
$ git clone https://github.com/ScilCoop/pasteque-android-gradle.git pasteque-android
$ cd pasteque-android
```

Make sure you have installed the last:
* [Android SDK][android] 
* [Gradle][gradle]

Specify the path to your Android SDK in the ANDROID_HOME variable

`$ export ANDROID_HOME='your path'`

You can know use Gradle to assemble and install the project on your pluged device with

`$ gradle installVanillaDebug`

[android]: https://developer.android.com/sdk/index.html
[gradle]: http://gradle.org/getting-started-android/
[gnu]: http://www.gnu.org/licenses/gpl-3.0.en.html
Readme powered by Markdown Syntax
