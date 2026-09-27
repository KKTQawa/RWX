package io.github.rwx.app

import io.github.rwx.PlatformFilePickerHost
import io.github.rwx.i18n.I18n
import io.github.rwx.ui.component.Icon
import io.github.rwx.ui.model.*

/** UI-independent adapter: the dialog store owns temporary selections, not a captured local file. */
internal fun modImportDialog(picker: PlatformFilePickerHost?, onImport: (String) -> Unit) = Dialog(
    title = I18n.modimport.title(),
    message = I18n.modimport.message(),
    dismissButtonIndex = 1,
    textInput = DialogTextInput(
        hint = "/path/to/mod.rwmod",
        trailingIcon = Icon.Import,
        trailingIconTooltip = I18n.modimport.chooseFile(),
        onChooseInput = { complete ->
            if (picker == null) complete(null) else picker.openFilePicker(
                title = I18n.modimport.chooseTitle(),
                allowedExtensions = setOf("rwmod", "zip", "jar", "ini", "rwxkey", "rwxpub", "rwxlicense"),
                allowDirectories = true,
            ) { selection -> complete(selection?.let { DialogInputChoice(it.displayPath, it.path, it.release) }) }
        },
    ),
    buttons = listOf(DialogButton(I18n.mods.importFile(), onInputPress = onImport), DialogButton(I18n.common.cancel())),
)
