package io.github.rwx.slick

import io.github.rwx.session.BattleRoomLaunchConfig
import io.github.rwx.session.MapSnapshot
import java.util.concurrent.atomic.AtomicReference


sealed interface SlickSessionRequest {
    val loadingPath: String?

    val loadsLevel: Boolean
        get() = true
    data object EnginePreparation : SlickSessionRequest {
        override val loadingPath: String? = null
        override val loadsLevel: Boolean = false
    }

    data class Map(
        val mapPath: String,
    ) : SlickSessionRequest {
        override val loadingPath: String = mapPath
    }

    data class SavedGame(
        val saveName: String,
    ) : SlickSessionRequest {
        override val loadingPath: String = saveName
    }

    data class Replay(
        val replayName: String,
    ) : SlickSessionRequest {
        override val loadingPath: String = replayName
    }

    data class BattleRoom(
        val config: BattleRoomLaunchConfig,
    ) : SlickSessionRequest {
        override val loadingPath: String = config.room.mapPath
    }

    data class MapSnapshotRequest(
        val snapshot: MapSnapshot,
    ) : SlickSessionRequest {
        override val loadingPath: String = snapshot.mapPath
    }

    /** Adopt a battle room the engine has already started (host pressed "Start game"). */
    data class StartedGame(
        val mapPath: String,
    ) : SlickSessionRequest {
        override val loadingPath: String = mapPath
    }

    data object MenuBackground : SlickSessionRequest {
        override val loadingPath: String? = null
    }
}

/**
 * Tracks only the hand-off between GameSession and the Slick render thread.
 * Map ownership and loading status remain in GameSession.
 *
 * Lock-free: every transition is a CAS on one immutable [Snapshot], so producers on any thread and
 * the dispatching consumer never take a lock, and [nextDispatch] hands each request to exactly one
 * caller even under contention.
 */
internal class SlickSessionRequestState {
    data class Snapshot(
        val desired: SlickSessionRequest?,
        val dispatched: SlickSessionRequest?,
    )

    private val state = AtomicReference(Snapshot(null, null))

    val desired: SlickSessionRequest?
        get() = state.get().desired

    val dispatched: SlickSessionRequest?
        get() = state.get().dispatched

    fun replace(request: SlickSessionRequest) {
        state.set(Snapshot(desired = request, dispatched = null))
    }

    /** Installs [request] only when no request is desired yet. Returns true when installed. */
    fun replaceIfEmpty(request: SlickSessionRequest): Boolean =
        update { current ->
            if (current.desired == null) Snapshot(desired = request, dispatched = null) else current
        }

    /** Makes [request] the desired request unless it already is. Returns true when it changed. */
    fun synchronize(request: SlickSessionRequest): Boolean =
        update { current ->
            if (current.desired == request) current else Snapshot(desired = request, dispatched = null)
        }

    fun nextDispatch(): SlickSessionRequest? {
        while (true) {
            val current = state.get()
            val request = current.desired ?: return null
            if (request == current.dispatched) return null
            if (state.compareAndSet(current, current.copy(dispatched = request))) {
                return request
            }
        }
    }

    fun clearRequest() {
        state.set(Snapshot(null, null))
    }

    /** Atomically clears the request when [predicate] holds for the current state. */
    fun clearRequestIf(predicate: (Snapshot) -> Boolean): Boolean =
        update { current ->
            if (predicate(current)) Snapshot(null, null) else current
        }

    fun runtimeStopped() {
        update { current ->
            if (current.dispatched == null) current else current.copy(dispatched = null)
        }
    }

    /** CAS loop applying [transform]; returns true when the state actually changed. */
    private inline fun update(transform: (Snapshot) -> Snapshot): Boolean {
        while (true) {
            val current = state.get()
            val next = transform(current)
            if (next == current) return false
            if (state.compareAndSet(current, next)) return true
        }
    }
}
