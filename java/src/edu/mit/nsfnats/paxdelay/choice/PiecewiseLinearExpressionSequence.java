package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.List;

public class PiecewiseLinearExpressionSequence extends ExpressionSequence {

	public PiecewiseLinearExpressionSequence(int numAlternatives,
			ExpressionSequence exprSeq, int[] breaks) {
		super(numAlternatives);

		int numDigits = (Integer.toString(breaks[breaks.length - 1])).length();
		String format = "%1$0" + numDigits + "d";

		List<Expression> newExprList = new ArrayList<Expression>();
		m_alternativeExpressions = new int[numAlternatives][];
		for (int i = 0; i < numAlternatives; ++i) {
			int startIndex = newExprList.size();
			Expression[] expressions = exprSeq.getExpressionsForAlternative(i);
			for (int j = 0; j < expressions.length; ++j) {
				String name = expressions[j].getName();
				String exprStr = expressions[j].getExpressionString();

				int minimum;
				int maximum = breaks[0];

				// Create the first piece
				String newName = name + "L" + String.format(format, maximum);
				String newExpr = Expression.product(Expression.lessThan(
						exprStr, maximum), Expression.min(exprStr, maximum));
				newExprList.add(new Expression(newName, newExpr));

				// Create the intermediate pieces
				for (int n = 1; n < breaks.length; ++n) {
					minimum = breaks[n - 1];
					maximum = breaks[n];
					newName = name + "L" + String.format(format, maximum);
					newExpr = Expression.product(Expression.inRange(exprStr,
							minimum, maximum), Expression.min(Expression.diff(
							exprStr, minimum), maximum - minimum));
					newExprList.add(new Expression(newName, newExpr));
				}
				minimum = breaks[breaks.length - 1];

				// Create the last piece
				newName = name + "G" + String.format(format, minimum);
				newExpr = Expression.product(Expression.greaterThanOrEqual(
						exprStr, minimum), Expression.diff(exprStr, minimum));
				newExprList.add(new Expression(newName, newExpr));
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
