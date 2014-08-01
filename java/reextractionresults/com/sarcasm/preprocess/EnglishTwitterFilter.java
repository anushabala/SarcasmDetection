package com.sarcasm.preprocess;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter ;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;

import com.sarcasm.util.TextUtility;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

public class EnglishTwitterFilter 
{
	
	private String [] hashtags = {"#sarcasm", "#sarcastic" , "#angry" , "#awful" , "#disappointed" ,
			  "#excited", "#fear" ,"#frustrated", "#grateful", "#happy" ,"#hate",
			  "#joy" , "#loved", "#love", "#lucky", "#sad", "#scared", "#stressed",
			  "#wonderful", "#positive", "#positivity", "#disappointed"} ;
	

	
	 private static final int MAX_RANDOM = 200000 ;

	public EnglishTwitterFilter()
	{
		
	}
	
	public void loadFileForFiltering( String path ) throws IOException, LangDetectException
	{
		
		File f = new File(path) ;
		
		File[] files = f.listFiles() ;
		
		BufferedReader reader = null ;
		BufferedWriter writer  = null ;
		
		DetectorFactory.loadProfile("./lib/langdetect-03-03-2014/profiles");
		 
		Detector langDetector = DetectorFactory.create();
	      
		for ( File file : files )
		{
			
			if(file.getName().contains("Store") || file.getName().contains("new_tweet"))
			{
				continue ;
			}
			 reader = new BufferedReader ( new InputStreamReader ( new FileInputStream
				 ( path + "/" + file.getName()), "UTF-8" ) );
		
			 writer = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream
				 ( path + "/" + file.getName() + ".06052014"), "UTF-8" ) );
		
			//the tweets collected from the tweeter has the "hash" name 
			 //in the file name. So just parse the name to extract the hashtag
			 //and generate the type (e.g. positive/sarcasm/negative).
			 
			String hash = getHash(file.getName());
			String type = TextUtility.getMessageType(hash);
			
			if ( null == type )
			{
				System.out.println("error in message category: check ");
				continue ;
			}
			
			//pass this hash to retrieve the list of hashes for the type
			List<String> hashes = collectHashes() ;
			
			Set<String> sarcasmSet = new HashSet<String>() ;
			List<String> uniqList = new ArrayList<String>();
			
			List<String> removedList = new ArrayList<String>(); 
			
			
			
			while ( true )
			{
				String line = reader.readLine() ;
				if ( null == line )
				{
					break;
				}
				//378953906581037056	Sat Sep 14 14:50:26 EDT 2013	CourteneyLohman	I really like it when you don't text me back #sarcasm
				
				line = line.toLowerCase() ;
				
				String features[] = line.split("\t") ;
				
				if(features.length < 2)
				{
					continue ;
				}
				String id = features[0];
				String tweet = features[1] ;
			
				//we need to filter this tweet
				
				//detect language
				langDetector.append(tweet) ;
				String language = langDetector.detect();
				if(!language.equalsIgnoreCase("en"))
				{
					System.out.println("other language? " + language) ;
					continue ;
				}
				
				tweet = StringUtils.stripAccents(tweet) ;
				
				//remove the word with hash
				tweet = TextUtility.removeHashes(tweet,hashes);
				StrTokenizer tokenizer = new StrTokenizer(tweet) ;
				
				String[] words = tokenizer.getTokenArray() ;
		
				
				//URL filter
				Boolean url = TextUtility.checkURL(words) ;
				if (url)
				{
					removedList.add(tweet);
					continue ;
				}
				
				//number of words in respect to the hash
				Boolean numWords = TextUtility.checkHashPosition(words,hashes) ;
				if ( numWords)
				{
					removedList.add(tweet);
					continue ;
				}
				
				String RTRemoved = TextUtility.checkRT(words);
				
				//final empty
				if(RTRemoved.isEmpty())
				{
					continue ;
				}
		
				//only url and touser? - then remove the tweet
				if(!(TextUtility.checkURLUser(words)))
				{
					removedList.add(tweet);
					continue ;
				}
				
				if(uniqList.contains(RTRemoved))
				{
					continue ;
				}
				else
				{
					uniqList.add(RTRemoved);
				}
				
				//we need at least some characters! (this is true/false)
				if(!TextUtility.checkAlphaNumeric(RTRemoved))
				{
					removedList.add(tweet);
					continue ;
				}
				
				//we can control the # of training documents and we 
				//can delete any tweet from training < 3 words
				if(RTRemoved.split("\\s++").length <2)
				{
					removedList.add(tweet);
					continue ;
				}
				
				sarcasmSet.add(type + "\t"+id+"\t"+RTRemoved) ;
				
			}
			
			List<String> sarcasmList = new ArrayList<String>(sarcasmSet);
			
			java.util.Collections.shuffle(sarcasmList) ;
			
			System.out.println("After filtering, we have " + sarcasmList.size() + " for " + file.getName()) ;
			
			for ( String sarcasm : sarcasmList )
			{
				writer.write(sarcasm) ;
				writer.newLine() ;
			}
			
			writer.close() ;
			reader.close();
			
