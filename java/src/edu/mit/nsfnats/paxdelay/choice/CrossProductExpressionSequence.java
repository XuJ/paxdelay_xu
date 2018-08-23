package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.List;

public class CrossProductExpressionSequence extends ExpressionSequence {
	public static final String CROSS_PRODUCT_SEPARATOR = "_";

	public CrossProductExpressionSequence(int numAlternatives,
			ExpressionSequence firstExprSeq, ExpressionSequence secondExprSeq) {
		super(numAlternatives);

		List<Expression> newExprList = new ArrayList<Expression>();
		m_alternativeExpressions = new int[numAlternatives][];
		for (int i = 0; i < numAlternatives; ++i) {
			int startIndex = newExprList.size();
			Expression[] firstExprs = firstExprSeq
					.getExpressionsForAlternative(i);
			Expression[] secondExprs = secondExprSeq
					.getExpressionsForAlternative(i);
			m_alternativeExpressions[i] = new int[firstExprs.length
					* secondExprs.length];
			int exprIndex = 0;
			for (int n = 0; n < firstExprs.length; ++n) {
				String firstName = firstExprs[n].getName();
				String firstExprStr = firstExprs[n].getExpressionString();
				for (int m = 0; m < secondExprs.length; ++m) {
					String secondName = secondExprs[m].getName();
					String secondExprStr = secondExprs[m].getExpressionString();

					String newName = firstName + CROSS_PRODUCT_SEPARATOR
							+ secondName;
					String newExprString = Expression.product(firstExprStr,
							secondExprStr);
					newExprList.add(new Expression(newName, newExprString));
					m_alternativeExpressions[i][exprIndex] = startIndex
							+ exprIndex;
					exprIndex += 1;
				}
			}
		}
		m_expressions = new Expression[newExprList.size()];
		newExprList.toArray(m_expressions);
	}
}
