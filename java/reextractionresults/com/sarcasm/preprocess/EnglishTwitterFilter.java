package com.sarcasm.preprocess;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.sarcasm.util.TextUtility;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class EnglishTwitterFilter {
    private static Logger logger = Logger.getLogger(EnglishTwitterFilter.class);
    private static String LANG_DETECTOR_PROFILE = "/Users/anushabala/Downloads/langdetect-03-03-2014/profiles";
    private String[] hashtags = {"#sarcasm", "#sarcastic", "#angry", "#awful", "#disappointed",
            "#excited", "#fear", "#frustrated", "#grateful", "#happy", "#hate",
            "#joy", "#loved", "#love", "#lucky", "#sad", "#scared", "#stressed",
            "#wonderful", "#positive", "#positivity", "#disappointed"};
    private static final String PROPERTY_FILE = "com/config/preprocess.properties";
    public String OUT_DIR;
    public Properties properties;

    private static final int MAX_RANDOM = 200000;

    public EnglishTwitterFilter() throws LangDetectException {
        DetectorFactory.loadProfile(LANG_DETECTOR_PROFILE);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
        properties = new Properties();
        if (inputStream==null)
            logger.warn("Property file at "+PROPERTY_FILE+" not found.");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.warn("Unable to load properties file at "+PROPERTY_FILE);
        }
        OUT_DIR = properties.getProperty("FILTERED_DATA_DIR");
    }

    public void loadFileForFiltering(String path) throws IOException, LangDetectException {
        File f = new File(path);

        File[] files = f.listFiles();

        BufferedReader reader = null;
        String week = "";
        if (path.contains("Week_")) {
            week = '_' + path.substring(path.indexOf("Week_"));
        }
        String outPath = OUT_DIR + "/training" + week + ".dat";
        List<String> sarcasticTweets = new ArrayList<String>();
        List<String> sentimentTweets = new ArrayList<String>();
        int otherLangCount = 0;

        for (File file : files) {

            if (file.getName().contains("Store") || file.getName().contains("new_tweet")) {
                continue;
            }
            reader = new BufferedReader(new InputStreamReader(new FileInputStream
                    (path + "/" + file.getName()), "UTF-8"));


            //the tweets collected from the tweeter has the "hash" name
            //in the file name. So just parse the name to extract the hashtag
            //and generate the type (e.g. positive/sarcasm/negative).

            String hash = getHash(file.getName());
            String type = TextUtility.getMessageType(hash);

            if (null == type) {
                logger.warn("Error in message category - check filename: " + file.getName());
                continue;
            }
            /*
            Modify the type of the tweet so that the only differentiation is between sarcasm (1) and sentiment (2). By
            default, the getMessageType() method returns 1,2, or 3 for sarcasm, positive, and negative type respectively.
             */
            String modifiedType = type;
            if (type.equals("3"))
                modifiedType = "2";
            //pass this hash to retrieve the list of hashes for the type
            List<String> hashes = collectHashes();

            Set<String> filteredSet = new HashSet<String>();
            List<String> uniqList = new ArrayList<String>();

            List<String> removedList = new ArrayList<String>();


            while (true) {
                String line = reader.readLine();
                if (null == line) {
                    break;
                }
                //378953906581037056	Sat Sep 14 14:50:26 EDT 2013	CourteneyLohman	I really like it when you don't text me back #sarcasm

                line = line.toLowerCase();

                String features[] = line.split("\t");

                if (features.length != 4) {
//                    logger.debug("Not properly formatted: "+line);
                    continue;
                }
                String id = features[0];
                String tweet = features[3];

                //we need to filter this tweet

                //detect language
                String language = detectLanguage(tweet);
                if (language==null || !language.equalsIgnoreCase("en")) {
//					logger.debug("Tweet is in another language: "+language+"\t"+tweet);
                    otherLangCount++;
                    continue;
                }

                tweet = StringUtils.stripAccents(tweet);
                tweet = TextUtility.replaceDoubleQuotes(tweet);
                if (!TextUtility.hashesConsistent(tweet, type)) {
//                    logger.debug("Hashtag type(s) not consistent with the type of the file that the tweet belongs to: "
//                            +tweet);
                    continue;
                }

                if (TextUtility.isTweetTruncated(tweet)) {
//                    logger.debug("Tweet is truncated: "+tweet);
                    continue;
                }
                //remove the word with hash
                tweet = TextUtility.removeHashes(tweet, hashes);
                StrTokenizer tokenizer = new StrTokenizer(tweet);
//				tokenizer.setTrimmerMatcher(StrMatcher.quoteMatcher());
                String[] words = tokenizer.getTokenArray();

                //URL filter
                Boolean url = TextUtility.checkURL(words);
                if (url) {
                    removedList.add(tweet);
//                    logger.debug("Tweet contains only URL and no other information: "+tweet);
                    continue;
                }

                //number of words in respect to the hash
                Boolean numWords = TextUtility.checkHashPosition(words, hashes);
                if (numWords) {
//                    logger.debug("Tweet contains a majority of hashes: "+tweet);
                    removedList.add(tweet);
                    continue;
                }

                String RTRemoved = TextUtility.checkRT(words);

                //final empty
                if (RTRemoved.isEmpty()) {
//                    logger.debug("Tweet is meaningless: "+tweet);
                    continue;
                }

                //only url and touser? - then remove the tweet
                if (!(TextUtility.checkURLUser(words))) {
//                    logger.debug("Tweet contains only RT and URL: "+tweet);
                    removedList.add(tweet);
                    continue;
                }

                //If the tweet is a reply to another tweet, ignore it
                if (TextUtility.isReply(words, true)) {
//                    logger.debug("Tweet is a reply to another tweet: "+tweet);
                    continue;
                }

                if (uniqList.contains(RTRemoved)) {
//                    logger.debug("Tweet is duplicated: "+tweet);
                    continue;
                } else {
                    uniqList.add(RTRemoved);
                }

                //we need at least some characters! (this is true/false)
                if (!TextUtility.checkAlphaNumeric(RTRemoved)) {
//                    logger.debug("Tweet contains non-alphanumeric characters: "+tweet);
                    removedList.add(tweet);
                    continue;
                }

                //we can control the # of training documents and we
                //can delete any tweet from training < 3 words
                if (RTRemoved.split("\\s++").length < 2) {
//                    logger.debug("Tweet has fewer than 3 words.");
                    removedList.add(tweet);
                    continue;
                }

                filteredSet.add(modifiedType + "\t" + id + "\t" + RTRemoved);

            }

            List<String> filteredList = new ArrayList<String>(filteredSet);
            java.util.Collections.shuffle(filteredList);

//			System.out.println("After filtering, we have " + filteredList.size() + " for " + file.getName()) ;
            switch (Integer.parseInt(type)) {
                case 1:
                    sarcasticTweets.addAll(filteredList);
                    break;
                case 2:
                    sentimentTweets.addAll(filteredList);
                    break;
            }

            reader.close();

            //		writeTheRemoved(removedList);
        }
        List<String> dataset = createBalancedSet(sarcasticTweets, sentimentTweets);
        logger.info("Number of tweets in another language: "+otherLangCount);
        writeData(outPath, dataset);
    }

    private List<String> createBalancedSet(List<String> sarcasticTweets, List<String> sentimentTweets) {
        logger.info("Number of sarcastic tweets: " + sarcasticTweets.size());
        logger.info("Number of sentiment tweets: " + sentimentTweets.size());
        ArrayList<String> dataset = new ArrayList<String>();
        if (sarcasticTweets.size() == sentimentTweets.size()) {
            dataset.addAll(sarcasticTweets);
            dataset.addAll(sentimentTweets);
            return dataset;
        }
        List<String> biggerList = sentimentTweets;
        List<String> smallerList = sarcasticTweets;
        if (sarcasticTweets.size() > sentimentTweets.size()) {
            biggerList = sarcasticTweets;
            smallerList = sentimentTweets;
        }
        dataset.addAll(smallerList);

        final int BALANCED_SIZE = smallerList.size();
        Random selector = new Random();
        int added = 0;
        while (added < BALANCED_SIZE) {
            int pos = selector.nextInt(BALANCED_SIZE - added);
            dataset.add(biggerList.get(pos + added));
            Collections.swap(biggerList, pos + added, added);
            added++;
        }

        Collections.shuffle(dataset);
        return dataset;
    }

    private void writeData(String path, List<String> data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
        for (String item : data) {
            writer.write(item);
            writer.newLine();
        }

        writer.close();
        logger.info("Wrote data to " + path);
    }

    public void loadFileForFilteringRandomTweet(String path) throws IOException, LangDetectException {

        File f = new File(path);

        File[] files = f.listFiles();

        BufferedReader reader = null;
        BufferedWriter writer = null;

        DetectorFactory.loadProfile(LANG_DETECTOR_PROFILE);

        Detector langDetector = DetectorFactory.create();

        for (File file : files) {

            if (file.getName().contains("Store") || file.getName().contains("new_tweet")
                    || file.getName().contains(".date")) {
                continue;
            }

            //	if(!file.getName().contains("tweet.temp"))
            //	{
            //		continue ;
            //	}

            reader = new BufferedReader(new InputStreamReader(new FileInputStream
                    (path + "/" + file.getName()), "UTF-8"));

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                    (path + "/" + file.getName() + ".08012014"), "UTF-8"));

            //the tweets collected from the tweeter has the "hash" name
            //in the file name. So just parse the name to extract the hashtag
            //and generate the type (e.g. positive/sarcasm/negative).


            //pass this hash to retrieve the list of hashes for the type
            List<String> hashes = collectHashes();

            Set<String> sarcasmSet = new HashSet<String>();
            List<String> uniqList = new ArrayList<String>();

            List<String> removedList = new ArrayList<String>();

            while (true) {
                String line = reader.readLine();
                if (null == line) {
                    break;
                }
                //378953906581037056	Sat Sep 14 14:50:26 EDT 2013	CourteneyLohman	I really like it when you don't text me back #sarcasm

                line = line.toLowerCase();

                String features[] = line.split("\t");

                if (features.length < 4) {
                    logger.warn("Tweet not formatted properly");
                    continue;
                }
                String id = features[0];
                String tweet = features[3];
                logger.info("Tweet is: " + tweet);

                //we need to filter this tweet

                //detect language
                langDetector.append(tweet);
                String language = langDetector.detect();
                if (!language.equalsIgnoreCase("en")) {
                    System.out.println("other language? " + language);
                    continue;
                }

                tweet = StringUtils.stripAccents(tweet);

                //if the tweet contains any hash - we dont need that tweet
                boolean ret = TextUtility.checkHashPresence(tweet, hashes);
                if (ret) {
//                    logger.debug("Tweet has one of the hashes in: "+hashes);
                    continue;
                }

                StrTokenizer tokenizer = new StrTokenizer(tweet);

                String[] words = tokenizer.getTokenArray();

//				logger.debug("Something else wrong with tweet");
                //URL filter
                Boolean url = TextUtility.checkURL(words);
                if (url) {
                    removedList.add(tweet);
                    continue;
                }


                String RTRemoved = TextUtility.checkRT(words);

                //final empty
                if (RTRemoved.isEmpty()) {
                    continue;
                }

                //only url and touser? - then remove the tweet
                if (!(TextUtility.checkURLUser(words))) {
                    removedList.add(tweet);
                    continue;
                }

                if (uniqList.contains(RTRemoved)) {
                    continue;
                } else {
                    uniqList.add(RTRemoved);
                }

                //we need at least some characters! (this is true/false)
                if (!TextUtility.checkAlphaNumeric(RTRemoved)) {
                    removedList.add(tweet);
                    continue;
                }

                //we can control the # of training documents and we
                //can delete any tweet from training < 3 words
                if (RTRemoved.split("\\s++").length < 2) {
                    removedList.add(tweet);
                    continue;
                }

                sarcasmSet.add("OBJECTIVE" + "\t" + id + "\t" + RTRemoved);

                if (sarcasmSet.size() == MAX_RANDOM) {
                    break;
                }

            }

            List<String> sarcasmList = new ArrayList<String>(sarcasmSet);

            java.util.Collections.shuffle(sarcasmList);

            System.out.println("After filtering, we have " + sarcasmList.size() + " for " + file.getName());

            for (String sarcasm : sarcasmList) {
                writer.write(sarcasm);
                writer.newLine();
            }

            writer.close();
            reader.close();

            //		writeTheRemoved(removedList);
        }
    }

    private void writeTheRemoved(List<String> removedList) {
        for (String remove : removedList) {
            System.out.println(remove);
        }
    }

    private List<String> collectHashes() {
        List<String> hashes = new ArrayList<String>(Arrays.asList(hashtags));
        return hashes;
    }

    private String getHash(String file) {
        List<String> hashList = new ArrayList<String>(Arrays.asList(hashtags));

        for (String hash : hashList) {
            hash = hash.substring(1, hash.length());
            if (file.contains(hash)) {
                return "#" + hash;
            }
        }

        return null;
    }

    /**
     * Uses the Detector class to detect the language of a given piece of text.
     *
     * @param tweet The tweet to detect the language for.
     * @return The language code for the detected language.
     */
    private String detectLanguage(String tweet) throws LangDetectException {
        Detector langDetector = DetectorFactory.create();
        langDetector.append(tweet);
        String lang = "";
        try {
            lang = langDetector.detect();
        }
        catch (LangDetectException lde)
        {
            lang = null;
        }
        return lang;
    }

    /**
     * @param args
     * @throws IOException
     * @throws LangDetectException
     */
    public static void main(String[] args) throws IOException, LangDetectException {

        EnglishTwitterFilter twitterObj = new EnglishTwitterFilter();
        String path = twitterObj.properties.getProperty("WEEKLY_DATA_DIR_TEMPLATE");
//		twitterObj.loadFileForFilteringRandomTweet(path) ;
        for (int i = 1; i <= 18; i++) {
            String week_path = path + Integer.toString(i);
            logger.info(week_path);
            twitterObj.loadFileForFiltering(week_path);
        }

    }

}
