package com.sarcasm.preprocess;

import com.config.ConfigConstants;
import com.sarcasm.util.TextUtility;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.log4j.Logger;
import sun.rmi.runtime.Log;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

/**
 * @author Anusha Balakrishnan
 *         Date: 8/11/14
 *         Time: 5:36 AM
 */
public class VocabularyCreator {
    private HashMap<String, Integer> vocabulary;
    private static Logger logger = Logger.getLogger(VocabularyCreator.class);
    private static final String PROPERTY_FILE = ConfigConstants.PREPROCESS_PROPERTIES_FILE;
    private Properties properties;
    public VocabularyCreator()
    {
        vocabulary = new HashMap<String, Integer>();
        properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
        try {
            if(inputStream==null)
                throw new NullPointerException();
            properties.load(inputStream);
        }
        catch (IOException e) {
            logger.warn("Unable to load properties file at "+PROPERTY_FILE+". Modify com.config.ConfigConstants to " +
                    "modify the path or specify a new path for the properties file.");
        }
        catch (NullPointerException e) {
            logger.warn("Property file at "+ PROPERTY_FILE +" not found. Modify com.config.ConfigConstants to " +
                    "modify existing path or specify a new path for the properties file.");
        }
    }

    /**
     * Creates a vocabulary table that maps each word found in each file in the training directory to its frequency (total
     * number of occurrences across all files in the directory).
     * @param trainingDir The path of the directory in which all the training files are located
     * @param namePattern (String) Describes the pattern of the filenames for the training files to be read from. If not null,
     *                    vocabulary will only be extracted from files with filenames that match this pattern.
     *                    namePattern can be either a simple string or a regular expression.
     * @param write boolean parameter indicating whether the vocabulary should be written to the default vocabulary file
     *              or not. If true, the vocabulary created from the files in trainingDir is written to the vocabulary
     *              file specified by the path VOCABULARY_FILE in the properties file at
     *              ConfigConstants.PROCESS_PROPERTIES_FILE. If false, the vocabulary is written to standard output.
     */
    public void createVocabulary(String trainingDir, String namePattern, boolean write)
    {
        StrTokenizer tokenizer = new StrTokenizer();
        File dir = new File(trainingDir);
        if(!dir.isDirectory()) {
            logger.error("First parameter to createVocabulary(String, String) should be the path to a directory, not" +
                    " a file. Please check parameter and try again.\n\tProvided path: " + trainingDir);
            return;
        }
        TrainingFileFilter filter = new TrainingFileFilter(namePattern);
        for(File current: dir.listFiles(filter))
        {
            logger.debug("Updating vocabulary using tokens in "+current.getName());
            try {
                Scanner fileReader = new Scanner(new InputStreamReader(new FileInputStream(current), "UTF-8"));
                while(fileReader.hasNextLine())
                {
                    String line = fileReader.nextLine().trim();
                    String[] fields = line.split("\t");
                    String tweet;
                    try {
                        tweet = fields[2];
                    }
                    catch (ArrayIndexOutOfBoundsException ai)
                    {
                        tweet = line.trim();
                    }
                    tokenizer.reset(tweet);
                    String[] tokens = tokenizer.getTokenArray();
                    for(String token: tokens)
                    {
                        token = TextUtility.stripPunctuation(token);
                        if(token.length()>0) {
                            if (!vocabulary.containsKey(token))
                                vocabulary.put(token, 1);
                            else
                                vocabulary.put(token, vocabulary.get(token) + 1);
                        }
                    }
                }
                fileReader.close();

            } catch (FileNotFoundException e) {
                logger.warn("Could not find file at "+current.getAbsolutePath()+". Skipping file.");
            } catch (UnsupportedEncodingException e) {
                logger.warn("Could not use chosen encoding to read from file at "+current.getAbsolutePath()+". " +
                        "Skipping file.");
            }
        }

        if(write) {
            String vocabFile = getProperty("VOCABULARY_FILE");
            try {
                writeVocabulary(vocabFile);
                logger.info("Wrote tokens and frequencies from vocabulary to vocabulary file at "+vocabFile+". To change" +
                        " the write path, change the VOCABULARY_FILE property as needed in preprocess.properties.");
            } catch (FileNotFoundException e) {
                logger.warn("Could not find directory for vocabulary file at " + vocabFile);
            } catch (IOException e) {
                logger.warn("Error when writing to vocabulary file ("+vocabFile+")");
            }
        }
        else {
            displayVocabulary();
        }
    }

    private void displayVocabulary()
    {
        for(String key: vocabulary.keySet())
        {
            System.out.println(key+"\t"+vocabulary.get(key));
        }
    }
    private void writeVocabulary(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
        for(String key: vocabulary.keySet())
        {
            writer.write(key+"\t"+vocabulary.get(key));
            writer.newLine();
        }
        writer.close();
    }

    public String getProperty(String name)
    {
        return properties.getProperty(name);
    }

    public static void main(String[] args) {
        VocabularyCreator creator = new VocabularyCreator();
        creator.createVocabulary(creator.getProperty("TRAINING_DATA_DIR"), "train_week_", true);
    }
}

/**
 * A class that implements FilenameFilter and only accepts files whose names match a specified pattern.
 */
class TrainingFileFilter implements FilenameFilter {
    private static final Logger logger = Logger.getLogger(TrainingFileFilter.class);
    private Pattern namePattern;
    private Matcher nameMatcher;
    public TrainingFileFilter(String pattern)
    {
        super();
        if(pattern==null)
            pattern = ".*";
        namePattern = Pattern.compile(pattern);
        nameMatcher = null;
    }

    /**
     * Checks whether the file specified can be accepted by this filter or not. Only files that match the filter's
     * pattern and are not directories can be accepted by the filter.
     * @param dir The directory the file belongs to
     * @param name The name of the file
     * @return true iff the filename specified matches the filter's pattern and the filename doesn't refer to a
     * directory
     */
    @Override
    public boolean accept(File dir, String name) {

        if(nameMatcher==null)
            nameMatcher = namePattern.matcher(name);
        else
            nameMatcher.reset(name);
//        String absolutePath = dir+"/"+name;
//        File currentFile = new File(absolutePath);
        return nameMatcher.find();
    }

}