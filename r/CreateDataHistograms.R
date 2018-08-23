disruptionsDataFile <- "/home/dfearing/workspace/data/paxdelay/disruptions/PassengerDisruptionFeatures.csv"
disruptionsData <- read.table(disruptionsDataFile, header = TRUE, sep = ",")

histData <- hist(disruptionsData$Trip_Delay, breaks = 100, plot = FALSE)
histOutput <- cbind(histData$breaks[1: (length(histData$breaks) - 1)], histData$counts)
colnames(histOutput) <- c("Histogram_Breaks", "Histogram_Counts")
write.table(histOutput, file = "/home/dfearing/workspace/output/paxdelay/analysis/DisruptionDelayHistogram.csv", sep = ",", row.names = FALSE)

daytimeDisruptions <- disruptionsData[which(disruptionsData$Estimated_Disruption_Hour >= 5 & disruptionsData$Estimated_Disruption_Hour < 17),]
daytimeHistData <- hist(daytimeDisruptions$Trip_Delay, breaks = 100, plot = FALSE)
daytimeHistOutput <- cbind(daytimeHistData$breaks[1: (length(daytimeHistData$breaks) - 1)], daytimeHistData$counts)
colnames(daytimeHistOutput) <- c("Histogram_Breaks", "Histogram_Counts")
write.table(daytimeHistOutput, file = "/home/dfearing/workspace/output/paxdelay/analysis/DaytimeDisruptionDelayHistogram.csv", sep = ",", row.names = FALSE)

eveningDisruptions <- disruptionsData[which(disruptionsData$Estimated_Disruption_Hour >= 17 | disruptionsData$Estimated_Disruption_Hour < 5),]
eveningHistData <- hist(eveningDisruptions$Trip_Delay, breaks = 100, plot = FALSE)
eveningHistOutput <- cbind(eveningHistData$breaks[1: (length(eveningHistData$breaks) - 1)], eveningHistData$counts)
colnames(eveningHistOutput) <- c("Histogram_Breaks", "Histogram_Counts")
write.table(eveningHistOutput, file = "/home/dfearing/workspace/output/paxdelay/analysis/EveningDisruptionDelayHistogram.csv", sep = ",", row.names = FALSE)


cancellationData <- disruptionsData[which(disruptionsData$First_Disruption_Cause == 2),]
cancellationHistData <- hist(cancellationData$Trip_Delay, breaks = 100, plot = FALSE)
cancellationHistOutput <- cbind(cancellationHistData$breaks[1: (length(cancellationHistData$breaks) - 1)], cancellationHistData$counts)
colnames(cancellationHistOutput) <- c("Histogram_Breaks", "Histogram_Counts")
write.table(cancellationHistOutput, file = "/home/dfearing/workspace/output/paxdelay/analysis/CancellationDelayHistogram.csv", sep = ",", row.names = FALSE)

missedConnectionData <- disruptionsData[which(disruptionsData$First_Disruption_Cause == 1),]
missedConnectionHistData <- hist(missedConnectionData$Trip_Delay, breaks = 100, plot = FALSE)
missedConnectionHistOutput <- cbind(missedConnectionHistData$breaks[1: (length(missedConnectionHistData$breaks) - 1)], missedConnectionHistData$counts)
colnames(missedConnectionHistOutput) <- c("Histogram_Breaks", "Histogram_Counts")
write.table(missedConnectionHistOutput, file = "/home/dfearing/workspace/output/paxdelay/analysis/MissedConnectionDelayHistogram.csv", sep = ",", row.names = FALSE)


