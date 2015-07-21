# Pasteque Android
> http://www.pasteque.coop

Presentation
============
Pasteque Android is a POS (point of sale) project under the [GNU General Public License v3][gnu].
Our environment is the combinaison of [Gradle], his [Android plugin][plugin], and the [Android SDK][android]

Quick Start
===========
Download the project.
```
$ git clone https://github.com/ScilCoop/pasteque-android-gradle.git pasteque-android
$ cd pasteque-android
```

Make sure you have installed the last:
* [Android SDK][android]
* [Gradle]

Specify the path to your Android SDK in the ANDROID_HOME variable

`$ export ANDROID_HOME='your path'`

You can know use Gradle to assemble and install the project on your pluged device with

`$ gradle installVanillaDebug`

Design Decision
===============

Our project holds independent customisations and features for different APKs in the same build.
Everything works with the [Build Variants][flavor] feature of the [Android plugin][plugin]. Run `gradle flavors` to get the list of all the **product flavors** (in development)

> Build Type + Product Flavor = Build Variant

> Quick Start exemple: Debug + Vanilla = VanillaDebug

You can build the project by: **build type**, **product flavor** or **build variant** with:

1. `gradle assemble<Variant Name>`
2. `gradle assemble<Build Type Name>`
3. `gradle assemble<Product Flavor Name>`

But you can only install **build variants** on your device with:

`$ gradle install<VariantName>`

Our current project holds:

| Build Type | productFlavor |
|:----------:|:-------------:|
|    Debug   |    Vanilla    |
|   Release* |      Bnp      |
|            |      Wcr      |

(*) release not available right now

Tests
=====
> Work in progress


[build]: http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Building-and-Tasks
[flavor]: http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Build-Variants
[plugin]: http://tools.android.com/tech-docs/new-build-system/user-guide
[android]: https://developer.android.com/sdk/index.html
[gradle]: http://gradle.org/getting-started-android/
[gnu]: http://www.gnu.org/licenses/gpl-3.0.en.html
###### Readme powered by Markdown Syntax
