package Client.messages;

/**
 * Created by Matteo on 07/10/2016.
 */
public class GetData extends Inventory {

    @Override
    public String getCommand() {
        return "getdata";
    }

}
