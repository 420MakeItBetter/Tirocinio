package com.bitker.utils;

import org.jetbrains.annotations.Contract;

import java.net.InetAddress;

/**
 * This class contains some useful method
 * @author Matteo Franceschi mfranceschi94@gmail.com
 */
public class IOUtils {

    /**
     * Given the entire 256 byte double hash of something, it returns
     * the first 4 bytes of it to be used as checksum in a message.
     *
     * @param hash the byte array where the double hash-256 is placed
     * @return the first 4 byte of the hash param
     */
    @Contract("_ -> !null")
    public static byte [] getChecksum(byte [] hash){
        return new byte [] {hash[0], hash[1], hash[2], hash[3]};
    }

    /**
     * Converts an integer into an array of byte
     *
     * @param i the long to be converted
     * @return the byte array which represent the integer passed
     */
    @Contract("_ -> !null")
    public static byte [] intToByteArray(long i){
        return new byte [] {(byte) (i & 0xFF), (byte) ((i >>> 8) & 0xFF), (byte) ((i >>> 16) & 0xFF), (byte) ((i >>> 24) & 0xFF)};
    }

    public static int byteArrayToInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static byte [] addressToByte(InetAddress addr){
        if(addr.getAddress().length == 4)
            return new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                    (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                    (byte)0x00,(byte)0xFF,(byte)0xFF,addr.getAddress()[0],addr.getAddress()[1],addr.getAddress()[2],addr.getAddress()[3]};
        else
            return addr.getAddress();
    }

}
