package io.github.cowwoc.cat.hooks.skills;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import io.github.cowwoc.cat.hooks.JvmScope;

import static io.github.cowwoc.cat.hooks.skills.JsonHelper.getStringOrDefault;
import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;

/**
 * Output generator for /cat:token-report skill.
 *
 * Reads session JSONL directly and computes token table in Java,
 * eliminating the Python subprocess call.
 */
public final class GetTokenReportOutput
{
  /**
   * Column widths (fixed).
   */
  private static final int COL_TYPE = 17;
  private static final int COL_DESC = 30;
  private static final int COL_TOKENS = 8;
  private static final int COL_CONTEXT = 16;
  private static final int COL_DURATION = 10;

  /**
   * Context limit for percentage calculation.
   */
  private static final int CONTEXT_LIMIT = 200000;

  /**
   * The JVM scope for accessing shared services.
   */
  private final JvmScope scope;

  /**
   * Creates a GetTokenReportOutput instance.
   *
   * @param scope the JVM scope for accessing shared services
   * @throws NullPointerException if scope is null
   */
  public GetTokenReportOutput(JvmScope scope)
  {
    requireThat(scope, "scope").isNotNull();
    this.scope = scope;
  }

  /**
   * Run token table computation and return result.
   *
   * @param sessionId the session ID to analyze
   * @return the formatted output, or null if session file not found
   * @throws NullPointerException if sessionId is null
   * @throws IllegalArgumentException if sessionId is blank
   */
  public String getOutput(String sessionId)
  {
    requireThat(sessionId, "sessionId").isNotBlank();

    // Build session file path
    Path sessionFile = Path.of(System.getProperty("user.home"),
                               ".config", "claude", "projects", "-workspace", sessionId + ".jsonl");

    if (!Files.exists(sessionFile))
      return null;

    // Extract subagent data directly from session file
    SubagentData data = extractSubagentData(sessionFile);
    if (data == null)
      return null;

    if (data.subagents.isEmpty())
      return "No subagent data found in session.";

    // Build table lines
    List<String> lines = buildTableLines(data);

    String tableLines = String.join("\n", lines);

    return tableLines + "\n" +
           "\n" +
           "Summary: " + data.subagents.size() + " subagents, " + data.totalTokens + " total tokens\n" +
           "\n" +
           "Legend:\n" +
           "- Percentages show context utilization per subagent\n" +
           "- Warning emoji inside Context column indicates high (>=40%) usage\n" +
           "- Critical emoji inside Context column indicates exceeded (>=80%) limit";
  }

  /**
   * Extracts subagent data from session JSONL file.
   *
   * @param sessionFile the session file path
   * @return the extracted data, or null on error
   */
  private SubagentData extractSubagentData(Path sessionFile)
  {
    List<SubagentEntry> subagents = new ArrayList<>();
    int totalTokens = 0;
    int totalDurationMs = 0;

    try (BufferedReader reader = Files.newBufferedReader(sessionFile))
    {
      JsonMapper mapper = JsonMapper.builder().build();
      List<JsonNode> entries = new ArrayList<>();

      // Read all JSONL entries
      String line;
      while ((line = reader.readLine()) != null)
      {
        if (!line.trim().isEmpty())
        {
          JsonNode entry = mapper.readTree(line);
          entries.add(entry);
        }
      }

      // Find Task tool_use entries and their results
      for (int i = 0; i < entries.size(); ++i)
      {
        JsonNode entry = entries.get(i);
        if (!isAssistantEntry(entry))
          continue;

        // Look for Task tool_use in message content
        JsonNode message = entry.get("message");
        if (message == null)
          continue;

        JsonNode content = message.get("content");
        if (content == null || !content.isArray())
          continue;

        for (JsonNode item : content)
        {
          if (!isTaskToolUse(item))
            continue;

          String toolUseId = getStringOrDefault(item, "id", "");
          if (toolUseId.isEmpty())
            continue;

          // Extract task info
          JsonNode input = item.get("input");
          String taskType = "";
          String description = "Subagent task";

          if (input != null)
          {
            String prompt = getStringOrDefault(input, "prompt", "");
            if (!prompt.isEmpty())
            {
              // Get first line, remove "## " prefix
              String firstLine = prompt.split("\n")[0];
              if (firstLine.startsWith("## "))
                firstLine = firstLine.substring(3);
              taskType = truncate(firstLine, 25);
            }
            description = truncate(getStringOrDefault(input, "description", "Subagent task"), 28);
          }

          // Find the result for this tool_use
          SubagentResult result = findToolUseResult(entries, toolUseId, i);

          if (result.tokens > 0)
          {
            SubagentEntry subagent = new SubagentEntry();
            subagent.type = taskType;
            subagent.description = description;
            subagent.tokens = result.tokens;
            subagent.durationMs = result.durationMs;
            subagents.add(subagent);

            totalTokens += result.tokens;
            totalDurationMs += result.durationMs;
          }
        }
      }
    }
    catch (IOException e)
    {
      return null;
    }

    SubagentData data = new SubagentData();
    data.subagents = subagents;
    data.totalTokens = totalTokens;
    data.totalDurationMs = totalDurationMs;
    return data;
  }

