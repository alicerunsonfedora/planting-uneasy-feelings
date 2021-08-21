/*
 * InterfaceHUD.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix.ui

import godot.Control
import godot.Label
import godot.ProgressBar
import godot.Timer
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.NodePath
import godot.global.GD
import net.marquiskurt.GodotWildThirtySix.toPath
import kotlin.math.roundToInt

/** A class that represents the heads-up display present on each level. */
@RegisterClass
class InterfaceHUD : Control() {

	/** The progress bar that represents the percentage of those infected. */
	@RegisterProperty
	lateinit var infectProgressBar: ProgressBar

	/** The label that displays the required infection rate. */
	@RegisterProperty
	lateinit var infectRateText: Label

	/** The label that displays the time remaining. */
	@RegisterProperty
	lateinit var timeRemainsText: Label

	/** An individual timer that represents a second of time. */
	@RegisterProperty
	lateinit var timer: Timer

	/** The timer from the universe to track time left of verification.
	 *
	 * @see net.marquiskurt.GodotWildThirtySix.Universe.verifier
	 */
	@RegisterProperty
	lateinit var verifiedTimer: Timer

	/** The path to the verified timer, relative to the HUD. */
	@Export
	@RegisterProperty
	lateinit var verifiedTimerPath: NodePath

	private var startedAffliction = false

	@RegisterFunction
	override fun _ready() {
		infectProgressBar = getNode("VBoxContainer/InfectionProgressContainer/Progress".toPath()) as ProgressBar
		infectRateText = getNode("VBoxContainer/InfectionProgressContainer/Rate".toPath()) as Label
		timeRemainsText = getNode("VBoxContainer/TimeRemaining".toPath()) as Label
		timer = getNode("Timer".toPath()) as Timer

		// If the project isn't built already and the game does not have the exported variable show up in
		// the editor, the timer may fail to add properly. This piece of code works around this, creating
		// a fake timer to please the engine.
		try {
			verifiedTimer = getNode(verifiedTimerPath) as Timer
		} catch (error: Exception) {
			GD.printErr("An error occurred when looking for the timer: ${error.localizedMessage}")
			GD.printErr("Did you build the project and reload the editor?")
			GD.printErr(error.stackTrace)
			verifiedTimer = Timer()
			verifiedTimer.waitTime = 999.0
			verifiedTimer.autostart = false
			addChild(verifiedTimer)
		}

		timer.timeout.connect(this, this::updateTimeRemainingText)
		timeRemainsText.text = "Use the arrow keys to move. Press SPACE to inflict the curse on one person."
	}

	/** Update the infection rate percentage. */
	@RegisterFunction
	fun updateInfectionRate(rate: Double) {
		infectProgressBar.value = rate
	}

	/** Update the required infection rate label. */
	@RegisterFunction
	fun updateRequiredInfectionText(required: Double) {
		infectRateText.text = "Infection Goal: ${required * 100}%"
	}

	/** Update the time remaining from the verification timer.
	 *
	 * Typically, this is only called when the internal [timer] times out
	 * and resets itself.
	 */
	@RegisterFunction
	fun updateTimeRemainingText() {
		if (!startedAffliction && verifiedTimer.isStopped()) return
		timeRemainsText.text = "${verifiedTimer.timeLeft.roundToInt()} seconds remaining!"
		startedAffliction = !verifiedTimer.isStopped()
	}
}
