package client.messages;

/**
 * Created by Matteo on 07/10/2016.
 */
public class NotFound extends Inventory {

    @Override
    public String getCommand() {
        return "notfound";
    }
}