  /**
   * Checks if entry is an assistant message.
   *
   * @param entry the JSON entry
   * @return true if it's an assistant message
   */
  private boolean isAssistantEntry(JsonNode entry)
  {
    JsonNode type = entry.get("type");
    return type != null && type.isString() && type.asString().equals("assistant");
  }

  /**
   * Checks if content item is a Task tool_use.
   *
   * @param item the content item
   * @return true if it's a Task tool_use
   */
  private boolean isTaskToolUse(JsonNode item)
  {
    JsonNode type = item.get("type");
    JsonNode name = item.get("name");
    return type != null && type.isString() && type.asString().equals("tool_use") &&
           name != null && name.isString() && name.asString().equals("Task");
  }

  /**
   * Finds the tool_use result for a given tool_use ID.
   *
   * @param entries all session entries
   * @param toolUseId the tool_use ID to find
   * @param startIndex the index to start searching from
   * @return the result with tokens and duration
   */
  private SubagentResult findToolUseResult(List<JsonNode> entries, String toolUseId, int startIndex)
  {
    SubagentResult result = new SubagentResult();

    for (int i = startIndex + 1; i < entries.size(); ++i)
    {
      JsonNode entry = entries.get(i);
      JsonNode type = entry.get("type");
      if (type == null || !type.isString() || !type.asString().equals("user"))
        continue;

      JsonNode toolResult = entry.get("toolUseResult");
      if (toolResult == null)
        continue;

      JsonNode resultToolId = toolResult.get("tool_use_id");
      if (resultToolId == null || !resultToolId.isString())
        continue;

      if (resultToolId.asString().equals(toolUseId))
      {
        JsonNode tokens = toolResult.get("totalTokens");
        JsonNode duration = toolResult.get("durationMs");

        if (tokens != null && tokens.isNumber())
          result.tokens = tokens.asInt();
        if (duration != null && duration.isNumber())
          result.durationMs = duration.asInt();
        break;
      }
    }

    return result;
  }

  /**
   * Builds all table lines with exact formatting.
   *
   * @param data the subagent data
   * @return the list of table lines
   */
  private List<String> buildTableLines(SubagentData data)
  {
    List<String> lines = new ArrayList<>();

    // Top border
    String top = "\u256D" + "\u2500".repeat(COL_TYPE + 2) + "\u252C" +
                 "\u2500".repeat(COL_DESC + 2) + "\u252C" +
                 "\u2500".repeat(COL_TOKENS + 2) + "\u252C" +
                 "\u2500".repeat(COL_CONTEXT + 2) + "\u252C" +
                 "\u2500".repeat(COL_DURATION + 2) + "\u256E";
    lines.add(top);

    // Header row
    String header = "\u2502 " + padCell("Type", COL_TYPE) + " \u2502 " +
                    padCell("Description", COL_DESC) + " \u2502 " +
                    padCell("Tokens", COL_TOKENS) + " \u2502 " +
                    padCell("Context", COL_CONTEXT) + " \u2502 " +
                    padCell("Duration", COL_DURATION) + " \u2502";
    lines.add(header);

    // Header divider
    String divider = "\u251C" + "\u2500".repeat(COL_TYPE + 2) + "\u253C" +
                     "\u2500".repeat(COL_DESC + 2) + "\u253C" +
                     "\u2500".repeat(COL_TOKENS + 2) + "\u253C" +
                     "\u2500".repeat(COL_CONTEXT + 2) + "\u253C" +
                     "\u2500".repeat(COL_DURATION + 2) + "\u2524";
    lines.add(divider);

    // Data rows
    for (SubagentEntry subagent : data.subagents)
    {
      String typeVal = truncate(subagent.type, COL_TYPE);
      String descVal = truncate(subagent.description, COL_DESC);
      String tokensVal = formatTokens(subagent.tokens);
      String durationVal = formatDuration(subagent.durationMs);

      // Context with emoji indicator INSIDE
      ContextStatus status = contextStatus(subagent.tokens);
      String contextContent;
      if (status.level.equals("critical"))
        contextContent = status.text + "🚨";  // Police car light emoji
      else if (status.level.equals("warning"))
        contextContent = status.text + "⚠️";  // Warning emoji
      else
        contextContent = status.text;

      String row = "\u2502 " + padCell(typeVal, COL_TYPE) + " \u2502 " +
                   padCell(descVal, COL_DESC) + " \u2502 " +
                   padCell(tokensVal, COL_TOKENS) + " \u2502 " +
                   padCell(contextContent, COL_CONTEXT) + " \u2502 " +
                   padCell(durationVal, COL_DURATION) + " \u2502";
      lines.add(row);
    }

    // Footer divider
    lines.add(divider);

    // Total row
    String totalTokensStr = formatTokens(data.totalTokens);
    String totalDurationStr = formatDuration(data.totalDurationMs);
    String totalRow = "\u2502 " + padCell("", COL_TYPE) + " \u2502 " +
                      padCell("TOTAL", COL_DESC) + " \u2502 " +
                      padCell(totalTokensStr, COL_TOKENS) + " \u2502 " +
                      padCell("-", COL_CONTEXT) + " \u2502 " +
                      padCell(totalDurationStr, COL_DURATION) + " \u2502";
    lines.add(totalRow);

    // Bottom border
    String bottom = "\u2570" + "\u2500".repeat(COL_TYPE + 2) + "\u2534" +
                    "\u2500".repeat(COL_DESC + 2) + "\u2534" +
                    "\u2500".repeat(COL_TOKENS + 2) + "\u2534" +
                    "\u2500".repeat(COL_CONTEXT + 2) + "\u2534" +
                    "\u2500".repeat(COL_DURATION + 2) + "\u256F";
    lines.add(bottom);

    return lines;
  }

