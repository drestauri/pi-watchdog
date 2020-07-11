# pi-watchdog
This is a watchdog that runs on a Raspberry Pi to make sure that the Raspberry Pi has an active connection to wi-fi and the GMSEC Bolt message broker. If either connection is lost for 30 seconds or more, the device will reboot.

# Installation
This code is provided as a complete software package with the exception of the GMSEC API components. The GMSEC 
API can be downloaded from the links at the bottom of the page below for PC. Unless I get permission from NASA
to post the ARM build I have for Raspberry Pi, you will need to contact them to get that build. They are generally
pretty good to respond to support requests from the GMSEC community:
```
https://opensource.gsfc.nasa.gov/projects/GMSEC_API_30/index.php
```
Once downloaded, extract the files and add a GMSEC_HOME environment variable to the folder containing the /bin
folder, and then add %GMSEC_HOME%/bin to your PATH variable (Windows). See the Usage section below for Linux.

Launch bolt by running the following command:
```
java -jar %GMSEC_HOME%\bin\bolt.jar
```

The project should already be set to connect to a locally hosted Bolt message queue, but if not update App_WD.java
as follows to set the default values for gmsec_args:
```java
String gmsec_args[] = {"subscribe", "mw-id=bolt", "server=localhost:9100"}; 
```

Once everything works on the PC, export the project as a Runnable JAR file. Transfer the JAR file to your Raspberry Pi to a folder like
/home/pi/Desktop/project. 

Assuming you've obtained the ARM build of the GMSEC API, copy the files to the Raspberry Pi as well to a location
such as /home/pi/Desktop/GMSEC_API. 

# Usage
Now that all the files are on your Linux system/Raspberry Pi, launch the GMSEC Bolt message queue using the following command:
```
sudo java -jar /home/pi/Desktop/GMSEC_API/bin/bolt.jar
```

Launch this application using the following command, which includes linking the GMSEC libraries similar to what
was required for the environment variables used in Windows:
```
sudo java -jar -Djava.library.path=/home/pi/Desktop/GMSEC_API/bin/ /home/pi/Desktop/project/pi-watchdog.jar subscribe mw-id=bolt server=localhost:9100
```
If all goes well, you should see the pi-watchdog.jar code printing that it successfully detected the wi-fi connection every second, and you can see it disconnect and reconnect to the Bold middleware every 10 seconds.

# Anticipated Updates
None

# Contributing
This is for a home project and while you're free to copy and modify to your liking, I will not be accepting contributions.