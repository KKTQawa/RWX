package io.github.rwx.app

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/** Process-wide work, including retained native-session preparation across Activity recreation. */
internal object ApplicationScope : CoroutineScope {
    val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = job + Dispatchers.Default
}

/** Native UI dispatcher: Swing on desktop, the main looper on Android. */
object FrontendScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
}
