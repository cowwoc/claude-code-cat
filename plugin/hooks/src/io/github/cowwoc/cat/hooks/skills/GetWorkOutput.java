package io.github.cowwoc.cat.hooks.skills;

import io.github.cowwoc.cat.hooks.JvmScope;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;

/**
 * Output generator for /cat:work skill.
 *
 * Provides pre-rendered status boxes for the work skill.
 * Progress banners are handled by silent preprocessing via get-progress-banner.sh.
 */
public final class GetWorkOutput
{
  /**
   * The JVM scope for accessing shared services.
   */
  private final JvmScope scope;

  /**
   * Creates a GetWorkOutput instance.
   *
   * @param scope the JVM scope for accessing shared services
   * @throws NullPointerException if scope is null
   */
  public GetWorkOutput(JvmScope scope)
  {
    requireThat(scope, "scope").isNotNull();
    this.scope = scope;
  }

  /**
   * Provide pre-rendered status boxes for the work skill.
   *
   * Progress banners are now handled by silent preprocessing in SKILL.md
   * via get-progress-banner.sh. This generator only provides status boxes.
   *
   * @return the formatted box templates
   */
  public String getOutput()
  {
    return "--- NO_EXECUTABLE_TASKS ---\n" +
           buildNoExecutableTasks() + "\n" +
           "\n" +
           "--- TASK_NOT_FOUND ---\n" +
           buildTaskNotFound() + "\n" +
           "\n" +
           "--- FORK_IN_THE_ROAD ---\n" +
           buildForkInTheRoad() + "\n" +
           "\n" +
           "--- CHECKPOINT_TASK_COMPLETE ---\n" +
           buildCheckpointTaskComplete() + "\n" +
           "\n" +
           "--- CHECKPOINT_FEEDBACK_APPLIED ---\n" +
           buildCheckpointFeedbackApplied() + "\n" +
           "\n" +
           "--- TASK_COMPLETE_WITH_NEXT_TASK ---\n" +
           buildTaskCompleteWithNext() + "\n" +
           "\n" +
           "--- TASK_ALREADY_COMPLETE ---\n" +
           buildTaskAlreadyComplete() + "\n" +
           "\n" +
           "--- SCOPE_COMPLETE ---\n" +
           buildScopeComplete() + "\n" +
           "\n" +
           "--- TASK_COMPLETE_LOW_TRUST ---\n" +
           buildTaskCompleteLowTrust() + "\n" +
           "\n" +
           "--- VERSION_BOUNDARY_GATE ---\n" +
           buildVersionBoundaryGate();
  }

  /**
   * Builds the no executable tasks box.
   *
   * @return the formatted box
   */
  private String buildNoExecutableTasks()
  {
    DisplayUtils display = scope.getDisplayUtils();
    return display.buildSimpleBox(
      "\u2139\uFE0F",
      "No executable tasks",
      List.of(
        "",
        "Run /cat:status to see available tasks"
      )
    );
  }

