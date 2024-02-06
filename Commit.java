package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;

/** Commit class.
 * @author Christian Diaz **/
public class Commit implements Serializable {

    /** Constructor.
     * @param message message
     * @param parent parent
     * @param secondParent secondParent
     * @param blobs blobs
     * **/
    Commit(String message, Commit parent, Commit secondParent,
           LinkedHashMap<String, String> blobs) {
        _message = message;
        if (parent == null || Objects.equals(message, "initial commit")) {
            _timestamp = "Thu Jan 1 00:00:00 1970 -0800";
        } else {
            _timestamp = new SimpleDateFormat("EEE MMM dd"
                    + " HH:mm:ss yyyy Z").format(new Date());
        }
        _parent = parent;
        _secondParent = secondParent;
        _blobs = blobs;
        _hashcode = makeHash();
    }

    /** Makes a hash.
     * @return String **/
    public String makeHash() {
        byte [] hasher = Utils.serialize(this);
        return Utils.sha1(hasher);
    }

    /** Gets the time.
     * @return String **/
    public String getTimestamp() {
        String pattern = "EEE MMM d HH:mm:ss yyyy xxxx";
        SimpleDateFormat holder = new SimpleDateFormat(pattern);
        String date = holder.format(new Date());
        return date;
    }

    /** Gets the code.
     * @return String **/
    public String getHashcode() {
        return _hashcode;
    }

    /** Gets the message.
     * @return String **/
    public String getMsg() {
        return _message;
    }

    /** Gets the time.
     * @return String **/
    public String getTime() {
        return _timestamp;
    }

    /** Gets the parent.
     * @return String **/
    public Commit getParent() {
        return _parent;
    }

    /** Gets second parent.
     * @return String **/
    public Commit getSecondParent() {
        return _secondParent;
    }

    /** The log message for respective commit.
     * @return String **/
    private String _message;

    /** The parent reference of this commit. **/
    private Commit _parent;

    /** The second parent reference, in case of a merge situation. **/
    private Commit _secondParent;

    /** Filename to SHA-1. **/
    private LinkedHashMap<String, String> _blobs;

    /** SHA-1 hashcode for this commit. **/
    private String _hashcode;

    /** Gets the time. **/
    private String _timestamp;

}
