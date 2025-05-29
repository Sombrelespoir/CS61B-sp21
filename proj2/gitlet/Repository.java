package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;

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
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        BRANCHES.mkdir();

        Commit initialCommit = new Commit("initial commit", null);
        String commitId = sha1(initialCommit);
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
        String blobId = sha1(file);

        String currentCommitId = readContentsAsString(HEAD);
        File currentCommitFile = join(COMMIT_DIR, currentCommitId);
        Commit currentCommit = readObject(currentCommitFile, Commit.class);

        //check if file is already in current commit
        if (currentCommit.getBlobs().containsKey(filePath) && currentCommit.getBlobs().get(filePath).equals(blobId)) {
            stagingArea.remove(filePath);
            removalArea.remove(filePath);
        } else {
            stagingArea.put(filePath, blobId);
            File blobFile = join(BLOB_DIR, blobId);
            writeContents(blobFile, contents);
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

        HashMap<String, String> newBlobs = new HashMap<>(currentCommit.getBlobs());

        for (String file : stagingArea.keySet()) {
            newBlobs.put(file, stagingArea.get(file));
        }

        for (String file : removalArea.keySet()) {
            newBlobs.remove(file);
        }

        newCommit.setBlobs(newBlobs);

        String newCommitId = sha1(newCommit);
        newCommit.setId(newCommitId);

        File newCommitFile = join(COMMIT_DIR, newCommitId);
        writeObject(newCommitFile, newCommit);

        writeObject(HEAD, newCommitId);

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
        String currentBranch = readContentsAsString(HEAD);
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
            Commit commit = readObject(commitFile, Commit.class);

            System.out.println("===");
            System.out.println("commit" + commitId);
            System.out.println("Date:" + commit.getTimeStamp());
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
        boolean tracked = currentCommit.getBlobs().containsKey(fileName);

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
            for (String commit : commits) {
                if (commit.startsWith(commitId)) {
                    commitId = commit;
                    break;
                }
            }
        }
        File commitFile = join(COMMIT_DIR, commitId);

        if (!commitFile.exists()) {
            System.out.println("No commit with that id exits.");
            return;
        }

        Commit commit = readObject(commitFile, Commit.class);

        if (!commit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String blobId = commit.getBlobs().get(fileName);
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

        List<String> workingDirFiles = plainFilenamesIn(CWD);
        for (String file : workingDirFiles) {
            if (!currentCommit.getBlobs().containsKey(file) && targetCommit.getBlobs().containsKey(file)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        for (String file : currentCommit.getBlobs().keySet()) {
            if (!targetCommit.getBlobs().containsKey(file)) {
                File fileToDelete = join(CWD, file);
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                }
            }
        }

        for (String file : targetCommit.getBlobs().keySet()) {
            String blobId = targetCommit.getBlobs().get(file);
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
            for (String commit : commits) {
                if (commit.startsWith(commitId)) {
                    commitId = commit;
                    break;
                }
            }
        }

        File commitFile = join(COMMIT_DIR, commitId);

        if (!commitFile.exists()) {
            System.out.println("No commit with that id exist.");
            return;
        }

        Commit targetCommit = readObject(commitFile, Commit.class);

        String currentCommitId = readContentsAsString(HEAD);
        File currentCommitFile = join(COMMIT_DIR, currentCommitId);
        Commit currentCommit = readObject(currentCommitFile, Commit.class);

        List<String> workingDirFiles = plainFilenamesIn(CWD);
        for (String file : workingDirFiles) {
            if (!currentCommit.getBlobs().containsKey(file) && targetCommit.getBlobs().containsKey(file)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        for (String file : currentCommit.getBlobs().keySet()) {
            if (!targetCommit.getBlobs().containsKey(file)) {
                File fileToDelete = join(CWD, file);
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                }
            }
        }

        for (String file : targetCommit.getBlobs().keySet()) {
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
            System.out.println("commit:" + commitId);
            System.out.println("Date:" + commit.getTimeStamp());
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

}
