package edu.mit.nsfnats.paxdelay.util;

import java.util.Comparator;
import java.util.Random;

public class RandomValuesComparator implements Comparator<Integer> {
	double[] m_randomValues;

	public RandomValuesComparator(int length, Random randomEngine) {
		m_randomValues = new double[length];
		for (int i = 0; i < m_randomValues.length; ++i) {
			m_randomValues[i] = randomEngine.nextDouble();
		}
	}
	
	public RandomValuesComparator(int length) {
		this(length, new Random());
	}

	public int compare(Integer a, Integer b) {
		if (m_randomValues[a.intValue()] < m_randomValues[b.intValue()]) {
			return -1;
		} else if (m_randomValues[a.intValue()] > m_randomValues[b.intValue()]) {
			return 1;
		}
		return 0;
	}
}