  /**
   * Formats token count (e.g., 68400 -> "68.4k", 1500000 -> "1.5M").
   *
   * @param n the token count
   * @return the formatted string
   */
  private String formatTokens(int n)
  {
    if (n >= 1_000_000)
      return String.format("%.1fM", n / 1_000_000.0);
    else if (n >= 1000)
      return String.format("%.1fk", n / 1000.0);
    else
      return String.valueOf(n);
  }

  /**
   * Formats duration (e.g., 67000ms -> "1m 7s").
   *
   * @param ms the duration in milliseconds
   * @return the formatted string
   */
  private String formatDuration(int ms)
  {
    int secs = ms / 1000;
    if (secs >= 60)
    {
      int mins = secs / 60;
      int remainingSecs = secs % 60;
      return mins + "m " + remainingSecs + "s";
    }
    else
      return secs + "s";
  }

  /**
   * Computes context status with percentage and warning level.
   *
   * @param tokens the token count
   * @return the context status
   */
  private ContextStatus contextStatus(int tokens)
  {
    int pct = (tokens * 100) / CONTEXT_LIMIT;
    ContextStatus status = new ContextStatus();
    if (pct >= 80)
    {
      status.text = pct + "% ";
      status.level = "critical";
    }
    else if (pct >= 40)
    {
      status.text = pct + "% ";
      status.level = "warning";
    }
    else
    {
      status.text = pct + "%";
      status.level = "ok";
    }
    return status;
  }

  /**
   * Pads content to exact display width.
   *
   * @param content the content to pad
   * @param width the target width
   * @return the padded string
   */
  private String padCell(String content, int width)
  {
    DisplayUtils display = scope.getDisplayUtils();
    int contentWidth = display.displayWidth(content);
    int padding = width - contentWidth;
    if (padding < 0)
      padding = 0;
    return content + " ".repeat(padding);
  }

  /**
   * Truncates text to max display width.
   *
   * @param text the text to truncate
   * @param maxWidth the maximum width
   * @return the truncated text
   */
  private String truncate(String text, int maxWidth)
  {
    DisplayUtils display = scope.getDisplayUtils();
    int textWidth = display.displayWidth(text);
    if (textWidth <= maxWidth)
      return text;

    // Truncate character by character
    StringBuilder result = new StringBuilder();
    int targetWidth = maxWidth - 3;  // Reserve space for "..."
    int width = 0;

    for (int i = 0; i < text.length(); ++i)
    {
      char c = text.charAt(i);
      int charWidth = 1;
      if (width + charWidth > targetWidth)
        break;
      result.append(c);
      width += charWidth;
    }

    return result.toString() + "...";
  }

  /**
   * Holds aggregated subagent data.
   */
  private static class SubagentData
  {
    List<SubagentEntry> subagents = new ArrayList<>();
    int totalTokens = 0;
    int totalDurationMs = 0;
  }

  /**
   * Holds data for a single subagent.
   */
  private static class SubagentEntry
  {
    String type = "";
    String description = "";
    int tokens = 0;
    int durationMs = 0;
  }

  /**
   * Holds result from finding tool_use result.
   */
  private static class SubagentResult
  {
    int tokens = 0;
    int durationMs = 0;
  }

  /**
   * Holds context status information.
   */
  private static class ContextStatus
  {
    String text = "";
    String level = "ok";
  }
}
