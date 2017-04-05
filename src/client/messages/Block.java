package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the Implementation of the "block" {@link Message}
 *
 * @author Matteo Franceschi
 * @see client.messages.Message
 *
 */
public class Block extends Header {

    /**
     * the parameter which keeps all the {@link Transaction} of this Message
     */
    private List<Transaction> transactions;

    /**
     * initialize this Message
     */
    public Block(){
        transactions = new ArrayList<>();
    }


    /**
     * return the list of {@link Transaction}, the list returned is modificable, all the changes done to the list
     * are also done to the list of transaction of this {@link Message}
     * @return the list of {@link Transaction}
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * returns the command for this message
     * @return "block"
     */
    @Override
    public String getCommand() {
        return "block";
    }


    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        super.read(leis);
        for(long i = 0; i < getCount(); i++)
        {
            Transaction tran = new Transaction();
            tran.read(leis);
            transactions.add(tran);
        }

    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        super.write(leos);
        for(Transaction tran : transactions)
            tran.write(leos);
    }
}
