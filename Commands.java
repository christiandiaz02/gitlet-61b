package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Collections;


/** Commands class.
 * @author Christian Diaz
 * **/
@SuppressWarnings("unchecked")
public class Commands implements Serializable {
    /** Constructor. **/
    public Commands() {
        _stagingAdd = new LinkedHashMap<>();
        _stagingRemove = new LinkedHashMap<>();
        _head = null;
        _activeBranch = null;
        _allBranches = new LinkedHashMap<>();
        _inConflict = false;

    }
    /** Initialize a Gitlet repo. **/
    public void init() {
        File git = new File(".gitlet");
        if (git.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        } else {
            File commits = new File(".gitlet/commits");
            File staging = new File(".gitlet/staging");
            File blobs = new File(".gitlet/blobs");
            File branches = new File(".gitlet/branches");
            File global = new File(".gitlet/global-log");
            git.mkdirs();
            commits.mkdirs();
            staging.mkdirs();
            blobs.mkdirs();
            branches.mkdirs();
            global.mkdirs();
            Commit firstCommit = new Commit("initial commit", null, null,
                    new LinkedHashMap<>());
            _head = firstCommit;
            _activeBranch = "master";
            Utils.writeObject(new File(".gitlet/staging/stage.txt"),
                    _stagingAdd);
            Utils.writeObject(new File(".gitlet/staging/remove.txt"),
                    _stagingRemove);
            Utils.writeObject(new File(".gitlet/commits/"
                    + firstCommit.getHashcode() + ".txt"), firstCommit);
            Utils.writeObject(new File(".gitlet/global-log/"
                    + firstCommit.getHashcode()), firstCommit);
            Utils.writeObject(new File(".gitlet/commits/head.txt"), _head);
            _allBranches.put(_activeBranch, firstCommit);
            LinkedHashMap<String, String> tracking = new LinkedHashMap<>();
            Utils.writeObject(new File(".gitlet/branches/current.txt"),
                    _activeBranch);
            Utils.writeObject(new File(".gitlet/branches/allBranches.txt"),
                    _allBranches);
            Utils.writeObject(new File(".gitlet/commits/"
                    + firstCommit.getHashcode() + "tracking.txt"), tracking);
        }
    }

