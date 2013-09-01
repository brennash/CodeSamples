package classifier.filter;

import java.util.ArrayList;

import classifier.utils.Word;



public class MessageClassifier {

	private ArrayList <Word> words;
	private double threshold;
	
	//The overall probability for any given message to be SPAM/HAM
	private double probHam, probSpam;
	
	//The probabilty this message is Spam
	private double probMsgSpam;
	
	public MessageClassifier(double probHam, double probSpam)
	{
		this.probHam = probHam;
		this.probSpam = probSpam;
		words = new ArrayList<Word>();
		threshold = 0.4;
		
		//Set to a nonsense value initially
		probMsgSpam = -1.0;
	}
	
	/**
	 * Adds a provided word object to the array list. 
	 * 
	 * @param word The word object to be added to the array list.
	 */
	public void addWord(Word word)
	{
		words.add(word);
	}

	/**
	 * Gets the word from the array of word objects.
	 *
	 * @param index The index of the word in the array.
	 * @return A word object corresponding to the item at the index.
	 */
	public Word getWord(int index)
	{
		return words.get(index);
	}
	
	/**
	 * 
	 * @return The number of word objects currently in the array.
	 */
	public int size()
	{
		return words.size();
	}
	
	/**
	 * Checks if a specified word is contained within the current array.
	 * This method converts the provided word to lower-case, since the 
	 * word values are stored as lower-case in the spam filter. 
	 * 
	 * @param word A string giving the word to be searched.
	 * @return Boolean true if found, false otherwise.
	 */
	public boolean contains(String word)
	{
		for(int i=0; i<words.size(); i++)
		{
			if(words.get(i).getWord().equals(word.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the sum of the logs of word spamicity in the current 
	 * word set, i.e., 
	 * 
	 * Log-Likelihood = SUM[i=0;i<n] (log (word[i].getSpamicity()))
	 * 
	 * @return A double value giving the log-likelihood for the wordset.
	 */
	private double getLogLikelihood()
	{
		double loglik = 0.0;
		
		for(int i=0; i<words.size(); i++)
		{
			//Get the spamicity of a word
			double pSpam = words.get(i).getSpamicity();
			
			//Adjust extreme probability values
			if((pSpam == 1.0)||(pSpam == 0.0))
			{
				double frequency = (double) words.get(i).getFrequency();
				pSpam = (3.0*probSpam)+(frequency*pSpam);
				pSpam = pSpam / (3.0 + frequency);
			}
			
			//Add to the log-likelihood
			loglik += (Math.log(1.0-pSpam) - Math.log(pSpam));			
		}
		
		return loglik;
	}
	
	/**
	 * Returns true if the message is found to be SPAM, i.e., if the 
	 * probability associated with the message is greater than the 
	 * selection threshold. By default this threshold is set to 0.4. 
	 * 
	 * @return True is the message is assessed as being SPAM, false if HAM.
	 */
	public boolean isSpam()
	{
		//Get the log-likelihood
		double loglik = this.getLogLikelihood();
		
		//Convert it to a probability, e.g., p = 1/1+e^(log-likelihood)
		probMsgSpam = 1.0 / (1.0 + Math.exp(loglik));
		
		//Use the default threshold value of 0.4
		if(probMsgSpam > threshold)
		{
			//Indicate message is spam
			return true;
		}
		else
		{
			//Indicate message is ham
			return false;
		}
	}
	
	/**
	 * Returns true if the message is found to be SPAM, i.e., if the 
	 * probability associated with the message is greater than the 
	 * selection threshold. This threshold is provided by the user, and 
	 * must be between 0.0 and 1.0 (non-inclusive). 
	 * 
	 * @param threshold The probability threshold to assess SPAM/HAM
	 * @return True is the message is assessed as being SPAM, false if HAM.
	 */
	public boolean isSpam(double threshold)
	{
		if(threshold > 0.0 && threshold < 1.0)
		{
			double loglik = this.getLogLikelihood();
			double prob = 1.0 / (1.0 + Math.exp(loglik));
			
			if(prob > threshold)
			{
				return true;
			}
		}
		else
		{
			System.err.println("Threshold value ("+threshold+") must be 0.0 < threshold < 1.0");
			System.exit(0);
		}
		
		return false;
	}
	
	/**
	 * Gets the confidence level in the assessment of the message 
	 * being SPAM or HAM.
	 * 
	 * @return A double giving the probability a message is spam.
	 */
	public double getConfidenceLevel()
	{
		//Calculate the probability the message is SPAM, if not already done.
		if(probMsgSpam == -1.0)
		{
			//Get the log-likelihood
			double loglik = this.getLogLikelihood();
			
			//Convert it to a probability, e.g., p = 1/1+e^(log-likelihood)
			probMsgSpam = 1.0 / (1.0 + Math.exp(loglik));
		}
		
		return probMsgSpam;
	}
	
	/**
	 * Returns the type of the message, as assessed by the classifier.
	 * 
	 * @return A string, "HAM" is the message is classified as HAM, "SPAM" otherwise.
	 */
	public String getMessageType()
	{
		//Calculate the probability the message is SPAM, if not already done.
		if(probMsgSpam == -1.0)
		{
			//Get the log-likelihood
			double loglik = this.getLogLikelihood();
			
			//Convert it to a probability, e.g., p = 1/1+e^(log-likelihood)
			probMsgSpam = 1.0 / (1.0 + Math.exp(loglik));
		}
		
		//Use the default threshold value of 0.4
		if(probMsgSpam > threshold)
		{
			//Indicate message is spam
			return "SPAM";
		}
		else
		{
			//Indicate message is ham
			return "HAM";
		}
	}

}
