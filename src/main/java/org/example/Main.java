package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String filePath = "file.txt";
        long uniqueAddressesCount = countUniqueIPv4Addresses(filePath);
        System.out.println("Number of unique IPv4 addresses: " + uniqueAddressesCount);
    }

    public static long countUniqueIPv4Addresses(String filePath) {
        final int READ_SYMBOLS_SIMULTANEOUSLY = (int)Math.pow(2, 18);
        BitSet positiveIpAddresses = new BitSet(Integer.MAX_VALUE);
        BitSet negativeIpAddresses = new BitSet(Integer.MAX_VALUE);
        File file = new File(filePath);
        long fileSizeInBytes = file.length();
        final int SIZE_TO_READ_SIMULTANEOUSLY = (int)(Math.min(fileSizeInBytes, READ_SYMBOLS_SIMULTANEOUSLY));
        byte[] buffer = new byte[SIZE_TO_READ_SIMULTANEOUSLY]; // Adjust buffer size as needed

        try (InputStream inputStream = new FileInputStream(filePath)) {
            int bytesRead;
            String savedBuffer = "";
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String[] bufferedVal = Arrays.stream(new String(buffer, 0, bytesRead).
                        split("\n")).
                        filter(st->!st.isEmpty()).
                        toArray(String[]::new);
                if (buffer[0] == 10 && !savedBuffer.isEmpty()) {
                    processLine(savedBuffer, positiveIpAddresses, negativeIpAddresses);
                } else {
                    bufferedVal[0] = savedBuffer + bufferedVal[0];
                }
                //13 - code of special symbol '/r'(carriage return), 10 - code of special symbol '/n'(newline character)
                savedBuffer = bytesRead < SIZE_TO_READ_SIMULTANEOUSLY || buffer[SIZE_TO_READ_SIMULTANEOUSLY - 1] == 10
                        || buffer[SIZE_TO_READ_SIMULTANEOUSLY - 1] == 13? "":
                        bufferedVal[bufferedVal.length - 1];
                for (int i = 0; i<bufferedVal.length-(bytesRead<SIZE_TO_READ_SIMULTANEOUSLY ||
                        buffer[SIZE_TO_READ_SIMULTANEOUSLY - 1] == 10  ||
                        buffer[SIZE_TO_READ_SIMULTANEOUSLY-1] == 13? 0: 1); i++) {
                    processLine(bufferedVal[i], positiveIpAddresses, negativeIpAddresses);
                }
            }
            if(!savedBuffer.isEmpty()) {
                processLine(savedBuffer, positiveIpAddresses, negativeIpAddresses);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return positiveIpAddresses.stream().count()+positiveIpAddresses.stream().count();
    }

    private static void processLine(String line, BitSet positiveIpAddresses, BitSet negativeIpAddresses) {
        try {
            InetAddress inetAddress = InetAddress.getByName(line.trim());
            if (inetAddress instanceof Inet4Address) {
                int address = byteArrayToInt(inetAddress.getAddress());
                if(address>=0) {
                    positiveIpAddresses.set(address);
                } else {
                    negativeIpAddresses.set(-address);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int byteArrayToInt(byte[] bytes) {
        return (bytes[0] & 0xFF) << 24 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8 |
                (bytes[3] & 0xFF);
    }

}