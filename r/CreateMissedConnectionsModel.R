misconnectionsDataFile <- 
# "C:/Users/admin/Desktop/work/R/MissedConnectionsData.csv"
  "C:/Users/admin/Desktop/work/R/QuartileRegressionData.csv"
misconnectionsData <- 
  read.table(misconnectionsDataFile, header = TRUE, sep = ",")

Misconnection_Percent <- misconnectionsData$MisConnPax / 
  misconnectionsData$Pax
misconnectionsData <- cbind(misconnectionsData, Misconnection_Percent)

misconnectionsFormula <- "Misconnection_Percent ~ FD25 + FD50 + FD75 + 
  FD95 + CT05 + CT25 + CT50 + CT75 + CT95"

unweightedModel <- 
  lm(formula = misconnectionsFormula, data = misconnectionsData, 
  model = FALSE)

summary(unweightedModel)

weightedModel <- 
  lm(formula = misconnectionsFormula, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)
  
summary(weightedModel)

FD50_FD25 <- misconnectionsData$FD50 - misconnectionsData$FD25
FD75_FD50 <- misconnectionsData$FD75 - misconnectionsData$FD50
FD95_FD75 <- misconnectionsData$FD95 - misconnectionsData$FD75

FDD50_FDD25 <- misconnectionsData$FDD50 - misconnectionsData$FDD25
FDD75_FDD50 <- misconnectionsData$FDD75 - misconnectionsData$FDD50
FDD95_FDD75 <- misconnectionsData$FDD95 - misconnectionsData$FDD75

CT25_CT05 <- misconnectionsData$CT25 - misconnectionsData$CT05
CT50_CT25 <- misconnectionsData$CT50 - misconnectionsData$CT25
CT75_CT50 <- misconnectionsData$CT75 - misconnectionsData$CT50
CT95_CT75 <- misconnectionsData$CT95 - misconnectionsData$CT75
LCD <- misconnectionsData$LCD
RCD <- misconnectionsData$RCD

misconnectionsData <- cbind(misconnectionsData, FD50_FD25, FD75_FD50,
  FD95_FD75, CT25_CT05, CT50_CT25, CT75_CT50, CT95_CT75, LCD, RCD)

differenceFormula <- "Misconnection_Percent ~ FD25 + FD50_FD25 + 
  FD75_FD50 + FD95_FD75 + CT05 + CT25_CT05 + CT50_CT25 + CT75_CT50 + 
  CT95_CT75 + LCD + RCD"

differenceFormula0D <- "Misconnection_Percent ~ FDD50"

differenceFormula0 <- "Misconnection_Percent ~ FD50"

differenceFormula1D <- "Misconnection_Percent ~ FDD25 + FDD50_FDD25 + 
  FDD75_FDD50 + FDD95_FDD75"

differenceFormula1 <- "Misconnection_Percent ~ FD25 + FD50_FD25 + 
  FD75_FD50 + FD95_FD75"

differenceFormula2D <- "Misconnection_Percent ~ FDD25 + FDD50_FDD25 + 
  FDD75_FDD50 + FDD95_FDD75 + CT05 + CT25_CT05 + CT50_CT25 + CT75_CT50 + 
  CT95_CT75 + LCD + RCD"

differenceFormula2 <- "Misconnection_Percent ~ FD25 + FD50_FD25 + 
  FD75_FD50 + FD95_FD75 + CT05 + CT25_CT05 + CT50_CT25 + CT75_CT50 + 
  CT95_CT75 + LCD + RCD"

differenceFormula3D <- "Misconnection_Percent ~ FDD25 + FDD50_FDD25 + 
  FDD75_FDD50 + FDD95_FDD75 + CT05 + CT25_CT05 + CT50_CT25 + CT75_CT50 + 
  CT95_CT75"

differenceFormula3 <- "Misconnection_Percent ~ FD25 + FD50_FD25 + 
  FD75_FD50 + FD95_FD75 + CT05 + CT25_CT05 + CT50_CT25 + CT75_CT50 + 
  CT95_CT75"
  
unweightedDiffModel <- 
  lm(formula = differenceFormula, data = misconnectionsData, 
  model = FALSE)
  
summary(unweightedDiffModel)

weightedDiffModel0D <- 
  lm(formula = differenceFormula0D, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)

weightedDiffModel0 <- 
  lm(formula = differenceFormula0, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)

weightedDiffModel1D <- 
  lm(formula = differenceFormula1D, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)

weightedDiffModel1 <- 
  lm(formula = differenceFormula1, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)

weightedDiffModel2D <- 
  lm(formula = differenceFormula2D, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)

weightedDiffModel2 <- 
  lm(formula = differenceFormula2, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)

weightedDiffModel3D <- 
  lm(formula = differenceFormula3D, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)

weightedDiffModel3 <- 
  lm(formula = differenceFormula3, data = misconnectionsData, 
  weights = misconnectionsData$Pax, model = FALSE)

#summary(weightedDiffModel)


