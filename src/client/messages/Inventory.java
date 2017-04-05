package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 07/10/2016.
 *
 */
public class Inventory extends Message {

    private List<InventoryVector> inventoryVectors;

    public Inventory(){
        inventoryVectors = new ArrayList<>();
    }

    public List<InventoryVector> getInventoryVectors() {
        return inventoryVectors;
    }

    @Override
    public String getCommand() {
        return "inv";
    }

    @Override
    public long getLength() {
        int varint = 1;
        if(inventoryVectors.size() < 0xFD)
            varint = 1;
        else if(inventoryVectors.size() <= 0xFFFF)
            varint = 3;
        else if(inventoryVectors.size() <= 0xFFFFFFFF)
            varint = 5;
        return 36*inventoryVectors.size() + varint;

    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        long count = leis.readVariableSize();
        for(long i = 0; i< count; i++)
        {
            InventoryVector invItem = new InventoryVector();
            invItem.read(leis);
            inventoryVectors.add(invItem);
        }
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeVariableSize(inventoryVectors.size());
        for(InventoryVector invItem : inventoryVectors)
        {
            invItem.write(leos);
        }
    }
}
