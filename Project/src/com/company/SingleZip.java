package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SingleZip {
    public String fileNameZIP;//The ZIP filename
    public String extractName = null;
    public boolean extracted;
    public ArrayList<String> filenames;//Contents filenames for checking
    public String manifestFile; //MF
    public String signatureFile; //SF
    public String signedFile; //.rsa or whatever
    public String pathZip;

    // Create a class constructor for the MyClass class
    public SingleZip(String zipName,String pathName) {//ZIPNAME has to be the FULL path name
        pathZip = pathName;
        fileNameZIP = zipName;  // Set the initial value for the class attribute x
        unzip(pathZip+"\\" +fileNameZIP,pathZip+"\\" + fileNameZIP.substring(0,zipName.length()-4));
        //filenames = extract(zipName.substring(0,zipName.length()-4)+"folder");

    }

    public String[] runTests()
    {
        return null;
    }


    public boolean checkExtract()
    {
        return extracted;
    }

    public boolean hasMF()
    {
        return false;
    }

    public boolean hasSF()
    {
        return false;
    }

    public boolean hasSignature()
    {
        return false;
    }

    public boolean completeMF()
    {
        return false;
    }

    public boolean completeSF()
    {
        return false;
    }

    public boolean matchSFMF()
    {
        return false;
    }

    public boolean matchSignatureSF()
    {
        return false;
    }

    public boolean difSigners(String MF, String SF)
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
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
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
