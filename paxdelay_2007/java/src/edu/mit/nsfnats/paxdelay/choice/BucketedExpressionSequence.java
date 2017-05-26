package edu.mit.nsfnats.paxdelay.choice;

import java.util.ArrayList;
import java.util.List;

public class BucketedExpressionSequence extends ExpressionSequence {
	public static final String BUCKET_VALUE_SEPARATOR = "";

	/*
	 * Assumes the following: - bucketValues is sorted with at least 2 values -
	 * minValue is <= MIN(bucketValues) - maxValue is >= MAX(bucketValues)
	 */
	public BucketedExpressionSequence(int numAlternatives,
			ExpressionSequence exprSeq, double[] buckets, int min, int max) {
		super(numAlternatives);

		double first = buckets[0];
		double last = buckets[buckets.length - 1];
		int numDigits = (Integer.toString(max)).length();
		String format = "%1$0" + (numDigits + 2) + ".1f";

		List<Expression> newExprList = new ArrayList<Expression>();
		m_alternativeExpressions = new int[numAlternatives][];
		for (int i = 0; i < numAlternatives; ++i) {
			int startIndex = newExprList.size();
			Expression[] expressions = exprSeq.getExpressionsForAlternative(i);
			for (int j = 0; j < expressions.length; ++j) {
				String name = expressions[j].getName();
				String expStr = expressions[j].getExpressionString();

				// Create the first expression
				double prev = last;
				double curr = first;
				double next = buckets[1];
				String newExpStr = Expression.percentBucket(expStr, prev, curr,
						next, min, max);
				String newName = name + BUCKET_VALUE_SEPARATOR
						+ String.format(format, curr).replace(".", "-");
				newExprList.add(new Expression(newName, newExpStr));

				// Create the intermediate expressions
				for (int n = 1; n < buckets.length - 1; ++n) {
					prev = buckets[n - 1];
					curr = buckets[n];
					next = buckets[n + 1];
					newExpStr = Expression.percentBucket(expStr, prev, curr,
							next, min, max);
					newName = name + BUCKET_VALUE_SEPARATOR
							+ String.format(format, curr).replace(".", "-");
					newExprList.add(new Expression(newName, newExpStr));
				}

				// Create the last expression
				prev = buckets[buckets.length - 2];
				curr = last;
				next = first;
				newExpStr = Expression.percentBucket(expStr, prev, curr, next,
						min, max);
				newName = name + BUCKET_VALUE_SEPARATOR
						+ String.format(format, curr).replace(".", "-");
				newExprList.add(new Expression(newName, newExpStr));
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
