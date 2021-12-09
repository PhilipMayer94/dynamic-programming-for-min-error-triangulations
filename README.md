dynamic-programming-for-min-error-triangulations
=========================

DATA:
---------------

The raw data can be downloaded at [1] (altimetry data) and [2] (gauge data)

[1]: ESA, 2020. Sea Level CCI ECV dataset: Time series of gridded sea level anomalies (sla). 
URL: https://catalogue.ceda.ac.uk/uuid/142052b9dc754f6da47a631e35ec4609,
doi:10.5270/esa-sea\_level\_cci-msla-1993\_2015-v\_2.0-201612.
European Space Agency (ESA).

[2]: PSMSL, 2020. Tide gauge data. Retrieved 19 April 2021
from http://www.psmsl.org/data/obtaining/, Tech. Rep. ,
Permanent Service for Mean Sea Level (PSMSL).

- The needed tide gauge data is the RLR_monthly dataset and should be added into the folder *assets/rlr_monthly* folder.

- The altimeter Data can be bulk downloaded on the given page and the yearly files that can be found in *archive/neodc/esacci/sea_level_data_l4_MSLA/v2.0* should be inserted into the folder *assets/altimeter*

INPUT:
---------------

- Parameter one:
	- reconstruction (default)
	- fixed_edges

- Parameter one:
	- visualization
	- no_visualization(default)



- *reconstruction* performs the sea surface reconstruction experiments.
- *fixed_edges* performs the fixed edge graph experiments on random graphs.
- *visualization* outputs the computed triangulations

OUTPUT:
----------------
The output are .csv files in the folder results. 
- For the reconstruction_experiments we get the following columns:  
trainingEpoch, reconstructionEpoch, anchorLongitude, anchorLatitude, BLANK,   
order, instanceSize, averageNrOfConnectedComponents, cmax, BLANK,  
trainingError, errorForDelaunayWithTrainingData, reconstructionError, errorForDelaunayWithReconstructionData, BLANK,  
weightPreprocessingTime, geometricPreprocessingTime, optimizationTime, temporalDifference  

- For the fixed edge graph experiments we get the following columns:  
order, instanceSize, overallNumberOfCC, avgNumberOfCC,cmax


RUN:
---------------
All possible combinations of parameters:

	java -jar dynamic_programming_for_MTWT.jar reconstruction
	java -jar dynamic_programming_for_MTWT.jar fixed_edges
	java -jar dynamic_programming_for_MTWT.jar reconstruction visualization
	java -jar dynamic_programming_for_MTWT.jar reconstruction visualization

LICENSE:
---------------


This code is released under the GNU Public License (GPL) 3.0.

To read the GPL 3.0, read the file COPYING in this directory.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

NOTES:
---------------

NOTE (1): 
	Java SE Development Kit 15.0.1 or newer is needed for the visualization

NOTE (2): 	
	If there are any problems reach out to Philip Mayer (mayer@cs.uni-bonn.de)
