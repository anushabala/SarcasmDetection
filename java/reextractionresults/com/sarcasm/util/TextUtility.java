package com.sarcasm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtility 
{
	private static String[] positives = {"#excited",  "#grateful", "#happy" , "#joy" , "#loved", "#love", "#lucky",
            "#wonderful", "#positive", "#positivity"};
    private static String[] negatives = { "#angry" , "#awful" , "#disappointed" , "#fear" ,"#frustrated", "#hate",
            "#sad", "#scared", "#stressed", "#disappointed"} ;
    private static String [] sarcasm = {"#sarcasm", "#sarcastic"};
    private static String[] random = {"#random"};
    private enum Markers
	{
		rt, HTTP, HTTPS
	}
	public static int countChars(String line, char c) 
	{
		// TODO Auto-generated method stub
		int count =  0 ;
		char[] chars = line.toCharArray() ;
		
		for ( char x : chars )
		{
			if(x == c)
			{
				count++ ;
			}
		}
		return count;
	}
	
	public static Boolean checkHashPosition ( String[] words, String hash )
	{
		int total = 0 ;
		int index = 0 ;
		int hashIndex = 10000 ; 
		for (String word : words )
		{
			if(word.isEmpty())
			{
				continue ;
			}
			if(hash.equalsIgnoreCase(word.trim()))
			{
				hashIndex = index ;
			}
			
			index++ ;
		}
		
		if( ( hashIndex <=2 ) && (index <=2) )
		{
			return true ;
		}
		
		return false ;
	}

	public static Boolean checkURL(String[] words) 
	{
		// TODO Auto-generated method stub
		int index = 0 ;
		int urlIndex = Integer.MAX_VALUE ;
				
		for ( String word : words )
		{
			if(word.equalsIgnoreCase(Markers.rt.toString()))
			{
				continue ;
			}
			
			if(word.contains(Markers.HTTP.toString() +"://") || word.contains(Markers.HTTPS.toString()+"://"))
			{
				urlIndex = index ;
			}
			
			index++ ;
		}
		
		//a short tweet with URL in the beginning - a lot of times they are garbage
		if( (urlIndex <=2 ) && (index <=2  ) )
		{
			return true  ;
		}
		
		return false ;
	}
	public static String checkRT(String[] words) 
	{
		// TODO Auto-generated method stub
		StringBuffer ret = new StringBuffer() ;
		
		for ( String word : words )
		{
			if(word.equalsIgnoreCase(Markers.rt.toString()))
			{
				continue ;
			}
			else if (word.startsWith("@"))
			{
				if(ret.length()==0)
                    continue;
                word = "ToUser" ;
			}
            else if(word.startsWith("\"") && word.length()>1 && word.charAt(1)=='@')
            {
                if(ret.length()==0)
                    continue;
                word = "\"ToUser";
            }
			else if(word.contains(Markers.HTTP.toString() +"://") || word.contains(Markers.HTTPS.toString()+"://"))
			{
				word = "URL" ;
			}
			ret.append(word);
			ret.append(" ") ;
		}
		
		return ret.toString().trim() ;
	}


	public static Boolean hashFilter(String[] words, List<String> hashes) 
	{
		// TODO Auto-generated method stub
		for ( String word : words )
		{
			for ( String hash : hashes )
			{
				if(word.contains(hash))
				{
					return true ;
				}
			}
		}
		return false;
	}
	
	public static Boolean hashFilter(String[] words, String hash) 
	{
		// TODO Auto-generated method stub
		for ( String word : words )
		{
			if(word.contains(hash))
			{
				return true ;
			}
			
		}
		return false;
	}

	public static Boolean checkHashPosition(String[] words, List<String> hashes) 
	{
		// TODO Auto-generated method stub
		int index = 0 ;
		int hashIndex = Integer.MAX_VALUE ; 
		for (String word : words )
		{
			if(word.isEmpty())
			{
				continue ;
			}
			for ( String hash : hashes )
			{
				if(hash.equalsIgnoreCase(word.trim()))
				{
					if(!(index > hashIndex))
					{
						hashIndex = index ;
					}
				}
			}
			
			index++ ;
		}
		
		if( ( hashIndex <=2 ) && (index <=2) )
		{
			return true ;
		}
		
		return false ;
	}

	public static String removeHashes(String tweets, List<String> hashes) 
	{
		// TODO Auto-generated method stub
		for ( String hash : hashes )
		{
			tweets = tweets.replace(hash, "");
		}
		
		return tweets ;
	}
	
	public static String removeHashes(String tweets, String hash) 
	{
		// TODO Auto-generated method stub
		tweets = tweets.replace(hash, "");
		return tweets ;
	}

	public static String getMessageType(String hash) 
	{
		// TODO Auto-generated method stub

		List<String> posHashes = new ArrayList<String>(Arrays.asList(positives));
		List<String> negHashes = new ArrayList<String>(Arrays.asList(negatives));
		List<String> sarcHashes = new ArrayList<String>(Arrays.asList(sarcasm));
		List<String> randomHashes = new ArrayList<String>(Arrays.asList(random));

		if (posHashes.contains(hash))
		{
			return "2" ;
		}
		else if ( negHashes.contains(hash))
		{
			return "3" ;
		}
		else if ( sarcHashes.contains(hash))
		{
			return "1" ;
		}
		else if ( randomHashes.contains(hash))
		{
			return "2" ;
		}
		
		return null;
	}

	public static boolean  checkURLUser(String[] tokens) 
	{
		// TODO Auto-generated method stub
		
		for ( String word : tokens )
		{
			if ( !(word.contains("ToUser")) || (word.contains("URL")) )
			{
				return true ;
			}
			
		}
		return false;
	}

	public static boolean checkAlphaNumeric(String rTRemoved) 
	{
		// TODO Auto-generated method stub
		char[] chars = rTRemoved.toCharArray() ;
		for ( char c : chars )
		{
			if(Character.isAlphabetic(c))
			{
				return true ;
			}
			if(Character.isDigit(c))
			{
				return true ;
			}
		}
		return false ;
	}

	public static boolean checkHashPresence(String tweet, List<String> hashes) 
	{
		// TODO Auto-generated method stub
		for ( String hash : hashes )
		{
			if(tweet.contains(hash))
			{
				return true ;
			}
		}
		return false;
	}

    /**
     * If the tweet is a reply/response to another tweet, we want to discard it because the original tweet might contain
     * the sarcasm rather than the new tweet, and it would be difficult to separate the two since the formatting isn't
     * consistent.
     * @param words All the tokens in the tweet
     * @param mentionsReplaced set to true if all the user mentions have already been replaced by ToUser, false otherwise
     * @return true if the tweet is a reply to another tweet, false otherwise.
     */
    public static boolean isReply(String[] words, boolean mentionsReplaced) {
        // Tweets that are replies usually contain the pattern "@ or "ToUser (depending on whether user mentions in the
        // tweet have been replaced by ToUser or not. If this pattern is found, then the tweet is a reply.
        String search = "\"@";
        if(!mentionsReplaced)
            search = "\"@";
        for(String word: words)
        {
            if(word.trim().startsWith(search))
                return true;
        }
        return false;
    }

    /**
     * Checks whether the hashtags present in the tweet are all consistent with the original inferred type of the tweet
     * or not.
     * @param tweet All the tokens in the tweet
     * @param type The type of the tweet (as inferred from the filename of the file it was extracted from)
     * @return true if the hashtags in the tweet are consistent with type, false otherwise.
     */
    public static boolean hashesConsistent(String tweet, String type)
    {
        int hashPos = tweet.indexOf('#');
        if(hashPos==-1)
            return false; //tweet should contain at least one hashtag pertaining to the message type
        while (hashPos>0)
        {
            int end = tweet.indexOf(' ', hashPos);
            if(end<0)
                end = tweet.length()-1;
            String word = tweet.substring(hashPos, end);
            if(word.charAt(0)=='#')
            {
                // If a hashtag is found and it's of a different type than the tweet type, return false.
                String hashType = getMessageType(word);
                if(hashType!=null && !hashType.equals(type))
                    return false;
            }
            hashPos = tweet.indexOf('#', end);
        }
        return true;
    }
}
