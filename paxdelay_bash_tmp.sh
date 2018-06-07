#080617 XuJ: this is the master file to run the whole paxdelay codes
#!/bin/bash
javac -d "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/" -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/:/mdsg/paxdelay_general_Xu/java/lib/*" /thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/edu/mit/nsfnats/paxdelay/data/ItineraryGenerator.java \
&& java -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/:/mdsg/paxdelay_general_Xu/java/lib/*" edu.mit.nsfnats.paxdelay.data.ItineraryGenerator \
&& javac -d "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/" -cp "/mdsg/paxdelay_general_Xu/java/lib/*" /thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/edu/mit/nsfnats/paxdelay/CreateTempItinerariesTable.java \
&& java -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/:/mdsg/paxdelay_general_Xu/java/lib/*" edu.mit.nsfnats.paxdelay.CreateTempItinerariesTable \
&& javac -d "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/" -cp "/mdsg/paxdelay_general_Xu/java/lib/*" /thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/edu/mit/nsfnats/paxdelay/CreateTempItinerariesTable_load.java \
&& java -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/:/mdsg/paxdelay_general_Xu/java/lib/*" edu.mit.nsfnats.paxdelay.CreateTempItinerariesTable_load \
&& mysql -u root -ppaxdelay < /mdsg/paxdelay_general_Xu/sql/Itineraries.sql \
&& mv /mdsg/paxdelay_general_Xu/Allocation_Output /mdsg/paxdelay_general_Xu/Allocation_Output_2015 \
&& mkdir /mdsg/paxdelay_general_Xu/Allocation_Output \
&& javac -d "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/" -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/:/mdsg/paxdelay_general_Xu/java/lib/*" /thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/edu/mit/nsfnats/paxdelay/allocation/AutomatedPassengerAllocator.java \
&& java -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/:/mdsg/paxdelay_general_Xu/java/lib/*" edu.mit.nsfnats.paxdelay.allocation.AutomatedPassengerAllocator \
&& javac -d "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/" -cp "/mdsg/paxdelay_general_Xu/java/lib/*" /thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/edu/mit/nsfnats/paxdelay/CreateItineraryLoadAllData.java \
&& java -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/:/mdsg/paxdelay_general_Xu/java/lib/*" edu.mit.nsfnats.paxdelay.CreateItineraryLoadAllData \
&& mysql -u root -ppaxdelay < /mdsg/paxdelay_general_Xu/sql/ItineraryAllocations.sql \
&& mv /mdsg/paxdelay_general_Xu/ProcessedItineraryDelays.csv /mdsg/paxdelay_general_Xu/ProcessedItineraryDelays_2015.csv \
&& javac -d "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/" -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/:/mdsg/paxdelay_general_Xu/java/lib/*" /thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/src/edu/mit/nsfnats/paxdelay/calculation/PassengerDelayCalculator.java \
&& java -cp "/thayerfs/home/f002bmp/workspace/paxdelay_general_Xu/bin/:/mdsg/paxdelay_general_Xu/java/lib/*" edu.mit.nsfnats.paxdelay.calculation.PassengerDelayCalculator \
&& mysql -u root -ppaxdelay < /mdsg/paxdelay_general_Xu/sql/PaxDelayCalculation.sql \
&& mysqldump -u root -ppaxdelay paxdelay > /mdsg/mysql_backup/paxdelay_2016.sql \
&& mysql -u root -ppaxdelay < /mdsg/paxdelay_general_Xu/sql/Backup.sql \
&& mysql -u root -ppaxdelay < /mdsg/paxdelay_general_Xu/sql/Table5Creation.sql
