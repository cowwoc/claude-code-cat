package io.github.cowwoc.cat.hooks.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;

/**
 * Display utilities for building terminal box displays.
 *
 * Provides methods for calculating display width (accounting for emojis)
 * and building box structures with Unicode box-drawing characters.
 */
public final class DisplayUtils
{
  // Box-drawing characters - rounded corners
  private static final String TOP_LEFT = "╭";
  private static final String TOP_RIGHT = "╮";
  private static final String BOTTOM_LEFT = "╰";
  private static final String BOTTOM_RIGHT = "╯";

  // Box-drawing characters - sharp corners
  private static final String SHARP_TOP_LEFT = "┌";
  private static final String SHARP_TOP_RIGHT = "┐";
  private static final String SHARP_BOTTOM_LEFT = "└";
  private static final String SHARP_BOTTOM_RIGHT = "┘";

  // Box-drawing characters - lines
  private static final String HORIZONTAL = "─";
  private static final String VERTICAL = "│";

  // Box-drawing characters - T-junctions
  private static final String T_LEFT = "├";
  private static final String T_RIGHT = "┤";

  // Progress bar characters
  private static final String PROGRESS_FILLED = "█";
  private static final String PROGRESS_EMPTY = "░";

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>()
  {
  };

  /**
   * Cached emoji widths map.
   */
  private final Map<String, Integer> emojiWidths;

  /**
   * Sorted emojis for matching (longest first).
   */
  private final List<String> sortedEmojis;

  /**
   * Creates a DisplayUtils instance with auto-detected terminal and plugin root.
   *
   * @throws IOException if emoji-widths.json cannot be loaded
   */
  public DisplayUtils() throws IOException
  {
    this(findPluginRoot(), TerminalType.detect());
  }

  /**
   * Creates a DisplayUtils instance with auto-detected terminal type.
   *
   * @param pluginRoot the plugin root directory containing emoji-widths.json
   * @throws NullPointerException if pluginRoot is null
   * @throws IOException if emoji-widths.json cannot be loaded
   */
  public DisplayUtils(Path pluginRoot) throws IOException
  {
    this(pluginRoot, TerminalType.detect());
  }

  /**
   * Creates a DisplayUtils instance loading emoji widths from the JSON file.
   *
   * @param pluginRoot the plugin root directory containing emoji-widths.json
   * @param terminalType the terminal type to look up in the JSON
   * @throws NullPointerException if pluginRoot or terminalType is null
   * @throws IOException if emoji-widths.json cannot be loaded
   */
  public DisplayUtils(Path pluginRoot, TerminalType terminalType) throws IOException
  {
    requireThat(pluginRoot, "pluginRoot").isNotNull();
    requireThat(terminalType, "terminalType").isNotNull();

    Path widthsFile = pluginRoot.resolve("emoji-widths.json");
    Map<String, Integer> widths = loadEmojiWidthsFromFile(widthsFile, terminalType);

    this.emojiWidths = widths;
    this.sortedEmojis = new ArrayList<>(widths.keySet());
    this.sortedEmojis.sort(Comparator.comparingInt(String::length).reversed());
  }

  /**
   * Finds the plugin root directory.
   *
   * Checks CLAUDE_PLUGIN_ROOT environment variable first, then walks up
   * from current directory looking for emoji-widths.json.
   *
   * @return the plugin root path
   * @throws IOException if plugin root cannot be found
   */
  private static Path findPluginRoot() throws IOException
  {
    // Check environment variable first
    String envRoot = System.getenv("CLAUDE_PLUGIN_ROOT");
    if (envRoot != null && !envRoot.isEmpty())
    {
      Path path = Path.of(envRoot);
      if (Files.exists(path.resolve("emoji-widths.json")))
        return path;
    }

    // Walk up from current directory to find emoji-widths.json
    Path current = Path.of(System.getProperty("user.dir"));
    for (int i = 0; i < 10; ++i)
    {
      Path candidate = current.resolve("plugin").resolve("emoji-widths.json");
      if (Files.exists(candidate))
        return candidate.getParent();

      // Also check directly in current
      candidate = current.resolve("emoji-widths.json");
      if (Files.exists(candidate))
        return current;

      Path parent = current.getParent();
      if (parent == null)
        break;
      current = parent;
    }

    throw new IOException("Cannot find emoji-widths.json - set CLAUDE_PLUGIN_ROOT environment variable");
  }

