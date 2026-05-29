# Contributing to FreeIPTV

First off, thank you for considering contributing to FreeIPTV! It's people like you that make this app a great tool for everyone.

## Where do I go from here?

If you've noticed a bug or have a feature request, make sure to check the [Issues](https://github.com/MdSagorMunshi/FreeIPTV/issues) tab to see if it's already being tracked. If not, feel free to open a new issue!

## Development Setup

1. Fork the repo and clone it locally.
2. Open the project in **Android Studio** (Koala or newer recommended).
3. The project uses **Gradle 8.6** and **AGP 8.3.2**. 
4. Sync the Gradle files to download dependencies.
5. Create a new branch for your feature or bug fix:
   ```bash
   git checkout -b feature/my-awesome-feature
   ```

## Coding Guidelines

- **Kotlin:** Follow standard Kotlin coding conventions.
- **Jetpack Compose:** Use declarative UI patterns. Keep your composables pure and hoist state where appropriate.
- **Architecture:** The app uses MVVM (Model-View-ViewModel). Place UI logic in ViewModels and keep the Compose UI layer as stateless as possible.
- **Formatting:** Ensure your code is properly formatted before submitting a PR.

## Submitting a Pull Request

1. Push your branch to your fork on GitHub.
2. Open a Pull Request against the `main` branch of this repository.
3. Provide a clear and concise description of the changes you've made.
4. Wait for a review! We'll try to review your PR as quickly as possible.

## License

By contributing to FreeIPTV, you agree that your contributions will be licensed under its MIT License.
