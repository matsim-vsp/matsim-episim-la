library(tidyverse)
library(directlabels)
require(scales)

runA <- "seed_4711-calibrationParam_4.0E-5-startDiseaseImport_2020-03-01-dailyImportedCases1_1-endDiseaseImport_2020-03-15-ciFactorMay_1.0-ciFactorJune_0.9-ciFactorJuly_0.8/calibration4"
runB <- "seed_4711-calibrationParam_4.0E-5-startDiseaseImport_2020-03-01-dailyImportedCases1_1-endDiseaseImport_2020-03-15-ciFactorMay_1.0-ciFactorJune_0.9-ciFactorJuly_1.0/calibration3"
runC <- "seed_4711-calibrationParam_4.0E-5-startDiseaseImport_2020-03-01-dailyImportedCases1_1-endDiseaseImport_2020-03-15-ciFactorMay_1.0-ciFactorJune_1.0-ciFactorJuly_0.8/calibration2"
runD <- "seed_4711-calibrationParam_4.0E-5-startDiseaseImport_2020-03-01-dailyImportedCases1_1-endDiseaseImport_2020-03-15-ciFactorMay_1.0-ciFactorJune_1.0-ciFactorJuly_1.0/calibration1"
runE <- "seed_4711-calibrationParam_4.0E-5-startDiseaseImport_2020-03-01-dailyImportedCases1_1-endDiseaseImport_2020-03-15-ciFactorMay_1.0-ciFactorJune_1.0-ciFactorJuly_1.0_120/calibration1"
baseDir <- "./output-2020-12-13/"

fileEnding <- ".infections.txt"
dirRunA <- paste(baseDir, runA, fileEnding, sep = "")
dirRunB <- paste(baseDir, runB, fileEnding, sep = "")
dirRunC <- paste(baseDir, runC, fileEnding, sep = "")
dirRunD <- paste(baseDir, runD, fileEnding, sep = "")
dirRunE <- paste(baseDir, runE, fileEnding, sep = "")

output1 <- paste(baseDir, "validation-total-cases-log.png", sep = "")
outputNewCasesLog <- paste(baseDir, "validation-new-cases-log.png", sep = "")
output2 <- paste(baseDir, "validation-total-cases.png", sep = "")
outputNewCases <- paste(baseDir, "validation-new-cases.png", sep = "")
outputHospitalization <- paste(baseDir, "validation-hospitalization.png", sep = "")


reportedDataLAcounty <- read.csv("./validation/LA_County_Covid19_cases_deaths_date_table.csv", sep=",")
reportedDataCDPHSCAG <- read_delim("./validation/CDPH_data_scag.csv",
                                   delim=";",
                                   locale=locale(decimal_mark = "."))

simulationDataA <- read.csv(dirRunA, sep="\t")
simulationDataB <- read.csv(dirRunB, sep="\t")
simulationDataC <- read.csv(dirRunC, sep="\t")
simulationDataD <- read.csv(dirRunD, sep="\t")
simulationDataE <- read.csv(dirRunE, sep="\t")

