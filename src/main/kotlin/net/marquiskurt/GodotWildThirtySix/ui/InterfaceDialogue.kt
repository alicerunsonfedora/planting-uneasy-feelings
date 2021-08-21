/*
 * InterfaceDialogue.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix.ui

import godot.*
import godot.annotation.*
import godot.core.Dictionary
import godot.core.VariantArray
import godot.signals.signal
import net.marquiskurt.GodotWildThirtySix.toPath
import godot.File as GDFile

/** A class that represents a dialogue window which displays text. */
@RegisterClass
class InterfaceDialogue : PopupDialog() {

	/** The path to the JSON file that will be loaded. */
	@Export
	@File
	@RegisterProperty
	var dialoguePath: String = "res://resources/dialogue_tutorial.json"

	/** A signal emitted when the dialogue finishes. */
	val signalDialogueFinished by signal()

	private lateinit var dialogue: VariantArray<Dictionary<String, String>>

	private lateinit var who: Label
	private lateinit var what: RichTextLabel

	private lateinit var nextBtn: Button

	@RegisterFunction
	override fun _ready() {
		who = getNode("VStack/Who".toPath()) as Label
		what = getNode("VStack/What".toPath()) as RichTextLabel

		nextBtn = getNode("VStack/HStack/Button".toPath()) as Button
		nextBtn.buttonUp.connect(this, this::next)

		dialogue = VariantArray()
		loadDialogueFromFile()
		next()
	}

	/** Load the dialogue JSON file. */
	@RegisterFunction
	fun loadDialogueFromFile() {
		if (dialoguePath.isEmpty()) return
		with(GDFile()) {
			if (!this.fileExists(dialoguePath)) return
			open(dialoguePath, GDFile.READ)
			val jsonData = JSON.parse(this.getAsText()) ?: return
			dialogue = jsonData.result as VariantArray<Dictionary<String, String>>
			close()
		}
	}

	/** Load the next line into the dialogue bank, or close the dialogue interface if there is no more text. */
	@RegisterFunction
	fun next() {
		if (dialogue.isEmpty()) {
			hide()
			signalDialogueFinished.emit()
			return
		}
		val nextLine = dialogue.popFront()
		who.text = nextLine["who"]
		what.text = nextLine["what"]
	}

}
