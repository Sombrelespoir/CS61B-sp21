package gitlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import static gitlet.Utils.*;





public class RepositoryTest {
    
    @TempDir
    Path tempDir;
    
    private File originalCWD;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        // Save original CWD and redirect stdout
        originalCWD = new File(System.getProperty("user.dir"));
        System.setProperty("user.dir", tempDir.toString());
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }
    
    @AfterEach
    void tearDown() {
        // Restore original CWD and stdout
        System.setProperty("user.dir", originalCWD.getAbsolutePath());
        System.setOut(originalOut);
    }
    
    @Test
    void testSetupPersistence() {
        Repository.setupPersistence();
        
        // Check that .gitlet directory and subdirectories are created
        assertTrue(Repository.GITLET_DIR.exists());
        assertTrue(Repository.COMMIT_DIR.exists());
        assertTrue(Repository.BLOB_DIR.exists());
        assertTrue(Repository.BRANCHES.exists());
        
        // Check that initial files are created
        assertTrue(Repository.HEAD.exists());
        assertTrue(Repository.CURRENT_BRANCH.exists());
        assertTrue(Repository.STAGING_AREA.exists());
        assertTrue(Repository.REMOVAL_AREA.exists());
        
        // Check initial commit exists
        String headCommitId = readContentsAsString(Repository.HEAD);
        assertNotNull(headCommitId);
        File initialCommitFile = join(Repository.COMMIT_DIR, headCommitId);
        assertTrue(initialCommitFile.exists());
        
        // Check master branch exists and points to initial commit
        File masterBranch = join(Repository.BRANCHES, "master");
        assertTrue(masterBranch.exists());
        assertEquals(headCommitId, readContentsAsString(masterBranch));
        
        // Check current branch is master
        assertEquals("master", readContentsAsString(Repository.CURRENT_BRANCH));
    }
    
    @Test
    void testSetupPersistenceAlreadyExists() {
        Repository.setupPersistence();
        Repository.setupPersistence(); // Second call should print error
        
        String output = outContent.toString();
        assertTrue(output.contains("A Gitlet version-control system already exists"));
    }
    
    @Test
    void testAddFile() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Create a test file
        File testFile = join(tempDir.toFile(), "test.txt");
        writeContents(testFile, "test content");
        
        repo.add("test.txt");
        
        // Check file is staged
        HashMap<String, String> stagingArea = readObject(Repository.STAGING_AREA, HashMap.class);
        assertTrue(stagingArea.containsKey("test.txt"));
        
        // Check blob is created
        String blobId = stagingArea.get("test.txt");
        File blobFile = join(Repository.BLOB_DIR, blobId);
        assertTrue(blobFile.exists());
        assertEquals("test content", readContentsAsString(blobFile));
    }
    
    @Test
    void testAddNonexistentFile() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.add("nonexistent.txt");
        
        String output = outContent.toString();
        assertTrue(output.contains("File does not exist."));
    }
    
    @Test
    void testCommit() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Add a file and commit
        File testFile = join(tempDir.toFile(), "test.txt");
        writeContents(testFile, "test content");
        repo.add("test.txt");
        
        String oldHeadId = readContentsAsString(Repository.HEAD);
        repo.commit("test commit");
        String newHeadId = readContentsAsString(Repository.HEAD);
        
        // Check new commit was created
        assertNotEquals(oldHeadId, newHeadId);
        
        // Check commit file exists and has correct message
        File commitFile = join(Repository.COMMIT_DIR, newHeadId);
        assertTrue(commitFile.exists());
        Commit commit = readObject(commitFile, Commit.class);
        assertEquals("test commit", commit.getMessage());
        assertEquals(oldHeadId, commit.getParent());
        
        // Check staging area is cleared
        HashMap<String, String> stagingArea = readObject(Repository.STAGING_AREA, HashMap.class);
        assertTrue(stagingArea.isEmpty());
    }
    
    @Test
    void testCommitEmptyMessage() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.commit("");
        
        String output = outContent.toString();
        assertTrue(output.contains("Please enter a commit message."));
    }
    
    @Test
    void testCommitNoChanges() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.commit("no changes");
        
        String output = outContent.toString();
        assertTrue(output.contains("No changes added to the commit."));
    }
    
    @Test
    void testCheckoutFile() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Add and commit a file
        File testFile = join(tempDir.toFile(), "test.txt");
        writeContents(testFile, "original content");
        repo.add("test.txt");
        repo.commit("initial commit with file");
        
        // Modify the file
        writeContents(testFile, "modified content");
        assertEquals("modified content", readContentsAsString(testFile));
        
        // Checkout the file - should restore original content
        repo.checkoutFile("test.txt");
        assertEquals("original content", readContentsAsString(testFile));
    }
    
    @Test
    void testCheckoutFileFromCommit() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Add and commit a file
        File testFile = join(tempDir.toFile(), "test.txt");
        writeContents(testFile, "version 1");
        repo.add("test.txt");
        repo.commit("commit 1");
        String commit1Id = readContentsAsString(Repository.HEAD);
        
        // Modify and commit again
        writeContents(testFile, "version 2");
        repo.add("test.txt");
        repo.commit("commit 2");
        
        // Checkout file from first commit
        repo.checkoutFileFromCommit(commit1Id, "test.txt");
        assertEquals("version 1", readContentsAsString(testFile));
    }
    
    @Test
    void testCheckoutFileNonexistentCommit() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.checkoutFileFromCommit("nonexistent", "test.txt");
        
        String output = outContent.toString();
        assertTrue(output.contains("No commit with that id exists."));
    }
    
    @Test
    void testCheckoutFileNotInCommit() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        String commitId = readContentsAsString(Repository.HEAD);
        repo.checkoutFileFromCommit(commitId, "nonexistent.txt");
        
        String output = outContent.toString();
        assertTrue(output.contains("File does not exist in that commit."));
    }
    
    @Test
    void testRemoveFile() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Add and commit a file
        File testFile = join(tempDir.toFile(), "test.txt");
        writeContents(testFile, "test content");
        repo.add("test.txt");
        repo.commit("add test file");
        
        // Remove the file
        repo.rm("test.txt");
        
        // Check file is marked for removal
        HashMap<String, Boolean> removalArea = readObject(Repository.REMOVAL_AREA, HashMap.class);
        assertTrue(removalArea.containsKey("test.txt"));
        
        // Check file is deleted from working directory
        assertFalse(testFile.exists());
    }
    
    @Test
    void testRemoveStagedFile() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Add a file (but don't commit)
        File testFile = join(tempDir.toFile(), "test.txt");
        writeContents(testFile, "test content");
        repo.add("test.txt");
        
        // Remove the file
        repo.rm("test.txt");
        
        // Check file is removed from staging area
        HashMap<String, String> stagingArea = readObject(Repository.STAGING_AREA, HashMap.class);
        assertFalse(stagingArea.containsKey("test.txt"));
    }
    
    @Test
    void testRemoveNoReason() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.rm("nonexistent.txt");
        
        String output = outContent.toString();
        assertTrue(output.contains("No reason to remove the file."));
    }
    
    @Test
    void testBranch() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.branch("new-branch");
        
        // Check branch file exists
        File branchFile = join(Repository.BRANCHES, "new-branch");
        assertTrue(branchFile.exists());
        
        // Check it points to current commit
        String currentCommitId = readContentsAsString(Repository.HEAD);
        assertEquals(currentCommitId, readContentsAsString(branchFile));
    }
    
    @Test
    void testBranchAlreadyExists() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.branch("master"); // master already exists
        
        String output = outContent.toString();
        assertTrue(output.contains("A branch with that name already exists."));
    }
    
    @Test
    void testCheckoutBranch() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Create and switch to new branch
        repo.branch("new-branch");
        repo.checkoutBranch("new-branch");
        
        // Check current branch changed
        assertEquals("new-branch", readContentsAsString(Repository.CURRENT_BRANCH));
    }
    
    @Test
    void testCheckoutNonexistentBranch() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.checkoutBranch("nonexistent");
        
        String output = outContent.toString();
        assertTrue(output.contains("No such branch exists."));
    }
    
    @Test
    void testCheckoutCurrentBranch() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.checkoutBranch("master");
        
        String output = outContent.toString();
        assertTrue(output.contains("No need to checkout the current branch."));
    }
    
    @Test
    void testStatus() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.status();
        
        String output = outContent.toString();
        assertTrue(output.contains("=== Branches ==="));
        assertTrue(output.contains("*master"));
        assertTrue(output.contains("=== Staged Files ==="));
        assertTrue(output.contains("=== Removed Files ==="));
    }
    
    @Test
    void testLog() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.log();
        
        String output = outContent.toString();
        assertTrue(output.contains("==="));
        assertTrue(output.contains("commit"));
        assertTrue(output.contains("initial commit"));
        assertTrue(output.contains("Date:"));
    }
    
    @Test
    void testGlobalLog() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.globalLog();
        
        String output = outContent.toString();
        assertTrue(output.contains("==="));
        assertTrue(output.contains("commit"));
        assertTrue(output.contains("initial commit"));
    }
    
    @Test
    void testFind() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.find("initial commit");
        
        String output = outContent.toString();
        assertFalse(output.contains("Found no commit with that message."));
    }
    
    @Test
    void testFindNoMatch() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.find("nonexistent message");
        
        String output = outContent.toString();
        assertTrue(output.contains("Found no commit with that message."));
    }
    
    @Test
    void testReset() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Add and commit a file
        File testFile = join(tempDir.toFile(), "test.txt");
        writeContents(testFile, "version 1");
        repo.add("test.txt");
        repo.commit("commit 1");
        String commit1Id = readContentsAsString(Repository.HEAD);
        
        // Add another commit
        writeContents(testFile, "version 2");
        repo.add("test.txt");
        repo.commit("commit 2");
        
        // Reset to first commit
        repo.reset(commit1Id);
        
        // Check HEAD points to first commit
        assertEquals(commit1Id, readContentsAsString(Repository.HEAD));
        
        // Check file content is restored
        assertEquals("version 1", readContentsAsString(testFile));
    }
    
    @Test
    void testResetNonexistentCommit() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        repo.reset("nonexistent");
        
        String output = outContent.toString();
        assertTrue(output.contains("No commit with that id exists."));
    }
    
    @Test
    void testAddFileAlreadyInCommit() {
        Repository.setupPersistence();
        Repository repo = new Repository();
        
        // Add and commit a file
        File testFile = join(tempDir.toFile(), "test.txt");
        writeContents(testFile, "content");
        repo.add("test.txt");
        repo.commit("add test file");
        
        // Add same file again (no changes)
        repo.add("test.txt");
        
        // Should not be in staging area
        HashMap<String, String> stagingArea = readObject(Repository.STAGING_AREA, HashMap.class);
        assertFalse(stagingArea.containsKey("test.txt"));
    }
}