  /**
   * Builds the task not found box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildTaskNotFound()
  {
    DisplayUtils display = scope.getDisplayUtils();
    return display.buildSimpleBox(
      "\u2754",
      "Task \"{task-name}\" not found",
      List.of(
        "",
        "Did you mean: {suggestion}?",
        "Run /cat:status to see all tasks"
      )
    );
  }

  /**
   * Builds the fork in the road box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildForkInTheRoad()
  {
    DisplayUtils display = scope.getDisplayUtils();
    List<String> contentLines = List.of(
      "   Task: {task-name}",
      "",
      "   Multiple viable paths - how would you prefer to proceed?",
      "",
      "   CHOOSE YOUR PATH",
      "   \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
      "",
      "   [A] {approach-name}",
      "       {description}",
      "       Risk: {level} | Scope: {N} files | Config alignment: {N}%",
      "",
      "   [B] {approach-name}",
      "       {description}",
      "       Risk: {level} | Scope: {N} files | Config alignment: {N}%",
      "",
      "   [C] {approach-name} (if exists)",
      "       ...",
      ""
    );

    // Calculate max width
    int maxWidth = 0;
    for (String line : contentLines)
    {
      int w = display.displayWidth(line);
      if (w > maxWidth)
        maxWidth = w;
    }

    List<String> lines = new ArrayList<>();
    lines.add("\uD83D\uDD00 FORK IN THE ROAD");
    lines.add("\u256D" + "\u2500".repeat(maxWidth + 2) + "\u256E");
    for (String content : contentLines)
    {
      lines.add(display.buildLine(content, maxWidth));
    }
    lines.add("\u2570" + "\u2500".repeat(maxWidth + 2) + "\u256F");
    return String.join("\n", lines);
  }

  /**
   * Builds the checkpoint task complete box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildCheckpointTaskComplete()
  {
    List<String> contentLines = List.of(
      "",
      "**Task:** {task-name}",
      ""
    );
    List<String> metricsLines = List.of(
      "**Time:** {N} minutes | **Tokens:** {N} ({percentage}% of context)"
    );
    List<String> branchLines = List.of(
      "**Branch:** {task-branch}",
      ""
    );

    return buildMultiSectionBox("\u2705 **CHECKPOINT: Task Complete**",
                                contentLines, metricsLines, branchLines);
  }

  /**
   * Builds the checkpoint feedback applied box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildCheckpointFeedbackApplied()
  {
    List<String> contentLines = List.of(
      "",
      "**Task:** {task-name}",
      "**Feedback iteration:** {N}",
      ""
    );
    List<String> metricsLines = List.of(
      "**Feedback subagent:** {N}K tokens",
      "**Total tokens (all iterations):** {total}K"
    );
    List<String> branchLines = List.of(
      "**Branch:** {task-branch}",
      ""
    );

    return buildMultiSectionBox("\u2705 **CHECKPOINT: Feedback Applied**",
                                contentLines, metricsLines, branchLines);
  }

  /**
   * Builds the task complete with next task box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildTaskCompleteWithNext()
  {
    String header = "\u2713 Task Complete";

    List<String> contentLines = List.of(
      "",
      "**{task-name}** merged to main.",
      ""
    );

    List<String> separatorContent = List.of(
      "**Next:** {next-task-name}",
      "{goal from PLAN.md}",
      "",
      "Auto-continuing in 3s...",
      "\u2022 Type \"stop\" to pause after this task",
      "\u2022 Type \"abort\" to cancel immediately"
    );

    List<String> footerContent = List.of("");

    return buildTaskBox(header, contentLines, separatorContent, footerContent);
  }

  /**
   * Builds the task already complete box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildTaskAlreadyComplete()
  {
    String header = "\u2713 Task Already Complete";

    List<String> contentLines = List.of(
      "",
      "**{task-name}** was already implemented.",
      "Commit: {commit-hash}",
      "",
      "STATE.md updated to reflect completion.",
      ""
    );

    List<String> separatorContent = List.of(
      "**Next:** {next-task-name}",
      "{goal from PLAN.md}",
      "",
      "Auto-continuing in 3s...",
      "\u2022 Type \"stop\" to pause after this task",
      "\u2022 Type \"abort\" to cancel immediately"
    );

    List<String> footerContent = List.of("");

    return buildTaskBox(header, contentLines, separatorContent, footerContent);
  }

  /**
   * Builds the scope complete box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildScopeComplete()
  {
    DisplayUtils display = scope.getDisplayUtils();
    String header = "\u2713 Scope Complete";

    List<String> contentLines = List.of(
      "",
      "**{scope description}** - all tasks complete!",
      "",
      "{For minor: \"v0.5 complete\"}",
      "{For major: \"v0.x complete\"}",
      "{For all: \"All versions complete!\"}",
      ""
    );

    int maxWidth = calculateMaxWidth(display, header, contentLines);

    List<String> lines = new ArrayList<>();
    lines.add(display.buildHeaderTop(header, maxWidth));
    for (String content : contentLines)
    {
      lines.add(display.buildLine(content, maxWidth));
    }
    lines.add(display.buildBorder(maxWidth, false));

    return String.join("\n", lines);
  }

  /**
   * Builds the task complete low trust box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildTaskCompleteLowTrust()
  {
    String header = "\u2713 Task Complete";

    List<String> contentLines = List.of(
      "",
      "**{task-name}** merged to main.",
      ""
    );

    List<String> separatorContent = List.of(
      "**Next Up:** {next-task-name}",
      "{goal from PLAN.md}",
      "",
      "`/clear` then `/cat:work` to continue"
    );

    List<String> footerContent = List.of("");

    return buildTaskBox(header, contentLines, separatorContent, footerContent);
  }

  /**
   * Builds the version boundary gate box template.
   *
   * @return the formatted box with placeholders
   */
  private String buildVersionBoundaryGate()
  {
    String header = "\u2713 Version Complete";

    List<String> contentLines = List.of(
      "",
      "**v{current-version}** is now complete!",
      ""
    );

    List<String> summaryContent = List.of(
      "**Summary:**",
      "\u2022 Tasks completed: {count}",
      "",
      "**Before continuing, consider:**",
      "\u2022 Publishing/releasing this version",
      "\u2022 Tagging the release in git",
      "\u2022 Updating documentation",
      ""
    );

    List<String> nextVersionContent = List.of(
      "**Next Version:** v{next-version}",
      "{next-task-name}",
      ""
    );

    return buildTaskBox(header, contentLines, summaryContent, nextVersionContent);
  }

