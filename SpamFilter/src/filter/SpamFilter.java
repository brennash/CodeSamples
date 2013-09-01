package classifier.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import classifier.utils.Message;
import classifier.utils.MonitorEvent;
import classifier.utils.PerformanceMonitor;
import classifier.utils.Word;


public class SpamFilter implements PerformanceMonitor{
	
	private int hamTotal, spamTotal;			//Total HAM/SPAM messages
	private int spamMsgCount, hamMsgCount;		//Word count for each type
	private int spamSize, hamSize;				//Mean words in each type
	private int vocabularySize; 				//The num. words in spam filter
	
	//The double values to calculate probabilities
	private double probSpamTotal, probHamTotal;
	
	private HashMap<String, Word> wordList;		//The spam filter hashmap
	
	/**
	 * The constructor for the spam filter. Takes a pipe-delimited 
	 * file as input, and re-creates the hashmap linking various words
	 * to spam/ham counts. 
	 * 
	 * @param trainingData The name of the spam filter file. 
	 */
	public SpamFilter(String trainingData)
	{
		try
		{
			File file = new File(trainingData);
			
			//Check the training data file exists
			if(file.exists())
			{
				wordList = new HashMap<String, Word>();
				
				//The line count
				int lineCount = 0;
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line, header;
					
				header = reader.readLine();
				parseHeader(header);
					
				System.out.print("Loading spam filter");

				//Read the file line by line
				while ((line = reader.readLine()) != null)
				{
					parseFilter(line);
					lineCount++;
						
					if(lineCount%5000 == 0)
					{
						System.out.print(".");
					}
				}
				
				System.out.println(" finished");

				//Set the size of the spam filter vocabulary
				vocabularySize = wordList.size();	
			}
			else
			{
				System.err.println("File "+trainingData+" doesn't exist, exiting");
			}
		}
		catch (IOException ie)
		{
			ie.printStackTrace();
		}		
	}
	
	
	/**
	 * Parses the header for the spam filter file, adding the ham 
	 * and spam counts from the training file to the hamCount and 
	 * spamCount global variables. 
	 * 
	 * @param line The string giving the pipe-delimited header line. 
	 */
	private void parseHeader(String line)
	{
		//Tokenize the header line
		StringTokenizer strtok = new StringTokenizer(line, "|");
		try
		{
			String hamCountStr = strtok.nextToken();
			String spamCountStr = strtok.nextToken();
			String hamWordsStr = strtok.nextToken();
			String spamWordsStr = strtok.nextToken();
			
			//Set the ham, spam and overall totals
			hamTotal = Integer.parseInt(hamCountStr);
			spamTotal = Integer.parseInt(spamCountStr);
			hamMsgCount = Integer.parseInt(hamWordsStr);
			spamMsgCount = Integer.parseInt(spamWordsStr);
			
			//Calc. the probabilities here for re-use later
			probHamTotal = ((double) hamTotal)/((double) hamTotal+spamTotal);
			probSpamTotal = ((double) spamTotal)/((double) hamTotal+spamTotal);
			
			//The mean size of messages from the two types
			hamSize = hamMsgCount/hamTotal;
			spamSize = spamMsgCount/spamTotal;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
	
	
	/**
	 * Parses a pipe-delimited line, representing a word within the spam 
	 * filter. 
	 * 
	 * @param line A string giving the pipe-delimited line in the file.
	 */
	private void parseFilter(String line)
	{
		String delims = "\\|";		
		String[] tokens = line.split(delims);
		
		String hamString, spamString, spamicityString;
		int hamOccurrences, spamOccurrences;
		double spamicity;
		
		String word;
		Word wordObject;
				
		try
		{
			if(tokens.length != 4)
			{
				System.err.println("Line "+line+" cannot be parsed into word|ham|spam");
			}
			else
			{
				word = tokens[0];
				hamString = tokens[1];
				spamString = tokens[2];
				spamicityString = tokens[3];
				
				hamOccurrences = Integer.parseInt(hamString);
				spamOccurrences = Integer.parseInt(spamString);
				spamicity = Double.parseDouble(spamicityString);
				
				wordObject = new Word(word, hamOccurrences, spamOccurrences, spamicity);
				wordList.put(word, wordObject);
			}
		}
		catch(NumberFormatException ex)
		{
			ex.printStackTrace();
			System.err.println("Error parsing value in line: "+line);
		}
	}
	
	
	/**
	 * Returns an instantiated classifier object for the specified text. 
	 * This object allows the user to assess whether the classifier deems
	 * the text to be SPAM or HAM, and provides the level of confidence in 
	 * this estimate.
	 * 
	 * @param type The type of each message, either SPAM or HAM/
	 * @param text The raw text comprising the message (include MIME headers).
	 * @return The instantiated SPAM classifier for this message. 
	 */
	public MessageClassifier classifyMessage(String text)
	{		
		//Instantiate a MIME message object with the raw text
		Message message = new Message(text);
		
		//Get the set of cleaned, lower-case words from this message
		HashSet<String> wordSet = message.getWordSet();
		
		//Get the iterator for the string elements comprising the message
		Iterator<String> iterator = wordSet.iterator();
				
		//Consider only the most predictive spam terms per email to reduce noise
		MessageClassifier classifier = new MessageClassifier(probHamTotal, probSpamTotal);
		
		//Tick through the message words, and build the sorted set.
		while(iterator.hasNext())
		{
			Word word = this.wordList.get(iterator.next());
			if(word != null)
			{
				classifier.addWord(word);
			}
		}
		
		return classifier;
	}
	
	
	/**
	 * After reading in the evaluation dataset and run the tests on 
	 * this data using the previously built spam-filter. 
	 * 
	 * @param inputFilename The filename of the evaluation dataset. 
	 */
	public void evaluateFilter(String inputFilename, String evalOutput, double threshold)
	{
		double estSpamIsSpam = 0.0;		//True positives
		double estHamIsSpam = 0.0;		//False negatives
		
		double estSpamIsHam = 0.0;		//False positives
		double estHamIsHam = 0.0;		//True negatives
		
		int messageCount = 0;
				
		//Initialize the monitoring event for speed/memory usage
		MonitorEvent event = new MonitorEvent();		
		monitorStart(event);
		
		//Check the input file, containing the evaluation data exists
		File file = new File(inputFilename);
		
		if(!file.exists())
		{
			System.err.println("Sorry, can't open evaluation file - "+inputFilename+", exiting...");
			System.exit(0);
		}

		try
		{
			//Initialize the buffered reader to read the input file
			System.out.println("Evaluation input file "+file.getAbsolutePath());
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			
			//The string builder is used to compile messages line-by-line
			StringBuilder builder = new StringBuilder();
	
			//Variables used to record new messages and the message type (classification)
			boolean newMessage = false;
			String messageType = null;
			
			//Read from the file
			while ((line = reader.readLine()) != null)
			{	
				//If the new-message delimiter has been read
				if(line.equals("%%%%%"))
				{
					//Set the flag indicating a new message
					newMessage = true;
					
					//Process the previous message
					if(builder.length() > 0)
					{
						MessageClassifier classifier = classifyMessage(builder.toString());
						boolean spam = classifier.isSpam(threshold);
						double confidence = classifier.getConfidenceLevel();
						String type = classifier.getMessageType();
						
						if(spam && messageType.equals("HAM"))
						{
							estSpamIsHam += 1.0;
						}
						else if(spam && messageType.equals("SPAM"))
						{
							estSpamIsSpam += 1.0;
						}
						else if(!spam && messageType.equals("HAM"))
						{
							estHamIsHam += 1.0;
						}
						else if(!spam && messageType.equals("SPAM"))
						{
							estHamIsSpam += 1.0;
						}							
						
						System.out.println("Classifies Messages as "+type+" with confidence "+confidence+", actual type is "+messageType);
					}
					
					messageCount++;
				}
				//Otherwise continue reading the rest of the evaluation data
				else
				{
					if(newMessage)
					{
						messageType = line;
						builder = new StringBuilder();
						newMessage = false;
					}
					else
					{
						builder.append(line+"\n");
					}
				}
			}
	
			
			//Process the last message
			if(builder.length() > 0)
			{
				MessageClassifier classifier = classifyMessage(builder.toString());
				boolean spam = classifier.isSpam(threshold);
				double confidence = classifier.getConfidenceLevel();
				String type = classifier.getMessageType();
				
				if(spam && messageType.equals("HAM"))
				{
					estSpamIsHam += 1.0;
				}
				else if(spam && messageType.equals("SPAM"))
				{
					estSpamIsSpam += 1.0;
				}
				else if(!spam && messageType.equals("HAM"))
				{
					estHamIsHam += 1.0;
				}
				else if(!spam && messageType.equals("SPAM"))
				{
					estHamIsSpam += 1.0;
				}							
				
				System.out.println("Classifies Messages as "+type+" with confidence "+confidence+", actual type is "+messageType);
			}
	
			monitorStop(event);

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);

		//Output the true-positive/false-negative rates to build the Received Operating Characteristic
		System.out.println("\n==============================");
		System.out.println("Total Messages Evaluated:\t"+messageCount);
		double tpr = (estSpamIsSpam / (estSpamIsHam + estSpamIsSpam));
		double fpr = (estHamIsSpam / (estHamIsSpam + estHamIsHam));
		
		System.err.println("SS: "+estSpamIsSpam);
		System.err.println("HS: "+estHamIsSpam);
		System.err.println("SH: "+estSpamIsHam);
		System.err.println("HH: "+estHamIsHam);
		
		System.out.println("Threshold:"+threshold+"\t TPR: "+nf.format(tpr)+"\t FPR: "+nf.format(fpr));
		writeFile(evalOutput, threshold+"\t"+tpr+"\t"+fpr+"\n", true);
		
		System.out.print("Correct:          \t"+(estSpamIsSpam+estHamIsHam));
		System.out.print("\t("+nf.format((estSpamIsSpam+estHamIsHam)*100 / (double) messageCount)+"%)\n");
		
		System.out.print("Incorrect:        \t"+(estSpamIsHam+estHamIsSpam));
		System.out.print("\t("+nf.format((estSpamIsHam+estHamIsSpam)*100 / (double) messageCount)+"%)\n");
		
		System.out.print("False-Positives:  \t"+estHamIsSpam);
		System.out.print("\t("+nf.format(estHamIsSpam*100 / (double) messageCount)+"%)\n");		
		
		System.out.print("False-Negatives:  \t"+estSpamIsHam);
		System.out.print("\t("+nf.format(estSpamIsHam*100 / (double) messageCount)+"%)\n");
	}
	
	/**
	* writeFile() creates an XML representation of the route request
	* and writes the contents to a user-specifed file. 
	*
	* @param filename The name of the file to be written to.
	* @param data The data to be written.
	* @param append Set to true if data is appended to the file, false if not.
	*/
	public void writeFile(String filename, String data, boolean append)
	{		
		try
		{
			File file = new File(filename);
			
			if(!append && file.exists())
			{
				System.out.println("Warning - over-writing evaluation output file "+file.getAbsolutePath());
			}
			
			FileWriter out = new FileWriter(file, append);
			out.write(data);
			out.close();
		}
		catch (Exception io)
		{
			io.printStackTrace();
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




		

