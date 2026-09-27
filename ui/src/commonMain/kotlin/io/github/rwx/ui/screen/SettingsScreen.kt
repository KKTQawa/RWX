package io.github.rwx.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.Icon
import io.github.rwx.ui.component.itemAppear
import io.github.rwx.ui.component.pressScale
import io.github.rwx.ui.model.*
import io.github.rwx.ui.ColorSchemeRegistry
import io.github.rwx.ui.theme.Corners
import io.github.rwx.ui.theme.Layout
import io.github.rwx.ui.theme.LocalColorScheme
import io.github.rwx.ui.theme.Spacing
import io.github.rwx.ui.theme.schemeForCompose
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Settings rendering only: the shared model and ActionRouter still own edits and persistence. */
@Composable
fun SettingsScreen(
    state: SettingsPageState,
    pages: List<SettingsPage>,
    onAction: (SettingsUiAction) -> Unit,
    enableAnimations: Boolean = true,
) {
    val palette = LocalColorScheme.current.palette
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Box(
        Modifier.fillMaxSize().background(palette.panelOverlay),
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.widthIn(max = 1560.dp).fillMaxSize().padding(Spacing.xl)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OutlinedButton(onClick = { onAction(SettingsUiAction.Back) }, modifier = Modifier.testTag("settings-back")) {
                    Text(I18n.common.back())
                }
                Row(
                    Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    pages.forEach { page ->
                        FilterChip(
                            selected = state.page == page,
                            onClick = { onAction(SettingsUiAction.SelectPage(page)) },
                            label = { Text(page.tabTitle) },
                            modifier = Modifier.testTag("settings-page-${page.name}"),
                        )
                    }
                }
            }
            if (state.page == SettingsPage.KeyBindings) {
                state.keyBindings?.let { KeyBindingsContent(it, onAction, Modifier.weight(1f), enableAnimations = enableAnimations) }
            } else {
                key(state.page) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).testTag("settings-list"),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(bottom = Spacing.lg),
                    ) {
                        items(state.items, key = { it.stableKey }) { item ->
                            Box(Modifier.then(if (enableAnimations) Modifier.animateItem() else Modifier).then(Modifier.itemAppear(enableAnimations))) {
                                when (item) {
                                is SettingsItemState.Toggle -> SettingsToggle(item, enableAnimations = enableAnimations) {
                                    onAction(SettingsUiAction.Toggle(state.page, item.key, it))
                                }
                                is SettingsItemState.Slider -> SettingsSlider(
                                    item = item,
                                    onChange = { onAction(SettingsUiAction.PreviewSlider(state.page, item.key, it)) },
                                    onCommit = { onAction(SettingsUiAction.CommitSlider(state.page, item.key)) },
                                )
                                is SettingsItemState.ColorScheme -> SettingsColorScheme(item, enableAnimations = enableAnimations) {
                                    onAction(SettingsUiAction.SelectColorScheme(item.id))
                                }
                                is SettingsItemState.StorageLocation -> SettingsStorageLocation(
                                    item = item,
                                    onSelectInternal = { onAction(SettingsUiAction.SelectStorage(0)) },
                                    onSelectExternal = { onAction(SettingsUiAction.RequestExternalStorage) },
                                )
                                }
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.widthIn(max = 1560.dp).fillMaxSize().align(Alignment.Center)) {
            SnackbarHost(
                hostState = snackbarHost,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp),
            )
            ExtendedFloatingActionButton(
                onClick = {
                    onAction(SettingsUiAction.Save)
                    scope.launch { snackbarHost.showSnackbar(I18n.settings.saved()) }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg).testTag("settings-save"),
                containerColor = palette.primaryContainer,
                contentColor = palette.onPrimary,
            ) {
                Icon(Icon.Save, Layout.contentIconSize, palette.onPrimary)
                Spacer(Modifier.width(Spacing.sm))
                Text(I18n.common.save())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsStorageLocation(
    item: SettingsItemState.StorageLocation,
    onSelectInternal: () -> Unit,
    onSelectExternal: () -> Unit,
) {
    var expanded by remember(item.selectedType) { mutableStateOf(false) }
    val selected = AndroidStoragePreference.fromStorageType(item.selectedType)
    fun label(preference: AndroidStoragePreference): String = when (preference) {
        AndroidStoragePreference.Internal -> I18n.settings.storage.internal()
        AndroidStoragePreference.External -> I18n.settings.storage.external()
    }
    SettingsCard {
        Column(Modifier.padding(Spacing.lg)) {
            Text(I18n.settings.storage.location())
            Spacer(Modifier.height(Spacing.sm))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = label(selected),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().testTag("storage-selector"),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    AndroidStoragePreference.entries.forEach { preference ->
                        DropdownMenuItem(
                            text = { Text(label(preference)) },
                            leadingIcon = { RadioButton(selected = preference == selected, onClick = null) },
                            onClick = {
                                expanded = false
                                if (preference == AndroidStoragePreference.External) onSelectExternal() else onSelectInternal()
                            },
                            modifier = Modifier.testTag("storage-${preference.name}"),
                        )
                    }
                }
            }
            Text(I18n.settings.storage.restartRequired())
        }
    }
}

private val SettingsItemState.stableKey: String
    get() = when (this) {
        is SettingsItemState.Toggle -> key
        is SettingsItemState.Slider -> key
        is SettingsItemState.ColorScheme -> "color-scheme:${id.value}"
        is SettingsItemState.StorageLocation -> "storage-location:${selectedType}"
    }

@Composable
private fun SettingsCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val palette = LocalColorScheme.current.palette
    Surface(
        modifier = modifier.fillMaxWidth().border(1.dp, palette.borderSubtle, RoundedCornerShape(Corners.sm)),
        color = palette.surfaceSunken,
        shape = RoundedCornerShape(Corners.sm),
        content = content,
    )
}

