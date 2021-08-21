/*
 * GameStateManager.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix

import godot.Directory
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty

/** A singleton class that is used to manage the internal state of the game. */
@RegisterClass
class GameStateManager : Node() {

    enum class GameState {
        MAIN_MENU,
        IN_GAME,
        COMPLETE,
        EXIT
    }

    /** The current level number. */
    @RegisterProperty
    var currentLevel: Int = 0

    /** The current game state. */
    val state: GameState
        get() = _state

    private var _state: GameState = GameState.MAIN_MENU

    private var totalLevelCount: Int = 0

    @RegisterFunction
    override fun _ready() {
        // Read the contents of res://scenes to count how many levels exist in the game.
        // This will be used to determine the maximum number of levels.
        with(Directory()) {
            open("res://scenes")
            listDirBegin(skipNavigational = true, skipHidden = true)
            while (true) {
                var file = getNext()
                if (file == "") break
                if (!file.startsWith("level")) continue
                totalLevelCount += 1
            }
            listDirEnd()
        }
    }

    /** Load the main menu. */
    @RegisterFunction
    fun goToMainMenu() {
        _state = GameState.MAIN_MENU
        getTree()?.changeScene("res://ui/main.tscn")
    }

    /** Load the current level, or the complete screen if the game is complete. */
    @RegisterFunction
    fun loadCurrentLevel() {
        when (_state) {
            GameState.IN_GAME -> getTree()?.changeScene("res://scenes/level$currentLevel.tscn")
            GameState.COMPLETE -> getTree()?.changeScene("res://ui/complete.tscn")
            else -> return
        }
    }

    /** Progress to the next level. If the game is complete, change the state to a complete one. */
    @RegisterFunction
    fun progress() {
        currentLevel = (currentLevel + 1).coerceIn(0..totalLevelCount)
        if (currentLevel == totalLevelCount) {
            _state = GameState.COMPLETE
            return
        }
    }

    /** Start the game. */
    @RegisterFunction
    fun startGame() {
        _state = GameState.IN_GAME
        getTree()?.changeScene("res://scenes/tutorial.tscn")
    }

    /** Quit the game. */
    @RegisterFunction
    fun quit() {
        _state = GameState.EXIT
        getTree()?.quit()
    }

}