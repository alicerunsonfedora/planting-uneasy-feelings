/*
 * Entity.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix

import godot.*
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.global.GD
import godot.signals.signal

@RegisterClass
class Entity : Area2D() {

	/** A parameterless signal that emits when the entity has been infected.
	 *
	 * This can be used to start infection timers or to pause execution of verification. [Universe] uses this to call on
	 * the HUD to refresh with new data.
	 */
	@RegisterSignal
	val signalInfected by signal()

	/** Whether the entity is infected.
	 *
	 * Defaults to false.
	 */
	@RegisterProperty
	var isInfected: Boolean = false

	/** A value that represents how likely it is for the entity to be immune to the curse.
	 *
	 * While all entities cannot escape the curse, the immunity value more or less reflects if the entity will be able
	 * to spread it more easily. This value is assigned a random value at runtime. Those with a higher immunity are less
	 * likely to spread the curse, while those with a lower immunity are more likely to spread it.
	 *
	 * @see Universe.infectNearbyEntities
	 *
	 * Note: This value can never be less than 0, and it can never be greater than 1. The compiler will auto-clamp the
	 * value on reassignment.
	 */
	@RegisterProperty
	var immunity: Double
		get() = _immunity
		set(value) {
			_immunity = GD.clamp(value, 0, 1)
		}

	/**The circle shape that corresponds to the area where infections can occur. */
	@RegisterProperty
	lateinit var infectionBubble: CircleShape2D

	/** A private instance of the entity's immunity.
	 * @see immunity
	 */
	private var _immunity: Double = GD.randf()

	private val _outfitNames = listOf("Scientist", "Receptionist")

	private lateinit var _outfits: AnimatedSprite
	private lateinit var _infection: AnimatedSprite
	private lateinit var _nakedBody: AnimatedSprite
	private lateinit var _collider: CollisionShape2D
	private lateinit var _scream: AudioStreamPlayer2D

	@RegisterFunction
	override fun __new() {
		// Ensure the node gets a unique random seed before starting.
		GD.randomize()
		super.__new()
	}

	@RegisterFunction
	override fun _ready() {
		_outfits = getNode("Outfits".toPath()) as AnimatedSprite
		_nakedBody = getNode("Base".toPath()) as AnimatedSprite
		_infection = getNode("Infection".toPath()) as AnimatedSprite
		_collider = getNode("Detector".toPath()) as CollisionShape2D
		_scream = getNode("Screamer".toPath()) as AudioStreamPlayer2D

		// Pick a random scream noise to make it more distinct.
		_scream.stream = GD.load("res://assets/scream${GD.randRange(1, 2)}.wav")

		if (_collider.shape != null) {
			infectionBubble = _collider.shape as CircleShape2D
		}

		// Pick a random outfit name and apply it. If this entity has a weak immunity,
		// apply the masked variant.
		_outfits.animation = _outfitNames.random()
		if (_immunity < 0.4) {
			_outfits.animation += " (Masked)"
		}

		// If we're named "Tom", show Tom instead.
		if (name == "Tom") {
			_nakedBody.visible = false
			_outfits.animation = "Tom"
		}

		// Play the animation loop on all of the animated sprites at once. This is done
		// here and not in the editor because the editor can cause the animations to go
		// out of sync when animations are added to any of them.
		_outfits.play(_outfits.animation)
		_nakedBody.play("default")
		_infection.play("default")
	}

	/** Infect this entity with the curse if they haven't been already. */
	@RegisterFunction
	fun infect() {
		if (isInfected) return
		isInfected = true
		immunity -= 0.2
		_infection.visible = true
		_scream.play()
		signalInfected.emit()
	}

}
