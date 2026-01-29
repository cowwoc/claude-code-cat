package io.github.cowwoc.cat.hooks.skills;

import io.github.cowwoc.cat.hooks.JvmScope;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements13.java.DefaultJavaValidators.requireThat;

/**
 * Output generator for /cat:cleanup skill.
 *
 * Generates box displays for survey results, cleanup plan, and verification.
 */
public final class GetCleanupOutput
{
  /**
   * The JVM scope for accessing shared services.
   */
  private final JvmScope scope;

  /**
   * Creates a GetCleanupOutput instance.
   *
   * @param scope the JVM scope for accessing shared services
   * @throws NullPointerException if scope is null
   */
  public GetCleanupOutput(JvmScope scope)
  {
    requireThat(scope, "scope").isNotNull();
    this.scope = scope;
  }

  /**
   * Represents a worktree entry for display.
   *
   * @param path the worktree path
   * @param branch the branch name
   * @param state the worktree state (may be empty)
   */
  public record Worktree(String path, String branch, String state)
  {
  }

  /**
   * Represents a lock entry for display.
   *
   * @param taskId the task ID
   * @param session the session ID
   * @param age the lock age in seconds
   */
  public record Lock(String taskId, String session, int age)
  {
  }

  /**
   * Represents a stale remote for display.
   *
   * @param branch the branch name
   * @param author the author name
   * @param relative the relative time description
   * @param staleness the staleness description (for plan display)
   */
  public record StaleRemote(String branch, String author, String relative, String staleness)
  {
  }

  /**
   * Represents a worktree to remove for plan display.
   *
   * @param path the worktree path
   * @param branch the branch name
   */
  public record WorktreeToRemove(String path, String branch)
  {
  }

  /**
   * Represents removed counts for verification display.
   *
   * @param locks the number of locks removed
   * @param worktrees the number of worktrees removed
   * @param branches the number of branches removed
   */
  public record RemovedCounts(int locks, int worktrees, int branches)
  {
  }

  /**
   * Generate output display for survey phase.
   *
   * @param worktrees the list of worktrees found
   * @param locks the list of locks found
   * @param branches the list of branch names found
   * @param staleRemotes the list of stale remotes found
   * @param contextFile the context file path (may be null)
   * @return the formatted survey display
   * @throws NullPointerException if any list parameter is null
   */
  public String getSurveyOutput(List<Worktree> worktrees, List<Lock> locks,
                                List<String> branches, List<StaleRemote> staleRemotes,
                                String contextFile)
  {
    requireThat(worktrees, "worktrees").isNotNull();
    requireThat(locks, "locks").isNotNull();
    requireThat(branches, "branches").isNotNull();
    requireThat(staleRemotes, "staleRemotes").isNotNull();

    DisplayUtils display = scope.getDisplayUtils();
    List<String> allInnerBoxes = new ArrayList<>();

    // Worktrees inner box
    List<String> wtItems = new ArrayList<>();
    for (Worktree wt : worktrees)
    {
      if (wt.state() != null && !wt.state().isEmpty())
        wtItems.add(wt.path() + ": " + wt.branch() + " [" + wt.state() + "]");
      else
        wtItems.add(wt.path() + ": " + wt.branch());
    }
    if (wtItems.isEmpty())
      wtItems.add("None found");
    allInnerBoxes.addAll(display.buildInnerBox("\uD83D\uDCC1 Worktrees", wtItems));
    allInnerBoxes.add("");

    // Locks inner box
    List<String> lockItems = new ArrayList<>();
    for (Lock lock : locks)
    {
      String session = lock.session();
      if (session != null && session.length() > 8)
        session = session.substring(0, 8);
      lockItems.add(lock.taskId() + ": session=" + session + ", age=" + lock.age() + "s");
    }
    if (lockItems.isEmpty())
      lockItems.add("None found");
    allInnerBoxes.addAll(display.buildInnerBox("\uD83D\uDD12 Task Locks", lockItems));
    allInnerBoxes.add("");

    // Branches inner box
    List<String> branchItems;
    if (branches.isEmpty())
      branchItems = List.of("None found");
    else
      branchItems = branches;
    allInnerBoxes.addAll(display.buildInnerBox("\uD83C\uDF3F CAT Branches", branchItems));
    allInnerBoxes.add("");

    // Stale remotes inner box
    List<String> remoteItems = new ArrayList<>();
    for (StaleRemote remote : staleRemotes)
    {
      remoteItems.add(remote.branch() + ": " + remote.author() + ", " + remote.relative());
    }
    if (remoteItems.isEmpty())
      remoteItems.add("None found");
    allInnerBoxes.addAll(display.buildInnerBox("\u23F3 Stale Remotes (1-7 days)", remoteItems));
    allInnerBoxes.add("");

    // Context file line
    if (contextFile != null && !contextFile.isEmpty())
      allInnerBoxes.add("\uD83D\uDCDD Context: " + contextFile);
    else
      allInnerBoxes.add("\uD83D\uDCDD Context: None");

    // Build outer box with header
    String header = "\uD83D\uDD0D Survey Results";
    String finalBox = display.buildHeaderBox(header, allInnerBoxes, List.of(), 50, "\u2500 ");

    // Summary counts
    String counts = "Found: " + worktrees.size() + " worktrees, " + locks.size() +
                    " locks, " + branches.size() + " branches, " + staleRemotes.size() + " stale remotes";

    return finalBox + "\n" +
           "\n" +
           counts;
  }

