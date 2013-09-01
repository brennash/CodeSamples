package classifier.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import classifier.filter.MessageClassifier;
import classifier.filter.SpamFilter;


public class Classifier {
	
	public static void main(String[] args)
	{
		SpamFilter filterz = new SpamFilter(args[0]);
		filterz.evaluateFilter(args[1], "evaluation.dat", 0.4);
		System.exit(0);
		
		if(args.length == 0)
		{
			System.err.println("Error, cannot find the email message");
			System.err.println("Try running ./classify < [email-filename]");					
		}
		else if((args.length == 1) &&  (args[0].startsWith("classifier.main")) ) 
		{
			String messageText = "";
				
			try
			{
    			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    			String str = "";
    			while (str != null)
    			{
    				messageText = messageText.concat(str+"\n");
        			str = in.readLine();
    			}

    			File checkFile = new File("spam-filter.dat");
    			
    			if(checkFile.exists())
    			{
    				SpamFilter filter = new SpamFilter("spam-filter.dat");
    				MessageClassifier result = filter.classifyMessage(messageText);
    				System.out.println("Message classified as "+result.getMessageType());
    				System.out.println("Confidence Level "+result.getConfidenceLevel()); 
    			}
    			else
    			{
    				System.err.println("Error, cannot find spam-filter.dat");
    				System.err.println("Try running ./train to build the spam filter first!");
    			}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		//Error
		else
		{	
			System.err.println("Error, the input needs appropriate parameters");
			System.err.println("Try running ./classify < [email-filename]");
			System.err.println("\n\nAlternatively, try running the Java directly,");
			System.err.println("java -jar classifier.main.Classifier < [email-filename]");
		}			
	}
}
