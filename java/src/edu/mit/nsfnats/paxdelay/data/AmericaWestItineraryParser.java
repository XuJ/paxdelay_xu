package edu.mit.nsfnats.paxdelay.data;

import java.io.*;
import java.util.*;

public class AmericaWestItineraryParser {

	String m_sFolderName = "C:\\project\\data\\paxdelay\\americawest\\";
	String m_sTicketDataFile = m_sFolderName + "ticket_data.csv"; // The only
																	// input
																	// file
	String m_sItineraryFile = m_sFolderName + "AmericaWestItineraries.txt"; // Output
																			// file
																			// 1
	String m_sFlightLegFile = m_sFolderName + "AmericaWestFlightLegs.txt"; // Output
																			// file
																			// 2

	Map<String, Integer> m_oMonthToDays;
	Map<String, String> m_oMonthStringToInt;

	public void Initialize() {
		m_oMonthToDays = new HashMap<String, Integer>();
		m_oMonthToDays.put("JAN", 0);
		m_oMonthToDays.put("FEB", 31);
		m_oMonthToDays.put("MAR", 60);
		m_oMonthToDays.put("APR", 91);
		m_oMonthToDays.put("MAY", 121);
		m_oMonthToDays.put("JUN", 152);
		m_oMonthToDays.put("JUL", 182);
		m_oMonthToDays.put("AUG", 213);
		m_oMonthToDays.put("SEP", 244);
		m_oMonthToDays.put("OCT", 274);
		m_oMonthToDays.put("NOV", 305);
		m_oMonthToDays.put("DEC", 335);
		m_oMonthStringToInt = new HashMap<String, String>();
		m_oMonthStringToInt.put("JAN", "01");
		m_oMonthStringToInt.put("FEB", "02");
		m_oMonthStringToInt.put("MAR", "03");
		m_oMonthStringToInt.put("APR", "04");
		m_oMonthStringToInt.put("MAY", "05");
		m_oMonthStringToInt.put("JUN", "06");
		m_oMonthStringToInt.put("JUL", "07");
		m_oMonthStringToInt.put("AUG", "08");
		m_oMonthStringToInt.put("SEP", "09");
		m_oMonthStringToInt.put("OCT", "10");
		m_oMonthStringToInt.put("NOV", "11");
		m_oMonthStringToInt.put("DEC", "12");

	}

	public int GetMinutes(String sDate, int iTime) {
		int iDay = Integer.parseInt(sDate.substring(0, 2));
		String sMonth = sDate.substring(2, 5);
		if (!m_oMonthToDays.containsKey(sMonth)) {
			System.out.println("m_oMonthToDays.size() = "
					+ m_oMonthToDays.size());
			return -999;
		}
		int iYear = Integer.parseInt(sDate.substring(5));
		if (iYear < 2004) {
			return -999;
		}
		int iYearOffset = 0;
		iYearOffset = (iYear - 2004) * 365 + (iYear == 2004 ? 0 : 1); // 1 is
																		// for
																		// 2004
																		// being
																		// a
																		// leap
																		// year
		// System.out.println("iYearOffset = " + iYearOffset);
		int iDays = iDay + m_oMonthToDays.get(sMonth) - 1;
		int iMin = iTime % 100;
		int iHr = (iTime - iMin) / 100;
		int iTotalMins = iYearOffset + iDays * 24 * 60 + iHr * 60 + iMin;
		return iTotalMins;
	}

	public String GetFormattedTime(int iTime) {
		String sTime = ((iTime - iTime % 100) / 100) + ":";
		String sMin = ((iTime % 100) < 10) ? ("0" + (iTime % 100))
				: ("" + (iTime % 100));
		sTime = sTime + sMin;
		return sTime;
	}