  /**
   * Loads emoji widths from the JSON file.
   *
   * @param path the path to emoji-widths.json
   * @param terminalType the terminal type to look up
   * @return the map of emoji to width
   * @throws IOException if the file cannot be loaded
   */
  private Map<String, Integer> loadEmojiWidthsFromFile(Path path, TerminalType terminalType) throws IOException
  {
    String content = Files.readString(path);
    JsonMapper mapper = JsonMapper.builder().build();
    Map<String, Object> data = mapper.readValue(content, MAP_TYPE);

    @SuppressWarnings("unchecked")
    Map<String, Object> terminals = (Map<String, Object>) data.get("terminals");

    if (terminals == null || terminals.isEmpty())
      throw new IOException("No terminals found in " + path);

    // Try to find the specified terminal
    Map<String, Integer> result = new HashMap<>();
    Object terminalData = terminals.get(terminalType.getJsonKey());

    // Fall back to first terminal if specified terminal not found
    if (terminalData == null)
    {
      terminalData = terminals.values().iterator().next();
    }

    if (terminalData instanceof Map)
    {
      @SuppressWarnings("unchecked")
      Map<String, Object> terminalWidths = (Map<String, Object>) terminalData;
      for (Map.Entry<String, Object> entry : terminalWidths.entrySet())
      {
        if (entry.getValue() instanceof Number num)
          result.put(entry.getKey(), num.intValue());
      }
    }

    if (result.isEmpty())
      throw new IOException("No emoji widths found in " + path);

    return result;
  }

  /**
   * Calculate the terminal display width of text.
   *
   * Accounts for emoji display widths which may differ from character count.
   *
   * @param text the text to measure
   * @return the display width in terminal columns
   */
  public int displayWidth(String text)
  {
    requireThat(text, "text").isNotNull();

    int width = 0;
    int i = 0;
    int textLen = text.length();

    while (i < textLen)
    {
      boolean matched = false;
      String remaining = text.substring(i);

      // Try to match known emojis (longest first)
      for (String emoji : sortedEmojis)
      {
        if (remaining.startsWith(emoji))
        {
          width += emojiWidths.get(emoji);
          i += emoji.length();
          matched = true;
          break;
        }
      }

      if (!matched)
      {
        // Regular character = width 1
        ++width;
        ++i;
      }
    }

    return width;
  }

  /**
   * Build a single box line with content and padding.
   *
   * @param content the content to display
   * @param maxWidth the maximum content width (excluding borders)
   * @return the formatted box line
   */
  public String buildLine(String content, int maxWidth)
  {
    requireThat(content, "content").isNotNull();
    requireThat(maxWidth, "maxWidth").isNotNegative();

    int contentWidth = displayWidth(content);
    int padding = maxWidth - contentWidth;
    if (padding < 0)
      padding = 0;

    return VERTICAL + " " + content + " ".repeat(padding) + " " + VERTICAL;
  }

  /**
   * Build a top or bottom border.
   *
   * @param maxWidth the maximum content width (excluding borders)
   * @param isTop true for top border, false for bottom border
   * @return the formatted border line
   */
  public String buildBorder(int maxWidth, boolean isTop)
  {
    requireThat(maxWidth, "maxWidth").isNotNegative();

    int dashCount = maxWidth + 2;
    String dashes = HORIZONTAL.repeat(dashCount);
    if (isTop)
      return TOP_LEFT + dashes + TOP_RIGHT;
    else
      return BOTTOM_LEFT + dashes + BOTTOM_RIGHT;
  }

  /**
   * Build a horizontal separator line.
   *
   * @param maxWidth the maximum content width (excluding borders)
   * @return the formatted separator line
   */
  public String buildSeparator(int maxWidth)
  {
    requireThat(maxWidth, "maxWidth").isNotNegative();

    int dashCount = maxWidth + 2;
    String dashes = HORIZONTAL.repeat(dashCount);
    return T_LEFT + dashes + T_RIGHT;
  }

  /**
   * Build a box with header and optional separators.
   *
   * @param header header text to display
   * @param contentLines lines of content inside the box
   * @return the complete box as a string
   */
  public String buildHeaderBox(String header, List<String> contentLines)
  {
    return buildHeaderBox(header, contentLines, List.of(), null, null);
  }

  /**
   * Build a box with header and optional separators.
   *
   * @param header header text to display
   * @param contentLines lines of content inside the box
   * @param minWidth minimum box width, or null for auto
   * @return the complete box as a string
   */
  public String buildHeaderBox(String header, List<String> contentLines, Integer minWidth)
  {
    return buildHeaderBox(header, contentLines, List.of(), minWidth, null);
  }

