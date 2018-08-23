package edu.mit.nsfnats.paxdelay.choice;

public class UpdatedBiogemeModel extends BiogemeModel {

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

		ExpressionSequence shortDeltaTime = new RangedExpressionSequence(
				m_numAlternatives, deltaTime, new double[] { 6.0, 18.0 }, 0,
				24, true, new boolean[] { true, false });

		ExpressionSequence longDeltaTime = new RangedExpressionSequence(
				m_numAlternatives, deltaTime, new double[] { 6.0, 18.0 }, 0,
				24, false);

		ExpressionSequence hourOfDay = new AlternativeFeatureSequence(
				m_numAlternatives, "HR", "Hour_of_Day");
		ExpressionSequence rangedShortHourOfDay = new RangedExpressionSequence(
				m_numAlternatives, hourOfDay, new double[] { 0.0, 5.0, 9.0,
						14.0, 19.0, 24.0 }, 0, 24, false);

		ExpressionSequence rangedLongHourOfDay = new RangedExpressionSequence(
				m_numAlternatives, hourOfDay, new double[] { 5.0, 11.0, 16.0,
						21.0, 19.0 }, 0, 24, false);

		ExpressionSequence dayOfWeek = new AlternativeFeatureSequence(
				m_numAlternatives, "DW", "Day_of_Week");
		ExpressionSequence discreteDayOfWeek = new DiscreteExpressionSequence(
				m_numAlternatives, dayOfWeek, new int[] { 1, 2, 3, 4, 5, 6, 7 });

		/*
		 * ExpressionSequence deltaCrossHour = new
		 * CrossProductExpressionSequence(m_numAlternatives, rangedDeltaTime,
		 * rangedHourOfDay);
		 */

		ExpressionSequence dayCrossShortHour = new CrossProductExpressionSequence(
				m_numAlternatives, discreteDayOfWeek, rangedShortHourOfDay);

		ExpressionSequence dayCrossLongHour = new CrossProductExpressionSequence(
				m_numAlternatives, discreteDayOfWeek, rangedLongHourOfDay);

		ExpressionSequence deltaShortCrossDayHour = new CrossProductExpressionSequence(
				m_numAlternatives, shortDeltaTime, dayCrossShortHour);

		ExpressionSequence deltaLongCrossDayHour = new CrossProductExpressionSequence(
				m_numAlternatives, longDeltaTime, dayCrossLongHour);

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

		m_primaryExprSeqs = new ExpressionSequence[] { deltaShortCrossDayHour,
				deltaLongCrossDayHour, multiLegLayover };
	}
}
