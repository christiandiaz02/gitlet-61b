# Gitlet Design Document

**Name**: Christian Diaz

# Main Class
-Creates a new Commands system "Gitlet", an instance of the Commands class
-Does input validation and throws respective errors
-If no errors are found, then the args are read and the respective command
is called on Gitlet

#Commit Class
-constructs and serves as a Commit object for Gitlet
-constructor initializes:
  -message passed in
  -timestamp
  -parent commit reference passed in
  -hashcode made by makeHash() method

There are getter methods for each of these attributes, for use in the Commands class

# Commands Class
- constructor creates new blankattributes which will serve as the primary data structures for the objects of Gitlet
- _stagingAdd and _stagingRemove are LinkedHashMaps for the staging area for addition and removal, respectively, which maps the file name to its SHA1 code
- _head commit, and new Commit instance
- _activeBranch, a string representing the same of the current/active branch
- _allBranches, a LinkedHashMap which maps a name of a branch to its head commit

  ## Init method
- initializes a new Gitlet repository, creating directories for:
  - commits
  - staging
  - branches
  - global-log list
  - blobs
  - .gitlet
- creates initial commit with appropiate features
- creates master branch, as _activeBranch
- points _head commit to the initial commit
- saves the current branch and its head to _activeBranches
- an empty HashMap called tracking is initialized, and serialized to the commit directory, named after the hashcode of the commit it belongs to + tracking.txt
- serializes every object used here

  ## Add method
- creates a file with the name passed in as argument
- gets its blob contents and the name and blob contents are added to _stagingAdd area, mapping to each othre
- serializes the contents to the blob directory
- serializes both stage HashMaps

  ##Commit method
- mappings of file names to contents are removed from _stagingAdd and transfered to the tracking HashMap of the current head
- mapping of file names to contents are removed from _stagingRemove and also removed from the tracking HashMap of the current commit if they are present
- The files are also deleted
- A new Commit is initialized with the current head being the parent reference and the newly modified tracking HashMap being what it is tracking
- stages are cleared
- the new head pointer goes to the newly created commit
- the _activeBranch and this new head pointer are added to _allBranches, overriding the old _head pointer
- all the objects are serialized

  ##rm Method
- file is removed from _stagingAdd if contained there
- if the tracking HashMap contains the file, then the contents of the file are overriden with the new contents, _stagingRemove is cleared and the file is deleted
- objects are serialized

  ##log Method
- the head commit is named as the currentCommit
- with a while loop and with the use of getter methods to Commit, the hashcode, time, and message of the currentCommit are printed out
- After this, the currentCommit is checked for if it contains a parent reference and if it is true, that reference become the currentCommit and goes through the loop
- We stop when the currentCommit no longer has a parent reference, meaning we are at the first commit and done

  ##global-log Method
- the global-log directory contains the hashcodes of all the commits ever made so it is deserialized for its list of hashcodes
- with a for loop going throught the list of names received, a holder instance of Commit creates a commit by using the hashcode were at to deserialize the respective commit from the commits directory
- its respective information is printed until loop ends

  ##find Method
- once again, global-log directory is deserialized to get the hashcodes of all commits made
- an empty arrayList is created, which will hold the hashcodes of any commits that match the hashcode passed in as argument
- a for loop is used to loop through the list of hashcodes in global-log, checking each time if the hashcode we are at matches the one passed in
  - if the hashcode we are at matches, the code is added to the arrayList
    - after the loop is over, we print out each entry in the arraylist

  ##checkoutBranch Method
- the args, which is a branch name, are passed into _allBranches.get() to get the head Commit of that branch name
- the head commit that is found is assigned to newPointer
- newTracking points to the tracking HashMap of newPointer, after deserializing it from commits directory using its hashcode
- the _activeBranch is assigned to the string passed in, the name of the new active branch
- _head points to the newPointer commit
- _activeBranch and _head are now added to _allCommits to override the old pointers
- objects are serialized

  ##status Method
- the staging areas, _activeBranch, and _allBranches are deserialized
- the keyset of _stagingAdd, which would be the file names, are added to a new List instance called addKeys
- this new List that now contains the names of the files in _stagingAdd is reversed so we can have lexicographical order
- _allBranches is looped through, and the branches are printed out, checking if one of the names matches the name in _activeBranch so an asterick can be added to its name before being printed
- the items of addKeys are printed out
- the items of the _stagingRemove are printed out
- 

  ##branch Method
- the args, which is the name of the new branch we want to make, is added to _allBranches, alongside the current head commit so this new branch points to the head commit at the time it was made

  ##rmBranch Method
- the args, which is the name of the branch to be deleted, is deleted from the _allBranches HashMap so its entry no longer exists in it

  ##reset Method
- a new Commit instance called resetCommit is created and it is assigned to the deserialized Commit taken from the commits dir, which was found using the ID passed in
- newTracking is the tracking HashMap for this resetCommit which is also deserialized from commits dir, using the ID given + "tracking.txt"
- resetCommit becomes the new head commit of the branch we are in
- the files of newTracking are given contents
- the _activeBranch and new head pointer are added to _allBranches, overriding the old pointer
- stages are cleared
- objects are serialzied