  /**
   * Build a box with header and optional separators.
   *
   * @param header header text to display
   * @param contentLines lines of content inside the box
   * @param separatorIndices indices where separator lines should be inserted
   * @return the complete box as a string
   */
  public String buildHeaderBox(String header, List<String> contentLines, List<Integer> separatorIndices)
  {
    return buildHeaderBox(header, contentLines, separatorIndices, null, null);
  }

  /**
   * Build a box with header and optional separators.
   *
   * @param header header text to display
   * @param contentLines lines of content inside the box
   * @param separatorIndices indices where separator lines should be inserted
   * @param minWidth minimum box width, or null for auto
   * @param prefix header prefix (default "--- ", use "- " for simpler style)
   * @return the complete box as a string
   */
  public String buildHeaderBox(String header, List<String> contentLines,
                               List<Integer> separatorIndices, Integer minWidth, String prefix)
  {
    requireThat(header, "header").isNotNull();
    requireThat(contentLines, "contentLines").isNotNull();

    if (separatorIndices == null)
      separatorIndices = List.of();

    if (prefix == null)
      prefix = HORIZONTAL + HORIZONTAL + HORIZONTAL + " ";  // "--- "

    // Calculate max width
    int maxContentWidth = 0;
    for (String line : contentLines)
    {
      int w = displayWidth(line);
      if (w > maxContentWidth)
        maxContentWidth = w;
    }

    // Account for prefix in header width calculation
    int headerWidth = displayWidth(header) + prefix.length() + 1;  // +1 for space before suffix dashes
    int maxWidth = Math.max(maxContentWidth, headerWidth);
    if (minWidth != null && minWidth > maxWidth)
      maxWidth = minWidth;

    // Build header
    int suffixDashCount = maxWidth - prefix.length() - displayWidth(header) + 1;
    if (suffixDashCount < 1)
      suffixDashCount = 1;
    String suffixDashes = HORIZONTAL.repeat(suffixDashCount);
    String top = TOP_LEFT + prefix + header + " " + suffixDashes + TOP_RIGHT;

    List<String> lines = new ArrayList<>();
    lines.add(top);

    for (int i = 0; i < contentLines.size(); i++)
    {
      if (separatorIndices.contains(i))
        lines.add(buildSeparator(maxWidth));
      lines.add(buildLine(contentLines.get(i), maxWidth));
    }

    lines.add(buildBorder(maxWidth, false));
    return String.join("\n", lines);
  }

  /**
   * Build a progress bar string.
   *
   * @param percent progress percentage (0-100)
   * @param width width of the progress bar in characters
   * @return the progress bar string
   */
  public String buildProgressBar(int percent, int width)
  {
    requireThat(percent, "percent").isBetween(0, 100);
    requireThat(width, "width").isPositive();

    int filled = percent * width / 100;
    int empty = width - filled;
    return PROGRESS_FILLED.repeat(filled) + PROGRESS_EMPTY.repeat(empty);
  }

  /**
   * Build a progress bar string with default width of 25.
   *
   * @param percent progress percentage (0-100)
   * @return the progress bar string
   */
  public String buildProgressBar(int percent)
  {
    return buildProgressBar(percent, 25);
  }

  /**
   * Build an inner box with header and content.
   *
   * @param header header text
   * @param contentItems content lines
   * @return list of lines forming the inner box
   */
  public List<String> buildInnerBox(String header, List<String> contentItems)
  {
    return buildInnerBox(header, contentItems, null);
  }

  /**
   * Build an inner box with header and content.
   *
   * @param header header text
   * @param contentItems content lines
   * @param forcedWidth forced width, or null for auto
   * @return list of lines forming the inner box
   */
  public List<String> buildInnerBox(String header, List<String> contentItems, Integer forcedWidth)
  {
    requireThat(header, "header").isNotNull();
    requireThat(contentItems, "contentItems").isNotNull();

    if (contentItems.isEmpty())
      contentItems = List.of("");

    int maxContentWidth = 0;
    for (String item : contentItems)
    {
      int w = displayWidth(item);
      if (w > maxContentWidth)
        maxContentWidth = w;
    }

    int headerWidth = displayWidth(header);
    int headerMinWidth = headerWidth + 1;

    int innerMax = Math.max(headerMinWidth, maxContentWidth);
    if (forcedWidth != null && forcedWidth > innerMax)
      innerMax = forcedWidth;

    int remaining = innerMax - headerWidth - 1;
    if (remaining < 0)
      remaining = 0;
    String dashes = remaining > 0 ? HORIZONTAL.repeat(remaining) : "";
    String innerTop = TOP_LEFT + HORIZONTAL + " " + header + " " + dashes + TOP_RIGHT;

    List<String> lines = new ArrayList<>();
    lines.add(innerTop);
    for (String item : contentItems)
    {
      lines.add(buildLine(item, innerMax));
    }
    lines.add(buildBorder(innerMax, false));

    return lines;
  }

