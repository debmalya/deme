/**
 * Copyright 2015-2016 Debmalya Jash
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deb;

import java.util.Scanner;

/**
 * @author debmalyajash
 *
 */
public class NationSave {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 execute();

	}

	private static void execute() {
		NationSave ns = new NationSave();
		try (Scanner in = new Scanner(System.in)) {
			int x = in.nextInt();
			for (int i = 0; i < x; i++) {
				double beta = in.nextDouble();
				double eta = in.nextDouble();
				double alpha = in.nextDouble();
				double delta = in.nextDouble();
				double rho = in.nextDouble();
				double sigma = in.nextDouble();
				int N = in.nextInt();
				int T = in.nextInt();

				ns.SetEconomyParameters(beta, eta, alpha, delta, rho, sigma, N, T);

				double[] ct = new double[N];
				for (int j = 0; j < T; j++) {
					double[] kt = new double[N];
					double[] zt = new double[N];
					for (int k = 0; k < N; k++) {
						kt[k] = in.nextDouble();
						zt[k] = in.nextDouble();
					}

					ct = ns.ConsumptionDecisionRule(kt, zt);

				}

				for (int j = 0; j < N; j++) {
					System.out.println(ct[j]);
				}
			}
		}
	}

	/**
	 * 
	 * @param Kt
	 *            - Kt - Invested capital at current time for each of the N
	 *            concurrent simulations
	 * @param Zt
	 *            - Zt - Shock at current time for each of the N concurrent
	 *            simulations
	 * @return the consumption for this time period for each of the N concurrent
	 *         simulations. Consumption at each time period for each simulation
	 *         must be real, non-negative, and not greater than the current wage
	 */
	public double[] ConsumptionDecisionRule(double[] Kt, double[] Zt) {
		double[] result = new double[Kt.length];
		return result;
	}

	/**
	 * This will be called only once.
	 * 
	 * @param beta
	 *            - β (beta) - Future utility discount factor
	 * @param eta
	 *            - η (eta) - Consumption utility parameter
	 * @param alpha
	 *            - α (alpha) - Productivity of capital
	 * @param delta
	 *            - δ (delta) - Depreciation rate of capital
	 * @param rho
	 *            - ρ (rho) - Degree of correlation with previous shocks
	 * @param sigma
	 *            σ (sigma) - Standard deviation of εt
	 * @param N
	 *            - N - Number of concurrent simulations
	 * @param T
	 *            -T - Total number of time periods
	 * @return
	 */
	public int SetEconomyParameters(double beta, double eta, double alpha, double delta, double rho, double sigma,
			int N, int T) {
		return 0;
	}

}
