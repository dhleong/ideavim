package com.maddyhome.idea.vim.extension.repeat;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.command.MappingMode;
import com.maddyhome.idea.vim.extension.VimExtensionHandler;
import com.maddyhome.idea.vim.extension.VimNonDisposableExtension;
import com.maddyhome.idea.vim.extension.VimPlugHandler;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

import static com.maddyhome.idea.vim.extension.VimExtensionFacade.*;
import static com.maddyhome.idea.vim.helper.StringHelper.parseKeys;

/**
 * @author dhleong
 */
public class VimRepeat extends VimNonDisposableExtension {

  static String repeatSequence;

  @NotNull
  @Override
  public String getName() {
    return "repeat";
  }

  @Override
  protected void initOnce() {
    putExtensionHandlerPlugMapping(MappingMode.N, "<Plug>RepeatDot", new RepeatRunHandler());
    putPlugMapping(MappingMode.N, parseKeys("."), "<Plug>RepeatDot");
  }

  static class RepeatRunHandler implements VimExtensionHandler {
    @Override
    public void execute(@NotNull Editor editor, @NotNull DataContext context) {
      final String sequence = repeatSequence;
      String plugMapName = VimPlugHandler.extractRegisteredName(sequence);
      if (plugMapName == null) {
        // fallback to the normal thing
        executeNormalNoremap(parseKeys("."), editor, context);
        return;
      }

      String keys = sequence.substring(plugMapName.length());
      feedKeys(editor, parseKeys(keys));
      VimPlugHandler.forNamed(plugMapName).execute(editor, context);
    }

  }

  public static void set(@NotNull String sequence) {
    repeatSequence = sequence;
  }

}