  /**
   * Builds a multi-section box with three content sections.
   *
   * @param header the header text
   * @param content1 the first section content
   * @param content2 the second section content
   * @param content3 the third section content
   * @return the formatted box
   */
  private String buildMultiSectionBox(String header,
                                      List<String> content1,
                                      List<String> content2,
                                      List<String> content3)
  {
    DisplayUtils display = scope.getDisplayUtils();
    List<String> allContent = new ArrayList<>();
    allContent.addAll(content1);
    allContent.addAll(content2);
    allContent.addAll(content3);

    int headerWidth = display.displayWidth(header);
    int maxWidth = headerWidth;
    for (String line : allContent)
    {
      int w = display.displayWidth(line);
      if (w > maxWidth)
        maxWidth = w;
    }

    List<String> lines = new ArrayList<>();
    lines.add(header);
    lines.add("\u256D" + "\u2500".repeat(maxWidth + 2) + "\u256E");
    for (String content : content1)
    {
      lines.add(display.buildLine(content, maxWidth));
    }
    lines.add(display.buildSeparator(maxWidth));
    for (String content : content2)
    {
      lines.add(display.buildLine(content, maxWidth));
    }
    lines.add(display.buildSeparator(maxWidth));
    for (String content : content3)
    {
      lines.add(display.buildLine(content, maxWidth));
    }
    lines.add(display.buildBorder(maxWidth, false));

    return String.join("\n", lines);
  }

  /**
   * Builds a task box with separators between sections.
   *
   * @param header the header text
   * @param contentLines the main content
   * @param separatorContent the middle section after first separator
   * @param footerContent the footer section after second separator
   * @return the formatted box
   */
  private String buildTaskBox(String header,
                              List<String> contentLines,
                              List<String> separatorContent,
                              List<String> footerContent)
  {
    DisplayUtils display = scope.getDisplayUtils();
    List<String> allContent = new ArrayList<>();
    allContent.addAll(contentLines);
    allContent.addAll(separatorContent);
    allContent.addAll(footerContent);

    int maxWidth = calculateMaxWidth(display, header, allContent);

    List<String> lines = new ArrayList<>();
    lines.add(display.buildHeaderTop(header, maxWidth));
    for (String content : contentLines)
    {
      lines.add(display.buildLine(content, maxWidth));
    }
    lines.add(display.buildSeparator(maxWidth));
    for (String content : separatorContent)
    {
      lines.add(display.buildLine(content, maxWidth));
    }
    lines.add(display.buildSeparator(maxWidth));
    for (String content : footerContent)
    {
      lines.add(display.buildLine(content, maxWidth));
    }
    lines.add(display.buildBorder(maxWidth, false));

    return String.join("\n", lines);
  }

  /**
   * Calculates the maximum width needed for a box.
   *
   * @param display the display utilities
   * @param header the header text
   * @param contentLines the content lines
   * @return the maximum width
   */
  private int calculateMaxWidth(DisplayUtils display, String header, List<String> contentLines)
  {
    int headerWidth = display.displayWidth(header) + 5;  // Account for "--- header ---" format
    int maxWidth = headerWidth;
    for (String line : contentLines)
    {
      int w = display.displayWidth(line);
      if (w > maxWidth)
        maxWidth = w;
    }
    return maxWidth;
  }

}
