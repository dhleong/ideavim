package com.maddyhome.idea.vim.extension;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vcs.VcsExceptionsHotFixer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

  public static @Nullable String extractRegisteredName(@Nullable final String sequence) {
    if (sequence == null) {
      return null;
    }

    if (!checkName(sequence)) {
      return null;
    }
    final String withoutPlug = stripPlug(sequence);
    if (REGISTRY.containsKey(withoutPlug)) {
      // easy case, but probably unlikely
      return sequence;
    }

    // NB: There could be functions like: `Func` and `FuncMore`;
    //  `Func` would also match if our sequence was `FuncMore`,
    //  so we have to filter down to candidates and take the
    //  longest one
    final List<String> candidates = ContainerUtil.filter(
        REGISTRY.keySet(),
        new Condition<String>() {
      @Override
      public boolean value(String funcName) {
        return withoutPlug.startsWith(funcName);
      }
    });

    if (candidates.isEmpty()) {
      return null;
    }

    Collections.sort(candidates, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o2.length() - o1.length();
      }
    });
    return "<Plug>" + candidates.get(0);
  }
}
