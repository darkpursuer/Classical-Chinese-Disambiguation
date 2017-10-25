package ccd;

import ccd.bayes.BayesDisambiguation;
import ccd.training.TrainingMain;

public class MainProcess {

	public static void main(String[] args)
	{
		String mode = args[0];
		
		if(args.length == 2 && mode.equals("-t"))
		{
			TrainingMain.Training(args[1]);
			
			System.out.println("Training Successfully!");
			
			return;
		}
		
		if(args.length == 3 && mode.equals("-d"))
		{
			//DisambiguationMain.Disambiguation(args[1], args[2]);
			
			return;
		}
		
		System.out.println("usage: java -jar Disambiguation.jar -t [filename]");
		System.out.println("usage: java -jar Disambiguation.jar -d [word] [context]");
		
		return;
		
		//
	}
}
