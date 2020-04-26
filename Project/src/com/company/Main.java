package com.company;

public class Main {

    public static void main(String[] args) {
        SingleZip test = new SingleZip(args[1],args[0]);
        test.runTests();
        //System.out.println(test.checkExtract());
    }

}
