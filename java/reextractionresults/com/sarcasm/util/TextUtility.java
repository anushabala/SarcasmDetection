package com.sarcasm.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtility {

    private static Logger logger = Logger.getLogger(TextUtility.class);
    private static String[] positives = {"#excited", "#grateful", "#happy", "#joy", "#loved", "#love", "#lucky",
            "#wonderful", "#positive", "#positivity"};
    private static String[] negatives = {"#angry", "#awful", "#disappointed", "#fear", "#frustrated", "#hate",
            "#sad", "#scared", "#stressed", "#disappointed"};
    private static String[] sarcasm = {"#sarcasm", "#sarcastic"};
    private static String[] random = {"#random"};
    private static Pattern userMention = Pattern.compile("[^ \t“\"A-Za-z]@[a-zA-Z0-9]");
    private static Matcher userMentionMatcher = null;
    private enum Markers {
        rt, HTTP, HTTPS
    }

    public static int countChars(String line, char c) {
        int count = 0;
        char[] chars = line.toCharArray();

        for (char x : chars) {
            if (x == c) {
                count++;
            }
        }
        return count;
    }

    public static Boolean checkHashPosition(String[] words, String hash) {
        int total = 0;
        int index = 0;
        int hashIndex = 10000;
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (hash.equalsIgnoreCase(word.trim())) {
                hashIndex = index;
            }

            index++;
        }

        if ((hashIndex <= 2) && (index <= 2)) {
            return true;
        }

        return false;
    }

    public static Boolean checkURL(String[] words) {
        int index = 0;
        int urlIndex = Integer.MAX_VALUE;

        for (String word : words) {
            if (word.equalsIgnoreCase(Markers.rt.toString())) {
                continue;
            }

            if (word.contains(Markers.HTTP.toString().toLowerCase() + "://") ||
                    word.contains(Markers.HTTPS.toString().toLowerCase() + "://")) {
                urlIndex = index;
            }

            index++;
        }
        //a short tweet with URL in the beginning - a lot of times they are garbage
        if ((urlIndex <= 2) && (index <= 2)) {
            return true;
        }

        return false;
    }

    public static String checkRT(String[] words) {
        StringBuffer ret = new StringBuffer();

        for (String word : words) {
            if (word.equalsIgnoreCase(Markers.rt.toString())) {
                continue;
            } else if (word.startsWith("@")) {
                if (ret.length() == 0)
                    continue;
                word = "ToUser";
            } else if (word.startsWith("\"") && word.length() > 1 && word.charAt(1) == '@') {
                if (ret.length() == 0)
                    continue;
                word = "\"ToUser";
            } else if (word.contains(Markers.HTTP.toString().toLowerCase() + "://") ||
                    word.contains(Markers.HTTPS.toString().toLowerCase() + "://")) {
                word = "URL";
            }
            ret.append(word);
            ret.append(" ");
        }

        return ret.toString().trim();
    }


    public static Boolean hashFilter(String[] words, List<String> hashes) {
        for (String word : words) {
            for (String hash : hashes) {
                if (word.contains(hash)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Boolean hashFilter(String[] words, String hash) {
        for (String word : words) {
            if (word.contains(hash)) {
                return true;
            }

        }
        return false;
    }

    public static Boolean checkHashPosition(String[] words, List<String> hashes) {
        int index = 0;
        int hashIndex = Integer.MAX_VALUE;
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            for (String hash : hashes) {
                if (hash.equalsIgnoreCase(word.trim())) {
                    if (!(index > hashIndex)) {
                        hashIndex = index;
                    }
                }
            }

            index++;
        }

        if ((hashIndex <= 2) && (index <= 2)) {
            return true;
        }

        return false;
    }

    public static String removeHashes(String tweets, List<String> hashes) {
        for (String hash : hashes) {
            tweets = tweets.replace(hash, "");
        }

        return tweets;
    }

    public static String removeHashes(String tweets, String hash) {
        tweets = tweets.replace(hash, "");
        return tweets;
    }

    public static String getMessageType(String hash) {

        List<String> posHashes = new ArrayList<String>(Arrays.asList(positives));
        List<String> negHashes = new ArrayList<String>(Arrays.asList(negatives));
        List<String> sarcHashes = new ArrayList<String>(Arrays.asList(sarcasm));
        List<String> randomHashes = new ArrayList<String>(Arrays.asList(random));

        if (posHashes.contains(hash)) {
            return "2";
        } else if (negHashes.contains(hash)) {
            return "3";
        } else if (sarcHashes.contains(hash)) {
            return "1";
        } else if (randomHashes.contains(hash)) {
            return "2";
        }

        return null;
    }

    public static boolean checkURLUser(String[] tokens) {

        for (String word : tokens) {
            if (!(word.contains("ToUser")) || (word.contains("URL"))) {
                return true;
            }

        }
        return false;
    }

    public static boolean checkAlphaNumeric(String rTRemoved) {
        char[] chars = rTRemoved.toCharArray();
        for (char c : chars) {
            if (Character.isAlphabetic(c)) {
                return true;
            }
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkHashPresence(String tweet, List<String> hashes) {
        for (String hash : hashes) {
            if (tweet.contains(hash)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the tweet is a reply/response to another tweet, we want to discard it because the original tweet might contain
     * the sarcasm rather than the new tweet, and it would be difficult to separate the two since the formatting isn't
     * consistent.
     *
     * @param words            All the tokens in the tweet
     * @return true if the tweet is a reply to another tweet, false otherwise.
     */
    public static boolean isReply(String[] words) {
        // Tweets that are replies usually contain the pattern "@ or "ToUser (depending on whether user mentions in the
        // tweet have been replaced by ToUser or not. If this pattern is found, then the tweet is a reply.
        String search = "\"@";
        for (String word : words) {
            if (word.trim().contains(search))
                return true;
        }
        return false;
    }

    /**
     * Checks whether the hashtags present in the tweet are all consistent with the original inferred type of the tweet
     * or not.
     *
     * @param tweet All the tokens in the tweet
     * @param type  The type of the tweet (as inferred from the filename of the file it was extracted from)
     * @return true if the hashtags in the tweet are consistent with type, false otherwise.
     */
    public static boolean hashesConsistent(String tweet, String type) {
        int hashPos = tweet.indexOf('#');
        if (hashPos < 0)
            return false; //tweet should contain at least one hashtag pertaining to the message type
        while (hashPos > 0) {
            int end = tweet.indexOf(' ', hashPos);
            if (end < 0)
                end = tweet.length() - 1;
            String word = tweet.substring(hashPos, end);
            if (word.length() == 0)
                return false; //tweet has an empty hashtag, so it was probably truncated while getting it from Twitter
            if (word.charAt(0) == '#') {
                // If a hashtag is found and it's of a different type than the tweet type, return false.
                String hashType = getMessageType(word);
                if (hashType != null && !hashType.equals(type))
                    return false;
            }
            hashPos = tweet.indexOf('#', end);
        }
        return true;
    }

    /**
     * Replace all occurrences of curved double quotes characters with regular double quotes.
     *
     * @param tweet The tweet to be filtered.
     * @return The tweet with quotes replaced.
     */
    public static String replaceDoubleQuotes(String tweet) {
        tweet = StringUtils.replace(tweet, "”", "\"");
        return StringUtils.replace(tweet, "“", "\"");
    }

    /**
     * Returns true if the tweet ends with the ellipsis character, indicating that it was truncated while getting it
     * from the Twitter API.
     *
     * @param tweet The tweet to be checked
     * @return true if the tweet appears truncated, false otherwise.
     */
    public static boolean isTweetTruncated(String tweet) {
        tweet = tweet.trim();
        return StringUtils.endsWith(tweet, "…");
    }

    /**
     * Finds any user mentions that are embedded within (or joined with) other words in a tweet, and separates them so
     * that user mention detection is effective. For example, if a user mention occurs as the token "&@someusername",
     * the method separates the string into two tokens, yielding "& @someusername".
     * Note: This method only finds user mentions that are preceded by special characters, not those that are preceded by letters. This is done to avoid flagging other uses of @ as user mentions (e.g. email addresses).
     * @param tweet the tweet for which user mentions need to be reformatted
     * @return The reformatted tweet
     */
    public static String reformatUserMentions(String tweet) throws StringIndexOutOfBoundsException
    {
        StringBuilder reformattedTweet = new StringBuilder();
        tweet = tweet.trim()+' ';
        int prevEnd = 0;
        if(userMentionMatcher==null)
            userMentionMatcher = userMention.matcher(tweet);
        else
            userMentionMatcher.reset(tweet);
        while (userMentionMatcher.find())
        {
            int start = userMentionMatcher.start();
            int mentionStart = start+1;

            int mentionEnd = tweet.indexOf(' ',userMentionMatcher.end());
            if(mentionEnd<0)
                mentionEnd = tweet.length();

            String prevWord = tweet.substring(prevEnd, mentionStart);
            if(prevWord.length()>0)
                reformattedTweet.append(prevWord).append(" ");

            String mention = tweet.substring(mentionStart, mentionEnd);
            if(mention.length()>0)
                reformattedTweet.append(mention).append(" ");
            prevEnd = mentionEnd+1;
        }
        if(prevEnd<tweet.length())
            reformattedTweet.append(tweet.substring(prevEnd));

        return reformattedTweet.toString().trim();
    }

    /**
     * Strips all punctuation (non-alphanumeric characters) from a word and returns the word.
     * @param word The word to be modified
     * @return The word, stripped of all punctuation
     */
    public static String stripPunctuation(String word)
    {
        word = word.replaceAll("[^a-zA-Z0-9]", "");
        return word;
    }

}
