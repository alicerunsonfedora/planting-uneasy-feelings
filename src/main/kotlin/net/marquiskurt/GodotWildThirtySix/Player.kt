/*
 * Player.kt
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
import godot.annotation.RegisterSignal
import godot.core.NodePath
import godot.core.Vector2
import godot.signals.signal

/** A class that represents the player which is responsible for providing the tree curse. */
@RegisterClass
class Player : KinematicBody2D() {

    /** A parameterless signal that represents that the player wishes to deploy the curse. */
    @RegisterSignal
    val signalDeployCurse by signal()

    /** The player's internal velocity vector. */
    private var velocity = Vector2.ZERO

    /** Whether the player can cast a spell.
     *
     * Typically, the player can only cast a spell once to spread the curse.
     */
    private var canCastSpell = true

    /** The animation tree that will handle which states the player is in for animation. */
    private lateinit var animStateMachine: AnimationTree

    /** The current animation state of the player.*/
    private lateinit var animationPlayback: AnimationNodeStateMachinePlayback

    @RegisterFunction
    override fun _ready() {
        animStateMachine = getNode("AnimationTree".toPath()) as AnimationTree
        animationPlayback = animStateMachine.get("parameters/playback") as AnimationNodeStateMachinePlayback
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        val movementVector = getMovementVector()
        val acceleration = 250
        val friction = 100
        val maxSpeed = 1000
        val speed = 500

        when {
            movementVector != Vector2.ZERO -> {
                // The blend position determines which way the player faces in the animation
                // cycle.
                animStateMachine.set("parameters/idle/blend_position", movementVector)
                animStateMachine.set("parameters/walk/blend_position", movementVector)
                animationPlayback.travel("walk")
                velocity += movementVector * acceleration * delta
                velocity = velocity.clamped(maxSpeed * delta)
            }
            else -> {
                animationPlayback.travel("idle")
                velocity = velocity.moveToward(Vector2.ZERO, friction * delta)
            }
        }
        moveAndSlide(velocity * delta * speed)
    }

    @RegisterFunction
    override fun _unhandledKeyInput(event: InputEventKey) {
        // The only key not handled by `Player.getMovementVector` that the Player is concerned with is the
        // interact key, which is responsible for deploying the curse.
        if (!event.isActionPressed("interact") || !canCastSpell) return
        signalDeployCurse.emit()
        canCastSpell = false
        super._unhandledKeyInput(event)
    }

    /** Returns a normalized vector that represents the user's preferred direction of movement. */
    @RegisterFunction
    fun getMovementVector(): Vector2 {
        return Vector2(
            x = Input.getActionStrength("moveRight") - Input.getActionStrength("moveLeft"),
            y = Input.getActionStrength("moveDown") - Input.getActionStrength("moveUp")
        ).normalized()
    }

}
