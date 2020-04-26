package com.company;

import java.io.File;
import java.security.MessageDigest;
import java.util.Base64;

public class Main {

    public static void main(String[] args) {




//        String testMessage =
//                "Name: res/raw/e_5_open_small.ogg\r\n" +
//                        "SHA1-Digest: sYEaEipZwyAPCGvSU6YibUDYuhk=\r\n\r\n";
//        byte[] testBytes = testMessage.getBytes();
//        try
//        {
//            MessageDigest digest = MessageDigest.getInstance("SHA-1");
//            byte[] manifestSHA  = digest.digest(testBytes);
//            byte[] base64Encoding = Base64.getEncoder().encode(manifestSHA);
//            System.out.println(new String(base64Encoding));
//        }
//        catch (Exception e)
//        {
//
//        }
        File[] listOFiles = new File(args[0]).listFiles();

        for(File f: listOFiles) {
            System.out.println(f.getName());
            SingleZip test = new SingleZip(f.getName(), args[0]);
            test.runTests();
        }


        //System.out.println(test.checkExtract());
    }

}
