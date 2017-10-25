package ccd.tools.domain;

import java.util.ArrayList;

public class WordSensePr {

	//a class contains the information of senses and their Pr(Sk)
	
	//senses
	public ArrayList<String> senses;
	
	//PrSk
	public ArrayList<Double> Prs;
	
	public WordSensePr()
	{
		senses = new ArrayList<String>();
		Prs = new ArrayList<Double>();
	}
}
