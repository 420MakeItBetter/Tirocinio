package client.messages;

/**
 * Created by Matteo on 07/10/2016.
 */
public class GetHeaders extends GetBlocks {

    @Override
    public String getCommand() {
        return "getheaders";
    }
}
