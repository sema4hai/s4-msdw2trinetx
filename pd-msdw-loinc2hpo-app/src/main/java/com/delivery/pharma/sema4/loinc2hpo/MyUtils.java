package com.delivery.pharma.sema4.loinc2hpo;

import java.io.*;

public class MyUtils {

    public static Writer getWriter(String path){
        BufferedWriter writer = null;
        if (path == null){
            writer = new BufferedWriter(new OutputStreamWriter(System.out));
            return writer;
        }
        try {
            writer = new BufferedWriter(new FileWriter(path));
        } catch (IOException e) {
            writer = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        return writer;
    }
}
