package io.github.rwx.ui.model

import io.github.rwx.ui.UiColor


/** A single player slot row in the battle-room table. */
data class BattleRoomPlayer(
    val id: String,
    val name: String,
    /** Spawn point label, e.g. "1".."10", or the spectator marker. */
    val spawnLabel: String,
    /** Ally-team label, e.g. "A".."J". */
    val teamLabel: String,
    /** Ping text, e.g. "50"; blank for local player and AIs. */
    val pingLabel: String = "",
    /** Player color index (RW team palette); -1 uses the default text color. */
    val nameColorIndex: Int = -1,
    /** Spawn-point index used only to tint the spawn cell; -1 for spectators. */
    val spawnColorIndex: Int = -1,
    /** Team index used only to tint the team cell; -1 uses the default color. */
    val teamColorIndex: Int = -1,
    val isSpectator: Boolean = false,
    /** A not-ready player is shown dimmed. */
    val isReady: Boolean = true,
    val isAI: Boolean = false,
    val isLocal: Boolean = false,
    /** Per-player starting-units override; null = use room default. */
    val startingUnitsOverride: Int? = null,
    /** Per-player AI-difficulty override; null = use room default. */
    val aiDifficultyOverride: Int? = null,
)

/** The room map/info shown in the left panel. */
data class BattleRoomInfo(
    val mapName: String,
    val mapTypeLabel: String,
    val detailLines: List<String> = emptyList(),
    val mapPreviewAssetPath: String? = null,
    val rwxModeLabel: String? = null,
    val rwxCompatibilityLabel: String? = null,
    val mapAssetPath: String? = null,
)

/**
 * One line in the battle-room chat log.
 * @param teamColorIndex author team id supplied by the engine for the tint; -1 = system/no team.
 */
data class BattleRoomChatLine(
    val text: String,
    val teamColorIndex: Int = -1,
)

/** Full battle-room presentation model. */
data class BattleRoomModel(
    val info: BattleRoomInfo,
    val players: List<BattleRoomPlayer>,
    val chatLines: List<BattleRoomChatLine>,
    val isHost: Boolean,
    val revision: Long = 0,
    val mapRevision: Long = 0,
    val isAvailable: Boolean = true,
) {
    fun canConfigurePlayer(playerId: String): Boolean = isAvailable && playerId.isNotBlank() &&
        players.any { it.id == playerId && (isHost || it.isLocal) }

    /** Background player/chat updates keep a room's identity; explicit room transitions invalidate actions. */
    internal fun resolveAction(requestRevision: Long, action: BattleRoomAction): BattleRoomAction? {
        if (requestRevision != revision) return null
        if (action == BattleRoomAction.Back) return action
        if (!isAvailable) return null
        return when (action) {
            BattleRoomAction.SelectMap, BattleRoomAction.OpenOptions, BattleRoomAction.Start, BattleRoomAction.AddAI -> action.takeIf { isHost }
            is BattleRoomAction.SelectPlayer -> action.takeIf { canConfigurePlayer(it.playerId) }
            is BattleRoomAction.SendChat -> action.message.trim().takeIf { it.isNotEmpty() }?.let { BattleRoomAction.SendChat(it) }
            BattleRoomAction.Back -> action
        }
    }
}

data class BattleRoomActions(
    val onBack: () -> Unit,
    val onSelectMap: () -> Unit,
    val onOpenOptions: () -> Unit,
    val onStart: () -> Unit,
    val onAddAI: () -> Unit,
    val onSelectPlayer: (String) -> Unit,
    val onSendChat: (String) -> Unit,
)

sealed interface BattleRoomAction {
    data object Back : BattleRoomAction

    /** Host: open the map-select screen. */
    data object SelectMap : BattleRoomAction

    /** Host: open the game-options dialog. */
    data object OpenOptions : BattleRoomAction

    /** Host: start the game. */
    data object Start : BattleRoomAction

    /** Host: add an AI player. */
    data object AddAI : BattleRoomAction

    /** Open a specific player's config dialog (host or self). */
    data class SelectPlayer(val playerId: String) : BattleRoomAction

    /** Send a chat message / command. */
    data class SendChat(val message: String) : BattleRoomAction
}

sealed interface BattleRoomOutcome {
    data object Close : BattleRoomOutcome
    data object StartGame : BattleRoomOutcome
    data object OpenMapSelect : BattleRoomOutcome
    data object OpenGameOptions : BattleRoomOutcome
    data class OpenPlayerConfig(val playerId: String) : BattleRoomOutcome
    data object AddAI : BattleRoomOutcome
    data class SendChat(val message: String) : BattleRoomOutcome
}


object BattleRoomNavigation {
    fun outcomeFor(action: BattleRoomAction): BattleRoomOutcome = when (action) {
        BattleRoomAction.Back -> BattleRoomOutcome.Close
        BattleRoomAction.SelectMap -> BattleRoomOutcome.OpenMapSelect
        BattleRoomAction.OpenOptions -> BattleRoomOutcome.OpenGameOptions
        BattleRoomAction.Start -> BattleRoomOutcome.StartGame
        BattleRoomAction.AddAI -> BattleRoomOutcome.AddAI
        is BattleRoomAction.SelectPlayer -> BattleRoomOutcome.OpenPlayerConfig(action.playerId)
        is BattleRoomAction.SendChat -> BattleRoomOutcome.SendChat(action.message)
    }
}

/**
 * RW team-color palette (green, red, blue, yellow, cyan, white, dark, pink, orange, purple), used to
 * tint player name / spawn / team cells by index. Out-of-range or negative indices use the supplied
 * fallback so spectators and default-colored players keep the theme's text color.
 */
object BattleRoomTeamColors {
    private val palette: List<UiColor> = listOf(
        UiColor("4caf50ff"), UiColor("e64545ff"), UiColor("5c8aedff"), UiColor("e2c541ff"),
        UiColor("46c5c5ff"), UiColor("eef1f4ff"), UiColor("8a8f96ff"), UiColor("e667b0ff"),
        UiColor("e8923cff"), UiColor("9b6bd0ff"),
    )

    fun colorFor(index: Int, fallback: UiColor): UiColor = palette.getOrNull(index) ?: fallback
}

fun battleRoomChatColorIndexFor(line: BattleRoomChatLine, players: List<BattleRoomPlayer>): Int? {
    // Prefer the team color the engine attached to the message; fall back to matching the author
    // name against the current players (covers locally-echoed lines that carry no index).
    line.teamColorIndex.takeIf { it >= 0 }?.let { return it }
    val author = line.text.substringBefore(":", missingDelimiterValue = "")
        .trim()
        .takeIf { it.isNotEmpty() }
        ?: return null
    return players.firstOrNull { it.name == author }
        ?.nameColorIndex
        ?.takeIf { it >= 0 }
}

