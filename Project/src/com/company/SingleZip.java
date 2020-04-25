package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        fileNameList = unzip(pathZip + "\\" + fileNameZIP, pathZip + "\\" + fileNameZIP.substring(0, zipName.length() - 4));
        //CHECK IF ZIP EXTRACT FAILED

        //filenames = extract(zipName.substring(0,zipName.length()-4)+"folder");
        extractLocation = pathZip + "\\" + fileNameZIP.substring(0, zipName.length() - 4);
        grabSF();
        grabMF();

        //POSTSETUP



        runTests();






    }

    public Boolean[] runTests() {//returns an array of bools coresponding to different tests. true if passed or inapplicable, false if failed.
        try {
            ArrayList<String> manifestNames = extractNames(manifestFile);
            System.out.println("function?");
            System.out.println(completeXF(manifestFile,manifestNames));
            System.out.println(completeXF(signatureFile,manifestNames));
        } catch (Exception e)
        {
            System.out.println(e);
        }
        return null;
    }


    public boolean checkExtract() {
        return extracted;
    }

    public boolean hasMF() {
        //return new File(extractLocation + "\\" + "META-INF" + "\\" + "MANIFEST.MF").isFile();
        return manifestFile != null;
    }

    public boolean hasSF() {
        //return new File(extractLocation + "\\" + "META-INF" + "\\" + "MANIFEST.MF").isFile();
        return signatureFile != null;
    }


    public boolean grabMF() {
        File[] possibleFiles = new File(extractLocation + "\\" + "META-INF" + "\\").listFiles();
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
        File[] possibleFiles = new File(extractLocation + "\\" + "META-INF" + "\\").listFiles();
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

    public boolean hasSignature() {
        return false;
    }

    public boolean completeXF(File XF, ArrayList<String> allNames) //Match the XF records with a list of filenames (filenames inclusive of META-INF)
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

    public boolean compareFileList(ArrayList<String> inclusive, ArrayList<String> exclusive)//Names are holdovers, just two arbitrary names both discounting the meta-inf components
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

            return parsedNames;

    }




    public boolean correctMF()
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

    public boolean difSigners(File MF, File SF)
    {
        return false;
    }

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

}
