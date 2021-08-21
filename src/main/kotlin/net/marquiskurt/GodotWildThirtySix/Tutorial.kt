/*
 * Tutorial.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix

import godot.Node2D
import godot.Timer
import godot.Tween
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.Color
import net.marquiskurt.GodotWildThirtySix.extensions.getStateManager
import net.marquiskurt.GodotWildThirtySix.ui.InterfaceDialogue

/** A class that represents the tutorial scene. */
@RegisterClass
class Tutorial : Node2D() {

	/** The dialogue box that displays a conversation between Tom and the player. */
	@RegisterProperty
	lateinit var dialogue: InterfaceDialogue

	/** The timer that is used to delay the tween ending and the scene changing.*/
	@RegisterProperty
	lateinit var delay: Timer

	/** The tween animation that is used to face out the scene. */
	@RegisterProperty
	lateinit var tween: Tween

	@RegisterFunction
	override fun _ready() {
		delay = getNode("Delay".toPath()) as Timer
		delay.timeout.connect(this, this::onTimerTimeout)
		tween = getNode("Tween".toPath()) as Tween
		tween.tweenAllCompleted.connect(this, this::onTweenComplete)
		tween.interpolateProperty(
			this,
			"modulate".toPath(),
			Color.white,
			Color.transparent,
			1.5,
			Tween.TRANS_LINEAR,
			Tween.EASE_IN_OUT
		)

		dialogue = getNode("CanvasLayer/Dialogue".toPath()) as InterfaceDialogue
		dialogue.signalDialogueFinished.connect(this, this::onDialogueFinish)
		dialogue.popup()
	}

	/** Start the tweening once the dialogue is complete. */
	@RegisterFunction
	fun onDialogueFinish() {
		tween.start()
	}

	/** Start the delayed timer once the tween finishes. */
	@RegisterFunction
	fun onTweenComplete() {
		delay.start()
	}

	/** Load the first level of the game after the timer ends. */
	@RegisterFunction
	fun onTimerTimeout() {
		getStateManager().loadCurrentLevel()
	}

}
