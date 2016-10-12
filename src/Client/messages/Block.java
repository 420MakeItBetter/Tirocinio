package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 08/10/2016.
 */
public class Block extends Header {

    List<Transaction> transactions;

    public Block(){
        transactions = new ArrayList<>();
    }


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
