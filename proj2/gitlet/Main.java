package gitlet;

import net.sf.saxon.trans.SymbolicName;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Zhang Yusen
 */
public class Main {

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
        switch(firstArg) {
            // Create a new Gitlet version-control system
            case "init":
                Repository.setupPersistence();
                break;
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
