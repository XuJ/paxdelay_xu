package edu.mit.nsfnats.paxdelay.choice;

public class FeatureValue {

	Feature m_feature;
	String[] m_values;

	public FeatureValue(Feature feature, String[] values) {
		m_feature = feature;
		m_values = values;
	}

	public Feature getFeature() {
		return m_feature;
	}

	public String[] getValues() {
		return m_values;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof FeatureValue)) {
			return false;
		}
		FeatureValue thatFeatureValue = (FeatureValue) that;
		if (!m_feature.equals(thatFeatureValue.m_feature)) {
			return false;
		}
		for (int i = 0; i < m_values.length; ++i) {
			if (!m_values[i].equals(thatFeatureValue.m_values[i])) {
				return false;
			}
		}
		return true;
	}

	protected String getHashKey() {
		StringBuffer hashKey = new StringBuffer();
		String[] columnHeaders = m_feature.getColumnHeaders();
		for (int i = 0; i < m_values.length; ++i) {
			if (i > 0) {
				hashKey.append("+");
			}
			hashKey.append(columnHeaders[i]);
			hashKey.append("=");
			hashKey.append(m_values[i]);
		}
		return hashKey.toString();
	}
}
