package ccd.crf;

import java.util.ArrayList;

public class Viterbi {

	private int N = 3;
	private int M = 2;
	
	//private float[][] A = {{0.33333f, 0.33333f, 0.33333f}, {0.33333f, 0.33333f, 0.33333f}, {0.33333f, 0.33333f, 0.33333f}};
	private float[][] A = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
	
	private float[][] B = {{0.5f, 0.5f}, {0.75f, 0.25f}, {0.25f, 0.75f}};
	private float[] pi = {0.3333f, 0.3333f, 0.3333f};
	
	public ArrayList<Integer> execute(int[] O, int T)
	{
		float delta[][] = new float[T][N];
		float psi[][] = new float[T][N];
		ArrayList<Integer> q = new ArrayList<Integer>();
		
		/*
		 * Initialization
		 */
		
		for(int i = 0; i < N; i++)
		{
			delta[0][i] = pi[i] * B[i][O[0]];
			psi[0][i] = 0;
		}
		
		printMatrix("delta", delta, T, N);
		printMatrix("psi", psi, T, N);
		
		/*
		 * Recursion
		 */
		
		for(int t = 1; t < T; t++)
		{
			for(int j = 0; j < N; j++)
			{
				float maxval = 0.0f;
				float maxvalind = 0;
				
				for(int i = 0; i < N; i++)
				{
					float val = delta[t-1][i] * A[i][j];
							
					if(val > maxval)
					{
						maxval = val;
						maxvalind = i;
						
						System.out.printf("maxval: %f, maxvalind: %f\n", maxval, maxvalind);
					}
				}
				
				delta[t][j] = maxval * B[j][O[t]];
				psi[t][j] = maxvalind;
			}
		}
		
		printMatrix("delta", delta, T, N);
		printMatrix("psi", psi, T, N);
		
		/*
		 * Termination
		 */
		
		float pprob = 0;
		
		for(int i = 0; i < N; i++)
		{
			if(delta[T - 1][i] > pprob)
			{
				pprob = delta[T - 1][i];
			}
		}
		
		for(int i = 0; i < N; i++)
		{
			if(delta[T - 1][i] == pprob)
			{
				q.add(i);
			}
		}

		System.out.println("pprob: " + pprob);
		System.out.println("log pprob: " + Math.log(pprob));
		
		return q;
	}
	
	public void printMatrix(String name, float[][] matrix, int height, int width)
	{
		System.out.println("Matrix: " + name);
		
		for(int i = 0; i < height; i++)
		{
			System.out.print(i + ":\t");
			
			for(int j = 0; j < width; j++)
			{
				System.out.print(matrix[i][j] + "\t");
			}
			
			System.out.println();
		}
		
		System.out.println();
	}
	
	public static void main(String[] args)
	{
		//Viterbi v = new Viterbi();
		
		//int[] O = {0,0,0,0,1,0,1,1,1,1};
		
		//System.out.println(v.execute(O, 10));
		
		int[][] abcd = {{1,3,1,10},
				{3,3,3,10},
				{3,3,9,10},
				{3,3,5,10},
				{3,3,4,10},
				{3,3,10,10},
				{1,3,9,10},
				{1,3,1,3}
		};
		
		for(int i = 0; i < 8; i++)
		{
			float a = abcd[i][0];
			float b = abcd[i][1];
			float c = abcd[i][2];
			float d = abcd[i][3];
			
			//System.out.println((c - a) / (d - b));
			if(b == d)
			{
				System.out.println(Math.pow(a, 2) / (b * c));
			}
			else
			{
				System.out.println(Math.pow(a, 2) / (b * c) * Math.pow(Math.E, -((c - a) / (d - b))));
			}
		}
	}
}
