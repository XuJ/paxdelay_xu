package edu.mit.nsfnats.paxdelay.choice;

public class CompleteBiogemeModel extends BiogemeModel {

	@Override
	public void initializeExpressions() {
		ExpressionSequence numChoices = new GeneralFeatureSequence(
				m_numAlternatives, "NC", "Number_Choices");
		int[] choiceThresholds = new int[m_numAlternatives];
		for (int i = 0; i < choiceThresholds.length; ++i) {
			choiceThresholds[i] = (i + 1);
		}

		m_availExprSeq = new ThresholdExpressionSequence(m_numAlternatives,
				numChoices, choiceThresholds, true);

		ExpressionSequence deltaTime = new GeneralFeatureSequence(
				m_numAlternatives, "DT", "Delta_Time");
		ExpressionSequence rangedDeltaTime = new RangedExpressionSequence(
				m_numAlternatives, deltaTime, new double[] { 6.0, 18.0 }, 0,
				24, true);

		ExpressionSequence hourOfDay = new AlternativeFeatureSequence(
				m_numAlternatives, "HR", "Hour_of_Day");
		ExpressionSequence rangedHourOfDay = new RangedExpressionSequence(
				m_numAlternatives, hourOfDay, new double[] { 1.0, 5.0, 9.0,
						13.0, 17.0, 21.0 }, 0, 24, true);

		ExpressionSequence dayOfWeek = new AlternativeFeatureSequence(
				m_numAlternatives, "DW", "Day_of_Week");
		ExpressionSequence discreteDayOfWeek = new DiscreteExpressionSequence(
				m_numAlternatives, dayOfWeek, new int[] { 1, 2, 3, 4, 5, 6, 7 });

		/*
		 * ExpressionSequence deltaCrossHour = new
		 * CrossProductExpressionSequence(m_numAlternatives, rangedDeltaTime,
		 * rangedHourOfDay);
		 */

		ExpressionSequence dayCrossHour = new CrossProductExpressionSequence(
				m_numAlternatives, discreteDayOfWeek, rangedHourOfDay);

		ExpressionSequence deltaCrossDayHour = new CrossProductExpressionSequence(
				m_numAlternatives, rangedDeltaTime, dayCrossHour);

		ExpressionSequence numFlights = new GeneralFeatureSequence(
				m_numAlternatives, "NF", "Number_Flights");
		int[] flightThresholds = new int[m_numAlternatives];
		for (int i = 0; i < flightThresholds.length; ++i) {
			flightThresholds[i] = 2;
		}
		ExpressionSequence multiLeg = new ThresholdExpressionSequence(
				m_numAlternatives, numFlights, flightThresholds, true);

		ExpressionSequence layoverDuration = new AlternativeFeatureSequence(
				m_numAlternatives, "LM", "Layover_Minutes");
		ExpressionSequence piecewiseLayoverDuration = new PiecewiseLinearExpressionSequence(
				m_numAlternatives, layoverDuration, new int[] { 45, 60 });

		ExpressionSequence multiLegLayover = new CrossProductExpressionSequence(
				m_numAlternatives, multiLeg, piecewiseLayoverDuration);
		/*
		 * m_primaryExprSeqs = new ExpressionSequence[]{ deltaCrossHour,
		 * dayCrossHour, multiLegLayover };
		 */

		m_primaryExprSeqs = new ExpressionSequence[] { deltaCrossDayHour,
				multiLegLayover };
	}
}
