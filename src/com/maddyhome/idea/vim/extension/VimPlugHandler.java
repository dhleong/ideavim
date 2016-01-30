package com.maddyhome.idea.vim.extension;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vcs.VcsExceptionsHotFixer;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author dhleong
 */
public class VimPlugHandler implements VimExtensionHandler {

  private static final Map<String, VimExtensionHandler> REGISTRY = new HashMap<String, VimExtensionHandler>();

  private final String name;

  VimPlugHandler(String plugMappingName) {
    verifyName(plugMappingName);
    this.name = stripPlug(plugMappingName);
  }

  @Override
  public void execute(@NotNull Editor editor, @NotNull DataContext context) {
    VimExtensionHandler handler = REGISTRY.get(name);
    if (handler != null) {
      handler.execute(editor, context);
    }
  }

  static void registerPlugMapping(@NotNull String plugMappingName, @NotNull VimExtensionHandler handler) {
    verifyName(plugMappingName);
    REGISTRY.put(stripPlug(plugMappingName), handler);
  }

  /**
   * Plug mapping names themselves are case-sensitive,
   *  but the &lt;Plug&gt; prefixes are not. So, we
   *  store and retrieve without the prefix
   */
  private static String stripPlug(String plugMappingName) {
    return plugMappingName.substring("<plug>".length());
  }

  private static void verifyName(@NotNull String plugMappingName) {
    if (!checkName(plugMappingName)) {
      throw new IllegalArgumentException("<Plug> mappings MUST start with <Plug>");
    }
  }

  public static VimExtensionHandler forNamed(String plugMapName) {
    return new VimPlugHandler(plugMapName);
  }

  /**
   * @return True if the `plugMappingName` appears to be
   *  the name of a &lt;Plug&gt; mapping
   */
  public static boolean checkName(@NotNull String plugMappingName) {
    return plugMappingName.regionMatches(true, 0, "<plug>", 0, "<plug>".length());
  }
}
