# OpenCASA

## Specifications

This program has been developed and tested on Windows 7 (64-bit) using  Imagej v1.49q and Java 1.8.0_101 (64-bit). There are no specific requierements to use this plugin but a special attention of RAM memory is suggested when video analysis is carried on. At least 5GB of heap memory size is recommended, but it depends on the size of the files. One good estimation could be to use a heap memory size of 2.5 times the size of the heaviest file that is going to be analyzed. Information about how to increase memory on ImageJ can be found following this link: https://imagej.net/Troubleshooting#OutOfMemoryError. For all tests, only videos in AVI format and images in JPEG or PNG format were used. The plugin has not been tested on Linux or MAC platforms.

## Installation

### For users

First of all, it is necessary to have installed ImageJ. The latest version of the program can be downloaded from https://imagej.nih.gov/ij/download.html. We recommend to download the 64-bit bundle with the latest java version.
To install OpenCASA plugin on ImageJ, we recommend to follow these instructions: https://imagej.net/Installing_3rd_party_plugins: just drag and drop the .jar file into the ImageJ menu and select the destination folder. Once this is done, the OpenCASA_ option in the menu bar will be added.

### For developers

To develop with ImageJ in Eclipse, it is recommended to follow the instructions specified in: https://imagej.net/Developing_ImageJ_in_Eclipse. Briefly, to set up and run an existing ImageJ project in eclipse, it is necessary to follow these four steps:
1.	Install the Java Development Kit
2.	Install and configure Eclipse
3.	Clone the source code
4.	Import the source code

Tip: In order to increase the heap memory in eclipse, after the project has been set up, in the menu bar go to  Run->Run Configuration, find the name of the class you have been running, select it, click the Arguments tab and then add:
-Xms5120M –Xmx5120M
where 5120 is the 5 gigabytes of memory that you want to assign (in megabytes). Remember that a heap memory size of 2.5 times the size of the heaviest file that is going to be analyzed is recommended.