@Composable
private fun SettingsToggle(item: SettingsItemState.Toggle, enableAnimations: Boolean = true, onChange: (Boolean) -> Unit) {
    SettingsCard {
        Row(
            Modifier.fillMaxWidth().testTag(item.key)
                .toggleable(item.checked, role = Role.Switch, onValueChange = onChange).padding(Spacing.lg)
                .pressScale(enableAnimations),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(item.label, modifier = Modifier.weight(1f))
            Switch(checked = item.checked, onCheckedChange = null)
        }
    }
}

@Composable
private fun SettingsSlider(item: SettingsItemState.Slider, onChange: (Float) -> Unit, onCommit: () -> Unit) {
    var dragging by remember(item.key) { mutableStateOf(false) }
    var draft by remember(item.key) { mutableFloatStateOf(item.value) }
    LaunchedEffect(item.value) {
        if (!dragging) draft = item.value
    }
    SettingsCard {
        Column(Modifier.padding(Spacing.lg)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(item.label, modifier = Modifier.weight(1f))
                Text(item.formattedValue)
            }
            Slider(
                value = draft.coerceIn(item.min, item.max),
                onValueChange = {
                    dragging = true
                    draft = it
                    onChange(it)
                },
                onValueChangeFinished = {
                    dragging = false
                    onCommit()
                },
                valueRange = item.min..item.max,
                steps = if (item.step > 0f) ((item.max - item.min) / item.step).roundToInt().minus(1).coerceAtLeast(0) else 0,
                modifier = Modifier.fillMaxWidth().testTag(item.key),
            )
        }
    }
}

@Composable
private fun SettingsColorScheme(item: SettingsItemState.ColorScheme, enableAnimations: Boolean = true, onSelect: () -> Unit) {
    val preview = ColorSchemeRegistry.schemeForCompose(item.id).palette
    SettingsCard {
        Row(
            Modifier.fillMaxWidth().testTag("color-scheme:${item.id.value}")
                .selectable(item.selected, role = Role.RadioButton, onClick = onSelect).padding(Spacing.lg)
                .pressScale(enableAnimations),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            RadioButton(selected = item.selected, onClick = null)
            Text(item.label, modifier = Modifier.weight(1f))
            listOf(preview.primary, preview.secondary, preview.surfaceBase).forEach { color ->
                Box(Modifier.size(24.dp).background(color, CircleShape))
            }
        }
    }
}
