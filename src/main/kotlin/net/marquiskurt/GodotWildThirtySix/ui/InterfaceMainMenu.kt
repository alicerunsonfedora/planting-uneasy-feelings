/*
 * InterfaceMainMenu.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix.ui

import godot.Button
import godot.Control
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import net.marquiskurt.GodotWildThirtySix.extensions.getStateManager
import net.marquiskurt.GodotWildThirtySix.toPath

/** A class that manages the main menu on startup. */
@RegisterClass
class InterfaceMainMenu : Control() {

	private lateinit var startButton: Button
	private lateinit var quitButton: Button

	@RegisterFunction
	override fun _ready() {
		startButton = getNode("VBoxContainer/StartButton".toPath()) as Button
		quitButton = getNode("VBoxContainer/CloseButton".toPath()) as Button

		startButton.buttonUp.connect(this, this::onReceiveStartPress)
		quitButton.buttonUp.connect(this, this::onReceiveQuitPress)
	}

	/** Start the game. */
	@RegisterFunction
	fun onReceiveStartPress() {
		getStateManager().startGame()
	}

	/** Quit the game. */
	@RegisterFunction
	fun onReceiveQuitPress() {
		getStateManager().quit()
	}
}
