/**
 * The Message class is used to represent the MIME-type messages found in corpus.txt. This class provides a number of 
 * generic methods to extract and format the MIME headers, text content and email attachments present in some of the 
 * messages. In addition, multipart MIME messages are parsed, and the body text concatenated for later analysis. 
 * This class relies on the JavaMail libraries, which are bundled within the attached lib sub-directory. 
 * 
 *  @author Shane Brennan
 *  @date 18th July 2011
 *  @version 1.0
 */

package classifier.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class Message {

	//The header details in the MIME message
	private String sender; 
	private String subject;
	private String contentType, attachment;
	
	//The number of words in this message (including duplicates)
	private int wordCount;

	//Stores the words as objects in a Hash set (no duplicates)
	private HashSet<String> wordSet;
	
	/**
	 * The constructor takes a raw text file including the MIME headers and 
	 * the message type, i.e., "SPAM" or "HAM". The message class is simply a 
	 * data object, used both to read and parse raw MIME-formatted text Strings, 
	 * and then hold the resulting state as variables, e.g., sending, subject, etc.
	 *
	 * @param rawText The raw text of the email, with the MIME headers, attachements, etc. included. 
	 */
	public Message(String rawText)
	{		
		//Initialize the email fields as blank strings
		sender = "";
		subject = "";
		contentType = "";
		attachment = "";
		
		//Initialize the set containing all the words
		wordSet = new HashSet<String>();

		MimeMessage message = null;
		
		try
		{
			Properties props = new Properties();		
			props.put("mail.imaps.partialfetch", false);
			Session mailSession = Session.getDefaultInstance(props, null);
			InputStream source = new ByteArrayInputStream(rawText.getBytes());
			message = new MimeMessage(mailSession, source);
			
			contentType = message.getContentType();
			
			if(message.getFrom()[0] != null)
			{
				sender = message.getFrom()[0].toString();
			}
			
			subject = message.getSubject();
	
			if(contentType.contains("text/")) 
			{
				parseText(message, rawText);
			}
			else if(contentType.contains("multipart/"))
			{
				parseMultiPart(message, rawText);
			}
			else
			{
				parseOther(message, rawText);
			}
		}
		catch(Exception ex)
		{
			parseOther(message, rawText);
		}
	}
	
	/**
	 * Parses multi-part messages. Any attachments to the messages have their
	 * filenames included in the body text. The function is declared private, as it 
	 * is only called from within the Message object constructor. 
	 * 
	 * @param message The MIME message object. 
	 * @param type The type of message, e.g., SPAM or HAM.
	 * @param rawText The raw text of the message including MIME headers.
	 */
	private void parseMultiPart(MimeMessage message, String rawText)
	{
		try
		{
			Multipart multipart = (Multipart) message.getContent();
			String bodyText = sender.replaceAll("([^\\w])", " ");
			
			if(subject.length() > 0)
			{
				bodyText = bodyText.concat(" "+subject+" ");
			}
			
			for (int i=0; i < multipart.getCount(); i++)
			{
				  BodyPart bodyPart = multipart.getBodyPart(i);
	
				  if(bodyPart.getContentType().contains("text/"))
				  {
					  bodyText = bodyText.concat(bodyPart.getContent().toString());
				  }
				  else
				  {
					  bodyText = bodyText.concat(bodyPart.getContentType());
				  }
			}
			
			bodyText = cleanText(bodyText);
			setWordCount(rawText);
			bodyText = bodyText.toLowerCase();
			wordSet = getUniqueWords(bodyText);
		}
		catch(Exception ex)
		{
			parseOther(message, rawText);
		}
	}
	
	private void parseText(MimeMessage message, String rawText)
	{
		try
		{
			String bodyText = sender.replaceAll("([^\\w])", " ");
			
			if(subject.length() > 0)
			{
				bodyText = bodyText.concat(" "+subject+" ");
			}
			
			bodyText = bodyText.concat(message.getContent().toString());
			bodyText = cleanText(bodyText);
			setWordCount(rawText);
			bodyText = bodyText.toLowerCase();
			wordSet = getUniqueWords(bodyText);
		}
		catch(Exception ex)
		{
			parseOther(message, rawText);
		}
	}

	/**
	 * The parseOther function is called when an error occurs parsing the raw text 
	 * as a MIME type message. This requires the message to be parsed using some simple
	 * parsing rules to strip out unwanted header fields and reveal the body text.
	 * 
	 * @param message The incorrectly instantiated MIME message.
	 * @param type The message type, i.e., SPAM or HAM.
	 * @param rawText The raw string containing the email headers and text.
	 */
	private void parseOther(MimeMessage message, String rawText)
	{
		String bodyText = parseRawText(rawText);
		bodyText = cleanText(bodyText);
		setWordCount(rawText);
		bodyText = bodyText.toLowerCase();
		wordSet = getUniqueWords(bodyText);
	}
	
	/**
	 * This function cleans the raw email message for parsing and analysis. 
	 * 
	 * @param rawText The raw email message including all the headers and unformatted text.
	 * @return A string with MIME headers, CSS fields, HEX, and HTML formatting removed.
	 */
	private String parseRawText(String rawText)
	{
		String delims = "\\r?\\n";		
		String[] lines = rawText.split(delims);
		
		StringBuilder builder = new StringBuilder();
		
		//Add the email sender to the text
		builder.append(sender.replaceAll("([^\\w])", " ")+"\n");

		
		boolean skipLines = true;
		
		Whitelist list = new Whitelist();
		list.addTags("img");
		list.addTags("a");
		list.addTags("b");
		list.addTags("p");
		list.addTags("ul");
		list.addTags("em");
		list.addTags("strong");
		list.addTags("li");
		list.addTags("h1");
		list.addTags("h2");
		list.addTags("h3");
		
		for(int i=0; i<lines.length; i++)
		{
			//Discards all lines of text that look like MIME headers, Hex strings, multipart-splits or CSS headers. 
			if( (!lines[i].matches("^([a-zA-Z]+)(-*)(.*):(.*)")) && (lines[i].indexOf(" ") != -1) && 
					(!lines[i].startsWith("-----")) && (!lines[i].startsWith(".")) && (!lines[i].startsWith("#"))) {
					
				//Check if the remaining line text looks like HTML, if so, clean it.
				if(lines[i].matches("(.*)<([a-zA-Z]+)>(.*)"))
				{
					builder.append(Jsoup.clean(lines[i], list));
				}
				//Otherwise concatenate the text to the string builder
				else
				{
					builder.append(lines[i]+"\n");
				}
			}
		}
		
		//Return the cleaned text.
		return builder.toString();
	}
	
	/**
	 * Cleans up the text of some additional html tags, as well as 
	 * removes punctuation marks, numbers and any other symbols existing
	 * within the text String.
	 * 
	 * @param text The current text String.
	 * @return A String value cleared of any non-word tags. 
	 */
	private String cleanText(String text)
	{		
		String cleanText = text.replaceAll("[^\\p{L}\\p{N}]", " ");
		cleanText = cleanText.replaceAll("[0-9]", " ");		

		return cleanText;
	}
	
	/**
	 * Gets the unique words in a given text string. This is useful in 
	 * that it cuts down on later parsing for common words during the 
	 * spam training stage. 
	 * 
	 * @param text The text string containing duplicate words.
	 * @return A new-line delimited string containing unique words. 
	 */
	public HashSet<String> getUniqueWords(String text)
	{	
		String delims = " ";		
		String[] tokens = text.split(delims);
		
		for(int i=0; i<tokens.length; i++)
		{
			if((tokens[i].length()>2)&&(tokens[i].length()<16))
			{
				wordSet.add(tokens[i]);
			}
		}
		
		return wordSet;
	}
	
	
	public String getSender()
	{
		return sender;
	}
	
	/**
	 * This function returns a string listing the domain associated 
	 * with the message. For instance, if the sender was joe.blogs@email.com, 
	 * this function would return 'com' as the domain. 
	 * 
	 * @return The domain associated with the sender of the email.
	 */
	public String getSenderDomain()
	{
		if(sender != null)
		{
			int startIndex = sender.lastIndexOf(".");
			int endIndex = sender.length();
			
			if((startIndex != -1)&&(startIndex < endIndex))
			{
				return sender.substring(startIndex+1, endIndex);
			}
		}
		
		return "";
	}

	/**
	 * Sets the word count value using the message body text. This 
	 * should have been cleaned, by removing MIME headers and HTML tags. 
	 * 
	 * @param text The String giving the (cleaned) message body text. 
	 */
	public void setWordCount(String text)
	{
		//Split the text into discrete words. 
		String delims = "\\s";
		
		//Count the number of these words
		String[] words = text.split(delims);
		wordCount = words.length;
	}
	
	/**
	 * Returns the number of words, i.e., those separated by whitespace, 
	 * in the raw message text. Duplicate words are counted, but HTML tags
	 * and MIME headers are excluded from the total. 
	 * 
	 * @return The number of words in the message body text.
	 */
	public int getWordCount()
	{
		return wordCount;
	}
	
	public String getContentType()
	{
		return contentType;
	}
	
	public HashSet<String> getWordSet()
	{
		return wordSet;
	}
	
	public int getNumWords()
	{
		return wordSet.size();
	}
}
