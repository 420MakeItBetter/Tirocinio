package client.utils;

/**
 * Created by Matteo on 11/10/2016.
 */
public class IOUtils {

    public static byte [] getChecksum(byte [] hash){
        return new byte [] {hash[0], hash[1], hash[2], hash[3]};
    }

    public static byte [] intToByteArray(long i){
        return new byte [] {(byte) (i & 0xFF), (byte) ((i >>> 8) & 0xFF), (byte) ((i >>> 16) & 0xFF), (byte) ((i >>> 24) & 0xFF)};
    }

}
