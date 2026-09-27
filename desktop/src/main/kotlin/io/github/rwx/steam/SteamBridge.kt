package io.github.rwx.steam

import com.codedisaster.steamworks.SteamAPI
import com.codedisaster.steamworks.SteamLibraryLoader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal typealias ResourceReader = (String) -> InputStream?
internal typealias NativeLoader = (String) -> Unit
internal typealias TempDirFactory = () -> Path

internal object SteamNativeExtractor {
    fun resourceNames(osName: String, arch: String): List<String> {
        val os = osName.lowercase()
        val is64 = arch.lowercase().contains("64")
        return when {
            os.contains("win") && is64 -> listOf("steam_api64.dll", "steamworks4j64.dll")
            os.contains("win") -> listOf("steam_api.dll", "steamworks4j.dll")
            os.contains("mac") || os.contains("darwin") -> listOf("libsteam_api.dylib", "libsteamworks4j.dylib")
            else -> listOf("libsteam_api.so", "libsteamworks4j.so")
        }
    }

    fun extractAndLoad(
        osName: String = System.getProperty("os.name", ""),
        arch: String = System.getProperty("os.arch", ""),
        openResource: ResourceReader = { name -> SteamworksBackend::class.java.getResourceAsStream("/$name") },
        systemLoad: NativeLoader = System::load,
        createTempDir: TempDirFactory = { Files.createTempDirectory("steamworks4j") },
    ): Boolean =
        try {
            val names = resourceNames(osName, arch)
            val dir = createTempDir()
            val paths = names.map { name ->
                val stream = openResource(name) ?: throw IOException("Missing native resource: $name")
                val target = dir.resolve(name)
                stream.use { input -> Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING) }
                target.toFile().deleteOnExit()
                target.toAbsolutePath().toString()
            }
            paths.forEach { systemLoad(it) }
            true
        } catch (_: Throwable) {
            false
        }
}

interface SteamBackend {
    fun loadAndInit(): Boolean
    fun pump()
    fun shutdown()
}

class SteamworksBackend(
    private val openResource: ResourceReader = { name -> SteamworksBackend::class.java.getResourceAsStream("/$name") },
    private val systemLoad: NativeLoader = System::load,
    private val createTempDir: TempDirFactory = { Files.createTempDirectory("steamworks4j") },
) : SteamBackend {
    private val loader = Loader()

    override fun loadAndInit(): Boolean =
        try {
            if (!SteamAPI.loadLibraries(loader)) return false
            SteamAPI.initEx() == SteamAPI.InitResult.OK
        } catch (_: Throwable) {
            false
        }

    override fun pump() {
        try {
            SteamAPI.runCallbacks()
        } catch (_: Throwable) {
        }
    }

    override fun shutdown() {
        try {
            SteamAPI.shutdown()
        } catch (_: Throwable) {
        }
    }

    private inner class Loader : SteamLibraryLoader {
        @Volatile
        private var loaded = false

        override fun loadLibrary(libraryName: String): Boolean {
            try {
                synchronized(this) {
                    if (loaded) return true
                    val ok = SteamNativeExtractor.extractAndLoad(
                        openResource = openResource,
                        systemLoad = systemLoad,
                        createTempDir = createTempDir,
                    )
                    if (ok) loaded = true
                    return ok
                }
            } catch (_: Throwable) {
                return false
            }
        }
    }
}

class SteamBridge(
    private val backend: SteamBackend = SteamworksBackend(),
    private val env: (String) -> String? = System::getenv,
    private val workingDir: File = File(System.getProperty("user.dir")),
) {
    var enabled: Boolean = false
        private set

    fun init(noSteam: Boolean): Boolean {
        if (noSteam) return false
        if (env(NO_STEAM_ENV) == "1") return false
        ensureAppIdFile()
        enabled =
            try {
                backend.loadAndInit()
            } catch (_: Throwable) {
                false
            }
        return enabled
    }

    private fun ensureAppIdFile() {
        try {
            val file = File(workingDir, "steam_appid.txt")
            if (!file.exists()) {
                file.writeText(STEAM_APP_ID.toString(), Charsets.US_ASCII)
            }
        } catch (_: Throwable) {
        }
    }

    fun pump() {
        if (!enabled) return
        try {
            backend.pump()
        } catch (_: Throwable) {
        }
    }

    fun shutdown() {
        if (!enabled) return
        try {
            backend.shutdown()
        } catch (_: Throwable) {
        } finally {
            enabled = false
        }
    }

    companion object {

        const val STEAM_APP_ID = 647960
        const val NO_STEAM_ENV = "RWX_NO_STEAM"
    }
}
