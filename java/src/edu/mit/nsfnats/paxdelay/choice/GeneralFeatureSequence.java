package edu.mit.nsfnats.paxdelay.choice;

public class GeneralFeatureSequence extends ExpressionSequence {
	public GeneralFeatureSequence(int numAlternatives, String name,
			String feature) {
		super(numAlternatives);
		m_alternativeExpressions = new int[numAlternatives][1];
		m_expressions = new Expression[numAlternatives];
		for (int i = 0; i < numAlternatives; ++i) {
			m_alternativeExpressions[i][0] = i;
			m_expressions[i] = new Expression(name, feature);
		}
	}
}
