package io.github.cowwoc.cat.hooks.skills;

import io.github.cowwoc.cat.hooks.JvmScope;

import java.util.List;

import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;

/**
 * Output generator for /cat:init skill.
 *
 * Generates all 8 init boxes as templates using DisplayUtils.
 */
public final class GetInitOutput
{
  /**
   * The JVM scope for accessing shared services.
   */
  private final JvmScope scope;

  /**
   * Standard internal width for init boxes.
   * Total box width = 70 characters (68 internal + 2 borders).
   */
  private static final int BOX_WIDTH = 68;

  /**
   * Creates a GetInitOutput instance.
   *
   * @param scope the JVM scope for accessing shared services
   * @throws NullPointerException if scope is null
   */
  public GetInitOutput(JvmScope scope)
  {
    requireThat(scope, "scope").isNotNull();
    this.scope = scope;
  }

  /**
   * Generate all init box templates.
   *
   * @return the formatted output with all 8 boxes
   */
  public String getOutput()
  {
    String defaultGates = buildDefaultGatesConfigured();
    String researchSkipped = buildResearchSkipped();
    String choosePartner = buildChooseYourPartner();
    String catInitialized = buildCatInitialized();
    String firstTaskWalkthrough = buildFirstTaskWalkthrough();
    String firstTaskCreated = buildFirstTaskCreated();
    String allSet = buildAllSet();
    String explore = buildExploreAtYourOwnPace();

    return "=== BOX: default_gates_configured ===\n" +
           "Variables: {N} = version count\n" +
           defaultGates + "\n" +
           "\n" +
           "=== BOX: research_skipped ===\n" +
           "Variables: {version} = example version number (shown in help text)\n" +
           researchSkipped + "\n" +
           "\n" +
           "=== BOX: choose_your_partner ===\n" +
           "Variables: none (static)\n" +
           choosePartner + "\n" +
           "\n" +
           "=== BOX: cat_initialized ===\n" +
           "Variables: {trust}, {curiosity}, {patience} = user preference values\n" +
           catInitialized + "\n" +
           "\n" +
           "=== BOX: first_task_walkthrough ===\n" +
           "Variables: none (static)\n" +
           firstTaskWalkthrough + "\n" +
           "\n" +
           "=== BOX: first_task_created ===\n" +
           "Variables: {task-name} = sanitized task name from user input\n" +
           firstTaskCreated + "\n" +
           "\n" +
           "=== BOX: all_set ===\n" +
           "Variables: none (static)\n" +
           allSet + "\n" +
           "\n" +
           "=== BOX: explore_at_your_own_pace ===\n" +
           "Variables: none (static)\n" +
           explore;
  }

  /**
   * Build the default gates configured box.
   *
   * @return the formatted box
   */
  private String buildDefaultGatesConfigured()
  {
    return scope.getDisplayUtils().buildHeaderBox(
      "📊 Default gates configured for {N} versions",
      List.of(
        "                                                                  ",
        "  Entry gates: Work proceeds sequentially                         ",
        "  - Each minor waits for previous minor to complete               ",
        "  - Each major waits for previous major to complete               ",
        "                                                                  ",
        "  Exit gates: Standard completion criteria                        ",
        "  - Minor versions: all tasks must complete                       ",
        "  - Major versions: all minor versions must complete              ",
        "                                                                  ",
        "  To customize gates for any version:                             ",
        "  → /cat:config → 📊 Version Gates                                "
      ),
      BOX_WIDTH
    );
  }

  /**
   * Build the research skipped box.
   *
   * @return the formatted box
   */
  private String buildResearchSkipped()
  {
    return scope.getDisplayUtils().buildHeaderBox(
      "ℹ️ RESEARCH SKIPPED",
      List.of(
        "                                                                  ",
        "  Stakeholder research was skipped during import.                 ",
        "                                                                  ",
        "  To research a pending version later:                            ",
        "  → /cat:research {version}                                       ",
        "                                                                  ",
        "  Example: /cat:research 1.2                                      "
      ),
      BOX_WIDTH
    );
  }

  /**
   * Build the choose your partner box.
   *
   * @return the formatted box
   */
  private String buildChooseYourPartner()
  {
    return scope.getDisplayUtils().buildHeaderBox(
      "🎮 CHOOSE YOUR PARTNER",
      List.of(
        "                                                                  ",
        "  Every developer has a style. These questions shape how your     ",
        "  AI partner approaches the work ahead.                           ",
        "                                                                  ",
        "  Choose wisely - your preferences guide every decision.          "
      ),
      BOX_WIDTH
    );
  }

  /**
   * Build the CAT initialized box.
   *
   * @return the formatted box
   */
  private String buildCatInitialized()
  {
    return scope.getDisplayUtils().buildHeaderBox(
      "🚀 CAT INITIALIZED",
      List.of(
        "                                                                  ",
        "  🤝 Trust: {trust}                                               ",
        "  🔍 Curiosity: {curiosity}                                       ",
        "  ⏳ Patience: {patience}                                         ",
        "                                                                  ",
        "  Your partner is ready. Let's build something solid.             ",
        "  Adjust anytime: /cat:config                                     "
      ),
      BOX_WIDTH
    );
  }

  /**
   * Build the first task walkthrough box.
   *
   * @return the formatted box
   */
  private String buildFirstTaskWalkthrough()
  {
    return scope.getDisplayUtils().buildHeaderBox(
      "📋 FIRST TASK WALKTHROUGH",
      List.of(
        "                                                                  ",
        "  Great! Let's create your first task together.                   ",
        "  I'll ask a few questions to understand what you want to build.  "
      ),
      BOX_WIDTH
    );
  }

  /**
   * Build the first task created box.
   *
   * @return the formatted box
   */
  private String buildFirstTaskCreated()
  {
    return scope.getDisplayUtils().buildHeaderBox(
      "✅ FIRST TASK CREATED",
      List.of(
        "                                                                  ",
        "  Task: {task-name}                                               ",
        "  Location: .claude/cat/issues/v0/v0.0/{task-name}/                ",
        "                                                                  ",
        "  Files created:                                                  ",
        "  - PLAN.md - What needs to be done                               ",
        "  - STATE.md - Progress tracking                                  "
      ),
      BOX_WIDTH
    );
  }

  /**
   * Build the all set box.
   *
   * @return the formatted box
   */
  private String buildAllSet()
  {
    return scope.getDisplayUtils().buildHeaderBox(
      "👋 ALL SET",
      List.of(
        "                                                                  ",
        "  Your project is ready. When you want to start:                  ",
        "                                                                  ",
        "  → /cat:work         Execute your first task                     ",
        "  → /cat:status       See project overview                        ",
        "  → /cat:add          Add more tasks or versions                  ",
        "  → /cat:help         Full command reference                      "
      ),
      BOX_WIDTH
    );
  }

  /**
   * Build the explore at your own pace box.
   *
   * @return the formatted box
   */
  private String buildExploreAtYourOwnPace()
  {
    return scope.getDisplayUtils().buildHeaderBox(
      "👋 EXPLORE AT YOUR OWN PACE",
      List.of(
        "                                                                  ",
        "  Essential commands to get started:                              ",
        "                                                                  ",
        "  → /cat:status       See what's happening                        ",
        "  → /cat:add          Add versions and tasks                      ",
        "  → /cat:work         Execute tasks                               ",
        "  → /cat:help         Full command reference                      ",
        "                                                                  ",
        "  Tip: Run /cat:status anytime to see suggested next steps.       "
      ),
      BOX_WIDTH
    );
  }

}
