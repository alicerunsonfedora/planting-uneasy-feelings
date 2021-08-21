/*
 * InterfaceLevelPrompt.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix.ui

import godot.Button
import godot.Label
import godot.PopupDialog
import godot.annotation.*
import godot.signals.signal
import net.marquiskurt.GodotWildThirtySix.toPath
import kotlin.math.roundToInt

/** A class that represents the dialog displayed when the level is over. */
@RegisterClass
class InterfaceLevelPrompt : PopupDialog() {

	/** An enumeration for the different level states. */
	enum class LevelState {
		/** The player has succeeded in infecting the required amount of entities. */
		SUCCESS,

		/** The player has infected every entity. */
		HUGE_SUCCESS,

		/** The player has failed in infecting the required amount of entities. */
		FAILURE
	}

	@EnumTypeHint
	@Export
	@RegisterProperty
	var state: LevelState = LevelState.SUCCESS

	@Export
	@RegisterProperty
	var requiredAmount: Double = 0.0

	@Export
	@RegisterProperty
	var scoredAmount: Double = 0.0

	@RegisterSignal
	val signalCallRestart by signal()

	@RegisterSignal
	val signalCallNext by signal()

	private lateinit var stateLabel: Label
	private lateinit var requiredPercentLabel: Label
	private lateinit var scoredPercentLabel: Label

	private lateinit var buttonRestart: Button
	private lateinit var buttonNextLvl: Button

	@RegisterFunction
	override fun _ready() {
		stateLabel = getNode("VBoxContainer/Title".toPath()) as Label
		requiredPercentLabel = getNode("VBoxContainer/Details/Required/Percentage".toPath()) as Label
		scoredPercentLabel = getNode("VBoxContainer/Details/Actual/Percentage".toPath()) as Label

		buttonRestart = getNode("VBoxContainer/Toolbar/Restart".toPath()) as Button
		buttonNextLvl = getNode("VBoxContainer/Toolbar/Next".toPath()) as Button

		buttonRestart.buttonUp.connect(this, this::buttonRestartPressed)
		buttonNextLvl.buttonUp.connect(this, this::buttonNextLevelPressed)

		renderResults()
	}

	@RegisterFunction
	fun renderResults() {
		stateLabel.text = when (state) {
			LevelState.SUCCESS -> "Great Job!"
			LevelState.HUGE_SUCCESS -> "Perfect Work!"
			LevelState.FAILURE -> "Try Again!"
		}
		buttonNextLvl.disabled = state == LevelState.FAILURE
		requiredPercentLabel.text = "${(requiredAmount * 100).roundToInt()}%"
		scoredPercentLabel.text = "${(scoredAmount * 100).roundToInt()}%"
	}

	@RegisterFunction
	fun buttonRestartPressed() {
		signalCallRestart.emit()
		hide()
	}

	@RegisterFunction
	fun buttonNextLevelPressed() {
		signalCallNext.emit()
		hide()
	}

}
