# Gallery App

A modern Android gallery application built with:

- **Kotlin** + **Jetpack Compose** + **Material3**
- **Clean Architecture** (Domain, Data, Presentation layers)
- **Hilt** for Dependency Injection
- **Coil** for image loading
- **MediaStore** for media access
- **Media3/ExoPlayer** for video playback
- **Coroutines** + **Flow** for async operations

## Architecture

```
app/
├── data/
│   ├── repository/          # Repository implementations
│   └── source/local/        # MediaStore data source
├── domain/
│   ├── model/               # Domain models (MediaItem, Album, etc.)
│   ├── repository/          # Repository interfaces
│   └── usecase/             # Use cases (business logic)
├── presentation/
│   ├── gallery/             # Gallery grid screen
│   ├── detail/              # Detail view with zoom/pan
│   └── common/              # Shared UI components
├── di/                      # Hilt modules
├── ui/theme/                # Material3 theming
└── util/                    # Utilities (Result, extensions)
```

## Features

- **Media Grid**: Responsive grid with configurable span count
- **Filter & Sort**: By type (images/videos), favorites, date, name, size
- **Albums**: Grouped by bucket/folder
- **Detail View**: Full-screen with pinch-to-zoom for images, video playback
- **Favorites**: Toggle favorite status
- **Modern UI**: Material3, dark/light theme, edge-to-edge

## Building

```bash
./gradlew assembleDebug
```

Requires:
- Android Studio Hedgehog+
- JDK 17
- Android SDK 34

## Permissions

- `READ_MEDIA_IMAGES` (API 33+)
- `READ_MEDIA_VIDEO` (API 33+)
- `READ_EXTERNAL_STORAGE` (API 32 and below)
- `MANAGE_EXTERNAL_STORAGE` for full access (optional)

## License

MIT