package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author ZhangYusen
 */
public class Commit implements Serializable {

    private String message;
    private String date;
    private String parent;
    private String id;
    private HashMap<String, String> blobs;
    private String secondParent;

    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.blobs = new HashMap<>();

        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

        if (this.parent == null) {
            Date epochDate = new Date(0L);
            this.date = formatter.format(epochDate);
        } else {
            Date now = new Date();
            this.date = formatter.format(now);
        }
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return date;
    }

    public String getParent() {
        return parent;
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public String getId() {
        return id;
    }

    public String getSecondParent() { return secondParent; }

    public void setId(String commitId) {
        this.id = commitId;
    }

    public void setBlobs(HashMap<String, String> blobs) {
        this.blobs = blobs;
    }

    public void setSecondParent(String secondParent) {
        this.secondParent = secondParent;
    }
}












