package edu.mit.nsfnats.paxdelay.choice;

import java.io.PrintWriter;

abstract class ExpressionSequence {
	public static final String ALTERNATIVE_SEPARATOR = "_";

	Expression[] m_expressions;
	int[][] m_alternativeExpressions;

	public ExpressionSequence(int numAlternatives) {

	}

	public void writeExpressions(PrintWriter writer) {
		for (int i = 0; i < m_alternativeExpressions.length; ++i) {
			for (int j = 0; j < m_alternativeExpressions[i].length; ++j) {
				Expression expression = m_expressions[m_alternativeExpressions[i][j]];
				String name = expression.getName() + ALTERNATIVE_SEPARATOR + i;
				String expressionString = expression.getExpressionString();
				writer.print(name);
				writer.print(" = ");
				writer.println(expressionString);
			}
			writer.println();
		}
	}

	Expression[] getExpressions() {
		return m_expressions;
	}

	Expression[] getExpressionsForAlternative(int alternative) {
		Expression[] expressions = new Expression[m_alternativeExpressions[alternative].length];
		for (int i = 0; i < expressions.length; ++i) {
			expressions[i] = m_expressions[m_alternativeExpressions[alternative][i]];
		}
		return expressions;
	}
}
