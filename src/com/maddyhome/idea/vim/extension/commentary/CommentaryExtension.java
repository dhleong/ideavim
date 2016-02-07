package com.maddyhome.idea.vim.extension.commentary;

import com.intellij.codeInsight.actions.MultiCaretCodeInsightActionHandler;
import com.intellij.codeInsight.generation.CommentByBlockCommentHandler;
import com.intellij.codeInsight.generation.CommentByLineCommentHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.command.CommandState;
import com.maddyhome.idea.vim.command.MappingMode;
import com.maddyhome.idea.vim.command.SelectionType;
import com.maddyhome.idea.vim.common.TextRange;
import com.maddyhome.idea.vim.extension.VimExtensionHandler;
import com.maddyhome.idea.vim.extension.VimNonDisposableExtension;
import com.maddyhome.idea.vim.key.OperatorFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.maddyhome.idea.vim.extension.VimExtensionFacade.*;
import static com.maddyhome.idea.vim.helper.StringHelper.parseKeys;

/**
 * Based loosely on tcomment or vim-commentary,
 *  this extension provides motion-based commenting.
 *  You can toggle comments on a line with `gcc`,
 *  or, for example, in a paragraph with `gcip`.
 *
 * @author dhleong
 */
public class CommentaryExtension extends VimNonDisposableExtension {
  @NotNull
  @Override
  public String getName() {
    return "commentary";
  }

  @Override
  protected void initOnce() {
    putExtensionHandlerMapping(MappingMode.N, parseKeys("gc"), new CommentMotionHandler(), true);
    putExtensionHandlerMapping(MappingMode.N, parseKeys("gcc"), new CommentLineHandler(), true);
    putExtensionHandlerMapping(MappingMode.VO, parseKeys("gc"), new CommentMotionHandler(), true);
  }

  private static class CommentMotionHandler implements VimExtensionHandler {
    @Override
    public void execute(@NotNull Editor editor, @NotNull DataContext context) {
      setOperatorFunction(new Operator());
      executeNormal(parseKeys("g@"), editor);
    }
  }

  private static class Operator implements OperatorFunction {
    @Override
    public boolean apply(@NotNull Editor editor, @NotNull DataContext context, @NotNull SelectionType selectionType) {
      final TextRange range = getCommentRange(editor);
      if (range == null) return false;

      if (CommandState.getInstance(editor).getMode() != CommandState.Mode.VISUAL) {
        editor.getSelectionModel().setSelection(range.getStartOffset(), range.getEndOffset());
      }

      final MultiCaretCodeInsightActionHandler handler =
        selectionType == SelectionType.CHARACTER_WISE
          ? new CommentByBlockCommentHandler()
          : new CommentByLineCommentHandler();

      try {
        PsiFile file = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
        handler.invoke(editor.getProject(), editor, editor.getCaretModel().getCurrentCaret(), file);
        handler.postInvoke();
        return true;
      } catch (RuntimeException e) {
        e.printStackTrace(); // ???
      } finally {
        // remove the selection
        editor.getSelectionModel().removeSelection();
      }

      return true;
    }

    @Nullable
    private TextRange getCommentRange(@NotNull Editor editor) {
      final CommandState.Mode mode = CommandState.getInstance(editor).getMode();
      switch (mode) {
        case COMMAND:
          return VimPlugin.getMark().getChangeMarks(editor);
        case VISUAL:
          return VimPlugin.getMark().getVisualSelectionMarks(editor);
        default:
          return null;
      }
    }
  }

  private static class CommentLineHandler implements VimExtensionHandler {
    @Override
    public void execute(@NotNull Editor editor, @NotNull DataContext context) {
      final int offset = editor.getCaretModel().getOffset();
      final int line = editor.getDocument().getLineNumber(offset);
      final int lineStart = editor.getDocument().getLineStartOffset(line);
      final int lineEnd = editor.getDocument().getLineEndOffset(line);
      VimPlugin.getMark().setChangeMarks(editor, new TextRange(lineStart, lineEnd));
      new Operator().apply(editor, context, SelectionType.LINE_WISE);
    }
  }
}
