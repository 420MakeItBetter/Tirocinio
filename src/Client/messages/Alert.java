package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * This is the implementation of the "alert" {@link Message}
 *
 * @author Matteo Franceschi
 * @see Message
 *
 *
 */
public class Alert extends Message {


    /**
     * the version of this alert
     */
    private int version;
    /**
     * indicates the time limit until this alert has to be relayed
     */
    private long relay_until;
    /**
     * indicates the time limit until this alert is valid
     */
    private long expiration;
    /**
     * unique id for this alert
     */
    private int id;
    /**
     * if an alert has id < cancel then that alert should be ignored
     */
    private int cancel;
    /**
     * if the id of an alert is inside this Set then that alert should be ignored
     */
    private int [] cancelSet;
    /**
     * the alert apply to version greater or equal to this version
     */
    private int minVer;
    /**
     * the alert apply to version less or equal to this version
     */
    private int maxVer;
    /**
     * if this set is not empty the alert apply only if your sub version is contained in this set
     */
    private String [] subVerSet;
    /**
     * the priority of this alert
     */
    private int priority;
    /**
     * a comment to this alert not to be displayed
     */
    private String comment;
    /**
     * the message to display for this alert
     */
    private String statusBar;
    /**
     * reserved
     */
    private String reserved;

    /**
     * the ECDSA signature for this alert
     */
    private byte [] signature;

    /**
     * setter for the version variable
     * @param version the version of this alert
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * setter for the relay parameter
     * @param relay_until the parameter as timestamp
     */
    public void setRelayUntil(long relay_until) {
        this.relay_until = relay_until;
    }

    /**
     * setter for the exipration parameter
     * @param expiration the parameter as timestamp
     */
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    /**
     * setter fot the id parameter
     * @param id the parameter
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * setter for the cancel parameter
     * @param cancel the parameter
     */
    public void setCancel(int cancel) {
        this.cancel = cancel;
    }

    /**
     * the setter for the cancelSet parameter
     * @param cancelSet the set as an int []
     */
    public void setCancelSet(int[] cancelSet) {
        this.cancelSet = cancelSet;
    }

    /**
     * the setter for the minVer parameter
     * @param minVer the parameter
     */
    public void setMinVer(int minVer) {
        this.minVer = minVer;
    }

    /**
     * the setter for the maxVer parameter
     * @param maxVer the parameter
     */
    public void setMaxVer(int maxVer) {
        this.maxVer = maxVer;
    }

    /**
     * the setter for the subVer parameter
     * @param subVerSet the parameter as a String []
     */
    public void setSubVerSet(String[] subVerSet) {
        subVerSet = subVerSet;
    }

    /**
     * the setter for the priority parameter
     * @param priority the parameter
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * the setter for the comment parameter
     * @param comment the parameter
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * the setter for the statusBar parameter
     * @param statusBar the parameter
     */
    public void setStatusBar(String statusBar) {
        this.statusBar = statusBar;
    }

    /**
     * the setter for the reserved parameter
     * @param reserved the parameter
     */
    public void setReserved(String reserved){
        this.reserved = reserved;
    }


    /**
     * the setter for the signature parameter
     * @param signature the parameter
     */
    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    /**
     * The command for this messagge
     * @return "alert"
     */
    @Override
    public String getCommand() {
        return "alert";
    }

    /**
     * Initialize this message reading from a {@link LittleEndianInputStream}
     * @param leis the InputStream
     * @throws IOException in case something went wrong
     */
    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        version = leis.readInt();
        relay_until = leis.readLong();
        expiration = leis.readLong();
        id = leis.readInt();
        cancel = leis.readInt();
        int length = (int) leis.readVariableSize();
        cancelSet = new int [length];
        for(int i = 0; i < length; i++)
            cancelSet[i] = leis.readInt();
        minVer = leis.readInt();
        maxVer = leis.readInt();
        length = (int) leis.readVariableSize();
        subVerSet = new String [length];
        for(int i = 0; i < length; i++)
            subVerSet[i] = leis.readString();
        priority = leis.readInt();
        comment = leis.readString();
        statusBar = leis.readString();
        reserved = leis.readString();
    }

    /**
     * Serialize accordingly to the Bitcoin protocol this message to the {@link LittleEndianOutputStream} passed as argument
     * @param leos the OutputStream
     * @throws IOException in case something went wrong
     */
    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeInt(version);
        leos.writeLong(relay_until);
        leos.writeLong(expiration);
        leos.writeInt(id);
        leos.writeInt(cancel);
        leos.writeVariableSize(cancelSet.length);
        for(int i : cancelSet)
            leos.writeInt(i);
        leos.writeInt(minVer);
        leos.writeInt(maxVer);
        leos.writeVariableSize(subVerSet.length);
        for(String s : subVerSet)
            leos.writeString(s);
        leos.writeInt(priority);
        leos.writeString(comment);
        leos.writeString(statusBar);
        leos.writeString(reserved);
    }
}
