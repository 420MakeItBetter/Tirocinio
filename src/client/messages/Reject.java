package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 */
public class Reject extends Message {

    private String message;
    private byte code;
    private String reason;
    private byte [] data;

    @Override
    public String getCommand() {
        return "reject";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(CodeTypes code) {
        switch (code)
        {
            case REJECT_MALFORMED:
                this.code = 0x01;
                break;
            case REJECT_INVALID:
                this.code = 0x10;
                break;
            case REJECT_OBSOLETE:
                this.code = 0x11;
                break;
            case REJECT_DUPLICATE:
                this.code = 0x12;
                break;
            case REJECT_NONSTANDARD:
                this.code = 0x40;
                break;
            case REJECT_DUST:
                this.code = 0x41;
                break;
            case REJECT_INSUFFICIENTFEE:
                this.code = 0x42;
                break;
            case REJECT_CHECKPOINT:
                this.code = 0x43;
                break;
        }
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        int message_length = (int) leis.readVariableSize();
        byte[] message = new byte[message_length];
        leis.read(message);
        this.message = String.valueOf(message);

        code = (byte) leis.read();

        int reason_length = (int) leis.readVariableSize();

        byte[] reason = new byte[reason_length];
        leis.read(reason);
        this.reason = String.valueOf(reason);
        int length = (int) (getLength() - (message_length + 1 + reason_length));
        if (length > 0)
        {
            data = new byte[length];
            leis.read(data);
        }
        else
            data = null;
    }
    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeString(message);
        leos.write(code);
        leos.writeString(reason);
        leos.write(data);
    }
}