  /**
   * Generate output display for plan phase.
   *
   * @param locksToRemove the list of lock IDs to remove
   * @param worktreesToRemove the list of worktrees to remove
   * @param branchesToRemove the list of branch names to remove
   * @param staleRemotes the list of stale remotes (for reporting)
   * @return the formatted plan display
   * @throws NullPointerException if any parameter is null
   */
  public String getPlanOutput(List<String> locksToRemove, List<WorktreeToRemove> worktreesToRemove,
                              List<String> branchesToRemove, List<StaleRemote> staleRemotes)
  {
    requireThat(locksToRemove, "locksToRemove").isNotNull();
    requireThat(worktreesToRemove, "worktreesToRemove").isNotNull();
    requireThat(branchesToRemove, "branchesToRemove").isNotNull();
    requireThat(staleRemotes, "staleRemotes").isNotNull();

    DisplayUtils display = scope.getDisplayUtils();
    List<String> contentItems = new ArrayList<>();

    // Locks section
    contentItems.add("\uD83D\uDD12 Locks to Remove:");
    if (!locksToRemove.isEmpty())
    {
      for (String lock : locksToRemove)
        contentItems.add("   \u2022 " + lock);
    }
    else
    {
      contentItems.add("   (none)");
    }
    contentItems.add("");

    // Worktrees section
    contentItems.add("\uD83D\uDCC1 Worktrees to Remove:");
    if (!worktreesToRemove.isEmpty())
    {
      for (WorktreeToRemove wt : worktreesToRemove)
      {
        contentItems.add("   \u2022 " + wt.path() + " \u2192 " + wt.branch());
      }
    }
    else
    {
      contentItems.add("   (none)");
    }
    contentItems.add("");

    // Branches section
    contentItems.add("\uD83C\uDF3F Branches to Remove:");
    if (!branchesToRemove.isEmpty())
    {
      for (String branch : branchesToRemove)
        contentItems.add("   \u2022 " + branch);
    }
    else
    {
      contentItems.add("   (none)");
    }
    contentItems.add("");

    // Stale remotes section (report only)
    contentItems.add("\u23F3 Stale Remotes (report only):");
    if (!staleRemotes.isEmpty())
    {
      for (StaleRemote remote : staleRemotes)
      {
        contentItems.add("   \u2022 " + remote.branch() + ": " + remote.staleness());
      }
    }
    else
    {
      contentItems.add("   (none)");
    }

    // Build outer box with header
    String header = "\uD83E\uDDF9 Cleanup Plan";
    String finalBox = display.buildHeaderBox(header, contentItems, List.of(), 50, "\u2500 ");

    // Count summary
    int total = locksToRemove.size() + worktreesToRemove.size() + branchesToRemove.size();

    return finalBox + "\n" +
           "\n" +
           "Total items to remove: " + total + "\n" +
           "\n" +
           "Confirm cleanup? (yes/no)";
  }

  /**
   * Generate output display for verification phase.
   *
   * @param remainingWorktrees the list of remaining worktree paths
   * @param remainingBranches the list of remaining branch names
   * @param remainingLocks the list of remaining lock IDs
   * @param removedCounts the counts of items removed
   * @return the formatted verification display
   * @throws NullPointerException if any parameter is null
   */
  public String getVerifyOutput(List<String> remainingWorktrees, List<String> remainingBranches,
                                List<String> remainingLocks, RemovedCounts removedCounts)
  {
    requireThat(remainingWorktrees, "remainingWorktrees").isNotNull();
    requireThat(remainingBranches, "remainingBranches").isNotNull();
    requireThat(remainingLocks, "remainingLocks").isNotNull();
    requireThat(removedCounts, "removedCounts").isNotNull();

    DisplayUtils display = scope.getDisplayUtils();
    List<String> contentItems = new ArrayList<>();

    // Removed summary
    contentItems.add("Removed:");
    contentItems.add("   \u2022 " + removedCounts.locks() + " lock(s)");
    contentItems.add("   \u2022 " + removedCounts.worktrees() + " worktree(s)");
    contentItems.add("   \u2022 " + removedCounts.branches() + " branch(es)");
    contentItems.add("");

    // Remaining worktrees
    contentItems.add("\uD83D\uDCC1 Remaining Worktrees:");
    if (!remainingWorktrees.isEmpty())
    {
      for (String wt : remainingWorktrees)
        contentItems.add("   \u2022 " + wt);
    }
    else
    {
      contentItems.add("   (none)");
    }
    contentItems.add("");

    // Remaining branches
    contentItems.add("\uD83C\uDF3F Remaining CAT Branches:");
    if (!remainingBranches.isEmpty())
    {
      for (String branch : remainingBranches)
        contentItems.add("   \u2022 " + branch);
    }
    else
    {
      contentItems.add("   (none)");
    }
    contentItems.add("");

    // Remaining locks
    contentItems.add("\uD83D\uDD12 Remaining Locks:");
    if (!remainingLocks.isEmpty())
    {
      for (String lock : remainingLocks)
        contentItems.add("   \u2022 " + lock);
    }
    else
    {
      contentItems.add("   (none)");
    }

    // Build outer box with header
    String header = "\u2705 Cleanup Complete";
    return display.buildHeaderBox(header, contentItems, List.of(), 50, "\u2500 ");
  }

}