	//		writeTheRemoved(removedList);
		}
	}
	
	public void loadFileForFilteringRandomTweet( String path ) throws IOException, LangDetectException
	{
		
		File f = new File(path) ;
		
		File[] files = f.listFiles() ;
		
		BufferedReader reader = null ;
		BufferedWriter writer  = null ;
		
		DetectorFactory.loadProfile("./lib/langdetect-03-03-2014/profiles");
		 
		Detector langDetector = DetectorFactory.create();
	      
		for ( File file : files )
		{
			
			if(file.getName().contains("Store") || file.getName().contains("new_tweet"))
			{
				continue ;
			}
			
		//	if(!file.getName().contains("tweet.temp"))
		//	{
		//		continue ;
		//	}
			
			 reader = new BufferedReader ( new InputStreamReader ( new FileInputStream
				 ( path + "/" + file.getName()), "UTF-8" ) );
		
			 writer = new BufferedWriter ( new OutputStreamWriter ( new FileOutputStream
				 ( path + "/" + file.getName() + ".07182014"), "UTF-8" ) );
		
			//the tweets collected from the tweeter has the "hash" name 
			 //in the file name. So just parse the name to extract the hashtag
			 //and generate the type (e.g. positive/sarcasm/negative).
			 
		
			
			//pass this hash to retrieve the list of hashes for the type
			List<String> hashes = collectHashes() ;
			
			Set<String> sarcasmSet = new HashSet<String>() ;
			List<String> uniqList = new ArrayList<String>();
			
			List<String> removedList = new ArrayList<String>(); 
			
			while ( true )
			{
				String line = reader.readLine() ;
				if ( null == line )
				{
					break;
				}
				//378953906581037056	Sat Sep 14 14:50:26 EDT 2013	CourteneyLohman	I really like it when you don't text me back #sarcasm
				
				line = line.toLowerCase() ;
				
				String features[] = line.split("\t") ;
				
				if(features.length < 2)
				{
					continue ;
				}
				String id = features[0];
				String tweet = features[3] ;
			
				//we need to filter this tweet
				
				//detect language
				langDetector.append(tweet) ;
				String language = langDetector.detect();
				if(!language.equalsIgnoreCase("en"))
				{
					System.out.println("other language? " + language) ;
					continue ;
				}
				
				tweet = StringUtils.stripAccents(tweet) ;
				
				//if the tweet contains any hash - we dont need that tweet
				boolean ret = TextUtility.checkHashPresence(tweet,hashes);
				if (ret)
				{
					continue ;
				}
				
				StrTokenizer tokenizer = new StrTokenizer(tweet) ;
				
				String[] words = tokenizer.getTokenArray() ;
		
				
				//URL filter
				Boolean url = TextUtility.checkURL(words) ;
				if (url)
				{
					removedList.add(tweet);
					continue ;
				}
				
			
				String RTRemoved = TextUtility.checkRT(words);
				
				//final empty
				if(RTRemoved.isEmpty())
				{
					continue ;
				}
		
				//only url and touser? - then remove the tweet
				if(!(TextUtility.checkURLUser(words)))
				{
					removedList.add(tweet);
					continue ;
				}
				
				if(uniqList.contains(RTRemoved))
				{
					continue ;
				}
				else
				{
					uniqList.add(RTRemoved);
				}
				
				//we need at least some characters! (this is true/false)
				if(!TextUtility.checkAlphaNumeric(RTRemoved))
				{
					removedList.add(tweet);
					continue ;
				}
				
				//we can control the # of training documents and we 
				//can delete any tweet from training < 3 words
				if(RTRemoved.split("\\s++").length <2)
				{
					removedList.add(tweet);
					continue ;
				}
				
				sarcasmSet.add("OBJECTIVE" + "\t"+id+"\t"+RTRemoved) ;
				
				if(sarcasmSet.size() == MAX_RANDOM)
				{
					break;
				}
				
			}
			
			List<String> sarcasmList = new ArrayList<String>(sarcasmSet);
			
			java.util.Collections.shuffle(sarcasmList) ;
			
			System.out.println("After filtering, we have " + sarcasmList.size() + " for " + file.getName()) ;
			
			for ( String sarcasm : sarcasmList )
			{
				writer.write(sarcasm) ;
				writer.newLine() ;
			}
			
			writer.close() ;
			reader.close();
			
	//		writeTheRemoved(removedList);
		}
	}
	
	
	
	private void writeTheRemoved(List<String> removedList) 
	{
		// TODO Auto-generated method stub
		for ( String remove : removedList )
		{
			System.out.println(remove);
		}
	}

	private List<String> collectHashes() 
	{
		// TODO Auto-generated method stub
		
		List<String> hashes = new ArrayList<String>(Arrays.asList(hashtags));
		return hashes;
	}

	private String getHash(String file) 
	{
		// TODO Auto-generated method stub
		List<String> hashList = new ArrayList<String>(Arrays.asList(hashtags)) ;
		
		for ( String hash : hashList)
		{
			hash = hash.substring(1,hash.length());
			if ( file.contains(hash))
			{
				return "#"+hash ;
			}
		}
		
		return null;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws LangDetectException 
	 */
	public static void main(String[] args) throws IOException, LangDetectException 
	{
		// TODO Auto-generated method stub
		String path = "./data/english/filtered/positive/select/" ;
		
		EnglishTwitterFilter twitterObj = new EnglishTwitterFilter() ;
		
		twitterObj.loadFileForFilteringRandomTweet(path) ;
		
	//	twitterObj.loadFileForFiltering(path) ;
	}

}
