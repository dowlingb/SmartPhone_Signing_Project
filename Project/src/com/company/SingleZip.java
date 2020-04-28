package com.company;


import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Base64;
import java.security.MessageDigest;

public class SingleZip {
    public String fileNameZIP;//The ZIP filename we are working off of
    public String extractName = null;//Redundant?
    public boolean extracted;
    public ArrayList<String> filenames;//Contents filenames for checking
    public File manifestFile; //MF
    public File signatureFile; //SF
    public File signedFile; //.rsa or whatever
    public String pathZip;//Where the files are to be extracted to (EXCLUDING FOLDER. the path TO where they are to be extracted)
    public String extractLocation;//The full folder name the files are extracted to
    public ArrayList<String> fileNameList;//All filenames from extraction. So this would corespond with the ZIP file identified filenames. need a crawler as well
    public String signType;

    // Create a class constructor for the MyClass class
    public SingleZip(String zipName, String pathName) {//ZIPNAME is simply the APK/ZIP file name and pathName is the folder path
        pathZip = pathName;
        fileNameZIP = zipName;  // Set the initial value for the class attribute x
        fileNameList = unzip(pathZip + File.separator + fileNameZIP, pathZip + File.separator + fileNameZIP.substring(0, zipName.length() - 4));
        //CHECK IF ZIP EXTRACT FAILED
        extractLocation = pathZip + File.separator + fileNameZIP.substring(0, zipName.length() - 4);
        //filenames = extract(zipName.substring(0,zipName.length()-4)+"folder");
        try {
            grabSF();//this will lead to basically everything failing
            grabMF();
        }
        catch (Exception e)
        {
            System.out.println(fileNameZIP);
            System.out.println(e);
        }
        //POSTSETUP

        //nothin much to do here anymore









    }

    public boolean[] runTests() {//returns an array of bools coresponding to different tests. true if passed or inapplicable, false if failed.
        try {
            ArrayList<String> manifestNames = extractNames(manifestFile);//Might be handy. who knows tbh
           // System.out.println("function?");
            boolean[] results = new boolean[12];
            results[0] = checkExtract();
            results[1] = attack1(fileNameList);
            results[2] = completeXF(manifestFile,fileNameList);
            results[3] = completeXF(signatureFile,fileNameList);
            results[4] = checkMETAINFSafe(extractLocation);
            results[5] =correctSF(manifestFile,signatureFile);
            results[6] = hasSF();
            results[7] =completeXF(manifestFile,fileNameList);
            results[8] =hasMF();
            results[9] = correctMF(manifestFile,extractLocation+File.separator);
            results[10] = hasSignature(extractLocation,signatureFile);
            results[11]= groupMismatch(manifestFile,signatureFile);
            return results;
//            System.out.println(checkExtract());//zip bug 2
//            System.out.println(attack1(fileNameList));//attack 1
//            System.out.println(completeXF(manifestFile,fileNameList));//attack 2? i mean technically
//            System.out.println(completeXF(signatureFile,fileNameList));//attack 2 as well? i mean technically
//            System.out.println(checkMETAINFSafe(extractLocation)); //vul 2
//            System.out.println(correctSF(manifestFile,signatureFile));//v1bug 2
//            System.out.println(hasSF());//v1 bug 3 (component)
//            System.out.println(completeXF(manifestFile,fileNameList));//v1 bug 4
//            System.out.println(hasMF());//v1 bug 5
//            System.out.println(correctMF(manifestFile,extractLocation+File.separator));//v1bug 6
//            System.out.println(hasSignature(extractLocation,signatureFile));//V1 bug 7
//            System.out.println(groupMismatch(manifestFile,signatureFile));//v1 bug 8





           // System.out.println(createBase64(manifestFile,"SHA-1"));//Basic SHA-1 encoding test for files. worked perfectly



        } catch (Exception e)
        {
           // System.out.println(fileNameZIP);
            System.out.println(fileNameZIP + "failed testing due to exception");
        }
        return null;
    }


    public boolean checkExtract() //Zip bug 2
    {
        return extracted;
    }

    public boolean checkMETAINFSafe(String baseFolder)//Vul-2. This combined with the other verifications indicates if there is anything unwanted in META-INF.
    // Let's also address that this is a pretty loose check and is just looking for things we DONT expect
            //This wouldn't actually stop something from renaming an arbitrary file as X.MF or X.SF or X.RSA and then using it as whatever.
    {
        File[] listOFiles = new File(baseFolder+File.separator+"META-INF").listFiles();
        String ext = "not a real file extension";
        for(File f: listOFiles)
        {
            int i = f.getName().lastIndexOf('.');
            if (i > 0) {
                ext= f.getName().substring(i+1);
            }
            if(!(ext.equals("MF")||ext.equals("SF")||ext.equals("RSA")||ext.equals("DSA")||ext.equals("EC")||ext.equals("")))
            {

                return false;
            }
        }
        return true;
    }

    public boolean hasMF()//V1 bug 5 technically? its listed differently
    {
        //return new File(extractLocation + File.separator + "META-INF" + File.separator + "MANIFEST.MF").isFile();
        return manifestFile != null;
    }

