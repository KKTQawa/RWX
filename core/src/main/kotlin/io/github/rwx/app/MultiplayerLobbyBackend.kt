package io.github.rwx.app

import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.network.MasterServerClient
import com.corrodinggames.rts.gameFramework.network.ServerInfo
import io.github.rwx.p2p.P2PLobbyService
import io.github.rwx.ui.CoreUiEventQueue
import io.github.rwx.ui.ServerListUiBridge
import io.github.rwx.ui.model.MultiplayerLobbyKind
import io.github.rwx.ui.model.MultiplayerRoomItem

/** Existing discovery services stay behind this boundary; controller tests do not contact real servers. */
internal interface MultiplayerLobbyBackend {
    fun unavailableReason(kind: MultiplayerLobbyKind): String?
    fun requestRefresh(kind: MultiplayerLobbyKind)
    fun readRooms(kind: MultiplayerLobbyKind): List<MultiplayerRoomItem>
}

internal object EngineMultiplayerLobbyBackend : MultiplayerLobbyBackend {
    override fun unavailableReason(kind: MultiplayerLobbyKind): String? =
        if (kind == MultiplayerLobbyKind.Original && GameEngine.getInstance()?.networkEngine == null) {
            "Original lobby requires the RW engine to be loaded"
        } else null

    override fun requestRefresh(kind: MultiplayerLobbyKind) {
        when (kind) {
            MultiplayerLobbyKind.Original -> {
                MasterServerClient.loadServerListAsync { CoreUiEventQueue.requestOriginalRoomListRefresh() }
            }
            MultiplayerLobbyKind.P2P -> P2PLobbyService.getInstance().apply {
                inLobby = true
                startIfNeeded()
                requestRefresh()
            }
        }
    }

    override fun readRooms(kind: MultiplayerLobbyKind): List<MultiplayerRoomItem> = when (kind) {
        MultiplayerLobbyKind.Original -> synchronized(MasterServerClient.serverListLock) {
            // Copy the values as well as the list while the engine's server records are stable.
            ServerListUiBridge.getServerList().filterIsInstance<ServerInfo>().map(::serverInfoToMultiplayerItem)
        }
        MultiplayerLobbyKind.P2P -> P2PLobbyService.getInstance().getRooms().map(::p2pRoomToMultiplayerItem)
    }
}
