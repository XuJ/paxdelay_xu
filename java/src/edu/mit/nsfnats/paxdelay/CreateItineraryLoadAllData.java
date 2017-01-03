package edu.mit.nsfnats.paxdelay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CreateItineraryLoadAllData {
	public static void main(String[] args) {
		String[] carriers = {"YV","XE","WN","US","UA","OO","OH","NW","MQ","HA","FL","F9","EV","DL","CO","B6","AS","AQ","AA","9E"};
		String[] prefixes = {"Load", "External"};
		String[] ms = {"Multiple","Single"};
		boolean first = true;
		FileWriter fw = null;
		try {
			fw = new FileWriter("output/ItineraryLoad_AllData.csv");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(String p:prefixes){
			for(String c:carriers){
				for(String m:ms){
					String fileName = "Itinerary"+p+"_Month_01_"+m+"_"+c+".txt";
					System.out.println(fileName);
					try {
						Scanner in = new Scanner(new File("input/"+fileName));
						String line = "";
						if(!first){
							line = in.nextLine(); // To discard the header row
						}
						if(first) first = false;
						while (in.hasNextLine()) {
							line = in.nextLine();
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
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}