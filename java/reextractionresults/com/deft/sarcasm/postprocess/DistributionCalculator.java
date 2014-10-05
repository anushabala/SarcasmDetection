package com.deft.sarcasm.postprocess;

// 24000 training, 1200 data
import com.config.ConfigConstants;
import org.apache.log4j.Logger;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author Anusha Balakrishnan
 *         Date: 9/29/14
 *         Time: 12:11 AM
 */
public class DistributionCalculator {
    private static final String PROPERTY_FILE = ConfigConstants.PREPROCESS_PROPERTIES_FILE;
    public static void calculateDistribution(String file)
    {
        HashMap<String, Integer> hashtags = new HashMap<String, Integer>();
        double total = 0.0;
        File inFile = new File(file);
        try {
            Scanner in = new Scanner(inFile);

            while(in.hasNextLine())
            {
                String line = in.nextLine().trim();
                String[] parts = line.split("\t");
                if(parts.length<2)
                    continue;
                if(parts[1].contains("sarcasm") || parts[1].contains("sarcastic"))
                    continue;
                total+=1.0;
                if(!hashtags.containsKey(parts[1]))
                {
                    hashtags.put(parts[1], 1);
                }
                else {
                    hashtags.put(parts[1], hashtags.get(parts[1])+1);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(String key: hashtags.keySet())
        {
            System.out.println(key+"\t"+hashtags.get(key)/total);
        }
    }


    public static void main(String[] args) {
        String trainFile = "/Users/anushabala/PycharmProjects/SarcasmDetection/weekly_data_constrained/training_data/test_week_7.dat";
        calculateDistribution(trainFile);

    }
}
