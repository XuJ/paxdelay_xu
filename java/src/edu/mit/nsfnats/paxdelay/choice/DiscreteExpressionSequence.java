package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.List;

public class DiscreteExpressionSequence extends ExpressionSequence {
	public static final String DISCRETE_VALUE_SEPARATOR = "";

	public DiscreteExpressionSequence(int numAlternatives,
			ExpressionSequence exprSeq, int[] values) {
		super(numAlternatives);

		int numDigits = (Integer.toString(values[values.length - 1])).length();
		String format = "%1$0" + numDigits + "d";

		List<Expression> newExprList = new ArrayList<Expression>();
		m_alternativeExpressions = new int[numAlternatives][];
		for (int i = 0; i < numAlternatives; ++i) {
			int startIndex = newExprList.size();
			Expression[] expressions = exprSeq.getExpressionsForAlternative(i);
			for (int j = 0; j < expressions.length; ++j) {
				String name = expressions[j].getName();
				String expStr = expressions[j].getExpressionString();

				for (int n = 0; n < values.length; ++n) {
					String newExpStr = Expression.equals(expStr, values[n]);
					String newName = name + DISCRETE_VALUE_SEPARATOR
							+ String.format(format, values[n]);
					newExprList.add(new Expression(newName, newExpStr));
				}
			}
			int endIndex = newExprList.size();
			m_alternativeExpressions[i] = new int[endIndex - startIndex];
			for (int j = 0; j < m_alternativeExpressions[i].length; ++j) {
				m_alternativeExpressions[i][j] = startIndex + j;
			}
		}
		m_expressions = new Expression[newExprList.size()];
		newExprList.toArray(m_expressions);
	}
}
