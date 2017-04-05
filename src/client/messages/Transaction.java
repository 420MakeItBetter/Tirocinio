package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 08/10/2016.
 */
public class Transaction extends Message {

    private int version;
    private List<TransactionInput> transactionsIn;
    private List<TransactionOutput> transactionsOut;
    private int lock_time;


    public Transaction(){
        transactionsIn = new ArrayList<>();
        transactionsOut = new ArrayList<>();
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<TransactionInput> getTransactionsIn() {
        return transactionsIn;
    }

    public List<TransactionOutput> getTransactionsOut() {
        return transactionsOut;
    }

    public void setLock_time(int lock_time) {
        this.lock_time = lock_time;
    }

    @Override
    public String getCommand() {
        return "tx";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        version = leis.readInt();
        long length = leis.readVariableSize();
        for(long i = 0; i < length; i++)
        {
            TransactionInput tranIn = new TransactionInput();
            tranIn.read(leis);
            transactionsIn.add(tranIn);
        }
        length = leis.readVariableSize();
        for(long i = 0; i < length; i++)
        {
            TransactionOutput tranOut = new TransactionOutput();
            tranOut.read(leis);
            transactionsOut.add(tranOut);
        }
        lock_time = leis.readInt();
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {

    }
}
