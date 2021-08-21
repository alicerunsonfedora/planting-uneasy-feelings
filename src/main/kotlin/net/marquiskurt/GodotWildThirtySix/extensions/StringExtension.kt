/*
 * StringExtension.kt
 * Copyright (c) 2021 marquiskurt. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.marquiskurt.GodotWildThirtySix

import godot.Node
import godot.core.NodePath

/** Returns a [NodePath] wrapper of this string, if it's a valid path.
 *
 * This can be used in conjunction with [Node.getNode] to remove boilerplate code of instantiating a NodePath.
 *
 * Example:
 * ```kt
 * var child = getNode("Child".toPath()) as Node
 * ```
 */
fun String.toPath(): NodePath {
    return NodePath(this)
}