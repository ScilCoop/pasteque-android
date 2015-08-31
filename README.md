# Pasteque Android
> http://www.pasteque.coop

Presentation
============
Pasteque Android is a POS (point of sale) project under the [GNU General Public License v3][gnu].
Our environment is the combination of [Gradle], its [Android plugin][plugin], and the [Android SDK][android]

Quick Start
===========
Download the project.

```
$ git clone https://github.com/ScilCoop/pasteque-android-gradle.git pasteque-android
$ cd pasteque-android
```

Make sure you have installed:
* [Android SDK][android]
* [Gradle]

Specify the path to your Android SDK in the ANDROID_HOME variable

`$ export ANDROID_HOME='your path'`

You can know use Gradle to assemble and install the project on your pluged device with

`$ gradle installVanillaDebug`

Design Decision
===============

Our project holds independent customisations and features for different APKs in the same build.
Everything works with the [Build Variants][flavor] feature of the [Android plugin][plugin]. Run `gradle -q flavors` to get the list of all the **product flavors**

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

Gradle
======
Our repository hold a master gradle: `./build.gradle` and a subproject, which is the application: `./app/build.gradle`.
You mostly have to edit the app subproject's gradle.

You can list all the available tasks with: `gradle tasks`. Every informations can be found in the [android's plugins reference][plugin]

##Add a flavor
To add a flavor, you have to create a new folder with the basic files of Vanilla.

```
cp -r app/src/vanilla app/src/yourFlavorName
```
and add the flavor's packageName in `app/build.gradle`

```
ext {
	flavors = [
		...
		"YourFlavorName": [packageName: "your.flavor.packagename"],
		...
    ]
}
```

Flavors attributes are:
* packageName: the Android 'applicationID' (required)
* applicationName: the 'app_name' of your application  (optionnal)

Tests
=====

##Architecture
There are 2 kinds of tests:

* Instrumental, for tests that requires a device/emulator
	Placed in the folder `app/src/androidTest`, you can run them with: 

	`gradle connectedAndroidTest` / `gradle connectedAndroidTest<VariantName>`

* Unit, for simple java unit tests
	Placed in the folder `app/src/test`, you can run them with:
	
	`gradle test` / `gradle test<VariantName>`

You might want to read [Testing Fundamentals][android_test] for Android tests explinations and the [Testing section of Gradle's Android plugin's documentation][plugin_test] for the gradle's behavior.

##Development

Our tests are powered by [Junit 4.12][junit] with [EasyMock] and his [PowerMock] extension.

You might want to read some [EasyMock Samples][easymock_samples] and the [Android good practice][android_test_practice] section to get some examples.

[android_test_practice]: http://developer.android.com/intl/ko/training/activity-testing/activity-basic-testing.html#build_run
[easymock_samples]: https://github.com/easymock/easymock/tree/master/easymock/src/samples/java/org/easymock/samples
[plugin_test]: http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Testing
[android_test]: http://developer.android.com/intl/ko/tools/testing/testing_android.html
[easymock]: http://easymock.org/
[powermock]: https://code.google.com/p/powermock/wiki/EasyMock
[junit]: http://junit.org/
[build]: http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Building-and-Tasks
[flavor]: http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Build-Variants
[plugin]: http://tools.android.com/tech-docs/new-build-system/user-guide
[android]: https://developer.android.com/sdk/index.html
[gradle]: http://gradle.org/getting-started-android/
[gnu]: http://www.gnu.org/licenses/gpl-3.0.en.html


[gnu]: http://www.gnu.org/licenses/gpl-3.0.en.html
