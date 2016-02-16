disruptionsDataFile <- "/home/dfearing/workspace/data/paxdelay/disruptions/PassengerDisruptionFeatures.csv"
disruptionsData <- read.table(disruptionsDataFile, header = TRUE, sep = ",")

Overnight_Time_Until_Morning <- rep(0, dim(disruptionsData)[1])
Overnight_Time_Until_Morning[
  which(disruptionsData$Actual_Disruption_Hour < 5)] <- 5 - 
    disruptionsData$Actual_Disruption_Hour[
      which(disruptionsData$Actual_Disruption_Hour < 5)]
Disruption_Hour_Level <- rep(0, dim(disruptionsData)[1])
Disruption_Hour_Level[which(disruptionsData$Actual_Disruption_Hour > 5 &
  disruptionsData$Actual_Disruption_Hour < 22)] <- 
    disruptionsData$Actual_Disruption_Hour[
      which(disruptionsData$Actual_Disruption_Hour > 5 &
        disruptionsData$Actual_Disruption_Hour < 22)]
Disruption_Hour_Level[
  which(disruptionsData$Actual_Disruption_Hour >= 22)] <- 22
Disruption_Hour_Factor <- as.factor(Disruption_Hour_Level)
Non_Stop_Empty_Alternatives <- disruptionsData$Non_Stop_Itineraries * 
  (1 - disruptionsData$Non_Stop_Load_Factor)
nonStopEmptyBreak <- 3
Non_Stop_Empty_First <- Non_Stop_Empty_Alternatives
Non_Stop_Empty_First[which(Non_Stop_Empty_First > nonStopEmptyBreak)] <-
  nonStopEmptyBreak
Non_Stop_Empty_Second <- Non_Stop_Empty_Alternatives
Non_Stop_Empty_Second[which(Non_Stop_Empty_Second < nonStopEmptyBreak)] <-
  nonStopEmptyBreak
Non_Stop_Empty_Second <- Non_Stop_Empty_Second - nonStopEmptyBreak
One_Stop_Empty_Alternatives <- disruptionsData$One_Stop_Itineraries *
  (1 - disruptionsData$Fullest_One_Stop_Load_Factor)
Is_Cancellation <- disruptionsData$First_Disruption_Cause - 1
Number_Stops_Remaining <- disruptionsData$Number_Flights_Remaining - 1
Low_Cost_Carrier_Flag <- rep(0, dim(disruptionsData)[1])
Low_Cost_Carrier_Flag[which(disruptionsData$Disruption_Carrier %in% 
  c("B6", "F9", "FL", "WN"))] <- 1
Regional_Carrier_Flag <- rep(0, dim(disruptionsData)[1])
Regional_Carrier_Flag[which(disruptionsData$Disruption_Carrier %in%
  c("9E", "EV", "MQ","OH", "OO", "XE", "YV"))] <- 1
oneStopBreakOne <- 30
One_Stop_First <- disruptionsData$One_Stop_Itineraries
One_Stop_First[which(One_Stop_First > oneStopBreakOne)] <- oneStopBreakOne
One_Stop_Second <- disruptionsData$One_Stop_Itineraries
One_Stop_Second[which(One_Stop_Second < oneStopBreakOne)] <- oneStopBreakOne
One_Stop_Second <- One_Stop_Second - oneStopBreakOne

disruptionsData <- cbind(disruptionsData, Disruption_Hour_Factor, Overnight_Time_Until_Morning, Number_Stops_Remaining, Is_Cancellation, Non_Stop_Empty_Alternatives, Non_Stop_Empty_First, Non_Stop_Empty_Second, One_Stop_First, One_Stop_Second, One_Stop_Empty_Alternatives, Low_Cost_Carrier_Flag, Regional_Carrier_Flag)

modelData <- disruptionsData[, c("Trip_Delay", "Disruption_Hour_Factor", "Non_Stop_Empty_First", "Non_Stop_Empty_Second", "Overnight_Time_Until_Morning", "Is_Cancellation", "Number_Stops_Remaining", "One_Stop_First", "One_Stop_Second", "Primary_Airport_Flag", "Average_Departure_Delay", "Cancelation_Rate", "Number_Passengers")]

rm("Overnight_Time_Until_Morning", "Disruption_Hour_Level", "Disruption_Hour_Factor", "Non_Stop_Empty_Alternatives", "Non_Stop_Empty_First", "Non_Stop_Empty_Second", "One_Stop_First", "One_Stop_Second", "One_Stop_Empty_Alternatives", "Is_Cancellation", "Number_Stops_Remaining", "Low_Cost_Carrier_Flag", "Regional_Carrier_Flag", "disruptionsData")

disruptionsFormula <- "Trip_Delay ~ Disruption_Hour_Factor + Non_Stop_Empty_First + Non_Stop_Empty_Second + Overnight_Time_Until_Morning + Is_Cancellation + Number_Stops_Remaining + One_Stop_First + One_Stop_Second + Number_Stops_Remaining : One_Stop_First + Number_Stops_Remaining : One_Stop_Second + Primary_Airport_Flag + Average_Departure_Delay + Cancelation_Rate"

disruptionsModel <- 
  lm(formula = disruptionsFormula, data = modelData, 
  weights = modelData$Number_Passengers, model = FALSE)

Residuals <- disruptionsModel$residuals
Fitted_Values <- disruptionsModel$fitted.values
modelData <- cbind(modelData, Residuals, Fitted_Values)

save(modelData, disruptionsModel, file = "/home/dfearing/workspace/output/paxdelay/disruptions/performanceDisruptionsModel_3-30.RData")

