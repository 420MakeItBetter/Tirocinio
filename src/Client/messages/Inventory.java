package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 07/10/2016.
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
