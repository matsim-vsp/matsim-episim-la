library(ggplot2)
library(directlabels)
require(scales)

runA <- "seed_4711-calibrationParam_1.0E-4-dailyImportedCases_1"
runB <- "seed_4711-calibrationParam_0.001-startDate1_2020-02-15-dailyImportedCases1_1-startDate2_2020-04-01-dailyImportedCases2_0"
runC <- "seed_4711-calibrationParam_1.0E-4-startDate1_2020-02-15-dailyImportedCases1_1-startDate2_2020-04-01-dailyImportedCases2_0"
runD <- "seed_4711-calibrationParam_1.0E-4-startDate1_2020-02-15-dailyImportedCases1_10-startDate2_2020-04-01-dailyImportedCases2_0"
runE <- "seed_4711-calibrationParam_1.0E-5-startDate1_2020-02-15-dailyImportedCases1_1-startDate2_2020-04-01-dailyImportedCases2_0"

baseDir <- "./output/"
fileEnding <- "/*.infections.txt"
dirRunA <- paste(baseDir, runA, fileEnding, sep = "")
dirRunB <- paste(baseDir, runB, fileEnding, sep = "")
dirRunC <- paste(baseDir, runC, fileEnding, sep = "")
dirRunD <- paste(baseDir, runD, fileEnding, sep = "")
dirRunE <- paste(baseDir, runE, fileEnding, sep = "")

reportedData <- read.csv("./LA_County_Covid19_cases_deaths_date_table.csv", sep=",")

simulationDataA <- read.csv(dirRunA, sep="\t")
simulationDataB <- read.csv(dirRunB, sep="\t")
simulationDataC <- read.csv(dirRunC, sep="\t")
simulationDataE <- read.csv(dirRunD, sep="\t")
simulationDataF <- read.csv(dirRunE, sep="\t")

ggplot() +
  geom_line(data = reportedData, aes(x = as.Date(date_use, "%Y-%m-%d"), y = total_cases, colour="reportedData")) +
  geom_line(data = simulationDataA, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptoms, colour="simulationDataA")) +
  geom_line(data = simulationDataB, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptoms, colour="simulationDataB")) +
  geom_line(data = simulationDataC, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptoms, colour="simulationDataC")) +
  geom_line(data = simulationDataD, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptoms, colour="simulationDataD")) +
  geom_line(data = simulationDataE, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptoms, colour="simulationDataE")) +
  
  labs(x = "Date",
       y = "Total cases",
       title = "Episim-LA validation") +
  scale_x_date(
    date_breaks = "1 month",
    #breaks = function(x) seq.Date(from = min(x), to = max(x), by = "1 month"),
    #labels = function(x) paste(x),
    date_labels = "%Y-%m"
    ) +
  scale_color_manual(name=NULL,
                     labels = c("reportedData",
                                "simulationDataA"=runA,
                                "simulationDataB"=runB,
                                "simulationDataC"=runC,
                                "simulationDataD"=runD,
                                "simulationDataE"=runE),
                     values = c("reportedData"="green",
                                "simulationDataA"="red",
                                "simulationDataB"="blue",
                                "simulationDataC"="yellow",
                                "simulationDataD"="brown",
                                "simulationDataE"="orange")) +
  #scale_y_log10() +
  scale_y_log10(breaks = trans_breaks("log10", function(x) 10^x),
                labels = trans_format("log10", math_format(10^.x))) +
  annotation_logticks() +
  theme(legend.position="bottom", legend.direction = "vertical")

    
    
    