    /** Adds file to staging area.
     * @param fileName fileName **/
    public void add(String fileName) {
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        LinkedHashMap tracking = Utils.readObject(new File(".gi"
                + "tlet/" + "commits/" + _head.getHashcode() + "tracking.txt"),
                LinkedHashMap.class);
        _stagingAdd = Utils.readObject(new File(".gitlet/staging/stage.txt"),
                LinkedHashMap.class);
        _stagingRemove = Utils.readObject(new File
        (".gitlet/staging/remove.txt"), LinkedHashMap.class);
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            byte [] blobs = Utils.readContents(file);
            String contents = Utils.sha1(blobs);
            if (_stagingAdd.containsKey(fileName)) {
                Utils.writeObject(file, contents);
            }
            if (tracking != null) {
                if (tracking.containsKey(fileName)) {
                    if (Objects.equals(tracking.get(fileName), contents)) {
                        if (_stagingAdd.containsKey(fileName)) {
                            _stagingAdd.remove(fileName);
                        }
                        if (_stagingRemove.containsKey(fileName)) {
                            _stagingRemove.remove(fileName);
                        }
                        Utils.writeContents(new File(".gitlet/blobs/"
                                        + contents + ".txt"), blobs);
                        Utils.writeObject(new File(".gitlet/staging/stage.txt"),
                                _stagingAdd);
                        Utils.writeObject(new File(".gitlet/staging/"
                                        + "remove.txt"), _stagingRemove);
                        System.exit(0);
                    }
                }
            }
            Utils.writeContents(new File(".gitlet/blobs/" + contents
                    + ".txt"), blobs);
            _stagingAdd.put(fileName, contents);
            Utils.writeObject(new File(".gitlet/staging/stage.txt"),
                            _stagingAdd);
            Utils.writeObject(new File(".gitlet/staging/remove.txt"),
                            _stagingRemove);
        }
    }
    /** Creates commit.
     * @param message message**/
    public void commit(String message) {
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        _stagingAdd = Utils.readObject(new File(".gitlet/staging/stage.txt"),
                LinkedHashMap.class);
        _stagingRemove = Utils.readObject(new File(".gitlet/staging/"
                + "remove.txt"), LinkedHashMap.class);
        _allBranches = Utils.readObject(new File(".gitlet/branches/"
                        + "allBranches.txt"), LinkedHashMap.class);
        _activeBranch = Utils.readObject(new File(".gitlet/branches/"
                        + "current.txt"), String.class);
        if (_stagingAdd.isEmpty() && _stagingRemove.isEmpty()) {
            System.out.println("No changes added to the commit");
            System.exit(0);
        }
        if (Objects.equals(message, "")) {
            System.out.println("Please enter a commit message");
            System.exit(0);
        }
        LinkedHashMap<String, String> tracking = Utils.readObject(new File(".g"
            + "itlet/commits/" + _head.getHashcode() + "tracking.txt"),
                LinkedHashMap.class);
        for (String entry : _stagingAdd.keySet()) {
            tracking.put(entry, _stagingAdd.get(entry));
        }
        for (String entry: _stagingRemove.keySet()) {
            tracking.remove(entry);
            new File(entry).delete();
        }
        Commit thisCommit = new Commit(message, _head, null, tracking);
        _stagingAdd.clear();
        _stagingRemove.clear();
        _head = thisCommit;
        _allBranches.put(_activeBranch, _head);
        Utils.writeObject(new File(".gitlet/commits/head.txt"), _head);
        Utils.writeObject(new File(".gitlet/commits/" + thisCommit.getHashcode()
                        + ".txt"), thisCommit);
        Utils.writeObject(new File(".gitlet/global-log/"
                        + thisCommit.getHashcode()), thisCommit);
        Utils.writeObject(new File(".gitlet/commits/tracking.txt"), tracking);
        Utils.writeObject(new File(".gitlet/commits/" + thisCommit.getHashcode()
                        + "tracking.txt"), tracking);
        Utils.writeObject(new File(".gitlet/staging/remove.txt"),
                _stagingRemove);
        Utils.writeObject(new File(".gitlet/staging/stage.txt"),
                _stagingAdd);
        Utils.writeObject(new File(".gitlet/branches/current.txt"),
                _activeBranch);
        Utils.writeObject(new File(".gitlet/branches/allBranches.txt"),
                _allBranches);
    }

    /** Removes file.
     * @param filename filename**/
    public void rm(String filename) {
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        LinkedHashMap<String, String> tracking = Utils.readObject(new File(".gi"
                + "tlet/commits/" + _head.getHashcode() + "tracking.txt"),
                LinkedHashMap.class);
        _stagingAdd = Utils.readObject(new File(".gitlet/staging/stage.txt"),
                LinkedHashMap.class);
        _stagingRemove = Utils.readObject(new File(".gitlet/staging/"
                        + "remove.txt"), LinkedHashMap.class);
        if (!tracking.containsKey(filename)
                && !_stagingAdd.containsKey(filename)) {
            System.out.println("No reason to remove the file");
            System.exit(0);
        }
        if (_stagingAdd.containsKey(filename)) {
            _stagingAdd.remove(filename);
        }
        if (tracking.containsKey(filename)) {
            if (new File(filename).exists()) {
                File file = new File(filename);
                byte[] blobs = Utils.readContents(file);
                String contents = Utils.sha1(blobs);
                Utils.writeContents(new File(".gitlet/blobs/" + contents
                                + ".txt"), blobs);
                _stagingRemove.put(filename, contents);
                Utils.restrictedDelete(filename);
            } else {
                _stagingRemove.put(filename, null);
            }
        } else {
            File file = new File(filename);
            byte[] blobs = Utils.readContents(file);
            String contents = Utils.sha1(blobs);
            Utils.writeContents(new File(".gitlet/blobs" + contents + ".txt"),
                    blobs);
        }
        Utils.writeObject(new File(".gitlet/staging/remove.txt"),
                _stagingRemove);
        Utils.writeObject(new File(".gitlet/staging/stage.txt"),
                _stagingAdd);
    }

    /** Uses head (most recent commit) to display log. **/
    public void log() {
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        Commit currentCommit = _head;
        String firstParent = null;
        String secondParent = null;
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currentCommit.getHashcode());
            if (currentCommit.getSecondParent() != null) {
                firstParent = currentCommit.getParent().getHashcode();
                secondParent = currentCommit.getSecondParent().getHashcode();
                System.out.println("Merge: " + firstParent.substring(0, 7)
                        + " " + secondParent.substring(0, 7));
            }
            System.out.println("Date: " + currentCommit.getTime());
            System.out.println(currentCommit.getMsg());
            System.out.println();
            if (currentCommit.getParent() != null) {
                currentCommit = currentCommit.getParent();
            } else {
                currentCommit = null;
            }
        }
    }
    /** Log of every commit ever. **/
    public void globalLog() {
        List<String> names = Utils.plainFilenamesIn(".gitlet/global-log");
        String firstParent;
        String secondParent;
        for (Object entry : names) {
            Commit commit = Utils.readObject(new File(".gitlet/commits/"
                    + entry + ".txt"), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commit.getHashcode());
            if (commit.getSecondParent() != null) {
                firstParent = commit.getParent().getHashcode();
                secondParent = commit.getSecondParent().getHashcode();
                System.out.println("Merge: " + firstParent.substring(0, 7)
                        + " " + secondParent.substring(0, 7));
            }
            System.out.println("Date: " + commit.getTime());
            System.out.println(commit.getMsg());
            System.out.println();
        }
    }

    /** Finds commit with give message.
     * @param message message**/
    public void find(String message) {
        List<String> names = Utils.plainFilenamesIn(".gitlet/global-log");
        ArrayList<String> found = new ArrayList<String>();
        for (Object entry: names) {
            Commit commit = Utils.readObject(new File(".gitlet/commits/"
                    + entry + ".txt"), Commit.class);
            if (Objects.equals(commit.getMsg(), message)) {
                found.add(commit.getHashcode());
            }
        }
        if (found.size() == 0) {
            System.out.println("Found no commit with that message.");
        }
        for (String entry: found) {
            System.out.println(entry);
        }
    }

    /** Checks out an entire branch.
    @param args args **/
    public void checkoutBranch(String[] args) {
        _allBranches = Utils.readObject(new File(".gitlet/branches/"
                + "allBranches.txt"), LinkedHashMap.class);
        _activeBranch = Utils.readObject(new File(".gitlet/branches/"
                + "current.txt"), String.class);
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        _stagingAdd = Utils.readObject(new File(".gitlet/staging/stage.txt"),
                LinkedHashMap.class);
        _stagingRemove = Utils.readObject(new File(".gitlet/staging/"
                        + "remove.txt"), LinkedHashMap.class);
        LinkedHashMap<String, String> currTracking =
                Utils.readObject(new File(".gitlet/commits/"
                + _head.getHashcode() + "tracking.txt"), LinkedHashMap.class);
        if (!_allBranches.containsKey(args[1])) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (Objects.equals(_activeBranch, args[1])) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit newPointer = _allBranches.get(args[1]);
        LinkedHashMap<String, String> newTracking =
                Utils.readObject(new File(".gitlet/commits/"
                        + newPointer.getHashcode() + "tracking.txt"),
                        LinkedHashMap.class);
        for (String entry : newTracking.keySet()) {
            if (!currTracking.containsKey(entry) && new File(entry).exists()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add and commit it first.");
                System.exit(0);
            }
        }
        for (String entry : currTracking.keySet()) {
            if (!newTracking.containsKey(entry)) {
                new File(entry).delete();
            }
        }
        _activeBranch = args[1];
        for (String entry : newTracking.keySet()) {
            String name = newTracking.get(entry);
            byte[] bits = Utils.readContents(new File(".gitlet/blobs/"
                        + name + ".txt"));
            Utils.writeContents(new File(entry), bits);
        }
        _head = newPointer;
        _stagingRemove.clear();
        _stagingAdd.clear();
        _allBranches.put(_activeBranch, _head);
        Utils.writeObject(new File(".gitlet/commits/head.txt"), _head);
        Utils.writeObject(new File(".gitlet/branches/current.txt"),
                _activeBranch);
        Utils.writeObject(new File(".gitlet/branches/allBranches.txt"),
                _allBranches);
        Utils.writeObject(new File(".gitlet/staging/remove.txt"),
                _stagingRemove);
        Utils.writeObject(new File(".gitlet/staging/stage.txt"),
                _stagingAdd);
    }

    /** Checks out file.
     * @param args args**/
    public void checkoutFile(String[] args) {
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        LinkedHashMap<String, String> tracking =
                Utils.readObject(new File(".gitlet/commits/tracking.txt"),
                        LinkedHashMap.class);
        if (!tracking.containsKey(args[2])) {
            System.out.println("File does not exist in that commit.");
        }
        String name = tracking.get(args[2]);
        byte [] bits = Utils.readContents(new File(".gitlet/blobs/" + name
                + ".txt"));
        Utils.writeContents(new File(args[2]), bits);
    }

    /** Checks out file with ID.
     * @param fileID fileID **/
    public void checkoutID(String[] fileID) {
        String code = fileID[1];
        String name = fileID[3];
        if (!Objects.equals(fileID[2], "--")) {
            System.out.println("Incorrect operands.");
        }
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        Commit currentCommit = _head;
        LinkedHashSet<String> commitID = new LinkedHashSet<>();
        while (currentCommit != null) {
            commitID.add(currentCommit.getHashcode());
            if (currentCommit.getParent() != null) {
                currentCommit = currentCommit.getParent();
            } else {
                currentCommit = null;
            }
        }
        if (code.length() < 10) {
            for (String commit: commitID) {
                if (commit.charAt(0) == code.charAt(0)
                        && commit.charAt(1) == code.charAt(1)) {
                    code = commit;
                }
            }
        }
        if (!commitID.contains(code)) {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        if (commitID.contains(code)) {
            currentCommit = Utils.readObject(new File(".gitlet/commits/" + code
                    + ".txt"), Commit.class);
        }
        LinkedHashMap<String, String> tracking =
                Utils.readObject(new File(".gitlet/commits/"
                        + code + "tracking.txt"), LinkedHashMap.class);
        if (!tracking.containsKey(fileID[3])) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String file = tracking.get(fileID[3]);
        byte [] bits = Utils.readContents(new File(".gitlet/blobs/"
                + file + ".txt"));
        Utils.writeContents(new File(fileID[3]), bits);
    }

    /** Prints the status.**/
    public void status() {
        _stagingAdd = Utils.readObject(new File(".gitlet/staging/stage.txt"),
                LinkedHashMap.class);
        _stagingRemove = Utils.readObject(new File(".gitlet/staging/"
                        + "remove.txt"), LinkedHashMap.class);
        _activeBranch = Utils.readObject(new File(".gitlet/branches/"
                        + "current.txt"), String.class);
        _allBranches = Utils.readObject(new File(".gitlet/branches/"
                        + "allBranches.txt"), LinkedHashMap.class);
        List<String> addKeys = new ArrayList<String>(_stagingAdd.keySet());
        Collections.reverse(addKeys);
        System.out.println("=== Branches ===");
        for (String branch : _allBranches.keySet()) {
            if (Objects.equals(branch, _activeBranch)) {
                System.out.println("*" + _activeBranch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String stage: addKeys) {
            System.out.println(stage);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String remove : _stagingRemove.keySet()) {
            System.out.println(remove);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Creates a new branch pointer.
     * @param branchName branchName**/
    public void branch(String branchName) {
        _allBranches = Utils.readObject(new File(".gitlet/branches/"
            + "allBranches.txt"), LinkedHashMap.class);
        _activeBranch = Utils.readObject(new File(".gitlet/branches/"
            + "current.txt"), String.class);
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        if (_allBranches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        }
        _allBranches.put(branchName, _head);
        Utils.writeObject(new File(".gitlet/branches/allBranches.txt"),
                _allBranches);
    }

    /** Removes a branch pointer.
     * @param branchName branchName **/
    public void rmBranch(String branchName) {
        _allBranches = Utils.readObject(new File(".gitlet/branches/"
                        + "allBranches.txt"), LinkedHashMap.class);
        _activeBranch = Utils.readObject(new File(".gitlet/branches/"
                + "current.txt"), String.class);
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        if (!_allBranches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (Objects.equals(_activeBranch, branchName)) {
            System.out.println("Cannot remove the current branch.");
        }
        _allBranches.remove(branchName);
        Utils.writeObject(new File(".gitlet/branches/allBranches.txt"),
                _allBranches);
    }

    /** Resets the commit.
     * @param stringCode StrinCode **/
    public void reset(String stringCode) {
        _allBranches = Utils.readObject(new File(".gitlet/branches/"
                + "allBranches.txt"), LinkedHashMap.class);
        _activeBranch = Utils.readObject(new File(".gitlet/branches/"
                + "current.txt"), String.class);
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        _stagingAdd = Utils.readObject(new File(".gitlet/staging/stage.txt"),
                LinkedHashMap.class);
        _stagingRemove = Utils.readObject(new File(".gitlet/staging/"
                        + "remove.txt"), LinkedHashMap.class);
        LinkedHashMap<String, String> currTracking =
                Utils.readObject(new File(".gitlet/commits/"
                + _head.getHashcode() + "tracking.txt"), LinkedHashMap.class);
        if (!new File(".gitlet/commits/" + stringCode + ".txt").exists()) {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        Commit resetCommit = Utils.readObject(new File(".gitlet/commits/"
                + stringCode + ".txt"), Commit.class);
        LinkedHashMap<String, String> newTracking =
                Utils.readObject(new File(".gitlet/commits/"
                + stringCode + "tracking.txt"), LinkedHashMap.class);
        for (String entry: newTracking.keySet()) {
            if (!currTracking.containsKey(entry) && new File(entry).exists()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String entry : currTracking.keySet()) {
            if (!newTracking.containsKey(entry)) {
                new File(entry).delete();
            }
        }
        _head = resetCommit;
        for (String entry : newTracking.keySet()) {
            String name = newTracking.get(entry);
            byte[] bits = Utils.readContents(new File(".gitlet/blobs/"
                    + name + ".txt"));
            Utils.writeContents(new File(entry), bits);
        }
        _allBranches.put(_activeBranch, _head);
        _stagingRemove.clear();
        _stagingAdd.clear();
        Utils.writeObject(new File(".gitlet/commits/head.txt"), _head);
        Utils.writeObject(new File(".gitlet/branches/current.txt"),
                _activeBranch);
        Utils.writeObject(new File(".gitlet/branches/allBranches.txt"),
                _allBranches);
        Utils.writeObject(new File(".gitlet/staging/remove.txt"),
                _stagingRemove);
        Utils.writeObject(new File(".gitlet/staging/stage.txt"),
                _stagingAdd);
    }

    /** Merges branches.
     * @param branchName branchName **/
    public void merge(String branchName) {
        _allBranches = Utils.readObject(new File(".gitlet/branches/"
                        + "allBranches.txt"), LinkedHashMap.class);
        _activeBranch = Utils.readObject(new File(".gitlet/branches/"
                        + "current.txt"), String.class);
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        _stagingAdd = Utils.readObject(new File(".gitlet/staging/stage.txt"),
                LinkedHashMap.class);
        _stagingRemove = Utils.readObject(new File(".gitlet/staging/"
                        + "remove.txt"), LinkedHashMap.class);
        checkForMergeErrors(branchName);
        String splitPoint = getSplitPoint(branchName);
        Commit givenBranch = _allBranches.get(branchName);
        LinkedHashMap<String, String> splitPointTracking =
                deserializeTrackings(splitPoint);
        LinkedHashMap<String, String> currTracking =
                deserializeTrackings(_head.getHashcode());
        LinkedHashMap<String, String> givenTracking =
                deserializeTrackings(givenBranch.getHashcode());
        for (String entry : givenTracking.keySet()) {
            if (!currTracking.containsKey(entry) && new File(entry).exists()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        LinkedHashMap<String, String> mergedTracking =
                getMergesTrack(givenTracking, currTracking,
                        splitPointTracking);
        for (String entry: _stagingRemove.keySet()) {
            _stagingRemove.remove(entry);
            new File(entry).delete();
        }
        Commit mergeCom = new Commit("Merged " + branchName + " into "
                + _activeBranch + ".", _head, givenBranch, mergedTracking);
        if (_inConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        _head = mergeCom;
        Utils.writeObject(new File(".gitlet/commits/head.txt"), _head);
        Utils.writeObject(new File(".gitlet/commits/" + mergeCom.getHashcode()
                + ".txt"), mergeCom);
        Utils.writeObject(new File(".gitlet/global-log/"
                        + mergeCom.getHashcode()), mergeCom);
        Utils.writeObject(new File(".gitlet/commits/tracking.txt"),
                mergedTracking);
        Utils.writeObject(new File(".gitlet/commits/" + mergeCom.getHashcode()
                + "tracking.txt"), mergedTracking);
        Utils.writeObject(new File(".gitlet/staging/remove.txt"),
                _stagingRemove);
        Utils.writeObject(new File(".gitlet/staging/stage.txt"),
                _stagingAdd);
        Utils.writeObject(new File(".gitlet/branches/current.txt"),
                _activeBranch);
        Utils.writeObject(new File(".gitlet/branches/allBranches.txt"),
                _allBranches);
    }

    /** Gets tracking info.
     *
     * @param hashCode hashCode
     * @return LinkedHashMap
     */
    public LinkedHashMap<String, String> deserializeTrackings(String hashCode) {
        return Utils.readObject(new File(".gitlet/commits/"
                        + hashCode + "tracking.txt"),
                LinkedHashMap.class);
    }

    /** Checks for error.
     * @param branchName branchName
     */
    public void checkForMergeErrors(String branchName) {
        if (!_stagingAdd.isEmpty() || !_stagingRemove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!_allBranches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (Objects.equals(_activeBranch, branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /** Gets tracking for merge.
     * @param givenTracking givenTracking
     * @param currTracking currTracking
     * @param splitPointTracking splitPointTracking
     * @return LinkedHashMap **/
    public LinkedHashMap getMergesTrack(LinkedHashMap<String, String>
                                                givenTracking,
                                        LinkedHashMap<String, String>
                                                currTracking,
                                        LinkedHashMap<String, String>
                                                splitPointTracking) {
        LinkedHashMap<String, String> mergedTracking = new LinkedHashMap<>();
        LinkedHashMap<String, String> allFiles = new LinkedHashMap<>();
        for (String entry: splitPointTracking.keySet()) {
            allFiles.put(entry, splitPointTracking.get(entry));
        }
        for (String entry: currTracking.keySet()) {
            if (!allFiles.containsKey(entry)) {
                allFiles.put(entry, currTracking.get(entry));
            }
        }
        for (String entry: givenTracking.keySet()) {
            if (!allFiles.containsKey(entry)) {
                allFiles.put(entry, givenTracking.get(entry));
            }
        }
        for (String entry: allFiles.keySet()) {
            String contentsAtSplit = null;
            String contentsAtGiven = null;
            String contentsAtCurr = null;
            String contents = null;
            if (splitPointTracking.containsKey(entry)) {
                contentsAtSplit = splitPointTracking.get(entry);
            }
            if (givenTracking.containsKey(entry)) {
                contentsAtGiven = givenTracking.get(entry);
            }
            if (currTracking.containsKey(entry)) {
                contentsAtCurr = currTracking.get(entry);
            }
            contents = whatToAddtoMerge(entry, contentsAtSplit, contentsAtGiven,
                    contentsAtCurr);
            if (contents != null) {
                mergedTracking.put(entry, contents);
            }
        }
        return mergedTracking;
    }

    /** Merge helper that chooses what to add.
     * @param name name
     * @param split split
     * @param given given
     * @param curr curr
     * @return String **/
    public String whatToAddtoMerge(String name, String split, String given,
                                   String curr) {
        if (Objects.equals(split, curr) && !Objects.equals(split, given)
                && split != null && given != null) {
            _stagingAdd.put(name, given);
            return given;
        } else if (!Objects.equals(split, curr) && Objects.equals(split, given)
                && split != null && curr != null) {
            return curr;
        } else if (!Objects.equals(split, curr) && !Objects.equals(split, given)
                && Objects.equals(curr, given) && split != null) {
            return given;
        } else if (split == null && given == null && curr != null) {
            return curr;
        } else if (split == null && given != null && curr == null) {
            String file = given;
            byte [] bits = Utils.readContents(new File(".gitlet/blobs/" + file
                    + ".txt"));
            Utils.writeContents(new File(name), bits);
            return given;
        } else if (Objects.equals(split, curr) && given == null) {
            _stagingRemove.put(name, split);
            return null;
        } else if (Objects.equals(split, given) && curr == null) {
            return null;
        } else if (!Objects.equals(split, given) && !Objects.equals(split, curr)
                && !Objects.equals(given, curr)) {
            _inConflict = true;
            String currConts = "";
            String givenConts = "";
            if (curr != null) {
                currConts = Utils.readContentsAsString(new File(".gitlet/blobs/"
                        + curr + ".txt"));
            }
            if (given != null) {
                givenConts = Utils.readContentsAsString(new File(".gitlet/"
                        + "blobs/" + given + ".txt"));
            }
            String conf =  "<<<<<<< HEAD" + "\n" + currConts + "=======" + "\n"
                    + givenConts
                    + ">>>>>>>" + "\n";
            Utils.writeContents(new File(name), conf);
        }
        return null;
    }


    /** Finds split point.
     *
     * @param branchName branchName
     * @return splitPoint split
     */
    public String getSplitPoint(String branchName) {
        _allBranches = Utils.readObject(new File(".gitlet/branches/"
                        + "allBranches.txt"), LinkedHashMap.class);
        _activeBranch = Utils.readObject(new File(".gitlet/branches/"
                        + "current.txt"), String.class);
        _head = Utils.readObject(new File(".gitlet/commits/head.txt"),
                Commit.class);
        _stagingAdd = Utils.readObject(new File(".gitlet/staging/stage.txt"),
                LinkedHashMap.class);
        _stagingRemove = Utils.readObject(new File(".gitlet/staging/"
                        + "remove.txt"),
                LinkedHashMap.class);
        Commit givenBranch = _allBranches.get(branchName);
        String splitPoint = null;
        LinkedHashSet<String> givenBranchTree = new LinkedHashSet<String>();
        LinkedHashSet<String> currBranchTree = new LinkedHashSet<String>();
        givenBranchTree.add(givenBranch.getHashcode());
        currBranchTree.add(_head.getHashcode());
        Commit copyGiven = givenBranch;
        Commit copyCurr = _head;
        while (copyGiven.getParent() != null) {
            givenBranchTree.add(copyGiven.getParent().getHashcode());
            if (copyGiven.getSecondParent() != null) {
                givenBranchTree.add(copyGiven.getSecondParent().getHashcode());
            }
            copyGiven = copyGiven.getParent();
        }
        while (copyCurr.getParent() != null) {
            currBranchTree.add(copyCurr.getParent().getHashcode());
            if (copyCurr.getSecondParent() != null) {
                currBranchTree.add(copyCurr.getSecondParent().getHashcode());
            }
            copyCurr = copyCurr.getParent();
        }
        for (String entry: currBranchTree) {
            if (givenBranchTree.contains(entry)) {
                splitPoint = entry;
                break;
            }
        }
        if (Objects.equals(givenBranch.getHashcode(), splitPoint)) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
            System.exit(0);
        }
        if (Objects.equals(splitPoint, _head.getHashcode())) {
            System.out.println("Current branch fast-forwarded.");
        }
        return splitPoint;
    }

    /** Staging area, which maps name of a file to its respective
     * string SHA-1 code of contents. */
    private static LinkedHashMap<String, String> _stagingAdd;

    /** Stage for removed files. **/
    private static LinkedHashMap<String, String> _stagingRemove;

    /** Pointer to head commit, which is current commit. */
    private static Commit _head;

    /** Name of the active branch. **/
    private static String _activeBranch;

    /** Branch name to Commit. */
    private static LinkedHashMap<String, Commit> _allBranches;

    /** Checks if there is a conflict. **/
    private boolean _inConflict;
}
