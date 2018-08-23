package edu.mit.nsfnats.paxdelay.choice;

public abstract class BiogemeModel {
	ExpressionSequence[] m_primaryExprSeqs;
	ExpressionSequence m_availExprSeq;
	int m_numAlternatives;

	public void initialize(int numAlternatives) {
		m_numAlternatives = numAlternatives;
		initializeExpressions();
	}

	public abstract void initializeExpressions();

	public ExpressionSequence getAvailabilityExpressionSequence() {
		return m_availExprSeq;
	}

	public ExpressionSequence[] getPrimaryExpressionSequences() {
		return m_primaryExprSeqs;
	}
}
