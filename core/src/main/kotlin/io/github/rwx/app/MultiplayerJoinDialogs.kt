package io.github.rwx.app

import io.github.rwx.i18n.I18n
import io.github.rwx.ui.model.*

/** A confirmation must retain its original transport and address even if the lobby changes later. */
internal data class MultiplayerJoinRequest(
    val lobbyKind: MultiplayerLobbyKind,
    val address: String,
    val roomLabel: String,
    val originalServerId: String? = null,
)

internal fun multiplayerJoinRoomDialog(
    lobbyKind: MultiplayerLobbyKind,
    room: MultiplayerRoomItem,
    onJoin: (MultiplayerJoinRequest) -> Unit,
): Dialog {
    val request = MultiplayerJoinRequest(
        lobbyKind = lobbyKind,
        address = if (lobbyKind == MultiplayerLobbyKind.P2P) room.roomId else room.joinAddress,
        roomLabel = room.joinDisplayLabel(),
        originalServerId = room.originalServerId.takeIf { lobbyKind == MultiplayerLobbyKind.Original },
    )
    return Dialog(
        title = I18n.multiplayer.joinServerQuestion(),
        message = joinRoomDialogMessage(room),
        buttons = listOf(
            DialogButton(I18n.common.join(), onPress = { onJoin(request) }),
            DialogButton(I18n.common.cancel()),
        ),
        scrollableMessage = true,
    )
}

internal fun multiplayerJoinDirectDialog(
    lobbyKind: MultiplayerLobbyKind,
    onJoin: (MultiplayerJoinRequest) -> Unit,
): Dialog = Dialog(
    title = when (lobbyKind) {
        MultiplayerLobbyKind.Original -> I18n.multiplayer.joinServer()
        MultiplayerLobbyKind.P2P -> I18n.multiplayer.joinP2pRoom()
    },
    message = when (lobbyKind) {
        MultiplayerLobbyKind.Original -> I18n.multiplayer.joinServerInput()
        MultiplayerLobbyKind.P2P -> I18n.multiplayer.joinP2pRoomInput()
    },
    textInput = DialogTextInput(hint = when (lobbyKind) {
        MultiplayerLobbyKind.Original -> I18n.multiplayer.joinServerHint()
        MultiplayerLobbyKind.P2P -> I18n.multiplayer.p2pRoomIdHint()
    }),
    buttons = listOf(
        DialogButton(I18n.common.join(), onInputPress = { value ->
            onJoin(MultiplayerJoinRequest(
                lobbyKind, value.trim(),
                roomLabel = if (lobbyKind == MultiplayerLobbyKind.P2P) "P2P room" else "server",
            ))
        }),
        DialogButton(I18n.common.cancel()),
    ),
)
