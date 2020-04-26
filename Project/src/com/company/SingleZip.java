package com.company;


import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        //filenames = extract(zipName.substring(0,zipName.length()-4)+"folder");
        extractLocation = pathZip + File.separator + fileNameZIP.substring(0, zipName.length() - 4);
        grabSF();
        grabMF();

        //POSTSETUP










    }

    public Boolean[] runTests() {//returns an array of bools coresponding to different tests. true if passed or inapplicable, false if failed.
        try {
            ArrayList<String> manifestNames = extractNames(manifestFile);//Might be handy. who knows tbh
            System.out.println("function?");
            System.out.println(checkExtract());//zip bug 2
            System.out.println(attack1(fileNameList));//attack 1
            System.out.println(completeXF(manifestFile,fileNameList));//attack 2? i mean technically
            System.out.println(completeXF(signatureFile,fileNameList));//attack 2 as well? i mean technically
            System.out.println(checkMETAINFSafe(extractLocation)); //vul 2
            System.out.println(hasSF());//v1 bug 3 (component)
            System.out.println(completeXF(manifestFile,fileNameList));//v1 bug 4
            System.out.println(hasMF());//v1 bug 5
            System.out.println(hasSignature(extractLocation,signatureFile));//V1 bug 7
            System.out.println(groupMismatch(manifestFile,signatureFile));//v1 bug 8



        } catch (Exception e)
        {
            System.out.println(e);
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
            System.out.println(e);
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





    public boolean correctMF()//All SHA-1 digests are good to go
    {
        return false;
    }

    public boolean correctSF()//All SHA-1 digests are good to go, including that of MF
    {
        return false;
    }

    public boolean matchSFMF(File MF, File SF)
    {
        return false;
    }

    public boolean matchSignatureSF()
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
        //buffer for read and write data to file
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
                filenames.add(fileName);
                File newFile = new File(destDir + File.separator + fileName);
                //System.out.println("Unzipping to "+newFile.getPath());
                //create directories for sub directories in zip
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
            //close last ZipEntry
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
        //I want to check if the SF was duplicated or if there are multiple
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
                    System.out.println("Duplicate SFs exist. See " + extractLocation);
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
                System.out.println(i);
                same = false;
            }
        }
        for(String i : exclusive)
        {
            if(inclusive.indexOf(i)==-1)
            {
                System.out.println(i);
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
            allLines.add(st);
        ArrayList<String> parsedNames = new ArrayList<String>();
        for(String i : allLines) {
            if(i.length()>= 6 && i.substring(0,5).equals("Name:"))
            {
                parsedNames.add(i.substring("Name: ".length()));
            }
        }
        br.close();
        return parsedNames;

    }
}
