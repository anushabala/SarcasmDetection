package com.sarcasm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtility 
{
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
			if ( word.startsWith("@"))
			{
				word = "ToUser" ;
			}
			if(word.contains(Markers.HTTP.toString() +"://") || word.contains(Markers.HTTPS.toString()+"://"))
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
		String [] positives = {"#excited",  "#grateful", "#happy" ,
				  "#joy" , "#loved", "#love", "#lucky", 
				  "#wonderful", "#positive", "#positivity"} ;
		
		List<String> posHashes = new ArrayList<String>(Arrays.asList(positives));
		
		
		String [] negatives = { "#angry" , "#awful" , "#disappointed" ,
				 "#fear" ,"#frustrated", "#hate",
				  "#sad", "#scared", "#stressed",
				  "#disappointed"} ;
		
		List<String> negHashes = new ArrayList<String>(Arrays.asList(negatives));
		
		
		String [] sarcasm = {"#sarcasm", "#sarcastic" } ;
		
		
		List<String> sarcHashes = new ArrayList<String>(Arrays.asList(sarcasm));
		
		String [] random = {"#random" } ;
		List<String> randomHashes = new ArrayList<String>(Arrays.asList(random));
		
		
			
		if ( posHashes.contains(hash))
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

}