ggplot() +
  geom_line(data = reportedDataLAcounty, aes(x = as.Date(date_use, "%Y-%m-%d"), y = total_cases, colour="reportedDataLAcounty")) +
  geom_line(data = reportedDataCDPHSCAG, aes(x = date, y = totalcountconfirmed, colour="reportedDataCDPHSCAG")) +
  geom_line(data = simulationDataA, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataA")) +
  geom_line(data = simulationDataB, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataB")) +
  geom_line(data = simulationDataC, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataC")) +
  geom_line(data = simulationDataD, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataD")) +
  geom_line(data = simulationDataE, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataE")) +
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
                     labels = c("reportedDataLAcounty",
                                "reportedDataCDPHSCAG",
                                "simulationDataA"=runA,
                                "simulationDataB"=runB,
                                "simulationDataC"=runC,
                                "simulationDataD"=runD,
                                "simulationDataE"=runE
                                ),
                     values = c("reportedDataLAcounty"="green",
                                "reportedDataCDPHSCAG"="darkolivegreen3",
                                "simulationDataA"="red",
                                "simulationDataB"="blue",
                                "simulationDataC"="yellow",
                                "simulationDataD"="brown",
                                "simulationDataE"="darkgreen"
                                )
                     ) +
  #scale_y_log10() +
  scale_y_log10(breaks = trans_breaks("log10", function(x) 10^x),
                labels = trans_format("log10", math_format(10^.x))) +
  annotation_logticks() +
  theme(legend.position="bottom", legend.direction = "vertical", legend.text=element_text(size=6)) +
  ggsave(output1)

ggplot() +
  geom_point(data = reportedDataLAcounty, aes(x = as.Date(date_use, "%Y-%m-%d"), y = new_case, colour="reportedDataLAcounty")) +
  geom_point(data = reportedDataCDPHSCAG, aes(x = date, y = newcountconfirmed, colour="reportedDataCDPHSCAG")) +
  geom_point(data = simulationDataA, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataA")) +
  geom_point(data = simulationDataB, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataB")) +
  geom_point(data = simulationDataC, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataC")) +
  geom_point(data = simulationDataD, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataD")) +
  geom_point(data = simulationDataE, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataE")) +
  labs(x = "Date",
       y = "New cases",
       title = "Episim-LA validation") +
  scale_x_date(
    date_breaks = "1 month",
    #breaks = function(x) seq.Date(from = min(x), to = max(x), by = "1 month"),
    #labels = function(x) paste(x),
    date_labels = "%Y-%m"
  ) +
  scale_color_manual(name=NULL,
                     labels = c("reportedDataLAcounty",
                                "reportedDataCDPHSCAG",
                                "simulationDataA"=runA,
                                "simulationDataB"=runB,
                                "simulationDataC"=runC,
                                "simulationDataD"=runD,
                                "simulationDataE"=runE
                     ),
                     values = c("reportedDataLAcounty"="green",
                                "reportedDataCDPHSCAG"="darkolivegreen3",
                                "simulationDataA"="red",
                                "simulationDataB"="blue",
                                "simulationDataC"="yellow",
                                "simulationDataD"="brown",
                                "simulationDataE"="darkgreen"
                     )
  ) +
  #scale_y_log10() +
  scale_y_log10(breaks = trans_breaks("log10", function(x) 10^x),
                labels = trans_format("log10", math_format(10^.x))) +
  annotation_logticks() +
  theme(legend.position="bottom", legend.direction = "vertical", legend.text=element_text(size=6)) +
  ggsave(outputNewCasesLog)

ggplot() +
  geom_point(data = reportedDataLAcounty, aes(x = as.Date(date_use, "%Y-%m-%d"), y = new_case, colour="reportedDataLAcounty")) +
  geom_point(data = reportedDataCDPHSCAG, aes(x = date, y = newcountconfirmed, colour="reportedDataCDPHSCAG")) +
  geom_point(data = simulationDataA, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataA")) +
  geom_point(data = simulationDataB, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataB")) +
  geom_point(data = simulationDataC, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataC")) +
  geom_point(data = simulationDataD, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataD")) +
  geom_point(data = simulationDataE, aes(x = as.Date(date, "%Y-%m-%d"), y = c(0,diff(nShowingSymptomsCumulative)), colour="simulationDataE")) +
  labs(x = "Date",
       y = "New cases",
       title = "Episim-LA validation") +
  scale_x_date(
    date_breaks = "1 month",
    #breaks = function(x) seq.Date(from = min(x), to = max(x), by = "1 month"),
    #labels = function(x) paste(x),
    date_labels = "%Y-%m"
  ) +
  scale_color_manual(name=NULL,
                     labels = c("reportedDataLAcounty",
                                "reportedDataCDPHSCAG",
                                "simulationDataA"=runA,
                                "simulationDataB"=runB,
                                "simulationDataC"=runC,
                                "simulationDataD"=runD,
                                "simulationDataE"=runE
                     ),
                     values = c("reportedDataLAcounty"="green",
                                "reportedDataCDPHSCAG"="darkolivegreen3",
                                "simulationDataA"="red",
                                "simulationDataB"="blue",
                                "simulationDataC"="yellow",
                                "simulationDataD"="brown",
                                "simulationDataE"="darkgreen"
                     )
  ) +
  #scale_y_log10() +
  # scale_y_log10(breaks = trans_breaks("log10", function(x) 10^x),
  #               labels = trans_format("log10", math_format(10^.x))) +
  # annotation_logticks() +
  theme(legend.position="bottom", legend.direction = "vertical", legend.text=element_text(size=6)) +
  ggsave(outputNewCases)

ggplot() +
  geom_point(data = reportedDataCDPHSCAG, aes(x = date, y = hospitalized_covid_confirmed_patients, colour="reportedDataCDPHSCAG")) +
  geom_point(data = simulationDataA, aes(x = as.Date(date, "%Y-%m-%d"), y = nSeriouslySick, colour="simulationDataA")) +
  geom_point(data = simulationDataB, aes(x = as.Date(date, "%Y-%m-%d"), y = nSeriouslySick, colour="simulationDataB")) +
  geom_point(data = simulationDataC, aes(x = as.Date(date, "%Y-%m-%d"), y = nSeriouslySick, colour="simulationDataC")) +
  geom_point(data = simulationDataD, aes(x = as.Date(date, "%Y-%m-%d"), y = nSeriouslySick, colour="simulationDataD")) +
  geom_point(data = simulationDataE, aes(x = as.Date(date, "%Y-%m-%d"), y = nSeriouslySick, colour="simulationDataE")) +
  labs(x = "Date",
       y = "Hospitalization",
       title = "Episim-LA validation") +
  scale_x_date(
    date_breaks = "1 month",
    #breaks = function(x) seq.Date(from = min(x), to = max(x), by = "1 month"),
    #labels = function(x) paste(x),
    date_labels = "%Y-%m"
  ) +
  scale_color_manual(name=NULL,
                     labels = c("reportedDataCDPHSCAG",
                                "simulationDataA"=runA,
                                "simulationDataB"=runB,
                                "simulationDataC"=runC,
                                "simulationDataD"=runD,
                                "simulationDataE"=runE
                     ),
                     values = c("reportedDataCDPHSCAG"="darkolivegreen3",
                                "simulationDataA"="red",
                                "simulationDataB"="blue",
                                "simulationDataC"="yellow",
                                "simulationDataD"="brown",
                                "simulationDataE"="darkgreen"
                     )
  ) +
  #scale_y_log10() +
  # scale_y_log10(breaks = trans_breaks("log10", function(x) 10^x),
  #               labels = trans_format("log10", math_format(10^.x))) +
  # annotation_logticks() +
  theme(legend.position="bottom", legend.direction = "vertical", legend.text=element_text(size=6)) +
  ggsave(outputHospitalization)

ggplot() +
  geom_line(data = reportedDataLAcounty, aes(x = as.Date(date_use, "%Y-%m-%d"), y = total_cases, colour="reportedDataLAcounty")) +
  geom_line(data = reportedDataCDPHSCAG, aes(x = date, y = totalcountconfirmed, colour="reportedDataCDPHSCAG")) +
  geom_line(data = simulationDataA, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataA")) +
  geom_line(data = simulationDataB, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataB")) +
  geom_line(data = simulationDataC, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataC")) +
  geom_line(data = simulationDataD, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataD")) +
  geom_line(data = simulationDataE, aes(x = as.Date(date, "%Y-%m-%d"), y = nShowingSymptomsCumulative, colour="simulationDataE")) +
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
                     labels = c("reportedDataLAcounty",
                                "reportedDataCDPHSCAG",
                                "simulationDataA"=runA,
                                "simulationDataB"=runB,
                                "simulationDataC"=runC,
                                "simulationDataD"=runD,
                                "simulationDataE"=runE
                     ),
                     values = c("reportedDataLAcounty"="green",
                                "reportedDataCDPHSCAG"="darkolivegreen3",
                                "simulationDataA"="red",
                                "simulationDataB"="blue",
                                "simulationDataC"="yellow",
                                "simulationDataD"="brown",
                                "simulationDataE"="darkgreen"
                     )
  ) +
  #scale_y_log10() +
  # scale_y_log10(breaks = trans_breaks("log10", function(x) 10^x),
  #               labels = trans_format("log10", math_format(10^.x))) +
  # annotation_logticks() +
  theme(legend.position="bottom", legend.direction = "vertical", legend.text=element_text(size=6)) +
  ggsave(output2)


