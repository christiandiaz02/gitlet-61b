package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Christian Diaz
 */
public class Main implements Serializable {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  @param args args */
    public static void main(String... args) {
        Commands gitlet = new Commands();
        errorChecking(args);
        switch (args[0]) {
        case "init":
            gitlet.init();
            break;
        case "add":
            gitlet.add(args[1]);
            break;
        case "rm":
            gitlet.rm(args[1]);
            break;
        case "commit":
            gitlet.commit(args[1]);
            break;
        case "checkout":
            if (args.length == 2) {
                gitlet.checkoutBranch(args);
            } else if (args.length == 3) {
                gitlet.checkoutFile(args);
            } else if (args.length == 4) {
                gitlet.checkoutID(args);
            }
            break;
        case "log":
            gitlet.log();
            break;
        case "status":
            gitlet.status();
            break;
        case "global-log":
            gitlet.globalLog();
            break;
        case "find":
            gitlet.find(args[1]);
            break;
        case "branch":
            gitlet.branch(args[1]);
            break;
        case "rm-branch":
            gitlet.rmBranch(args[1]);
            break;
        case "reset":
            gitlet.reset(args[1]);
            break;
        case "merge":
            gitlet.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

    /** Helper for error checking.
     *
     * @param args args
     */
    public static void errorChecking(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (!new File(".gitlet").exists()
                && !Objects.equals(args[0], "init")) {
            System.out.println("Not an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
