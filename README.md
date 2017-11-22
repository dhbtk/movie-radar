# Movie Radar

Build this project with gradle:

	./gradlew assembleDebug

## Libraries used

- *Anko:* used for the toast and intent shorthand functions.
- *Dagger:* used to provide dependency injection to ease configuring Retrofit.
- *Retrofit 2:* used as the API client, chosen because it has easy RxJava integration via its adapter.
- *Jackson:* used as the JSON deserializer. I chose it over Google's gson because I'm more familiar with it.
- *ThreeTenABP:* a JSR-310 backport, given that the Java 8 date/time APIs aren't available on Android unless you target the very latest versions.
- *Picasso:* used to easily load all the movie posters and background images. Has a really nice API.
- *RxJava:* used to better manage async operations, like interacting with the API.
- *RxAndroid:* used to integrate Android's threading with RxJava's scheduling mechanism.
- *RxKotlin:* used to better integrate RxJava's API with Kotlin.
- *RxBinding:* used to observe and debounce user input on the search input.
