# The runtime of Android OSGi

The runtime is based on [Apache Felix](http://felix.apache.org) and integrated
with Android application framework. It's aimed for providing a flexible and
dynamic module system for Android applications.

# Get started

## Install Android SDK

You can download it from [here](http://developer.android.com/sdk/index.html),
if you have already installed, please ignore this.

## Install Apache Ant

The apache ant is required for building & launching the runtime,and the version
was supposed be higher than 1.8, the binary distributions could be found at
[here](http://archive.apache.org/dist/ant/binaries/).

## Download the source code

`git clone https://github.com/aosgi/com.aosgi.runtime.git`

## Build

Before build, you have to update the project:
```
$ cd com.aosgi.runtime/main
$ android update project -p .
```

Then build the source code using Apache Ant:
```
$ ant debug
```

If build complete, `com.aosgi.runtime-debug.apk` could be found under the `bin`
directory, then you can install it.

## Install

Before install, you have to connect your Android device by USB cable or launch
the android emulator. The installation process is very simple, just execute
the following command in the terminal:
```
$ ant installd
```

## Run

You can launch the runtime by executing the following command in the terminal:
```
$ ant run
```

Then you wil find the Android OSGi Runtime has been started.

