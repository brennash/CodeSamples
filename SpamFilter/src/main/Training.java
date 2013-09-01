package classifier.main;

import classifier.filter.BuildFilter;

public class Training
{
	public static void main(String[] args)
	{
		if(args.length == 1)
		{
			new BuildFilter("corpus.txt");
		}
	    else if(args.length == 2)
		{
			new BuildFilter(args[1]);
		}
		else if(args.length == 3)
		{
			new BuildFilter(args[1], args[2]);
		}
		else
		{
			System.err.println("Usage: ");
			System.err.println("java -jar classifier.main.Training, (assumes corpus.txt)");
			System.err.println("java -jar classifier.Training <input-training-file>, or");
			System.err.println("java -jar classifier.Training <input-training-file> <output-filter-file>");
		}
	}
}
