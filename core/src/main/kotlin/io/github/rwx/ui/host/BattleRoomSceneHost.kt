package io.github.rwx.ui.host

import io.github.rwx.ui.model.BattleRoomAction
import io.github.rwx.ui.model.BattleRoomChatLine
import io.github.rwx.ui.model.BattleRoomInfo
import io.github.rwx.ui.model.BattleRoomModel
import io.github.rwx.ui.model.BattleRoomPlayer

class BattleRoomSceneHost(
    private val onAction: (BattleRoomAction) -> Unit = {},
) {
    private var players: List<BattleRoomPlayer> = emptyList()
    private var chatLines: List<BattleRoomChatLine> = emptyList()
    private var info: BattleRoomInfo = BattleRoomInfo(
        mapName = "No map selected",
        mapTypeLabel = "Battle room",
    )
    private var isHost: Boolean = false
    private var isAvailable = false
    private var revision = 0L
    private var mapRevision = 0L

    fun beginRoom() {
        revision += 1
        markUnavailable()
        chatLines = emptyList()
    }

    fun markUnavailable() {
        if (isAvailable) mapRevision += 1
        isAvailable = false
        isHost = false
        players = emptyList()
        info = BattleRoomInfo("No map selected", "Battle room")
    }

    fun snapshot(): BattleRoomModel = BattleRoomModel(
        info = info.copy(detailLines = info.detailLines.toList()),
        players = players.toList(),
        chatLines = chatLines.toList(),
        isHost = isHost,
        revision = revision,
        mapRevision = mapRevision,
        isAvailable = isAvailable,
    )

    fun updateRoom(model: BattleRoomModel) {
        if (info.mapAssetPath != model.info.mapAssetPath || info.mapName != model.info.mapName ||
            info.mapPreviewAssetPath != model.info.mapPreviewAssetPath || !isAvailable
        ) mapRevision += 1
        info = model.info.copy(detailLines = model.info.detailLines.toList())
        isAvailable = model.isAvailable
        isHost = model.isHost
        players = model.players.toList()
        chatLines = model.chatLines.toList()
    }

    fun appendChat(line: BattleRoomChatLine) {
        chatLines = chatLines + line
    }

    fun dispatch(action: BattleRoomAction, requestRevision: Long = revision) {
        snapshot().resolveAction(requestRevision, action)?.let(onAction)
    }
}
