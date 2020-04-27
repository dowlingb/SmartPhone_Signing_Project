package com.company;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
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
        ArrayList<boolean[]> results = new ArrayList<boolean[]>();
        for(File f: listOFiles) {
            System.out.println(f.getName());

            SingleZip test = new SingleZip(f.getName(), args[0]);
            results.add(test.runTests());
        }
        int[] problems = new int[5];
        int totalSurvey = 0;
        for(boolean[] booleans : results)
        {
            if(booleans!= null)
            {
                totalSurvey++;
                problems[0] += !booleans[0] ? 1 : 0;
                problems[1] += !booleans[1] ? 1 : 0;
                problems[2] += !(booleans[2] && booleans[3]) ? 1 : 0;
                problems[3] += !booleans[4] ? 1 : 0;
                problems[4] += !(booleans[5] && booleans[6] && booleans[7] && booleans[8] && booleans[9] && booleans[10] && booleans[11]) ? 1 : 0;
            }
        }

        System.out.println("Number tested " + totalSurvey);
        for(int i : problems)
        {
            System.out.println(i);
        }
        //In order, zip big, attack 1, attack 2, vul 2, v1 bug

        //System.out.println(test.checkExtract());
    }

}
