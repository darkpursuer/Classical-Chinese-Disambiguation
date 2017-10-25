package ccd.bayes;

public class BayesEmptyDisambiguation {

	public static final String[] emptylist = {"而","何","乎","乃","其","且","若","所","为","焉","也","以","因","于","与","则","者","之"};
	
	public static boolean isEmpty(String word)
	{
		for(String tmp : emptylist)
		{
			if(word.equals(tmp))
			{
				return true;
			}
		}
		
		return false;
	}

}
