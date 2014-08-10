package com.classify;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/**
 * @author Anusha Balakrishnan
 *         Date: 8/3/14
 *         Time: 7:53 PM
 */
public class DatasetSplitter {
    private static Logger logger = Logger.getLogger(DatasetSplitter.class);
    private static final String PROPERTY_FILE = "com/config/preprocess.properties";
    private Properties properties;
    private double split_ratio;

    public DatasetSplitter()
    {
        split_ratio = 0.8;
        properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
        if(inputStream==null)
        {
            logger.warn("Couldn't find properties file at "+PROPERTY_FILE);
        }
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.warn("Unable to load properties from file at "+PROPERTY_FILE);
        }
    }

    public DatasetSplitter(double split_ratio)
    {
        this.split_ratio = split_ratio;
    }

    /**
     * Randomly splits the data into training and testing data based on the split ratio.
     * @param filteredDataFile Path of file containing filtered Twitter data.
     */
    public void splitData(String filteredDataFile)
    {
        int weekNum = filteredDataFile.lastIndexOf('_');
        String baseName = String.format("week_%s", filteredDataFile.substring(weekNum+1));
        String trainOut = properties.getProperty("TRAINING_DATA_DIR")+"/train_"+baseName;
        String testOut = properties.getProperty("TEST_DATA_DIR")+"/test_"+baseName;
        try {
            Scanner fileReader = new Scanner(new FileInputStream(new File(filteredDataFile)), "UTF-8");
            BufferedWriter trainWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainOut), "UTF-8"));
            BufferedWriter testWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testOut), "UTF-8"));
            int totalTweets = 0, training = 0, test = 0;
            while (fileReader.hasNextLine())
            {
                String tweet = fileReader.nextLine().trim();
                totalTweets++;
                double p = Math.random();
                if(p>=0.2) {
                    trainWriter.write(tweet+"\n");
                    training++;
                }
                else {
                    testWriter.write(tweet+"\n");
                    test++;
                }
            }

            testSplitAccuracy(totalTweets, training, test);
            logger.info("Correctly split data for "+filteredDataFile);

            fileReader.close();
            trainWriter.close();
            testWriter.close();

        } catch (FileNotFoundException e) {
            logger.fatal("Could not read from file at "+filteredDataFile+". Suspending execution.");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Could not create output file with chosen encoding (UTF-8).");
            logger.warn("[Write failed when creating datasets for "+filteredDataFile+"]");
        } catch (IOException e) {
            logger.warn("Couldn't write to file: "+e.getMessage());
        }
    }

    @Test
    private void testSplitAccuracy(int totalTweets, int trainTweets, int testTweets)
    {
        assertEquals(trainTweets+testTweets, totalTweets);
        assertEquals(0.8, (double)trainTweets/totalTweets, 0.05);
        assertEquals(0.2, (double)testTweets/totalTweets, 0.05);

    }
    public String getProperty(String name)
    {
        return properties.getProperty(name);
    }

    /**
     *
     * @param args Two command-line arguments that numerically specify the first week and the last week of the
     *             data to be split into train/test. e.g. Providing command line arguments 1 18 will create training and
     *             test sets for weeks 1 through 18, inclusive.
     */
    public static void main(String[] args) {
        if(args.length!=2)
        {
            logger.fatal("Usage: java DatasetSplitter first_week second_week\n" +
                    "first_week: The number of the first week of the data.\n" +
                    "second_week: The number of the last week of the data.\n" +
                    "Example: java DatasetSplitter 1 18\n" +
                    "\tCreates training and test sets for all weeks between 1 and 18, inclusive.");
        }

        int start_week = Integer.parseInt(args[0]);
        int end_week = Integer.parseInt(args[1]);

        DatasetSplitter splitter = new DatasetSplitter();

        String week = "Week_%d.dat";
        for(int i=start_week; i<=end_week; i++)
        {
            String filePath = splitter.getProperty("FILTERED_DATA_DIR")+"/"+String.format(week, i);
            splitter.splitData(filePath);
        }

    }
}
