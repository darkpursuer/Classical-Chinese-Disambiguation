package ccd.tools;

public class MaxStringMerge {

	public static int LCS(String s1, String s2)
	{
		if(s1 == "" || s2 == "")
		{
			return -1;
		}
		
		int[] c = new int[s2.length() + 1];
		
		for(int j = 0; j < s2.length(); j++)
		{
			c[j] = 0;
		}
		
		int max_len = 0;
		
		for(int i = 0; i < s1.length(); i++)
		{
			for(int j = s2.length(); j > 0; j--)
			{
				if(s1.charAt(i) == s2.charAt(j - 1))
				{
					c[j] = c[j - 1] + 1;
					if(c[j] > max_len)
					{
						max_len = c[j];
					}
				}
				
				else
				{
					c[j] = 0;
				}
			}
		}
		
		return max_len;
		
	}
	
	public static void main(String[] args)
	{
		String s1 = "abcdaef";
		String s2 = "cabcdefg";
		
		int len = LCS(s1, s2);
		
		System.out.println(len);
	}
}
