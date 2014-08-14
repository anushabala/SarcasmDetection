package com.classify;

import com.config.ConfigConstants;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Methods of this class should be primarily used to quickly reduce the size of a balanced dataset or generate a smaller
 * dataset from a larger one. To actually create training and testing datasets from a larger dataset, use
 * com.classify.DatasetSplitter instead.
 * The methods of this class are data-agnostic, so they may be used to reduce the number of instances in any kind of file, whether that file contains actual data or feature vectors.
 * Methods of this class generate balanced datasets by default. To create unbalanced datasets, use one of the
 * parameterized constructors below.
 * Methods of this class are meant for use with binary classification datasets only. Methods assume labels 1 and 2 for
 * positive and negative instances respectively.
 *
 * @author Anusha Balakrishnan
 *         Date: 8/13/14
 *         Time: 8:52 PM
 */
public class DatasetTrimmer {
    private static Logger logger = Logger.getLogger(DatasetTrimmer.class);
    private double balanceRatio;
    private int reducedSize;
    private Properties properties;

    public DatasetTrimmer(int reducedSize)
    {
        balanceRatio = 0.5;
        this.reducedSize = reducedSize;
        init();
    }

    /**
     * Use this constructor to create unbalanced datasets (the default constructor creates balanced datasets by default).
     *
     * @param reducedSize The desired size of the new dataset
     * @param balanceRatio The ratio of positive (label 1) to negative (label 2) instances desired in the final dataset.
     */
    public DatasetTrimmer(int reducedSize, double balanceRatio)
    {
        this.reducedSize = reducedSize;
        this.balanceRatio = balanceRatio;
        init();
    }

    private void init()
    {
        properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(ConfigConstants.PREPROCESS_PROPERTIES_FILE);
        if(inputStream==null)
        {
            logger.fatal("Could not load properties from file at "+ConfigConstants.PREPROCESS_PROPERTIES_FILE+". Ensure " +
                    "that the properties file is at the correct location and that the PREPROCESS_PROPERTIES_FILE constant" +
                    "in com.config.ConfigConstants points to the right location.");
            System.exit(1);
        }
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.fatal("Could not load properties from file at "+ConfigConstants.PREPROCESS_PROPERTIES_FILE+". Ensure " +
                    "that the properties file is at the correct location and that the PREPROCESS_PROPERTIES_FILE constant" +
                    "in com.config.ConfigConstants points to the right location.");
            System.exit(1);
        }
    }

    /**
     * Creates a dataset of size reducedSize from the larger dataset at datasetPath. The reduced dataset is written to the specified path at outputPath.
     * @param datasetPath The path of the existing dataset to be reduced in size. NOTE: This method assumes that the
     *                    dataset at this path can be stored in memory.
     * @param outputPath The path that the reduced dataset is to be written to.
     */
    public void trimDataset(String datasetPath, String outputPath)
    {
        ArrayList<String> allInstances = readAllLines(datasetPath);
        if(allInstances.size()==0)
        {
            logger.warn("The dataset specified ("+datasetPath+") appears to be empty. Please specify a different path.");
            return;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath), "UTF-8"));
            if(allInstances.size()<reducedSize) {
                writeAll(allInstances, writer);
                logger.debug("This dataset only contains "+allInstances.size()+" instances: Copied all those " +
                                "instances to "+outputPath);
            }
            else {
                writeReduced(allInstances, writer);
                logger.debug("Wrote reduced dataset (containing "+reducedSize+" instances) to "+outputPath);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            logger.fatal("Could not write to file at "+outputPath+". Please ensure that the directory containing the " +
                    "output file exists.");
            System.exit(1);
        } catch (UnsupportedEncodingException e) {
            logger.warn("The selected encoding (UTF-8) is not supported.");
        } catch (IOException e) {
            logger.warn("Error while writing to file at "+outputPath);
        }
    }

    private void writeAll(ArrayList<String> allInstances, BufferedWriter writer) throws IOException {
        for(String instance: allInstances)
        {
            writer.write(instance+"\n");
        }

    }

    private void writeReduced(ArrayList<String> allInstances, BufferedWriter writer) throws IOException {
        Random gen = new Random();
        int positiveInstances = 0, negativeInstances = 0, totalInstances = 0;
        while(totalInstances< reducedSize)
        {
            int position = totalInstances + gen.nextInt(allInstances.size() - totalInstances);
            String instance = allInstances.get(position);
            if(instance.charAt(0)=='1' && positiveInstances <= reducedSize*balanceRatio) {
                Collections.swap(allInstances, totalInstances, position);
                positiveInstances++;
                totalInstances++;
                writer.write(instance+"\n");
            }
            else if(instance.charAt(0)=='2' && negativeInstances <= reducedSize*balanceRatio) {
                Collections.swap(allInstances, totalInstances, position);
                negativeInstances++;
                totalInstances++;
                writer.write(instance+"\n");
            }
        }

    }
    private ArrayList<String> readAllLines(String datasetPath)
    {
        ArrayList<String> allInstances = new ArrayList<String>();
        try {
            Scanner reader = new Scanner(new FileInputStream(new File(datasetPath)), "UTF-8");
            while(reader.hasNextLine())
            {
                allInstances.add(reader.nextLine().trim());
            }
        } catch (FileNotFoundException e) {
            logger.fatal("Could not find file at path "+datasetPath+". Please specify a different path.");
            System.exit(1);
        }
        return allInstances;
    }
    public String getProperty(String name)
    {
        return properties.getProperty(name);
    }

    public static void main(String[] args) {
        DatasetTrimmer trimmer = new DatasetTrimmer(2000);
        File dataDir = new File(trimmer.getProperty("FULL_FV_DIR"));
        String outputDir = trimmer.getProperty("TRIMMED_FV_DIR");
        for(File f: dataDir.listFiles())
        {
            if(f.getName().contains("test_week_"))
            {
                trimmer.trimDataset(dataDir+"/"+f.getName(), outputDir+"/"+f.getName());
            }
        }
    }
}
