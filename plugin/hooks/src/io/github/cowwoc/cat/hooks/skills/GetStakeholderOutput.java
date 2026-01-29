package io.github.cowwoc.cat.hooks.skills;

import io.github.cowwoc.cat.hooks.JvmScope;

import java.util.List;

import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;

/**
 * Output generator for /cat:stakeholder-review skill.
 *
 * Provides box templates for stakeholder selection and review output.
 */
public final class GetStakeholderOutput
{
  /**
   * The JVM scope for accessing shared services.
   */
  private final JvmScope scope;

  /**
   * Creates a GetStakeholderOutput instance.
   *
   * @param scope the JVM scope for accessing shared services
   * @throws NullPointerException if scope is null
   */
  public GetStakeholderOutput(JvmScope scope)
  {
    requireThat(scope, "scope").isNotNull();
    this.scope = scope;
  }

  /**
   * Provide box templates for stakeholder review.
   *
   * @return the formatted template instructions
   */
  public String getOutput()
  {
    return "--- STAKEHOLDER_SELECTION ---\n" +
           buildSelectionBox() + "\n" +
           "\n" +
           "--- STAKEHOLDER_REVIEW ---\n" +
           buildReviewBox() + "\n" +
           "\n" +
           "--- CRITICAL_CONCERN ---\n" +
           buildCriticalConcernBox() + "\n" +
           "\n" +
           "--- HIGH_CONCERN ---\n" +
           buildHighConcernBox();
  }

  /**
   * Builds the stakeholder selection box template.
   *
   * @return the formatted selection box
   */
  private String buildSelectionBox()
  {
    List<String> content = List.of(
      "",
      "Stakeholder Review: {N} of 10 stakeholders selected",
      "",
      "Running: {running-list}",
      "",
      "Skipped:",
      "  - {stakeholder1}: {reason1}",
      "  - {stakeholder2}: {reason2}",
      ""
    );
    return scope.getDisplayUtils().buildHeaderBox("STAKEHOLDER SELECTION", content);
  }

  /**
   * Builds the stakeholder review box template.
   *
   * @return the formatted review box
   */
  private String buildReviewBox()
  {
    List<String> content = List.of(
      "",
      "Task: {task-name}",
      ""
    );
    int separator1 = content.size();

    List<String> reviewerLines = List.of(
      "Spawning reviewers...",
      "\u251C\u2500\u2500 {stakeholder1} {status1}",
      "\u251C\u2500\u2500 {stakeholder2} {status2}",
      "\u2514\u2500\u2500 {stakeholderN} {statusN}"
    );
    int separator2 = separator1 + reviewerLines.size();

    List<String> resultLines = List.of(
      "Result: {APPROVED|CONCERNS|REJECTED} ({summary})",
      ""
    );

    // Combine all content
    List<String> allContent = new java.util.ArrayList<>(content);
    allContent.addAll(reviewerLines);
    allContent.addAll(resultLines);

    return scope.getDisplayUtils().buildHeaderBox("STAKEHOLDER REVIEW", allContent, List.of(separator1, separator2));
  }

  /**
   * Builds the critical concern box template.
   *
   * @return the formatted critical concern box
   */
  private String buildCriticalConcernBox()
  {
    List<String> concerns = List.of(
      "[{Stakeholder}] {concern-description}",
      "\u2514\u2500 {file-location}",
      ""
    );
    return scope.getDisplayUtils().buildConcernBox("CRITICAL", concerns);
  }

  /**
   * Builds the high concern box template.
   *
   * @return the formatted high concern box
   */
  private String buildHighConcernBox()
  {
    List<String> concerns = List.of(
      "[{Stakeholder}] {concern-description}",
      "\u2514\u2500 {file-location}",
      ""
    );
    return scope.getDisplayUtils().buildConcernBox("HIGH", concerns);
  }

}
