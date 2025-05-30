package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.Utils.*;



/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @author Zhang Yusen
 */
public class Repository {

    public static final File CWD = new File(System.getProperty("user.dir"));

    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File STAGING_AREA = join(GITLET_DIR, "staging");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File BRANCHES = join(GITLET_DIR, "branches");
    public static final File CURRENT_BRANCH = join(GITLET_DIR, "current_branch");
    public static final File REMOVAL_AREA = join(GITLET_DIR, "removal");


    public static void setupPersistence() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already"
                    + " exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        BRANCHES.mkdir();

        Commit initialCommit = new Commit("initial commit", null);
        String commitId = sha1(serialize(initialCommit));
        initialCommit.setId(commitId);

        File commitFile = join(COMMIT_DIR, commitId);
        writeObject(commitFile, initialCommit);

        File masterBranch = join(BRANCHES, "master");
        writeContents(masterBranch, commitId);

        writeContents(HEAD, commitId);

        writeContents(CURRENT_BRANCH, "master");

        HashMap<String, String> stagingArea = new HashMap<>();
        writeObject(STAGING_AREA, stagingArea);

        HashMap<String, Boolean> removalArea = new HashMap<>();
        writeObject(REMOVAL_AREA, removalArea);
    }

    public void add(String filePath) {
        File file = join(CWD, filePath);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        HashMap<String, String> stagingArea = readObject(STAGING_AREA, HashMap.class);

        HashMap<String, Boolean> removalArea = readObject(REMOVAL_AREA, HashMap.class);

        byte[] contents = readContents(file);
        String blobId = sha1(contents);

        String currentCommitId = readContentsAsString(HEAD);
        File currentCommitFile = join(COMMIT_DIR, currentCommitId);
        Commit currentCommit = readObject(currentCommitFile, Commit.class);

        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        if (currentBlobs == null) {
            currentBlobs = new HashMap<>();
        }

        //check if file is already in current commit
        if (currentBlobs.containsKey(filePath) && currentBlobs.get(filePath).equals(blobId)) {
            stagingArea.remove(filePath);
            removalArea.remove(filePath);
        } else {
            stagingArea.put(filePath, blobId);
            File blobFile = join(BLOB_DIR, blobId);
            writeContents(blobFile, contents);
            removalArea.remove(filePath);
        }

        writeObject(STAGING_AREA, stagingArea);
        writeObject(REMOVAL_AREA, removalArea);
    }

    public void commit(String commitMessage) {
        if (commitMessage.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        HashMap<String, String> stagingArea = readObject(STAGING_AREA, HashMap.class);

        HashMap<String, Boolean> removalArea = readObject(REMOVAL_AREA, HashMap.class);

        if (stagingArea.isEmpty() && removalArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }


        String currentCommitId = readContentsAsString(HEAD);
        File currentCommitFile = join(COMMIT_DIR, currentCommitId);
        Commit currentCommit = readObject(currentCommitFile, Commit.class);

        Commit newCommit = new Commit(commitMessage, currentCommitId);

        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        if (currentBlobs == null) {
            currentBlobs = new HashMap<>();
        }

        HashMap<String, String> newBlobs = new HashMap<>(currentBlobs);

        for (String file : stagingArea.keySet()) {
            newBlobs.put(file, stagingArea.get(file));
        }

        for (String file : removalArea.keySet()) {
            newBlobs.remove(file);
        }

        newCommit.setBlobs(newBlobs);

        String newCommitId = sha1(serialize(newCommit));
        newCommit.setId(newCommitId);

        File newCommitFile = join(COMMIT_DIR, newCommitId);
        writeObject(newCommitFile, newCommit);

        writeContents(HEAD, newCommitId);

        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        File branchFile = join(BRANCHES, currentBranch);
        writeContents(branchFile, newCommitId);

        stagingArea.clear();
        writeObject(STAGING_AREA, stagingArea);

        removalArea.clear();
        writeObject(REMOVAL_AREA, removalArea);

    }

    public void status() {
        System.out.println("=== Branches ===");
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        List<String> branches = plainFilenamesIn(BRANCHES);
        for (String branch : branches) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }

        System.out.println("\n=== Staged Files ===");
        HashMap<String, String> stagingArea = readObject(STAGING_AREA, HashMap.class);
        for (String file : stagingArea.keySet()) {
            System.out.println(file);
        }

        System.out.println("\n=== Removed Files ===");
        HashMap<String, Boolean> removalArea = readObject(REMOVAL_AREA, HashMap.class);
        for (String file : removalArea.keySet()) {
            System.out.println(file);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        // Wait for implement

        System.out.println("\n=== Untracked Files ===");
        // Wait for implement

    }

    public void log() {
        String commitId = readContentsAsString(HEAD);
        while (commitId != null) {
            File commitFile = join(COMMIT_DIR, commitId);
            if (!commitFile.exists()) {
                break;
            }
            Commit commit = readObject(commitFile, Commit.class);

            System.out.println("===");
            System.out.println("commit " + commitId);
            System.out.println("Date: " + commit.getTimeStamp());
            System.out.println(commit.getMessage());
            System.out.println();

            commitId = commit.getParent();
        }
    }

    public void rm(String fileName) {
        File file = join(CWD, fileName);
        HashMap<String, String> stagingArea = readObject(STAGING_AREA, HashMap.class);
        HashMap<String, Boolean> removalArea = readObject(REMOVAL_AREA, HashMap.class);

        String currentCommitId = readContentsAsString(HEAD);
        File currentCommitFile = join(COMMIT_DIR, currentCommitId);
        Commit currentCommit = readObject(currentCommitFile, Commit.class);

        boolean staged = stagingArea.containsKey(fileName);

        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        if (currentBlobs == null) {
            currentBlobs = new HashMap<>();
        }

        boolean tracked = currentBlobs.containsKey(fileName);

        if (!staged && !tracked) {
            System.out.println("No reason to remove the file.");
            return;
        }

        if (staged) {
            stagingArea.remove(fileName);
        }

        if (tracked) {
            removalArea.put(fileName, true);
            if (file.exists()) {
                file.delete();
            }
        }
        writeObject(STAGING_AREA, stagingArea);
        writeObject(REMOVAL_AREA, removalArea);

    }

    public void checkoutFile(String fileName) {
        String commitId = readContentsAsString(HEAD);
        checkoutFileFromCommit(commitId, fileName);

    }

    public void checkoutFileFromCommit(String commitId, String fileName) {
        if (commitId.length() < 40) {
            List<String> commits = plainFilenamesIn(COMMIT_DIR);
            List<String> matchingCommits = new ArrayList<>();
            for (String commit : commits) {
                if (commit.startsWith(commitId)) {
                    matchingCommits.add(commit);
                }
            }
            if (matchingCommits.isEmpty()) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (matchingCommits.size() > 1) {
                System.out.println("Ambiguous commit id: multiple matches found.");
                return;
            }
            commitId = matchingCommits.get(0);
        }
        File commitFile = join(COMMIT_DIR, commitId);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit commit = readObject(commitFile, Commit.class);

        HashMap<String, String> blobs = commit.getBlobs();
        if (blobs == null) {
            blobs = new HashMap<>();
        }

        if (!blobs.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String blobId = blobs.get(fileName);
        File blobFile = join(BLOB_DIR, blobId);

        byte[] contents = readContents(blobFile);

        File targetFile = join(CWD, fileName);
        writeContents(targetFile, contents);
    }

    public void checkoutBranch(String branchName) {
        File branchFile = join(BRANCHES, branchName);

        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        }

        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        String commitId = readContentsAsString(branchFile);
        File commitFile = join(COMMIT_DIR, commitId);
        Commit targetCommit = readObject(commitFile, Commit.class);

        String currentCommitId = readContentsAsString(HEAD);
        File currentCommitFile = join(COMMIT_DIR, currentCommitId);
        Commit currentCommit = readObject(currentCommitFile, Commit.class);

        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        if (currentBlobs == null) {
            currentBlobs = new HashMap<>();
        }
        HashMap<String, String> targetBlobs = targetCommit.getBlobs();
        if (targetBlobs == null) {
            targetBlobs = new HashMap<>();
        }

        List<String> workingDirFiles = plainFilenamesIn(CWD);
        for (String file : workingDirFiles) {
            if (!currentBlobs.containsKey(file) && targetBlobs.containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }

        for (String file : currentBlobs.keySet()) {
            if (!targetBlobs.containsKey(file)) {
                File fileToDelete = join(CWD, file);
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                }
            }
        }

        for (String file : targetBlobs.keySet()) {
            String blobId = targetBlobs.get(file);
            File blobFile = join(BLOB_DIR, blobId);
            File targetFile = join(CWD, file);

            byte[] contents = readContents(blobFile);
            writeContents(targetFile, contents);
        }

        HashMap<String, String> stagingArea = new HashMap<>();
        writeObject(STAGING_AREA, stagingArea);

        HashMap<String, Boolean> removalArea = new HashMap<>();
        writeObject(REMOVAL_AREA, removalArea);

        writeContents(HEAD, commitId);
        writeContents(CURRENT_BRANCH, branchName);
    }

    public void branch(String branchName) {
        File branchFile = join(BRANCHES, branchName);

        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }

        String currentCommitId = readContentsAsString(HEAD);
        writeContents(branchFile, currentCommitId);
    }

    public void reBranch(String branchName) {
        File branchFile = join(BRANCHES, branchName);

        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        branchFile.delete();
    }

    public void reset(String commitId) {
        if (commitId.length() < 40) {
            List<String> commits = plainFilenamesIn(COMMIT_DIR);
            List<String> matchingCommits = new ArrayList<>();
            for (String commit : commits) {
                if (commit.startsWith(commitId)) {
                    matchingCommits.add(commit);
                }
            }
            if (matchingCommits.isEmpty()) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (matchingCommits.size() > 1) {
                System.out.println("Ambiguous commit id: multiple matches found.");
                return;
            }
            commitId = matchingCommits.get(0);
        }

        File commitFile = join(COMMIT_DIR, commitId);

        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit targetCommit = readObject(commitFile, Commit.class);

        String currentCommitId = readContentsAsString(HEAD);
        File currentCommitFile = join(COMMIT_DIR, currentCommitId);
        Commit currentCommit = readObject(currentCommitFile, Commit.class);

        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        if (currentBlobs == null) {
            currentBlobs = new HashMap<>();
        }
        HashMap<String, String> targetBlobs = targetCommit.getBlobs();
        if (targetBlobs == null) {
            targetBlobs = new HashMap<>();
        }

        List<String> workingDirFiles = plainFilenamesIn(CWD);
        for (String file : workingDirFiles) {
            if (!currentBlobs.containsKey(file) && targetBlobs.containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }

        for (String file : currentBlobs.keySet()) {
            if (!targetBlobs.containsKey(file)) {
                File fileToDelete = join(CWD, file);
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                }
            }
        }

        for (String file : targetBlobs.keySet()) {
            checkoutFileFromCommit(commitId, file);
        }

        HashMap<String, String> stagingArea = new HashMap<>();
        writeObject(STAGING_AREA, stagingArea);

        HashMap<String, Boolean> removalArea = new HashMap<>();
        writeObject(REMOVAL_AREA, removalArea);

        writeContents(HEAD, commitId);
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        File branchFile = join(BRANCHES, currentBranch);
        writeContents(branchFile, commitId);

    }

    public void globalLog() {
        List<String> commitIds = plainFilenamesIn(COMMIT_DIR);

        for (String commitId : commitIds) {
            File commitFile = join(COMMIT_DIR, commitId);
            Commit commit = readObject(commitFile, Commit.class);

            System.out.println("===");
            System.out.println("commit " + commitId);
            System.out.println("Date: " + commit.getTimeStamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public void find(String message) {
        List<String> commitIds = plainFilenamesIn(COMMIT_DIR);
        boolean found = false;

        for (String commitId : commitIds) {
            File commitFile = join(COMMIT_DIR, commitId);
            Commit commit = readObject(commitFile, Commit.class);

            if (commit.getMessage().equals(message)) {
                System.out.println(commitId);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void merge(String branchName) {
        if (!validateMergePrerequisites(branchName)) {
            return;
        }
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        String currentCommitId = readContentsAsString(HEAD);
        String branchCommitId = readContentsAsString(join(BRANCHES, branchName));

        String splitPointId = findSplitPoint(currentCommitId, branchCommitId);
        if (handleSpecialCases(splitPointId, branchCommitId, branchName)) {
            return;
        }

        Commit currentCommit = readObject(join(COMMIT_DIR, currentCommitId), Commit.class);
        Commit branchCommit = readObject(join(COMMIT_DIR, branchCommitId), Commit.class);
        Commit splitCommit = readObject(join(COMMIT_DIR, splitPointId), Commit.class);

        HashMap<String, String> currentBlobs = currentCommit.getBlobs() != null ?
                currentCommit.getBlobs() : new HashMap<>();
        HashMap<String, String> branchBlobs = branchCommit.getBlobs() != null ?
                branchCommit.getBlobs() : new HashMap<>();
        HashMap<String, String> splitBlobs = splitCommit.getBlobs() != null ?
                splitCommit.getBlobs() : new HashMap<>();

        if (hasUntrackedConflicts(currentBlobs, branchBlobs)) {
            return;
        }

        HashMap<String, String> stagingArea = readObject(STAGING_AREA, HashMap.class);

        boolean hasConflicts = processMergeChanges(currentBlobs, branchBlobs, splitBlobs,
                stagingArea, branchCommitId);

        createMergeCommit(currentCommitId, branchCommitId, currentBranch, branchName,
                currentBlobs, stagingArea);

        if (hasConflicts) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private String findSplitPoint(String commit1, String commit2) {
        HashSet<String> ancestors1 = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.add(commit1);
        while (!queue.isEmpty()) {
            String commitId = queue.remove();
            ancestors1.add(commitId);

            File commitFile = join(COMMIT_DIR, commitId);
            if (commitFile.exists()) {
                Commit commit = readObject(commitFile, Commit.class);
                String parent = commit.getParent();
                if (parent != null && !ancestors1.contains(parent)) {
                    queue.add(parent);
                }

                String secondParent = commit.getSecondParent();
                if (secondParent != null && !ancestors1.contains(secondParent)) {
                    queue.add(secondParent);
                }
            }
        }

        queue.clear();
        queue.add(commit2);
        HashSet<String> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            String commitId = queue.remove();

            if (ancestors1.contains(commitId)) {
                return commitId;
            }

            visited.add(commitId);

            File commitFile = join(COMMIT_DIR, commitId);
            if (commitFile.exists()) {
                Commit commit = readObject(commitFile, Commit.class);
                String parent = commit.getParent();
                if (parent != null && !visited.contains(parent)) {
                    queue.add(parent);
                }
                String secondParent = commit.getSecondParent();
                if (secondParent != null && !visited.contains(secondParent)) {
                    queue.add(secondParent);
                }
            }
        }
        return null;
    }

    private boolean validateMergePrerequisites(String branchName) {
        File branchFile = join(BRANCHES, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }

        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        if (currentBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return false;
        }

        HashMap<String, String> stagingArea = readObject(STAGING_AREA, HashMap.class);
        HashMap<String, Boolean> removalArea = readObject(REMOVAL_AREA, HashMap.class);
        if (!stagingArea.isEmpty() || !removalArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return false;
        }

        return true;
    }

    private boolean handleSpecialCases(String splitPointId, String branchCommitId, String branchName) {
        if (splitPointId.equals(branchCommitId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return true;
        }

        String currentCommitId = readContentsAsString(HEAD);
        if (splitPointId.equals(currentCommitId)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }

        return false;
    }

    private boolean hasUntrackedConflicts(HashMap<String, String> currentBlobs,
                                          HashMap<String, String> branchBlobs) {
        List<String> workingDirFiles = plainFilenamesIn(CWD);
        for (String file : workingDirFiles) {
            if (!currentBlobs.containsKey(file) && branchBlobs.containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                                    + "delete it, or add and commit it first.");
                return true;
            }
        }

        return false;
    }

    private boolean processMergeChanges(HashMap<String, String> currentBlobs,
                                        HashMap<String, String> branchBlobs,
                                        HashMap<String, String> splitBlobs,
                                        HashMap<String, String> stagingArea,
                                        String branchCommitId) {
        boolean hasConflicts = false;
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(currentBlobs.keySet());
        allFiles.addAll(branchBlobs.keySet());
        allFiles.addAll(splitBlobs.keySet());

        for (String file : allFiles) {
            String currentBlobId = currentBlobs.getOrDefault(file, null);
            String branchBlobId = branchBlobs.getOrDefault(file, null);
            String splitBlobId = splitBlobs.getOrDefault(file, null);

            if (mergeFileNeedsConflictResolution(currentBlobId, branchBlobId, splitBlobId)) {
                handleMergeConflict(file, currentBlobId, branchBlobId, stagingArea);
                hasConflicts = true;
            } else if (shouldTakeBranchVersion(currentBlobId, branchBlobId, splitBlobId)) {
                checkoutFileFromCommit(branchCommitId, file);
                stagingArea.put(file, branchBlobId);
            } else if (shouldRemoveFile(currentBlobId, branchBlobId, splitBlobId)) {
                rm(file);
            }
        }

        return hasConflicts;
    }

    private boolean mergeFileNeedsConflictResolution(String currentBlobId, String branchBlobId,
                                                     String splitBlobId) {
        if (currentBlobId != null && branchBlobId != null && !currentBlobId.equals(branchBlobId)
                && (splitBlobId == null || (!currentBlobId.equals(splitBlobId) && !branchBlobId.equals(splitBlobId)))) {
            return true;
        }

        return (currentBlobId == null && branchBlobId != null && splitBlobId != null &&
                !branchBlobId.equals(splitBlobId))
                || (currentBlobId != null && branchBlobId == null && splitBlobId != null &&
                        !currentBlobId.equals(splitBlobId));
    }

    private boolean shouldTakeBranchVersion(String currentBlobId, String branchBlobId, String splitBlobId) {

        if (currentBlobId != null && branchBlobId != null && splitBlobId != null
                && splitBlobId.equals(currentBlobId) && !splitBlobId.equals(branchBlobId)) {
            return true;
        }

        return (splitBlobId == null && currentBlobId == null && branchBlobId != null) ||
                (currentBlobId == null && branchBlobId != null && splitBlobId != null);
    }

    private boolean shouldRemoveFile(String currentBlobId, String branchBlobId, String splitBlobId) {
        return currentBlobId != null && branchBlobId == null && splitBlobId != null;
    }

    private void createMergeCommit(String currentCommitId, String branchCommitId,
                                   String currentBranch, String branchName,
                                   HashMap<String, String> currentBlobs,
                                   HashMap<String, String> stagingArea) {
        HashMap<String, Boolean> removalArea = readObject(REMOVAL_AREA, HashMap.class);

        Commit mergeCommit = new Commit("Merged " + branchName + " into " + currentBranch + ".",
                currentCommitId);
        mergeCommit.setSecondParent(branchCommitId);

        HashMap<String, String> newBlobs = new HashMap<>(currentBlobs);
        for (String file : stagingArea.keySet()) {
            newBlobs.put(file, stagingArea.get(file));
        }
        for (String file : removalArea.keySet()) {
            newBlobs.remove(file);
        }
        mergeCommit.setBlobs(newBlobs);

        String mergeCommitId = sha1(serialize(mergeCommit));
        mergeCommit.setId(mergeCommitId);
        File mergeCommitFile = join(COMMIT_DIR, mergeCommitId);
        writeObject(mergeCommitFile, mergeCommit);
        
        writeContents(HEAD, mergeCommitId);
        File currentBranchFile = join(BRANCHES, currentBranch);
        writeContents(currentBranchFile, mergeCommitId);

        stagingArea.clear();
        removalArea.clear();
        writeObject(STAGING_AREA, stagingArea);
        writeObject(REMOVAL_AREA, removalArea);
    }

    private void handleMergeConflict(String file, String currentBlobId, String branchBlobId, 
                               HashMap<String, String> stagingArea) {
        String currentContent = "";
        String branchContent = "";

        if (currentBlobId != null) {
            File currentBlobFile = join(BLOB_DIR, currentBlobId);
            currentContent = readContentsAsString(currentBlobFile);
        }
    
        if (branchBlobId != null) {
            File branchBlobFile = join(BLOB_DIR, branchBlobId);
            branchContent = readContentsAsString(branchBlobFile);
        }
    
        String conflictContent = "<<<<<<< HEAD\n" 
                            + currentContent 
                           + "=======\n" 
                           + branchContent 
                           + ">>>>>>>\n";
    
        File conflictFile = join(CWD, file);
        writeContents(conflictFile, conflictContent);
    
        byte[] contents = readContents(conflictFile);
        String blobId = sha1(contents);
        File blobFile = join(BLOB_DIR, blobId);
        writeContents(blobFile, contents);
        stagingArea.put(file, blobId);

    }
}