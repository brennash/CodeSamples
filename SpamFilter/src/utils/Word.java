package classifier.utils;

/**
 * Simple data object used to represent a word object. 
 * Maintains a count of the incidence of the word with 
 * spam and non-spam messages. 
 * 
 * @author Shane Brennan
 * @date 22nd July 2011
 */

public class Word {

	//The word itself
	private String word;
	
	//The count of the number of times its occurs in SPAM/HAM
	private int spamCount, hamCount;
	
	//The spamicity of the word, after training the filter
	private double spamicity;
	
	public Word(String word, String messageType)
	{
		this.word = word;
		spamCount = 0;
		hamCount = 0;	
		add(messageType);
	}
	
	public Word(String word, int hamCount, int spamCount, double spamicity)
	{
		this.word = word;
		this.spamCount = spamCount;
		this.hamCount = hamCount;	
		this.spamicity = spamicity;
	}
	
	/**
	 * Selectively increments the HAM/SPAM counters 
	 * depending on the message 'type'.
	 * 
	 * @param type The message type, either 'SPAM' or 'HAM'
	 */
	public void add(String type)
	{
		if(type.equals("SPAM"))
		{
			addSpam();
		}
		else if(type.equals("HAM"))
		{
			addHam();
		}
		else
		{
			System.err.println("Error adding type '"+type+"' to "+word);
		}
	}
	
	/**
	 * 
	 * @return The word string associated with this word object.
	 */
	public String getWord()
	{
		return word;
	}
	
	/**
	 * Gets the total number of occurrences of this word, within the 
	 * parsed training set. 
	 * 
	 * @return An int value giving the number of occurrences.
	 */
	public int getFrequency()
	{
		return (spamCount+hamCount);
	}
	
	/**
	 * This returns a count of the number of times this word 
	 * has been used in the context of a "SPAM" message.
	 * 
	 * @return An int giving the usage count of this work in SPAM messages.
	 */
	public int getSpamCount()
	{
		return spamCount;
	}
	
	/**
	 * This returns a count of the number of times this word 
	 * has been used in the context of a "HAM" message.
	 * 
	 * @return An int giving the usage count of this work in HAM messages.
	 */
	public int getHamCount()
	{
		return hamCount;
	}
	
	/**
	 * Sets the spamicity of this word to the given value.
	 * 
	 * @param value The new spamicity value.
	 */
	public void setSpamicity(double value)
	{
		spamicity = value;
	}
	
	/**
	 * Gets the spamicity of this word. Typically this 
	 * spamicity value is set after training the spam filter.
	 * 
	 * @return A double value giving the spamicity of this word.
	 */
	public double getSpamicity()
	{
		return spamicity;
	}
	
	/**
	 * Increments the SPAM counter.
	 */
	public void addSpam()
	{
		spamCount++;
	}
	
	/**
	 * Increments the HAM counter.
	 */
	public void addHam()
	{
		hamCount++;		
	}
	
	/**
	 * Adds two objects of the same work type together.
	 * @param word The other word object.
	 */
	public void addWord(Word word)
	{
		int spamAdd = word.getSpamCount();
		spamCount += spamAdd;
		int hamAdd = word.getHamCount();
		hamCount += hamAdd;
	}
}
