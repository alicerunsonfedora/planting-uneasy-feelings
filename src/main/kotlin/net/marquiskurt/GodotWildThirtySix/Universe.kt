/*
 * Universe.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix

import godot.Node2D
import godot.Timer
import godot.annotation.*
import godot.global.GD
import net.marquiskurt.GodotWildThirtySix.extensions.getStateManager
import net.marquiskurt.GodotWildThirtySix.ui.InterfaceHUD
import net.marquiskurt.GodotWildThirtySix.ui.InterfaceLevelPrompt

/** The primary class that handles all game loop events for a standard level. */
@RegisterClass
class Universe : Node2D() {

	/** A mutable set of the entities that were last infected. */
	private var lastInfected: MutableSet<Entity> = mutableSetOf()

	/** The player object in the current level. */
	@RegisterProperty
	lateinit var player: Player

	/** A timer that helps spread the infection. */
	@RegisterProperty
	lateinit var destiny: Timer

	/** A timer that is used to count down how long before the game checks how many have been infected.
	 *
	 * The timer starts when the player inflicts the curse upon the first individual via a signal. The spreading is
	 * limited by the amount of time left in the verifier before considered for evaluation.
	 */
	@RegisterProperty
	lateinit var verifier: Timer

	/** The heads-up display on the current level. */
	@RegisterProperty
	lateinit var hud: InterfaceHUD

	@RegisterProperty
	lateinit var verifierPrompt: InterfaceLevelPrompt

	/** The minimum required infection rate percentage for the level to pass.
	 *
	 * The default percentage is 10% (0.1).
	 */
	@DoubleRange(0.0, 1.0, 0.01)
	@Export
	@RegisterProperty
	var requiredInfectionRate: Double = 0.1

	@RegisterFunction
	override fun _ready() {
		player = getNode("Player".toPath()) as Player
		player.signalDeployCurse.connect(this, this::onReceiveSignalCurse)

		destiny = getNode("Destiny".toPath()) as Timer
		destiny.timeout.connect(this, this::onReceiveSignalTimeout)

		verifier = getNode("Verifier".toPath()) as Timer
		verifier.timeout.connect(this, this::onReceiveVerifierTimeout)

		hud = getNode("CanvasLayer/HUD".toPath()) as InterfaceHUD
		hud.updateRequiredInfectionText(requiredInfectionRate)

		verifierPrompt = getNode("CanvasLayer/GameOver".toPath()) as InterfaceLevelPrompt
		verifierPrompt.requiredAmount = requiredInfectionRate
		verifierPrompt.signalCallRestart.connect(this, this::onReceiveRestartRequest)
		verifierPrompt.signalCallNext.connect(this, this::onReceiveNextLevelRequest)

		getEntities().forEach {
			it.signalInfected.connect(this, this::onReceiveInfectedSignal)
		}
	}

	/** Returns the list of entities in the current room. */
	@RegisterFunction
	fun getEntities(): List<Entity> {
		return getChildren().filterIsInstance<Entity>()
	}

	/** Returns the list of entities in the room that are infected. */
	private fun getInfectedEntities(): List<Entity> {
		return getEntities().filter { it.isInfected }
	}

	/** Returns the percentage of which entities in the room are infected. */
	private fun getInfectedPercentage(): Double {
		return getInfectedEntities().count().toDouble() / getEntities().count().toDouble()
	}

	/** Returns whether the percent of those infected meets at least the required infection rate. */
	private fun levelPassed(): Boolean {
		return getInfectedPercentage() >= requiredInfectionRate
	}

	/** Returns the entity closest to the player, or null if none exist. */
	private fun getClosestEntityToPlayer(): Entity? {
		var minimumDistance = Double.POSITIVE_INFINITY
		var entity: Entity? = null
		for (child in getChildren()) {
			if (child is Entity) {
				if (child.position.distanceTo(player.position) < minimumDistance) {
					minimumDistance = child.position.distanceTo(player.position)
					entity = child
				}
			} else continue

		}
		return entity
	}

