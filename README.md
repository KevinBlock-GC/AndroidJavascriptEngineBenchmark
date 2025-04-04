# Android Javascript Engine Benchmark
A small application to benchmark various javascript engines in Android.  All tests were ran on a Samsung Galaxy S23+

#### App Benchmark Results
The benchmark script [can be found here](https://github.com/KevinBlock-GC/AndroidJavascriptEngineBenchmark/blob/29d8fc37448aded7198ce98a5284a906c871f114/app/src/main/java/com/example/jsengines/MainActivity.kt#L42). This script is executed 25 times every time a button is clicked.

![Runtime per 25 script executions](https://github.com/user-attachments/assets/d372f8c4-abd1-4ee7-9d86-589c75790b75)


### Micro Benchmark Results
```
J2V8: 34,030 ns (4 allocs)
Javet: 71,219 ns (9 allocs)
Duktape: 128,174 ns (4 allocs)
QuickJS: 147,565 ns (7 allocs)
AndroidJavascript: 1,330,219 ns (123 allocs)
```

#### APK Size With Engine
These results simply use Android's APK analyze feature and looking at the "Download Size"

![APK Download Size](https://github.com/KevinBlock-GC/AndroidJavascriptEngineBenchmark/assets/112961407/4c1ffae7-0912-4445-8dd1-5019558e8f76)