	public String GetFormattedDate(String sDate) {
		String sDay = sDate.substring(0, 2);
		String sMonth = sDate.substring(2, 5);
		String sYear = sDate.substring(5);
		String sFormattedDate = m_oMonthStringToInt.get(sMonth) + "/" + sDay
				+ "/" + sYear;
		return sFormattedDate;
	}

	public void ReadTicketData() {
		try {
			FileReader fr = new FileReader(m_sTicketDataFile);
			BufferedReader in = new BufferedReader(fr);
			String line = in.readLine();
			line = in.readLine(); // To discard the header row

			FileWriter fwi = new FileWriter(m_sItineraryFile);
			FileWriter fwfl = new FileWriter(m_sFlightLegFile);

			fwi
					.write("Itinerary_ID\tTicket_Number\tNum_Flights\tOrigin\tDestination\tDeparture_Time\tDeparture_Date\tItinerary_Fare\tPassengers\n");
			fwfl
					.write("Itinerary_ID\tTicket_Number\tNum_Flights\tItinerary_Sequence\tCarrier\tFlight_Number\tDeparture_Date\tDeparture_Time\tArrival_Time\tOrigin\tDestination\tFareClass\tFare\n");

			int iItinID = 1;
			int iCount = 0;
			int iNullData = 0;
			int iOtherYears = 0;
			int iODSame = 0;
			int iBrokenTrip = 0;
			int iLongLayover = 0;
			int iNegativeLayover = 0;
			while (line != null) {
				iCount++;
				String[] temp = line.split(",");
				int iCnt = Integer.parseInt(temp[12]);
				boolean[] baTripBreak = new boolean[iCnt - 1];
				if (temp[9].equals("") || temp[10].equals("")
						|| temp[11].equals("")) {
					line = in.readLine();
					iNullData++;
					continue;
				}
				boolean bNullData = false;
				boolean bBrokenTrip = false;
				for (int i = 0; i < iCnt - 1; i++) {
					if (temp[14 + i * 8].equals("")
							|| temp[15 + i * 8].equals("")
							|| temp[18 + i * 8].equals("")) {
						bNullData = true;
						break;
					}
				}
				if (bNullData) {
					line = in.readLine();
					iNullData++;
					continue;
				}
				String sTktNo = temp[0];
				String sTOrg = temp[4];
				String sTDes = temp[5];
				if (sTOrg.equals(sTDes)) {
					iODSame++;
				}
				// String sTDate = temp[7];

				String sFYear1 = temp[6].substring(5);

				if (!sFYear1.equals("2004")) {
					iOtherYears++;
					line = in.readLine();
					continue;
				}

				String[] saFlt = new String[iCnt];
				String[] saOrg = new String[iCnt];
				String[] saDes = new String[iCnt];
				String[] saFDate = new String[iCnt];
				String[] saCls = new String[iCnt];
				double[] daFare = new double[iCnt];
				int[] iaArr = new int[iCnt];
				int[] iaDep = new int[iCnt];

				saFlt[0] = temp[1];
				saOrg[0] = temp[2];
				saDes[0] = temp[3];
				saFDate[0] = temp[6];
				saCls[0] = temp[8];
				daFare[0] = Double.parseDouble(temp[9]);
				iaArr[0] = Integer.parseInt(temp[10]);
				iaDep[0] = Integer.parseInt(temp[11]);

				boolean bLongLayover = false;
				boolean bNegativeLayover = false;
				for (int i = 0; i < iCnt - 1; i++) {
					baTripBreak[i] = false;
					saFlt[i + 1] = temp[12 + i * 8 + 1];
					saOrg[i + 1] = temp[12 + i * 8 + 2];
					saDes[i + 1] = temp[12 + i * 8 + 3];
					if (!saOrg[i + 1].equals(saDes[i])) {
						bBrokenTrip = true;
						baTripBreak[i] = true;
					}

					saFDate[i + 1] = temp[12 + i * 8 + 4];
					saCls[i + 1] = temp[12 + i * 8 + 5];
					daFare[i + 1] = Double.parseDouble(temp[12 + i * 8 + 6]);
					iaArr[i + 1] = Integer.parseInt(temp[12 + i * 8 + 7]);
					iaDep[i + 1] = Integer.parseInt(temp[12 + i * 8 + 8]);
					int a = GetMinutes(saFDate[i + 1], iaDep[i + 1]);
					int b = GetMinutes(saFDate[i], iaArr[i]);
					if (a < 0 || b < 0) {
						System.out.println(saFDate[i + 1] + "\t" + iaDep[i + 1]
								+ "\t" + saFDate[i] + "\t" + iaArr[i]);
						System.out.println(a + "\t" + b + "\t" + (a - b));
					}
					// System.out.println(saFDate[i+1] + "\t" + iaDep[i+1] +
					// "\t" + saFDate[i] + "\t" + iaArr[i]);
					// System.out.println(a + "\t" + b + "\t" + (a-b));
					int iLayover = a - b;
					if (iLayover < 0) {
						bNegativeLayover = true;
						break;
					}
					if (iLayover > 6 * 60) {
						bLongLayover = true;
						baTripBreak[i] = true;
					}
				}

				if (bNegativeLayover) {
					iNegativeLayover++;
					line = in.readLine();
					continue;
				}

				if (bBrokenTrip)
					iBrokenTrip++;
				if (bLongLayover && (!bBrokenTrip))
					iLongLayover++;

				int iStartLeg = 0;
				int iEndLeg = iCnt - 1;

				int i = 0;
				while (i < iCnt) {
					iStartLeg = i;

					double dItinFare = 0;
					while (i < iCnt - 1 && (!baTripBreak[i])) {
						dItinFare += daFare[i];
						i++;
					}
					dItinFare += daFare[i];
					iEndLeg = i;
					int iNumFlights = iEndLeg - iStartLeg + 1;
					String sOrg = saOrg[iStartLeg];
					String sDes = saDes[iEndLeg];
					String sDepDate = GetFormattedDate(saFDate[iStartLeg]);
					String sDepTime = GetFormattedTime(iaDep[iStartLeg]);
					int iPass = 1;
					fwi.write(iItinID + "\t" + sTktNo + "\t" + iNumFlights
							+ "\t" + sOrg + "\t" + sDes + "\t" + sDepTime
							+ "\t" + sDepDate + "\t" + dItinFare + "\t" + iPass
							+ "\n");

					String sCarrier = "HP";
					for (int j = iStartLeg; j <= iEndLeg; j++) {
						int iItinSequence = j - iStartLeg + 1;
						String sFlDepTime = GetFormattedTime(iaDep[j]);
						String sFlArrTime = GetFormattedTime(iaArr[j]);
						String sFlDepDate = GetFormattedDate(saFDate[j]);

						fwfl.write(iItinID + "\t" + sTktNo + "\t" + iNumFlights
								+ "\t" + iItinSequence + "\t" + sCarrier + "\t"
								+ saFlt[j] + "\t" + sFlDepDate + "\t"
								+ sFlDepTime + "\t" + sFlArrTime + "\t"
								+ saOrg[j] + "\t" + saDes[j] + "\t" + saCls[j]
								+ "\t" + daFare[j] + "\n");
					}

					i++;
					iItinID++;
				}

				line = in.readLine();
			}

			System.out.println("iNullData = " + iNullData);
			System.out.println("iNegativeLayover = " + iNegativeLayover);
			System.out.println("iOtherYears = " + iOtherYears);
			System.out.println("iODSame = " + iODSame);
			System.out.println("iBrokenTrip = " + iBrokenTrip);
			System.out.println("iLongLayover = " + iLongLayover);
			System.out.println("iCount = " + iCount);

			in.close();
			fr.close();
			fwi.close();
			fwfl.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		AmericaWestItineraryParser f = new AmericaWestItineraryParser();
		f.Initialize();
		f.ReadTicketData();
	}

}