  /**
   * Build a simple box with icon prefix header.
   *
   * @param icon the icon character(s) to display
   * @param title the title text
   * @param contentLines the content lines
   * @return the complete box as a string
   */
  public String buildSimpleBox(String icon, String title, List<String> contentLines)
  {
    requireThat(icon, "icon").isNotNull();
    requireThat(title, "title").isNotNull();
    requireThat(contentLines, "contentLines").isNotNull();

    String prefix = HORIZONTAL + " " + icon + " " + title;

    // Calculate max width from content
    int maxContentWidth = 0;
    for (String line : contentLines)
    {
      int w = displayWidth(line);
      if (w > maxContentWidth)
        maxContentWidth = w;
    }

    int prefixWidth = displayWidth(prefix);
    int innerWidth = Math.max(maxContentWidth, prefixWidth) + 2;

    List<String> lines = new ArrayList<>();

    // Top border with embedded prefix
    String suffixDashes = HORIZONTAL.repeat(innerWidth - prefixWidth);
    lines.add(TOP_LEFT + prefix + suffixDashes + TOP_RIGHT);

    // Content lines
    for (String content : contentLines)
    {
      int padding = innerWidth - displayWidth(content);
      lines.add(VERTICAL + " " + content + " ".repeat(padding - 1) + VERTICAL);
    }

    // Bottom border
    lines.add(BOTTOM_LEFT + HORIZONTAL.repeat(innerWidth) + BOTTOM_RIGHT);

    return String.join("\n", lines);
  }

  /**
   * Build a top border with embedded header text.
   *
   * @param header the header text
   * @param maxWidth the maximum content width
   * @return the formatted header top line
   */
  public String buildHeaderTop(String header, int maxWidth)
  {
    requireThat(header, "header").isNotNull();
    requireThat(maxWidth, "maxWidth").isNotNegative();

    int innerWidth = maxWidth + 2;
    int headerWidth = displayWidth(header);
    String prefixDashes = HORIZONTAL + HORIZONTAL + HORIZONTAL + " ";  // 4 chars
    int suffixDashesCount = innerWidth - 4 - headerWidth - 1;
    if (suffixDashesCount < 1)
      suffixDashesCount = 1;
    String suffixDashes = HORIZONTAL.repeat(suffixDashesCount);
    return TOP_LEFT + prefixDashes + header + " " + suffixDashes + TOP_RIGHT;
  }

  /**
   * Build a concern box with square corners.
   *
   * @param severity the severity label
   * @param concerns the list of concerns
   * @return the complete concern box as a string
   */
  public String buildConcernBox(String severity, List<String> concerns)
  {
    requireThat(severity, "severity").isNotNull();
    requireThat(concerns, "concerns").isNotNull();

    int maxContentWidth = 0;
    for (String concern : concerns)
    {
      int w = displayWidth(concern);
      if (w > maxContentWidth)
        maxContentWidth = w;
    }

    int headerWidth = displayWidth(severity) + 4;
    int maxWidth = Math.max(maxContentWidth, headerWidth);

    // Square corner box
    String top = SHARP_TOP_LEFT + HORIZONTAL + " " + severity + " " +
                 HORIZONTAL.repeat(maxWidth - displayWidth(severity) - 1) + SHARP_TOP_RIGHT;

    List<String> lines = new ArrayList<>();
    lines.add(top);
    for (String content : concerns)
    {
      int padding = maxWidth - displayWidth(content);
      lines.add(VERTICAL + " " + content + " ".repeat(padding) + " " + VERTICAL);
    }
    lines.add(SHARP_BOTTOM_LEFT + HORIZONTAL.repeat(maxWidth + 2) + SHARP_BOTTOM_RIGHT);

    return String.join("\n", lines);
  }
}
