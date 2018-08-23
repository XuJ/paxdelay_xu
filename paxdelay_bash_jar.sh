#052817 XuJ: this is the master file to run the whole paxdelay codes
#!/bin/bash
java -jar ~/paxdelay_general_Xu/paxdelay_2011/LoadT100SegmentData.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/LoadDB1BCoupons.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateDB1BUniqueSegments.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateDB1BRouteDemandsTable.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateDB1BT100SegmentComparisons.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateT100DB1BRouteDemandsTable.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateRouteDemandsTable.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/LoadAOTPData.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/LoadScheduleB43Data.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateAircraftCodeMappingsTable.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateAircraftCodeMappingsTable_load.jar \
&& mysql -u root -ppaxdelay < ~/paxdelay_general_Xu/paxdelay_2011/Flights.sql \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateAOTPCarriersTable.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateUniqueCarrierRoutesTable.jar \
&& mv /mdsg/paxdelay_general_Xu/GeneratedItineraries.csv /mdsg/paxdelay_general_Xu/GeneratedItineraries_2010.csv \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/ItineraryGenerator.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateTempItinerariesTable.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateTempItinerariesTable_load.jar \
&& mysql -u root -ppaxdelay < ~/paxdelay_general_Xu/paxdelay_2011/Itineraries.sql \
&& mv /mdsg/paxdelay_general_Xu/Allocation_Output /mdsg/paxdelay_general_Xu/Allocation_Output_2010 \
&& mkdir /mdsg/paxdelay_general_Xu/Allocation_Output \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/AutomatedPassengerAllocator.jar \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/CreateItineraryLoadAllData.jar \
&& mysql -u root -ppaxdelay < ~/paxdelay_general_Xu/paxdelay_2011/ItineraryAllocations.sql \
&& mv /mdsg/paxdelay_general_Xu/ProcessedItineraryDelays.csv /mdsg/paxdelay_general_Xu/ProcessedItineraryDelays_2010.csv \
&& java -jar ~/paxdelay_general_Xu/paxdelay_2011/PassengerDelayCalculator.jar \
&& mysql -u root -ppaxdelay < ~/paxdelay_general_Xu/paxdelay_2011/PaxDelayCalculation.sql \
&& mysqldump -u root -ppaxdelay paxdelay > /mdsg/mysql_backup/paxdelay_2011.sql \
&& mysql -u root -ppaxdelay < ~/paxdelay_general_Xu/paxdelay_2011/Backup.sql \
&& mysql -u root -ppaxdelay < ~/paxdelay_general_Xu/paxdelay_2011/Table5Creation.sql
