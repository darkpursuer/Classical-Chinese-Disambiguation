package ccd.bayes;

public class BayesEmptyDisambiguation {

	public static final String[] emptylist = {"��","��","��","��","��","��","��","��","Ϊ","��","Ҳ","��","��","��","��","��","��","֮"};
	
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
