package edu.mit.nsfnats.paxdelay.choice;

public class Expression {

	public static final String DECIMAL_FORMAT = "%1$.2f";

	String m_name;
	String m_exprStr;

	public Expression(String name, String exprStr) {
		m_name = name;
		m_exprStr = exprStr;
	}

	public String getName() {
		return m_name;
	}

	public String getExpressionString() {
		return m_exprStr;
	}

	public static String equals(String exprStr, int value) {
		return innerFunction(exprStr, Integer.toString(value), "==");
	}

	public static String greaterThanOrEqual(String exprStr, int value) {
		return innerFunction(exprStr, Integer.toString(value), ">=");
	}

	public static String lessThan(String exprStr, int value) {
		return innerFunction(exprStr, Integer.toString(value), "<");
	}

	public static String inRange(String exprStr, double minimum, double maximum) {
		if (minimum == maximum) {
			return "0";
		}
		StringBuffer expBuffer = new StringBuffer();
		expBuffer.append("( ").append(exprStr);
		expBuffer.append(" >= ");
		expBuffer.append(String.format(DECIMAL_FORMAT, minimum)).append(" && ");
		expBuffer.append(exprStr).append(" < ");
		expBuffer.append(String.format(DECIMAL_FORMAT, maximum)).append(" )");
		return expBuffer.toString();
	}

	public static String or(String exprStr1, String exprStr2) {
		return innerFunction(exprStr1, exprStr2, "||");
	}

	public static String product(String exprStr1, String exprStr2) {
		return innerFunction(exprStr1, exprStr2, "*");
	}

	public static String sum(String exprStr1, int value) {
		return sum(exprStr1, Integer.toString(value));
	}

	public static String sum(String exprStr1, String exprStr2) {
		return innerFunction(exprStr1, exprStr2, "+");
	}

	public static String diff(String exprStr1, int value) {
		return diff(exprStr1, Integer.toString(value));
	}

	public static String diff(String exprStr1, String exprStr2) {
		return innerFunction(exprStr1, exprStr2, "-");
	}

	protected static String innerFunction(String exprStr1, String exprStr2,
			String oper) {
		StringBuffer expBuffer = new StringBuffer("( ");
		expBuffer.append(exprStr1).append(" ").append(oper).append(" ").append(
				exprStr2);
		expBuffer.append(" )");
		return expBuffer.toString();
	}

	public static String min(String exprStr1, int value) {
		return outerFunction("min", exprStr1, Integer.toString(value));
	}

	public static String min(String exprStr1, String exprStr2) {
		return outerFunction("min", exprStr1, exprStr2);
	}

	public static String max(String exprStr1, int value) {
		return outerFunction("max", exprStr1, Integer.toString(value));
	}

	public static String max(String exprStr1, String exprStr2) {
		return outerFunction("max", exprStr1, exprStr2);
	}

	protected static String outerFunction(String fn, String exprStr1,
			String exprStr2) {
		StringBuffer expBuffer = new StringBuffer(fn);
		expBuffer.append("( ");
		expBuffer.append(exprStr1).append(" , ").append(exprStr2);
		expBuffer.append(" )");
		return expBuffer.toString();
	}

	public static String percentMinimum(String exprStr, double minimum,
			double maximum) {
		return percentMinimum(exprStr, minimum, maximum, 0.0, 0.0);
	}

	protected static String percentMinimum(String exprStr, double minimum,
			double maximum, double numerOffset, double denomOffset) {
		return product(inRange(exprStr, minimum, maximum), innerMinimum(
				exprStr, minimum, maximum, numerOffset, denomOffset));
	}

	public static String percentMaximum(String exprStr, double minimum,
			double maximum) {
		return percentMaximum(exprStr, minimum, maximum, 0.0, 0.0);
	}

	protected static String percentMaximum(String exprStr, double minimum,
			double maximum, double numerOffset, double denomOffset) {

		return product(inRange(exprStr, minimum, maximum), innerMaximum(
				exprStr, minimum, maximum, numerOffset, denomOffset));
	}

	protected static String innerMinimum(String exprStr, double minimum,
			double maximum, double numerOffset, double denomOffset) {
		StringBuffer expBuffer = new StringBuffer("( ( ");
		expBuffer.append(String.format(DECIMAL_FORMAT, maximum + numerOffset));
		expBuffer.append(" - ").append(exprStr).append(" ) / ");
		expBuffer.append(String.format(DECIMAL_FORMAT, maximum + denomOffset
				- minimum));
		expBuffer.append(" )");
		return expBuffer.toString();
	}

	protected static String innerMaximum(String exprStr, double minimum,
			double maximum, double numerOffset, double denomOffset) {
		StringBuffer expBuffer = new StringBuffer("( ( ");
		expBuffer.append(String.format(DECIMAL_FORMAT, minimum - numerOffset));
		expBuffer.append(" - ").append(exprStr).append(" ) / ");
		expBuffer.append(String.format(DECIMAL_FORMAT, minimum - maximum
				- denomOffset));
		expBuffer.append(" )");
		return expBuffer.toString();
	}

	// Assumes that previous, current, and next represent sequential buckets
	// in a cyclic ordering (with minimum and maximum the raw end points)
	public static String percentBucket(String exprStr, double previous,
			double current, double next, double minimum, double maximum) {
		String exprStr1 = null;
		if (previous < current) {
			exprStr1 = percentMaximum(exprStr, previous, current);
		} else if (previous != current) {
			String firstPart = percentMaximum(exprStr, previous, maximum, 0.0,
					current - minimum);
			String secondPart = percentMaximum(exprStr, minimum, current,
					maximum - previous, maximum - previous);
			exprStr1 = sum(firstPart, secondPart);
		}
		String exprStr2 = null;
		if (current < next) {
			exprStr2 = percentMinimum(exprStr, current, next);
		} else if (current != next) {
			String firstPart = percentMinimum(exprStr, current, maximum, next
					- minimum, next - minimum);
			String secondPart = percentMinimum(exprStr, minimum, next, 0.0,
					maximum - current);
			exprStr2 = sum(firstPart, secondPart);
		}
		return sum(exprStr1, exprStr2);
	}
}
