package io.github.cowwoc.cat.hooks.skills;

import io.github.cowwoc.cat.hooks.Config;
import io.github.cowwoc.cat.hooks.JvmScope;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;

/**
 * Output generator for /cat:config skill.
 *
 * Reads configuration and provides all config box templates.
 */
public final class GetConfigOutput
{
  /**
   * The JVM scope for accessing shared services.
   */
  private final JvmScope scope;

  /**
   * Creates a GetConfigOutput instance.
   *
   * @param scope the JVM scope for accessing shared services
   * @throws NullPointerException if scope is null
   */
  public GetConfigOutput(JvmScope scope)
  {
    requireThat(scope, "scope").isNotNull();
    this.scope = scope;
  }

  /**
   * Read config and return result with all box templates.
   *
   * @param projectRoot the project root path
   * @return the formatted output, or null if config file not found
   * @throws NullPointerException if projectRoot is null
   * @throws IllegalArgumentException if projectRoot is blank
   */
  public String getOutput(Path projectRoot)
  {
    if (projectRoot == null)
      return null;

    Path configFile = projectRoot.resolve(".claude").resolve("cat").resolve("cat-config.json");
    if (!Files.exists(configFile))
      return null;

    // Load config using the Config class
    Config config = Config.load(projectRoot);

    // Build current settings box with actual values
    String currentSettings = buildCurrentSettingsBox(config);

    return "--- CURRENT_SETTINGS ---\n" +
           currentSettings + "\n" +
           "\n" +
           "--- VERSION_GATES_OVERVIEW ---\n" +
           buildVersionGatesOverview() + "\n" +
           "\n" +
           "--- GATES_FOR_VERSION ---\n" +
           buildGatesForVersion() + "\n" +
           "\n" +
           "--- GATES_UPDATED ---\n" +
           buildGatesUpdated() + "\n" +
           "\n" +
           "--- SETTING_UPDATED ---\n" +
           buildSettingUpdated() + "\n" +
           "\n" +
           "--- CONFIGURATION_SAVED ---\n" +
           buildConfigurationSaved() + "\n" +
           "\n" +
           "--- NO_CHANGES ---\n" +
           buildNoChanges();
  }

  /**
   * Builds the current settings box with actual config values.
   *
   * @param config the loaded configuration
   * @return the formatted settings box
   */
  private String buildCurrentSettingsBox(Config config)
  {
    String trust = config.getString("trust", "medium");
    String verify = config.getString("verify", "changed");
    String curiosity = config.getString("curiosity", "low");
    String patience = config.getString("patience", "high");
    boolean autoRemove = config.getBoolean("autoRemoveWorktrees", true);

    String cleanupDisplay;
    if (autoRemove)
      cleanupDisplay = "Auto-remove";
    else
      cleanupDisplay = "Keep";

    return buildSimpleHeaderBox(
      "⚙️",
      "CURRENT SETTINGS",
      List.of(
        "",
        "  🤝 Trust: " + trust,
        "  ✅ Verify: " + verify,
        "  🔍 Curiosity: " + curiosity,
        "  ⏳ Patience: " + patience,
        "  🧹 Cleanup: " + cleanupDisplay,
        ""
      )
    );
  }

  /**
   * Builds the version gates overview box.
   *
   * @return the formatted overview box
   */
  private String buildVersionGatesOverview()
  {
    return buildSimpleHeaderBox(
      "📊",
      "VERSION GATES",
      List.of(
        "",
        "Entry and exit gates control version dependencies.",
        "",
        "Select a version to configure its gates,",
        "or choose 'Apply defaults to all'."
      )
    );
  }

  /**
   * Builds the gates for version box template.
   *
   * @return the formatted gates box with placeholders
   */
  private String buildGatesForVersion()
  {
    return buildSimpleHeaderBox(
      "🚧",
      "GATES FOR {version}",
      List.of(
        "",
        "Entry: {entry-gate-description}",
        "Exit: {exit-gate-description}"
      )
    );
  }

  /**
   * Builds the gates updated confirmation box.
   *
   * @return the formatted confirmation box with placeholders
   */
  private String buildGatesUpdated()
  {
    return buildSimpleHeaderBox(
      "✅",
      "GATES UPDATED",
      List.of(
        "",
        "Version: {version}",
        "Entry: {new-entry-gate}",
        "Exit: {new-exit-gate}"
      )
    );
  }

  /**
   * Builds the setting updated confirmation box.
   *
   * @return the formatted confirmation box with placeholders
   */
  private String buildSettingUpdated()
  {
    return buildSimpleHeaderBox(
      "✅",
      "SETTING UPDATED",
      List.of(
        "",
        "{setting-name}: {old-value} → {new-value}"
      )
    );
  }

  /**
   * Builds the configuration saved confirmation box.
   *
   * @return the formatted confirmation box
   */
  private String buildConfigurationSaved()
  {
    return buildSimpleHeaderBox(
      "✅",
      "CONFIGURATION SAVED",
      List.of(
        "",
        "Changes committed to cat-config.json"
      )
    );
  }

  /**
   * Builds the no changes box.
   *
   * @return the formatted no changes box
   */
  private String buildNoChanges()
  {
    return buildSimpleHeaderBox(
      "ℹ️",
      "NO CHANGES",
      List.of(
        "",
        "Configuration unchanged."
      )
    );
  }

  /**
   * Builds a simple header box with icon prefix.
   *
   * @param icon the icon character(s)
   * @param title the title text
   * @param contentLines the content lines
   * @return the formatted box
   */
  private String buildSimpleHeaderBox(String icon, String title, List<String> contentLines)
  {
    DisplayUtils display = scope.getDisplayUtils();
    String header = icon + " " + title;
    int headerWidth = display.displayWidth(header);

    // Calculate max width from content
    int maxWidth = headerWidth;
    for (String line : contentLines)
    {
      int w = display.displayWidth(line);
      if (w > maxWidth)
        maxWidth = w;
    }

    StringBuilder sb = new StringBuilder();

    // Header top with embedded title
    String prefix = "\u2500\u2500\u2500 ";
    int suffixDashCount = maxWidth - prefix.length() - headerWidth + 2;
    if (suffixDashCount < 1)
      suffixDashCount = 1;
    sb.append("\u256D").append(prefix).append(header).append(" ")
      .append("\u2500".repeat(suffixDashCount)).append("\u256E\n");

    // Content lines
    for (String content : contentLines)
    {
      sb.append(display.buildLine(content, maxWidth)).append("\n");
    }

    // Bottom border
    sb.append(display.buildBorder(maxWidth, false));

    return sb.toString();
  }

}
