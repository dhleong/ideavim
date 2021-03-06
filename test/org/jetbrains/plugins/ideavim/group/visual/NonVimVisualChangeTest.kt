/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2019 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.jetbrains.plugins.ideavim.group.visual

import com.intellij.codeInsight.editorActions.BackspaceHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.LogicalPosition
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.listener.VimListenerManager
import org.jetbrains.plugins.ideavim.VimTestCase

/**
 * @author Alex Plate
 */
class NonVimVisualChangeTest : VimTestCase() {
  fun `test save mode after removing text`() {
    // PyCharm uses BackspaceHandler.deleteToTargetPosition to remove indent
    // See https://github.com/JetBrains/ideavim/pull/186#issuecomment-486656093
    configureByText("""
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
        """.trimIndent())
    VimListenerManager.EditorListeners.add(myFixture.editor)
    typeText(parseKeys("i"))
    assertMode(CommandState.Mode.INSERT)
    ApplicationManager.getApplication().runWriteAction {
      CommandProcessor.getInstance().runUndoTransparentAction {
        BackspaceHandler.deleteToTargetPosition(myFixture.editor, LogicalPosition(2, 0))
      }
    }
    myFixture.checkResult("""
            A Discovery

            found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
        """.trimIndent())
    assertMode(CommandState.Mode.INSERT)
  }
}