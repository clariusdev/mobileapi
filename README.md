# Clarius App (Android)

This repository hosts **Android APK builds of the Clarius App** for direct installation
(sideloading) — useful for partners, researchers, and testing on devices or in
environments where the Google Play Store isn't used. Most users should install the Clarius
App from the **Google Play Store**; these APKs are provided for cases where a direct
download is needed.

## Downloads

Get the APK from the [**Releases**](https://github.com/clariusdev/app/releases) page:

- The build marked **Latest** is the current release.
- Older versions are kept for reference and compatibility — pick the version that matches
  your needs (for example, the App version that pairs with a given
  [Cast API](https://github.com/clariusdev/cast) release).

### Installing an APK

1. Download the `.apk` for the version you want from Releases.
2. On the Android device, allow installation from your browser/file manager (Settings →
   Apps → *your installer* → **Install unknown apps**).
3. Open the downloaded file to install.

> APKs are large (several hundred MB) as they contain the full application.

## Deprecation notice

This repository previously contained the **Mobile API** — an Android example demonstrating
inter-process communication (IPC) with the Clarius App. **That API and its example are
deprecated and no longer maintained.** For building on Clarius, use the current SDK:

- [**Cast**](https://github.com/clariusdev/cast) — stream images and data from a probe in
  real time while the Clarius App is running.
- [**Solum**](https://github.com/clariusdev/solum) — OEM API for standalone apps that
  connect directly to a probe.
- See the [**Clarius SDK home**](https://github.com/clariusdev/.github/blob/main/profile/README.md)
  for all repositories.