    public boolean hasSF() {//V1 bug 3 partial
        //return new File(extractLocation + File.separator + "META-INF" + File.separator + "MANIFEST.MF").isFile();
        return signatureFile != null;
    }



    public boolean hasSignature(String baseFolder,File SF) {//V1 bug 7. also as far as v1 bug 1 is going sadly due to time
        File[] listOFiles = new File(baseFolder+File.separator+"META-INF").listFiles();
        String curFile = "not a real file extension";

        String certName = SF.getName();
        certName=certName.substring(0,certName.length()-3);
        for(File f: listOFiles)
        {
            curFile = f.getName();

            if(curFile.equals(certName+".RSA")||curFile.equals(certName+".DSA")||curFile.equals(certName+".EC"))
            {

                return true;
            }
        }
        return false;
    }

    public boolean groupMismatch(File SF, File MF) {//V1 bug 8
        try {
            String SFGroup = grabGroup(SF);
            String MFGroup = grabGroup(MF);
            if(MFGroup.equals(SFGroup))
            {
                return true;
            }
        }
        catch (Exception e)//Something failed. it didn't pass the test
        {
            //System.out.println(e);
            return false;
        }


        return false;
    }

    public String grabGroup(File XF) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(XF));

        String st;
        while ((st = br.readLine()) != null)
        {
            if(st.length()>11 && st.substring(0,11).equals("Created-By:"))
            {
                return st;//I gave it a thought.... We can just compare the whole thing
            }
        }
        return null;
    }

    public boolean attack1(ArrayList<String> zipIndicatedFiles)//Any double files? attack 1. Simple filename duplicate checker. dump the files see if there are dupes.
    {
        Set<String> uniqueSet = new HashSet<String>(zipIndicatedFiles);
        return !(uniqueSet.size()<zipIndicatedFiles.size()); //Wow. look at that condensed code
    }

    public boolean completeXF(File XF, ArrayList<String> allNames) //Match the XF records with a list of filenames (filenames inclusive of META-INF). Fundamentally Attack 2 and also v1 bugs 3,4 but not including digest yet
    {
        try {
            ArrayList<String> namesFromXF = extractNames(XF);
            ArrayList<String> testNames = new ArrayList<String>();
            for(String i : allNames)
            {
                if(i.length()<"META-INF".length()||!i.substring(0,"META-INF".length()).equals("META-INF"))//Not part of metaINF
                {
                    testNames.add(i);
                }
            }

            return compareFileList(namesFromXF,testNames);
        }
        catch(Exception e)

        {

            return false;
        }
    }





    public boolean correctMF(File MF, String root)throws IOException//All SHA-1 digests are good to go
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader(MF));

            String st;
            while ((st = br.readLine()) != null)
            {
                if(st.length()>= 6 && st.substring(0,5).equals("Name:"))
                {
                    String curName = st.substring("Name: ".length());
                    String second = br.readLine();
                    if(second.length()>= 13 && second.substring(0,"SHA1-Digest: ".length()).equals("SHA1-Digest: "))
                    {
                        second = second.substring("SHA1-Digest: ".length());
                        File checkFile = new File(root+curName);
                        String shaDigest = createBase64(checkFile,"SHA-1");
                        if(!shaDigest.equals(second))
                        {
                            return false;
                        }
                    }
                    else
                    {
                        curName= curName+second.trim();
                        String hash = br.readLine();
                        hash = hash.substring("SHA1-Digest: ".length());
                        File checkFile = new File(root+curName);
                        String shaDigest = createBase64(checkFile,"SHA-1");
                        if(!shaDigest.equals(hash))
                        {
                            return false;
                        }
                    }

                }
            }
            return true;
        }catch (Exception e)
        {
           // System.out.println(e);
            return false;
        }


    }

    public boolean correctSF(File MF, File SF)//All SHA-1 digests are good to go, including that of MF.
    {
        try {
            BufferedReader brMF = new BufferedReader(new FileReader(MF));
            BufferedReader brSF = new BufferedReader(new FileReader(SF));
            String stSF;
            ArrayList<String> hashesSF = new ArrayList<String>();
            while ((stSF = brSF.readLine()) != null)
            {
                if(stSF.length()>= 13 && stSF.substring(0,"SHA1-Digest: ".length()).equals("SHA1-Digest: "))
                {
                    String hash = stSF;
                    hashesSF.add(hash.substring("SHA1-Digest: ".length()));//Cool now we have the SHA-1 hash

                }
            }
            String stMF;
            ArrayList<String> hashesMF = new ArrayList<String>();
            while ((stMF = brMF.readLine()) != null)
            {
                if(stMF.length()>= 6 && stMF.substring(0,5).equals("Name:"))
                {
                    String curName = stMF;
                    String second = brMF.readLine();
                    if(second.length()>= 13 && second.substring(0,"SHA1-Digest: ".length()).equals("SHA1-Digest: "))
                    {
                        hashesMF.add(createBase64(curName+"\r\n"+second+"\r\n\r\n","SHA-1"));
                    }
                    else
                    {
                        String third = brMF.readLine();
                        hashesMF.add(createBase64(curName+"\r\n"+second+"\r\n"+third+"\r\n\r\n","SHA-1"));
                    }
                    //System.out.println(curName+"\r\n"+hash+"\r\n\r\n");

                }
            }
            for(int i = 0; i<hashesSF.size();i++)
            {
                if(!(hashesSF.contains(hashesMF.get(i))))
                {
                    //System.out.println(hashesMF.get(i));
                    return false;
                }
            }
            if(hashesSF.size()!=hashesMF.size())
            {
                return false;
            }

        }catch (Exception e)
        {
           // System.out.println(e.getStackTrace());
            return false;
        }
        return true;
    }

    public boolean matchSFMF(File MF, File SF)//this is handled by correctSF. depriciated
    {
        return false;
    }

    public boolean matchSignatureSF() //V1 bug 1. depriciated
    {
        return false;
    }
















    //UTILITY STUFF





    private ArrayList<String> unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists())
        {
            dir.mkdirs();
        }
        FileInputStream fis;
        byte[] buffer = new byte[1024];
        try
        {
            ArrayList<String> filenames = new ArrayList<String>();

            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null)
            {
                String fileName = ze.getName();
                //System.out.println(fileName);
                filenames.add(fileName);
                File newFile = new File(destDir + File.separator + fileName);

                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fis.close();

            extractName = destDir;
            extracted = true;
            return filenames;


        } catch (IOException e)
        {
            e.printStackTrace();
            extractName = null;
            extracted = false;
            return null;//This means it has failed to extract the file in its entirety, failing a zip issue
        }

    }




    public boolean grabMF() {
        File[] possibleFiles = new File(extractLocation + File.separator + "META-INF" + File.separator).listFiles();
        File MFFile;
        //I want to check if the MF was duplicated or if there are multiple
        boolean found = false;
        for (int i = 0; i < possibleFiles.length; i++) {
            File possible = possibleFiles[i];
            //System.out.println(possible.getName().substring(possible.getName().length() - 3));
            if (possible.getName().substring(possible.getName().length() - 3).equals(".MF")) {
                if (found) {
                    System.out.println("Duplicate MFs exist. See " + extractLocation);//This really would be unexpected and pretty weird.
                } else {
                    found = true;
                    MFFile = possible;
                    manifestFile = possible;
                }

            }

        }


        return found;
    }


    public boolean grabSF() {
        File[] possibleFiles = new File(extractLocation + File.separator + "META-INF" + File.separator).listFiles();
        File SFFile;
        //I want to check if the SF was duplicated or if there are multiple
        boolean found = false;
        for (int i = 0; i < possibleFiles.length; i++) {
            File possible = possibleFiles[i];
            //System.out.println(possible.getName().substring(possible.getName().length() - 3));
            if (possible.getName().substring(possible.getName().length() - 3).equals(".SF")) {
                if (found == true) {
                    //System.out.println("Duplicate SFs exist. See " + extractLocation); // This isn't unexpected behaviour
                } else {
                    found = true;
                    SFFile = possible;
                    signatureFile = possible;
                }

            }

        }


        return found;
    }


    public boolean compareFileList(ArrayList<String> inclusive, ArrayList<String> exclusive)//Names are holdovers, just two arbitrary names both discounting the meta-inf components. used for V1 bugs 2,3,4
    {
        boolean same = true;
        for(String i : inclusive)
        {
            if(exclusive.indexOf(i)==-1)
            {
                //System.out.println(i);
                same = false;
            }
        }
        for(String i : exclusive)
        {
            if(inclusive.indexOf(i)==-1)
            {
                //System.out.println(i);
                same = false;
            }
        }
        return same;
    }

    public ArrayList<String> extractNames(File MF) throws IOException //Extracts file names included from either a MF or SF file (originally built for MF but actually works on both)
    {
        ArrayList<String> allLines = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(MF));

        String st;
        while ((st = br.readLine()) != null)
        {
            if(st.length()>= 6 && st.substring(0,5).equals("Name:"))
            {
                String cur = st.substring("Name: ".length());
                String possible = br.readLine();
                if(!possible.contains("SHA1-Digest:"))
                {
                    allLines.add(cur+possible.trim());
                }
                else
                {
                    allLines.add(cur);
                }
            }
        }
//        ArrayList<String> parsedNames = new ArrayList<String>();
//        for(String i : allLines) {
//            if(i.length()>= 6 && i.substring(0,5).equals("Name:"))
//            {
//                parsedNames.add(i.substring("Name: ".length()));
//            }
//        }
        br.close();
        return allLines;

    }

    public String createBase64(File file,String algorithm) throws Exception  {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        InputStream fis = new FileInputStream(file);
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        byte[] manifestSHA  = digest.digest();


        byte[] base64Encoding = Base64.getEncoder().encode(manifestSHA);

        return new String(base64Encoding);
    }

    public String createBase64(String line,String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] manifestSHA = digest.digest(line.getBytes());
            byte[] base64Encoding = Base64.getEncoder().encode(manifestSHA);
            return new String(base64Encoding);
        } catch (Exception e) {

            return null;
        }
    }

}
