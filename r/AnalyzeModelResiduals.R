outputDirectory <- "/home/dfearing/workspace/output/paxdelay/disruptions"
modelRDataFile <- paste(outputDirectory, "performanceDisruptionsModel.RData",
  sep = "/")
load(modelRDataFile)

One_Stop_Itineraries <- modelData$One_Stop_First + modelData$One_Stop_Second
maxOneStop <- max(One_Stop_Itineraries)
bucketSize <- 0.5
numBuckets <- ceiling(maxOneStop / bucketSize)
Itinerary_Counts <- rep(0, numBuckets)
Average_Residuals <- rep(0, numBuckets)
for (i in 1 : numBuckets)  {
  matchingRows <- which(One_Stop_Itineraries >= ((i - 1) * bucketSize) &
    One_Stop_Itineraries < i * bucketSize)
  if (length(matchingRows > 0)) {
    Itinerary_Counts[i] <- mean(One_Stop_Itineraries[matchingRows])
    Average_Residuals[i] <- mean(modelData[matchingRows, "Residuals"])
  }
}
results <- as.data.frame(cbind(Itinerary_Counts, Average_Residuals))
residualsOutputFile = paste(outputDirectory, "ResidualsAnalysis_30.csv",
  sep = "/")
write.csv(results, file = residualsOutputFile, row.names = FALSE)

