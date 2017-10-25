package ccd;

public class TestPrior {

	private static final double[] priors = {60,70,80,90,100,120,150,200};
	
	public static void main(String[] args)
	{
		for(double prior : priors)
		{
			System.out.println("prior: " + prior);
			
			TestAmbiguous.Disambiguous(prior);
		}
	}
}