	/** Infect any nearby entities using a randomized algorithm.
	 *
	 * No nearby infections occur if the closest entities from the specified [entity] are already infected. Otherwise,
	 * based on the neighboring entities' immunity values, one to two people will be infected.
	 *
	 * @param entity The entity that will attempt to infect nearby entities.
	 */
	@RegisterFunction
	fun infectNearbyEntities(entity: Entity) {
		if (!entity.isInfected || lastInfected.isEmpty()) return

		var neighbors = getEntities().filter {
			entity.position.distanceTo(it.position) < entity.infectionBubble.radius * 2
		}

		if (neighbors.isEmpty()) {
			lastInfected.remove(entity)
			return
		}

		if (neighbors.count() > 5) {
			neighbors = neighbors.slice(0..4)
		}

		val nonInfected = neighbors.filter { !it.isInfected }
		if (nonInfected.isEmpty()) {
			lastInfected.remove(entity)
			return
		}

		// Entities with a high immunity (0.9..1.0) don't spread the curse at all.
		when {
			// Infect a single individual. For cases where the immunity is greater but the list of non-infected
			// individuals is smaller, this is also executed to prevent a JVM exception.
			entity.immunity in 0.61..0.8 || nonInfected.count() < 3 -> {
				lastInfected.add(nonInfected.random())
				lastInfected.last().infect()
			}

			// Infect two people.
			entity.immunity in 0.4..0.6 -> {
				nonInfected.slice(0..2).forEach {
					it.infect()
					lastInfected.add(it)
				}
			}

			// Infect everyone near the entity.
			entity.immunity < 0.4 -> {
				nonInfected.forEach { it.infect() }
				lastInfected.addAll(nonInfected)
			}
		}

		// This prevents the entity from being counted in future infections.
		lastInfected.remove(entity)
	}

	/** Attempt to infect an individual with the curse.
	 *
	 * This method is fired when the player presses the interact key and sends the signal [Player.signalDeployCurse].
	 * If the entity is within 64 units from the player, they will be infected, and the universe will track that
	 * individual as the last infected.
	 */
	@RegisterFunction
	fun onReceiveSignalCurse() {
		val neighbor = getClosestEntityToPlayer() ?: return
		with(neighbor) {
			if (position.distanceTo(player.position) > 64) return
			infect()
			lastInfected.add(this)
			verifier.start()
		}
	}

	/** Start infecting nearby entities after timing out.
	 *
	 * This method is fired when the [destiny] timer has timed out from the signal [Timer.timeout] after 2.5 seconds, or
	 * whatever value is set in the timer.
	 */
	@RegisterFunction
	fun onReceiveSignalTimeout() {
		if (lastInfected.isEmpty()) return
		val currentLastInfected = lastInfected.toMutableSet()
		currentLastInfected.forEach { infectNearbyEntities(it) }
	}

	/** Verify the results of the curse spread. */
	@RegisterFunction
	fun onReceiveVerifierTimeout() {
		GD.print("Current rate: ${getInfectedPercentage() * 100} vs. required rate: ${requiredInfectionRate * 100}")

		verifierPrompt.scoredAmount = getInfectedPercentage()
		verifierPrompt.state = when {
			levelPassed() -> InterfaceLevelPrompt.LevelState.SUCCESS
			levelPassed() && (getInfectedPercentage() == 1.0) -> InterfaceLevelPrompt.LevelState.HUGE_SUCCESS
			else -> InterfaceLevelPrompt.LevelState.FAILURE
		}
		verifierPrompt.renderResults()
		verifierPrompt.popupCentered()
	}

	/** Update the HUD when a new individual is infected. */
	@RegisterFunction
	fun onReceiveInfectedSignal() {
		hud.updateInfectionRate(getInfectedPercentage())
	}

	/** Restart the current level when the player requests it. */
	@RegisterFunction
	fun onReceiveRestartRequest() {
		// NOTE: Using getTree()?.reloadCurrentScene() causes issues with the level for some unknown reason.
		// Force-loading it this way might fix the issue.
		getStateManager().loadCurrentLevel()
	}

	/** Move to the next level when the player requests it. */
	@RegisterFunction
	fun onReceiveNextLevelRequest() {
		with(getStateManager()) {
			progress()
			loadCurrentLevel()
		}
	}

}
