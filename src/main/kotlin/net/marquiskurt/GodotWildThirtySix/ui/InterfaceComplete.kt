/*
 * InterfaceComplete.kt
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
import godot.annotation.RegisterProperty
import net.marquiskurt.GodotWildThirtySix.extensions.getStateManager
import net.marquiskurt.GodotWildThirtySix.toPath

/** A class that represents the game complete screen. */
@RegisterClass
class InterfaceComplete : Control() {

	@RegisterProperty
	lateinit var mainMenuButton: Button

	@RegisterFunction
	override fun _ready() {
		mainMenuButton = getNode("VBoxContainer/MainMenu".toPath()) as Button
		mainMenuButton.buttonUp.connect(this, this::onReceiveMainPress)
	}

	@RegisterFunction
	fun onReceiveMainPress() {
		getStateManager().goToMainMenu()
	}

}
