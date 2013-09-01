package classifier.filter;

/**
 * This is the main class of the spam parser as defined within this
 * spam classifier. The parser uses a Naive Bayes classifier to identify
 * whether messages are determined to be spame or not (i.e., ham messages), 
 * based on an existing collection of pre-categorized data. 
 * 
 * The creation of the spam filter and the application of this filter to 
 * other email messages are separate. The following are the classes that
 * make up the codebase for this project:
 * 
 * BuildFilter 			The builder class, used to parse the messages and 
 *  					create the spam filter (training) data file.
 *
 * SpamFilter 			The class to hold the instance of the SPAM filter.
 * 
 * MessageClassifier	The Naive Bayes classifier implementation.
 * 
 * Message				An object to parse raw email text as a MIME message.
 * 
 * Word					Data object used to store word counts and incident of spam (spamicity).
 * 
 * MonitorEvent			A user event that records time and memory consumption.
 * 
 * PerformanceMonitor	An interface for the MonitorEvent class. 
 * 
 * 
 * @author Shane Brennan
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import classifier.utils.Message;
import classifier.utils.MonitorEvent;
import classifier.utils.PerformanceMonitor;
import classifier.utils.Word;


public class BuildFilter implements PerformanceMonitor
{	
	//The hashmap storing all the words
	//private ConcurrentHashMap<String, Word> wordList;			
	private HashMap<String, Word> wordList;			

	
	//Count the total number of HAM/SPAM messages respectively
	private int spamTotal, hamTotal, messageCount;
	
	//Count the number of words within HAM and SPAM messages;
	private int spamWordCount, hamWordCount;
	
	//Event monitor for performance analysis. 
	private MonitorEvent monitorEvent;						
	
	/**
	 * The BuildFilter class constructs a spam filter, by finding the 
	 * total occurrences of words within SPAM/HAM messages. These totals,
	 * along with the spamicity of each word, is saved by default to a 
	 * file called spam-filter.dat in the working directory.
	 * 
	 * @param inputFilename The name of the input training data (e.g., corpus.txt).
	 */
	public BuildFilter(String inputFilename)
	{
		//Set the total and SPAM/HAM counters
		messageCount = 0;
		spamTotal = 0;
		hamTotal = 0;
		
		//Sets the word count for the message types
		spamWordCount = 0;
		hamWordCount = 0;
		
		//Initialize the word list
		//wordList = new ConcurrentHashMap<String, Word>();
		wordList = new HashMap<String, Word>();
		
		//Setup the performance monitor event
		monitorEvent = new MonitorEvent();
		
		//Read the input file
		System.out.println("Processing file: "+inputFilename);			
		File file = new File(inputFilename);
		
		//If the file exists, parse it and build the spam filter
		if(file.exists())
		{
			//Parse the training file, creating the hashmap
			monitorStart(monitorEvent);
			parseTrainingFile(file);
			monitorStop(monitorEvent);
			
			//Save this hashmap to file
			saveSpamFilter("spam-filter.dat");
		}
		else
		{
			//If the training file does not exist, exit. 
			System.err.println("The file "+file.getAbsolutePath()+" could not be found, exiting...");
		}
	}

	/**
	 * Alternate constructor that specifies the input file containing the 
	 * training data (corpus.txt) and the output file to save the created
	 * SPAM filter. 
	 * 
	 * @param inputFilename The name of the input training data (e.g., corpus.txt).
	 * @param outputFilename The name of the output spam filter, by default spam-filter.dat.
	 */
	public BuildFilter(String inputFilename, String outputFilename)
	{
		//Set the total and SPAM/HAM counters
		messageCount = 0;
		spamTotal = 0;
		hamTotal = 0;
		
		//Sets the word count for the message types
		spamWordCount = 0;
		hamWordCount = 0;
		
		//Initialize the word list
		//wordList = new ConcurrentHashMap<String, Word>();
		wordList = new HashMap<String, Word>();
		
		//Setup the performance monitor event
		monitorEvent = new MonitorEvent();
		
		//Read the input file
		System.out.println("Processing file: "+inputFilename);			
		File file = new File(inputFilename);
		
		//If the file exists, parse it and build the spam filter
		if(file.exists())
		{
			//Parse the training file, creating the hashmap
			monitorStart(monitorEvent);
			parseTrainingFile(file);
			monitorStop(monitorEvent);
			
			//Save this hashmap to file
			saveSpamFilter(outputFilename);
		}
		else
		{
			//If the training file does not exist, exit. 
			System.err.println("The file "+file.getAbsolutePath()+" could not be found, exiting...");
		}
	}
	
	/**
	 * This function takes a file object pointing to the training data file, 
	 * reads the file line-by-line and builds up each email message for parsing. 
	 * 
	 * @param file The file object pointing to the training data file. 
	 */
	private void parseTrainingFile(File file)
	{	
		//The string builder is used to compile messages line-by-line
		StringBuilder builder = new StringBuilder();
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			
			boolean newMessage = false;
			String messageType = null;
			
			//Read the file line by line
			while ((line = reader.readLine()) != null)
			{
				//If the new-message delimiter has been read
				if(line.equals("%%%%%"))
				{
					//Set the flag indicating a new message
					newMessage = true;
					
					//Do the processing here
					if(builder.length() > 0)
					{				
						processMessage(messageType, builder.toString());
					}
					
					messageCount++;
					
					if(messageCount%2500 == 0)
					{
						System.out.println("Parsed "+messageCount+" messages");
						System.gc();
					}
				}
				//Otherwise continue reading the rest of the message
				else
				{
					if(newMessage)
					{
						messageType = line;
						builder = new StringBuilder();
						newMessage = false;
						updateTotals(messageType);
					}
					else
					{
						builder.append(line+"\n");
					}
				}								
			}	

			//Process the final message
			if(builder.length() > 0)
			{
				processMessage(messageType, builder.toString());   
			}		
		}
		catch (IOException ie)
		{
			ie.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * This function builds the email message using the JavaMail APIs, 
	 * and extracts the body text from the raw data. The set of unique 
	 * words within each message are stored individually within a Hashmap.
	 * This Hashmap forms the basis for the SPAM filter. 
	 * 
	 * @param messageType The message type, either "SPAM" or "HAM"
	 * @param rawText The raw email text, including the MIME headers. 
	 */
	private void processMessage(String messageType, String rawText)
	{	
		//Instantiate a MIME message object with the raw text
		Message message = new Message(rawText);
		
		//Records the number of words in each type of message
		updateWordCount(messageType, message.getWordCount());
		
		//Get the set of cleaned, lower-case words from this message
		HashSet<String> wordSet = message.getWordSet();
		
		//Get the iterator for this set
		Iterator<String> iterator = wordSet.iterator();
		String word;
		Word wordObject;
		
		//Iterate through each word in the set, adding or updating the hashmap
		while(iterator.hasNext())
		{
			//Get the word, and check if it already exists in the hashmap
			word = (String) iterator.next();			
			wordObject = wordList.get(word);
			
			//If not add it to the hashmap
			if(wordObject != null)
			{
				wordObject.add(messageType);
				wordList.put(word, wordObject);
			}
			//Otherwise increment the counters for SPAM/HAM for this word
			else
			{
				wordObject = new Word(word, messageType);
				wordList.put(word, wordObject);
			}
		}	
	}
	
	
	/**
	 * Writes the hash map out to file. This output file has the format, 
	 * word|<# ham occurrences>|<# spam occurrences>
	 * @param outputFile The output file to write the spam filter/hash map.
	 */
	private void saveSpamFilter(String outputFile)
	{
		System.out.println("Writing to file: "+outputFile);
		int filterSize = 0;
		
		try
		{
			File file = new File(outputFile);
			FileWriter out = new FileWriter(file);
			
			Set<String> keySet = wordList.keySet();
			Iterator<String> iterator = keySet.iterator();
			
			Word word;
			
			//Write a header giving the total spam/ham messages, and their word count
			out.write(hamTotal+"|"+spamTotal+"|"+hamWordCount+"|"+spamWordCount+"\n");
			
			//Include only words that occur more than three times
			//but less than one-in-every three messages (i.e., too frequent)
			int minThreshold = 3;
			int maxThreshold = messageCount/3; 
			
			//Calculate the spamicity of each word, and exclude those around 0.5
			double spamicity;
			
			//Used to format the spamicity value for writing to file
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(8);
			nf.setMinimumFractionDigits(2);
			
			//Iterate though the word list, saving any words that 
			//occur 3 or more times
			while(iterator.hasNext())
			{
				word = wordList.get(iterator.next());
				
				spamicity = ((double) word.getSpamCount()) / ( (double) word.getHamCount()+word.getSpamCount());
				
				//Exclude words with a spamicity near to 0.5
				if((spamicity < 0.45)||(spamicity > 0.55))
				{
					//Exclude infrequent or overly frequent words
					if((word.getFrequency() >= minThreshold)&&(word.getFrequency() < maxThreshold))
					{
						out.write(word.getWord()+"|"+word.getHamCount()+"|"
								+word.getSpamCount()+"|"+nf.format(spamicity)+"\n");
						
						filterSize++;
					}
				}
			}
			
			out.close();

			System.out.println("Wrote: "+filterSize+" words to "+outputFile);

		}
		catch (IOException io)
		{
			io.printStackTrace();
		}	
	}

	
	/**
	 * Function used to update the SPAM/HAM message totals whenever a 
	 * new message is read by this parser. 
	 *  
	 * @param messageType The type of message, either "SPAM" or "HAM".
	 */
	private void updateTotals(String messageType)
	{
		if(messageType.equals("SPAM"))
		{
			spamTotal++;
		}
		else if(messageType.equals("HAM"))
		{
			hamTotal++;
		}
		else
		{
			System.err.println("Error categorizing "+messageType+" type");
		}
	}
	
	
	/**
	 * Function used to update the SPAM/HAM message totals whenever a 
	 * new message is read by this parser. 
	 *  
	 * @param messageType The type of message, either "SPAM" or "HAM".
	 */
	private void updateWordCount(String messageType, int wordCount)
	{		
		if(messageType.equals("SPAM"))
		{
			spamWordCount += wordCount;
		}
		else if(messageType.equals("HAM"))
		{
			hamWordCount += wordCount;
		}
		else
		{
			System.err.println("Error updating "+messageType+" type");
		}
	}
	
	/**
	 * Function used to monitor performance
	 */
	public void monitorStart(MonitorEvent event)
	{
		event.start();
	}
	
	/**
	 * Function used to monitor performance
	 */
	public void monitorStop(MonitorEvent event)
	{
		event.stop();
		System.out.println(event.toString());
	}
}
