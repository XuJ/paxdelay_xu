disruptionsDataFile <- "/home/dfearing/workspace/data/paxdelay/disruptions/PassengerDisruptionFeatures.csv"
disruptionsData <- read.table(disruptionsDataFile, header = TRUE, sep = ",")

totalPaxDelay <- aggregate(disruptionsData$Trip_Delay * disruptionsData$Number_Passengers, by = list(disruptionsData$Disruption_Carrier), FUN = sum)

numPax <- aggregate(disruptionsData$Number_Passengers, by = list(disruptionsData$Disruption_Carrier), FUN = sum)

averageCarrierDisruptionDelays <- cbind(as.data.frame(totalPaxDelay$Group.1), as.data.frame(totalPaxDelay$x), as.data.frame(numPax$x))
colnames(averageCarrierDisruptionDelays) <- c("Carrier", "Total_Disruption_Delay", "Number_Disrupted_Passengers")

totalAirportPaxDelay <- aggregate(disruptionsData$Trip_Delay * disruptionsData$Number_Passengers, by = list(disruptionsData$Disruption_Origin), FUN = sum)
numAirportPax <- aggregate(disruptionsData$Number_Passengers, by = list(disruptionsData$Disruption_Origin), FUN = sum)

averageAirportDisruptionDelays <- cbind(as.data.frame(totalAirportPaxDelay$Group.1), as.data.frame(totalAirportPaxDelay$x), as.data.frame(numAirportPax$x))
colnames(averageAirportDisruptionDelays) <- c("Airport", "Total_Disruption_Delay", "Number_Disrupted_Passengers")

write.csv(averageCarrierDisruptionDelays, file = '/home/dfearing/workspace/output/paxdelay/disruptions/CarrierDisruptionDelays.csv', row.names = FALSE)

write.csv(averageAirportDisruptionDelays, file = '/home/dfearing/workspace/output/paxdelay/disruptions/AirportDisruptionDelays.csv', row.names = FALSE)

