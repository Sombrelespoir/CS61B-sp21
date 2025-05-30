package gitlet;

import java.io.File;
import static gitlet.Utils.join;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Zhang Yusen
 */
public class Main {

    public static final File CWD = new File(System.getProperty("user.dir"));

    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        Repository repo = new Repository();
        String firstArg = args[0];

        if (firstArg.equals("init")) {
            Repository.setupPersistence();
            return;
        }

        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        switch(firstArg) {
            case "add":
                validateNumArgs(args, 2);
                repo.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                repo.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                repo.rm(args[1]);
                break;
            case "status":
                repo.status();
                break;
            case "log":
                repo.log();
                break;
            case "global-log":
                repo.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                repo.find(args[1]);
                break;
            case "branch":
                validateNumArgs(args, 2);
                repo.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                repo.reBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                repo.reset(args[1]);
                break;
            case "checkout":
                handleCheckout(args, repo);
                break;
            case "merge":
                validateNumArgs(args, 2);
                repo.merge(args[1]);
                break;
            case "add-remote":
                validateNumArgs(args, 3);
                repo.addRemote(args[1], args[2]);
                break;
            case "re-remote":
                validateNumArgs(args, 2);
                repo.rmRemote(args[1]);
                break;
            case "push":
                validateNumArgs(args, 3);
                repo.push(args[1], args[2]);
                break;
            case "fetch":
                validateNumArgs(args, 3);
                repo.fetch(args[1], args[2]);
                break;

            default:
                System.out.println("No command with that name exists.");
        }
    }

    private static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    private static void handleCheckout(String[] args, Repository repo) {
        if (args.length == 3 && args[1].equals("--")) {
            // checkout -- [file name]
            repo.checkoutFile(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            // checkout [commit id] -- [file name]
            repo.checkoutFileFromCommit(args[1], args[3]);
        } else if (args.length == 2) {
            repo.checkoutBranch(args[1]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }
}
