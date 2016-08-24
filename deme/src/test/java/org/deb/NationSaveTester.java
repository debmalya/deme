package org.deb;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

public class NationSaveTester
{
	static String mExecCommand = null;
	static String mTestFile = "example_economies.csv";
	static boolean mDebug = true;
	static Process mSolution = null;
	
	private static Logger LOGGER = Logger.getLogger(NationSaveTester.class);
	
	static NationSave ns = new NationSave();
	
	public void printMessage(String s)
	{
		if(mDebug)
		{
//			System.out.println(s);
			LOGGER.debug(s);
		}
	}
	
	public double scoreAnswer(int N, int T, double beta, double eta, double alpha, double delta, double[][] C, double[][] K, double[][] Z)
	{
		// Scoring
		double eer = 0;
		for(int t = 0; t < T; t++)
		{
			double eer_t = 0;
			for(int i = 0; i < N; i++)
			{
				double eer_t_i = beta * Math.pow(C[t][i] / C[t + 1][i], eta) * (1 - delta + Z[t + 1][i] * alpha * Math.pow(K[t + 1][i], alpha - 1)) - 1;
				eer_t += eer_t_i;
			}
			eer_t /= N;
			eer += Math.abs(eer_t);
		}
		eer = 1 / (1 + eer / T);
		
		double score = eer * 1000000;
		
		return score;
	}
	
	public void doExec() throws Exception
	{
		// Launch solution
		printMessage("Executing your solution: " + mExecCommand + ".");
		mSolution = Runtime.getRuntime().exec(mExecCommand);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(mSolution.getInputStream()));
//        PrintWriter writer = new PrintWriter(mSolution.getOutputStream());
        PrintWriter writer = new PrintWriter(System.out);
        new ErrorStreamRedirector(mSolution.getErrorStream()).start();
        
        // Read test cases file
        List<double[]> testCases = new ArrayList<double[]>();
        printMessage("Reading test case file: " + mTestFile + ".");
        BufferedReader br = new BufferedReader(new FileReader(mTestFile));
        while (true)
        {
            String s = br.readLine();
            if (s == null)
            {
                break;
            }
            String[] tokens = s.split(",");
            if(tokens.length != 6)
            {
            	printMessage("Incorrent number of tokens for test case " + s + ". Expected 6 got " + tokens.length + ".");
            	printMessage("Score = 0");
            	return;
            }
            
            double beta = Double.parseDouble(tokens[0]);
            double eta = Double.parseDouble(tokens[1]);
            double alpha = Double.parseDouble(tokens[2]);
            double delta = Double.parseDouble(tokens[3]);
            double rho = Double.parseDouble(tokens[4]);
            double sigma = Double.parseDouble(tokens[5]);
            
            testCases.add(new double[] { beta, eta, alpha, delta, rho, sigma });
        }
        
        // Execute test cases
        printMessage("Executing " + testCases.size() + " test cases.");
        double score = 0;
        writer.println(testCases.size());
       
        for(int test = 0; test < testCases.size(); test++)
        {
        	printMessage("Test case " + (test + 1) + ".");
        	
        	// Send economy parameters
        	double beta = testCases.get(test)[0];
            double eta = testCases.get(test)[1];
            double alpha = testCases.get(test)[2];
            double delta = testCases.get(test)[3];
            double rho = testCases.get(test)[4];
            double sigma = testCases.get(test)[5];
        	
        	writer.println(beta);
        	printMessage(""+beta);
        	writer.println(eta);
        	writer.println(alpha);
        	writer.println(delta);
        	writer.println(rho);
        	writer.println(sigma);
        	
        	// Send number of simulations N and number of time periods T
        	final int N = 200;
        	final int T = 2000;
        	final double K0 = 1;
			final double Z0 = 1;
        	
        	//writer.println(N + "," + T);
			writer.println(N);
			writer.println(T);
        	
        	// Shock random numbers
        	double[][] shocks = new double[T][N];
        	Random rng = new Random(test + 1);
			for(int t = 0; t < T; t++)
			{
				for(int n = 0; n < N; n++)
				{
					shocks[t][n] = rng.nextGaussian() * sigma;
				}
			}
			
			// Consumption
			double[][] C = new double[T + 1][N];
			// Capital
			double[][] K = new double[T + 1][N];
			// Shock
			double[][] Z = new double[T + 1][N];
			
			// Set initial capital
			Arrays.fill(K[0], K0);
			// Set initial shock
			Arrays.fill(Z[0], Z0);
			
			for(int t = 0; t <= T; t++)
			{
				// Wage
				double[] W_t = new double[N];
				for(int n = 0; n < N; n++)
				{
					// Update shock
					if(t != 0) //Already set for t = 0
					{
						double eps_t = shocks[t - 1][n];
						Z[t][n] = Math.exp(rho * Math.log(Z[t - 1][n]) + eps_t);
					}
					// Compute wage
					W_t[n] = (1 - delta) * K[t][n] + Z[t][n] * Math.pow(K[t][n], alpha);
				}
				if(t != T)
				{
					for(int i = 0; i < N; i++)
					{
						writer.println(K[t][i]);
					}
					for(int i = 0; i < N; i++)
					{
						writer.println(Z[t][i]);
					}
					
					writer.flush();
					
					for(int i = 0; i < N; i++)
					{
						C[t][i] = Double.parseDouble(reader.readLine());
					}
					
					for(int i = 0; i < N; i++)
					{
						//Sanity check C[t][i]
						if(C[t][i] < 0)
						{
							printMessage("Consumption cannont be negative.");
							printMessage("Score = 0");
							return;
						}
						if(C[t][i] > W_t[i])
						{
							printMessage("Consumption cannont exceed wage.");
							printMessage("Score = 0");
							return;
						}
						if(Double.isNaN(C[t][i]))
						{
							printMessage("Consumption must be a real number.");
							printMessage("Score = 0");
							return;
						}
						
						//Update capital for next period
						K[t + 1][i] = W_t[i] - C[t][i];
					}
				}
				else
				{
					//On last time period, consume all of the wage
					C[t] = W_t;
				}
			}
			
			// Scoring
			double testScore = scoreAnswer(N, T, beta, eta, alpha, delta, C, K, Z);
			score += testScore;
			
			printMessage(Double.toString(testScore));
        }
        score /= testCases.size();
        System.out.println("Score = " + score);
	}
	
	public static void main(String[] args)
	{
		for(int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-exec"))
			{
				mExecCommand = args[++i];
			}
			else if (args[i].equals("-test"))
			{
                mTestFile = args[++i];
			}
			else if (args[i].equals("-silent"))
			{
                mDebug = false;
			}
			else
			{
				System.out.println("WARNING: unknown argument " + args[i] + ".");
			}
		}
		
		try
		{
			if(mExecCommand != null)
			{
				new NationSaveTester().doExec();
			}
			else
			{
				System.out.println("WARNING: nothing to do for this combination of arguments.");
			}
		}
		catch (Exception e)
		{
			System.out.println("FAILURE: " + e.getMessage());
            e.printStackTrace();
            mSolution.destroy();
		}
	}
	
	class ErrorStreamRedirector extends Thread
	{
        public BufferedReader mReader;

        public ErrorStreamRedirector(InputStream is)
        {
        	mReader = new BufferedReader(new InputStreamReader(is));
        }

        public void run()
        {
            while (true)
            {
                String s;
                try
                {
                    s = mReader.readLine();
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                    return;
                }
                if (s == null) {
                    break;
                }
                System.out.println(s);
            }
        }
    }
	
}
