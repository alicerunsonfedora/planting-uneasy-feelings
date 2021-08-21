/*
 * NodeExtension.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix.extensions

import godot.Node
import net.marquiskurt.GodotWildThirtySix.GameStateManager
import net.marquiskurt.GodotWildThirtySix.toPath

/** Returns the singleton instance of the [GameStateManager] class.
 *
 * This method should be used to call upon the state manager to prevent confusion with Kotlin static classes.
 */
fun Node.getStateManager(): GameStateManager {
    return getNode("/root/GameStateManager".toPath()) as GameStateManager
}