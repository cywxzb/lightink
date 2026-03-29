---
name: "coil-expert"
description: "Expert in Coil image loading library for Android. Invoke when needing help with Coil configuration, usage, or migration from other image loading libraries like Glide."
---

# Coil Expert

This skill provides expert guidance on Coil, the modern image loading library for Android, including:

## Key Features

- **Coil Configuration**: Help set up Coil in Android projects, including dependencies and initialization
- **Migration Support**: Assist in migrating from other image loading libraries (Glide, Picasso) to Coil
- **Advanced Usage**: Provide guidance on Coil's advanced features like transformations, caching, and request customization
- **Performance Optimization**: Offer recommendations for optimizing Coil performance and memory usage
- **Troubleshooting**: Diagnose and fix common Coil issues and errors

## When to Invoke

- When setting up Coil in a new project
- When migrating from Glide or Picasso to Coil
- When encountering Coil-related errors or performance issues
- When needing to implement advanced image loading features
- When optimizing image loading performance

## Common Use Cases

### Migration from Glide to Coil
```kotlin
// Glide
Glide.with(context).load(url).into(imageView)

// Coil
imageView.load(url)
```

### Advanced Image Transformations
```kotlin
imageView.load(url) {
    transformations(
        RoundedCornersTransformation(16.dp),
        CircleCropTransformation()
    )
    crossfade(true)
}
```

### Custom Caching Strategy
```kotlin
imageView.load(url) {
    memoryCachePolicy(CachePolicy.ENABLED)
    diskCachePolicy(CachePolicy.ENABLED)
    networkCachePolicy(CachePolicy.ENABLED)
}
```

## Troubleshooting Guide

### Common Issues
- **Image not loading**: Check network permissions, URL validity, and error listeners
- **Out of memory errors**: Use appropriate image sizes and memory cache policies
- **Slow loading**: Implement proper caching and consider using placeholder images
- **Transformation issues**: Ensure correct transformation order and parameters

### Performance Tips
- Use appropriate image sizes for your views
- Implement proper caching strategies
- Consider using `crossfade(false)` for faster loading
- Use `placeholder` and `error` images for better user experience

## Version Compatibility

| Coil Version | Kotlin Version | Android Min SDK |
|--------------|----------------|----------------|
| 2.5.0        | 1.8.0+         | API 21+        |
| 2.4.0        | 1.7.0+         | API 21+        |
| 2.0.0        | 1.6.0+         | API 21+        |

## Example Configuration

**build.gradle.kts**:
```kotlin
implementation("io.coil-kt:coil:2.5.0")
implementation("io.coil-kt:coil-svg:2.5.0") // For SVG support
implementation("io.coil-kt:coil-gif:2.5.0")  // For GIF support
```

**Application class**:
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader {
            ImageLoader.Builder(this)
                .crossfade(true)
                .okHttpClient {
                    OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build()
                }
                .build()
        }
    }
}
```

This skill is your go-to resource for all Coil-related questions and optimization needs.