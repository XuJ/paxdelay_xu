package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.List;

public class ThresholdExpressionSequence extends ExpressionSequence {
	public static final String THRESHOLD_GTE_VALUE_SEPARATOR = "_GTE";
	public static final String THRESHOLD_LT_VALUE_SEPARATOR = "_LT";

	public ThresholdExpressionSequence(int numAlternatives,
			ExpressionSequence exprSeq, int[] thresholds,
			boolean greaterThanOrEqual) {
		super(numAlternatives);

		m_alternativeExpressions = new int[numAlternatives][];
		List<Expression> newExprList = new ArrayList<Expression>();
		for (int i = 0; i < numAlternatives; ++i) {
			int startIndex = newExprList.size();
			Expression[] expressions = exprSeq.getExpressionsForAlternative(i);
			m_alternativeExpressions[i] = new int[expressions.length];
			for (int j = 0; j < expressions.length; ++j) {
				String name = expressions[j].getName();
				String exprStr = expressions[j].getExpressionString();

				String newName = null;
				String newExprStr = null;
				if (greaterThanOrEqual) {
					newName = name + THRESHOLD_GTE_VALUE_SEPARATOR
							+ thresholds[i];
					newExprStr = Expression.greaterThanOrEqual(exprStr,
							thresholds[i]);
				} else {
					newName = name + THRESHOLD_LT_VALUE_SEPARATOR
							+ thresholds[i];
					newExprStr = Expression.lessThan(exprStr, thresholds[i]);
				}
				newExprList.add(new Expression(newName, newExprStr));
				m_alternativeExpressions[i][j] = startIndex + j;
			}
		}
		m_expressions = new Expression[newExprList.size()];
		newExprList.toArray(m_expressions);
	}
}
