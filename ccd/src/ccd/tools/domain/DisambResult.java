package ccd.tools.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class DisambResult {

	public HashMap<String, Double> data;
	private double confidence;

	public DisambResult() {
		data = new HashMap<>();
		confidence = -1;
	}

	public ArrayList<String> getMax() {
		ArrayList<String> result = new ArrayList<String>();
		double pprob = 0;

		Iterator<Entry<String, Double>> iterator = data.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Double> entry = (Entry<String, Double>) iterator.next();

			if (entry.getValue() > pprob) {
				pprob = entry.getValue();
			}
		}

		iterator = data.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Double> entry = (Entry<String, Double>) iterator.next();

			if (entry.getValue() == pprob) {
				result.add(entry.getKey());
			}
		}

		return result;
	}

	public double getConfidence() {
		if (confidence == -1) {
			int n = data.size();
			if (n == 1) {
				confidence = 1;
			} else if (n >= 2) {
				double max = -Double.MAX_VALUE, second = -Double.MAX_VALUE;
				Iterator<Entry<String, Double>> iterator = data.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, Double> entry = (Entry<String, Double>) iterator.next();
					double value = entry.getValue();

					if (value >= max) {
						second = max;
						max = value;
					} else if (value >= second) {
						second = value;
					}
				}

				if (second < 0 && max > 0) {
					confidence = sigmoid(1 - max / second);
				} else if (second == 0) {
					confidence = sigmoid(1 + max);
				} else if (max == 0) {
					confidence = sigmoid(1 - second);
				} else {
					if (second < 0 && max < 0) {
						double tmp = max;
						max = -second;
						second = -tmp;
					}
					confidence = sigmoid(max / second);
				}
			}
		}
		return confidence;
	}

	private static double sigmoid(double x) {
		return (Math.pow(Math.E, -1) - Math.pow(Math.E, -x)) / Math.pow(Math.E, -1) / (1 + Math.pow(Math.E, -x));
		// return (1 / (1 + Math.pow(Math.E, (-1 * x))));
	}
}
