package io.github.rwx.ui.platform

/**
 * Platform-agnostic abstraction for creating the Compose host.
 * Desktop: creates ComposePanel-based host
 * Android: creates ComposeView-based host
 */
expect fun createPlatformComposeHost(): PlatformComposeHost
