disruptionsDataFile <- "C:/Project/data/paxdelay/disruptions/PassengerDisruptionFeatures.csv"
disruptionsData <- read.table(disruptionsDataFile, header = TRUE, sep = ",")

hourlyDisruptionDelays <- 
  aggregate(disruptionsData$Trip_Delay * disruptionsData$Number_Passengers,
    by = list(disruptionsData$Actual_Disruption_Hour), FUN = sum)
hourlyDisruptionDelays <- as.data.frame(hourlyDisruptionDelays)
colnames(hourlyDisruptionDelays) <- c("Disruption_Hour",
  "Total_Disruption_Delay")

hourlyDisruptedPax <- aggregate(disruptionsData$Number_Passengers,
  by = list(disruptionsData$Actual_Disruption_Hour), FUN = sum)
hourlyDisruptedPax <- as.data.frame(hourlyDisruptedPax)
colnames(hourlyDisruptedPax) <- c("Disruption_Hour",
  "Number_Passengers")
 
hourlyDisruptionResults <- merge(hourlyDisruptedPax,
  hourlyDisruptionDelays, by = "Disruption_Hour")
  
write.csv(hourlyDisruptionResults, 
  file = "C:/Project/output/paxdelay/disruptions/HourlyDisruptionDelays.csv",
  row.names = FALSE)

cancelationData <- disruptionsData[
  which(disruptionsData$First_Disruption_Cause == 2), ]
hourlyCancelationDelays <- 
  aggregate(cancelationData$Trip_Delay * cancelationData$Number_Passengers,
  by = list(cancelationData$Actual_Disruption_Hour), FUN = sum)
hourlyCancelationDelays <- as.data.frame(hourlyCancelationDelays)
colnames(hourlyCancelationDelays) <- c("Cancelation_Hour",
  "Total_Cancelation_Delay")

hourlyCanceledPax <- aggregate(cancelationData$Number_Passengers,
  by = list(cancelationData$Actual_Disruption_Hour), FUN = sum)
hourlyCanceledPax <- as.data.frame(hourlyCanceledPax)
colnames(hourlyCanceledPax) <- c("Cancelation_Hour",
  "Number_Passengers")
  
hourlyCancelationResults <- merge(hourlyCanceledPax,
  hourlyCancelationDelays, by = "Cancelation_Hour")
  
write.csv(hourlyCancelationResults, 
  file = "C:/Project/output/paxdelay/disruptions/HourlyCancelationDelays.csv",
  row.names = FALSE)

misconnectionData <- disruptionsData[
  which(disruptionsData$First_Disruption_Cause == 1), ]
hourlyMisconnectionDelays <- 
  aggregate(misconnectionData$Trip_Delay * misconnectionData$Number_Passengers,
  by = list(misconnectionData$Actual_Disruption_Hour), FUN = sum)
hourlyMisconnectionDelays <- as.data.frame(hourlyMisconnectionDelays)
colnames(hourlyMisconnectionDelays) <- c("Misconnection_Hour",
  "Total_Misconnection_Delay")

hourlyMisconnectedPax <- aggregate(misconnectionData$Number_Passengers,
  by = list(misconnectionData$Actual_Disruption_Hour), FUN = sum)
hourlyMisconnectedPax <- as.data.frame(hourlyMisconnectedPax)
colnames(hourlyMisconnectedPax) <- c("Misconnection_Hour",
  "Number_Passengers")
  
hourlyMisconnectionResults <- merge(hourlyMisconnectedPax,
  hourlyMisconnectionDelays, by = "Misconnection_Hour")
  
write.csv(hourlyMisconnectionResults, 
  file = "C:/Project/output/paxdelay/disruptions/HourlyMisconnectionDelays.csv",
  row.names = FALSE)

hourBuckets <- c(5, 12, 19)
numHourBuckets <- length(hourBuckets)
breakSize <- 60
maxDelay <- max(disruptionsData$Trip_Delay)
firstBreak <- -1 * breakSize
lastBreak <- ceiling(maxDelay / breakSize) * breakSize
histBreaks = seq(from = firstBreak, to = lastBreak, by = breakSize)
numBreaks <- length(histBreaks)
bucketedResults <- matrix(data = 0, nrow = numBuckets, 
  ncol = numBreaks - 1)
rownames(bucketedResults) <-
  paste(hourBuckets, c(hourBuckets[2 : length(hourBuckets)],
    hourBuckets[1]), sep = "_")
colnames(bucketedResults) <- 
  paste(histBreaks[1 : (length(histBreaks) - 1)],
    histBreaks[2 : length(histBreaks)], sep = "_")
for (i in 1 : numBuckets) {
  if (i < numBuckets) {
    bucketData <- disruptionsData[
      which(disruptionsData$Actual_Disruption_Hour >= hourBuckets[i] &
        disruptionsData$Actual_Disruption_Hour < hourBuckets[i + 1]), ]
  } else {
    bucketData <- disruptionsData[
      which(disruptionsData$Actual_Disruption_Hour >= hourBuckets[i] |
        disruptionsData$Actual_Disruption_Hour < hourBuckets[1]), ]
  }
  histBuckets <- ceiling(bucketData$Trip_Delay / breakSize)
  histCounts <- as.data.frame(aggregate(bucketData$Number_Passengers,
    by = list(histBuckets), FUN = sum))
  colnames(histCounts) <- c("Histogram_Bucket", "Passengers")
  Histogram_Bucket <- seq(from = 0, to = (lastBreak / breakSize), by = 1)
  allBuckets <- as.data.frame(Histogram_Bucket)
  allCounts <- merge(allBuckets, histCounts, all.x = TRUE)
  allCounts$Passengers[which(is.na(allCounts$Passengers))] <- 0
  allCounts <- allCounts[order(allCounts$Histogram_Bucket), ]
  bucketedResults[i, ] <- allCounts$Passengers
}

write.csv(bucketedResults,
  file = "C:/Project/output/paxdelay/disruptions/BucketedDelayFrequencies.csv",
  row.names = TRUE)

