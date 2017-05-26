//XuJiao
//Expand to whole year
//It took 2 min

package edu.mit.nsfnats.paxdelay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CreateItineraryLoadAllData {
	public static void main(String[] args) {
		
		long startTime = System.nanoTime();
		
		String[] carriers = {"YV","XE","WN","US","UA","OO","OH","NW","MQ","HA","FL","F9","EV","DL","CO","B6","AS","AQ","AA","9E"};
		String[] prefixes = {"Load"}; //{"Load", "External"}; VV: Mar 31st 2017. Because we don't need external files to create the database 
		String[] ms = {"Multiple","Single"};
		String[] month = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};//XuJ: 04/01/17. Expand to whole year
		boolean first = true;
		FileWriter fw = null;
		try {
			fw = new FileWriter("/mdsg/paxdelay_general_Xu/Allocation_Output/ItineraryLoad_AllData.csv");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(String p:prefixes){
			for(String c:carriers){
				for(String m:ms){
					for (String i:month){
						String fileName = "Itinerary"+p+"_Month_"+i+"_"+m+"_"+c+".txt";
//						System.out.println(fileName);
						try {
							Scanner in = new Scanner(new File("/mdsg/paxdelay_general_Xu/Allocation_Output/"+fileName));
							String line = "";
							if(!first){
								line = in.nextLine(); // To discard the header row
							}
							if(first) first = false;
							while (in.hasNextLine()) {
								line = in.nextLine();
//								System.out.println(line);
								String[] temp = line.split("\t");
								
								try {
									
									if(p.equals("Load")){
										fw.write(temp[0]);
										fw.write(",");
										fw.write(temp[1]);
										fw.write(",");
										fw.write(temp[2]);
										fw.write(",");
										fw.write(temp[3]);
										fw.write(",");
										fw.write(temp[4]);
										fw.write("\n");
									}else if(p.equals("External")){
										fw.write(temp[1]);
										fw.write(",");
										fw.write(temp[10]);
										fw.write(",");
										fw.write(temp[0]);
										fw.write(",");
										fw.write(temp[9]);
										fw.write(",");
										fw.write(temp[18]);
										fw.write("\n");
									}
									
								}
								catch (IOException e) {
									e.printStackTrace();
								}
							}
							in.close();
						} catch (FileNotFoundException e) {
						}
					}
				}	
			}
		}
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000/1000/60;
		System.out.println("That took " + duration + " minutes ");
	}
}
