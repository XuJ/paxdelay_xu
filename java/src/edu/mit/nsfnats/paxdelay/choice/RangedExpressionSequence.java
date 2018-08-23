package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.List;

public class RangedExpressionSequence extends ExpressionSequence {
	public static final String RANGE_VALUE_SEPARATOR = "-";

	public RangedExpressionSequence(int numAlternatives,
			ExpressionSequence exprSeq, double[] endPoints, int min, int max,
			boolean rangeCycles) {
		super(numAlternatives);

		boolean[] excludeRange = new boolean[endPoints.length
				+ (rangeCycles ? 0 : -1)];
		constructExpressions(numAlternatives, exprSeq, endPoints, min, max,
				rangeCycles, excludeRange);
	}

	public RangedExpressionSequence(int numAlternatives,
			ExpressionSequence exprSeq, double[] endPoints, int min, int max,
			boolean rangeCycles, boolean[] excludeRange) {
		super(numAlternatives);

		constructExpressions(numAlternatives, exprSeq, endPoints, min, max,
				rangeCycles, excludeRange);
	}

	public void constructExpressions(int numAlternatives,
			ExpressionSequence exprSeq, double[] endPoints, int min, int max,
			boolean rangeCycles, boolean[] excludeRange) {

		double first = endPoints[0];
		double last = endPoints[endPoints.length - 1];
		int numDigits = (Integer.toString(max)).length();
		String format = "%1$0" + (numDigits + 2) + ".1f";

		List<Expression> newExprList = new ArrayList<Expression>();
		m_alternativeExpressions = new int[numAlternatives][];
		for (int i = 0; i < numAlternatives; ++i) {
			int startIndex = newExprList.size();
			Expression[] expressions = exprSeq.getExpressionsForAlternative(i);
			for (int j = 0; j < expressions.length; ++j) {
				String name = expressions[j].getName();
				String exprStr = expressions[j].getExpressionString();

				double prev, curr;
				String newName, newExpr;
				// Create the range expressions
				for (int n = 1; n < endPoints.length; ++n) {
					if (excludeRange[n - 1]) {
						continue;
					}
					prev = endPoints[n - 1];
					curr = endPoints[n];

					newName = name + RANGE_VALUE_SEPARATOR
							+ String.format(format, curr).replace(".", "-");
					newExpr = Expression.inRange(exprStr, prev, curr);
					newExprList.add(new Expression(newName, newExpr));
				}

				// If the range cycles, create the cycle expression
				if (rangeCycles && !excludeRange[endPoints.length - 1]) {
					prev = last;
					curr = first;
					newName = name + RANGE_VALUE_SEPARATOR
							+ String.format(format, curr).replace(".", "-");
					newExpr = Expression.or(Expression.inRange(exprStr, prev,
							max), Expression.inRange(exprStr, min, curr));
					newExprList.add(new Expression(newName, newExpr));
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
