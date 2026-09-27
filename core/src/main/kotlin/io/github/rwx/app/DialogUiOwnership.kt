package io.github.rwx.app

import io.github.rwx.ui.AppScreen

internal data class DialogUiOwner(val screen: AppScreen, val roomRevision: Long? = null, val isHost: Boolean? = null)

/** Bind presented dialogs to their page; automatic startup navigation may transfer their owner. */
internal class DialogUiOwnership {
    private var presented: Pair<Long, DialogUiOwner>? = null

    fun canDispatch(revision: Long, owner: DialogUiOwner): Boolean = presented == (revision to owner)

    fun shouldDismiss(revision: Long, owner: DialogUiOwner): Boolean =
        presented?.first == revision && presented?.second != owner &&
            presented?.second?.screen != AppScreen.Loading

    fun publish(revision: Long?, owner: DialogUiOwner, composeVisible: Boolean) {
        if (presented?.first != revision) presented = null
        // Preserve an existing binding while a loading dialog/snackbar temporarily owns input.
        if (revision != null && composeVisible) presented = revision to owner
    }
}
