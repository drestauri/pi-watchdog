package home;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import home.gmsec.GMSECConnection;
import home.utils.Logger;

//import home.gmsec.GMSECConnection;

/*****************************************
 * TODO:
 * This watchdog will make sure the pi still has
 * an active connection to the wi-fi and to
 * bolt. If either connection is lost for
 * 30 seconds or more, the system will reboot.
 * 
 * TODO:
 * - Add connection to Bolt
 * - Test connection to Bolt
 * - Test connection to wi-fi
 * - Start counting time if either connections are bad
 * - Reboot after 60 seconds
 * - Add this file to the startup script on the Pi
 * - Add error logging and periodic (1 hour?) ok status logging
 * 
 ****************************************/

public class App_WD {

	private static final int MAX_ERROR_TIME = 30;
	
	public static Logger log;
	private static Runtime rt;
	private static BufferedReader stdInput;
	private static BufferedReader stdError;
	private static Process proc;
	private static int errorCount;
	private static int loopCount;
	
	public static GMSECConnection gmsec;
	
	public static void main(String[] args) {

		//============= GET AND HANDLE ARGS ================/
		log = new Logger();
		log.LogMessage_High("WATCHDOG: Started");
		
		//======== GET COMMAND LINE ARGUMENTS ==========
		log.LogMessage_Low("WATCHDOG: Getting command line args");
		String gmsec_args[] = {"subscribe", "mw-id=bolt", "server=localhost:9100"};
		
		if (args.length == 3)
		{
			// subscribe, middleware ID, and middleware location were provided 
			gmsec_args[0] = args[0];
			gmsec_args[1] = args[1];
			gmsec_args[2] = args[2];
		}else
		{
			System.out.println("usage: java -jar pi-watchdog.jar subscribe mw-id=<middleware> server=<ip_address>:<port>\n");
			System.out.println("\n== NOTE ==");
			System.out.println(" Example middleware IDs: bolt or activemq394");
			System.out.println(" Port is commonly 61616 for ActiveMQ and 9100 for Bolt");
			System.out.println("==========");
			//System.exit(-1); // Comment this out during dev
		}
		
		//============ INITIALIZE ==============
		rt = Runtime.getRuntime();
		errorCount = 0;
		loopCount = 0;
		// Send the arguments to the connection class
		// GMSECConnection() also starts the connection to the message bus
		gmsec = new GMSECConnection(gmsec_args);
		gmsec.connect();
		
		
		//============= START MAIN PROCESS ==============/
		while(true)
		{
			// Sleep for 1 second
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.LogMessage_High("WATCHDOG: Sleep error!");
				e.printStackTrace();
			}
			
			// Check if we have a wifi connection and a connection to Bolt
			if(!isWifiConnected()) // If no wifi connection...
				errorCount++;
			else if (loopCount>=10) // If wifi is good to go and we've looped at least 10 times, check that we are connected to GMSEC Bolt
			{
				loopCount = 0;
				if(!isConnectedToBolt()) // If not connected, increase error count
					errorCount+=10; // this check is only done every 10 seconds so increase the errorCount faster
				else // If we have wifi and a connection to Bolt, reset error count
					errorCount = 0;
			}
			
			// If the error count gets above 60, reboot
			if(errorCount>=MAX_ERROR_TIME)
			{
				gmsec.disconnect();
				doReboot();
			}
			
			loopCount++;
		}
	}
	
	private static void doReboot()
	{
		log.LogMessage_High("WATCHDOG: Rebooting");
		try {
			rt.exec("sudo reboot");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isWifiConnected()
	{
		// Check the wifi connection
		try {
			proc = rt.exec("iwgetid");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Grab the result
		stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		
		String s = null;
		
		// If there was a result it will go here:
		try {
			while ((s = stdInput.readLine()) != null) {
				System.out.print("Result:");
			    System.out.println(s);
			    if(s.contains("COOKIES"))
			    {
			    	System.out.println("Confirmed wifi connection");
			    	return true;
			    }
			    else
			    {
			    	System.out.print("Incorrect wifi result: ");
			    	System.out.println(s);
					log.LogMessage_High("WATCHDOG: Incorrect wifi");
			    	return false;
			    }
			} // END while
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If there was an error it will go here:
		try {
			while ((s = stdError.readLine()) != null) {
				System.out.print("Error:");
			    System.out.println(s);
				log.LogMessage_High("WATCHDOG: R-Pi returned WiFi Errors:");
				log.LogMessage_High(s);
			} // END while
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static boolean isConnectedToBolt()
	{
		// Disconnect from bolt then reconnect.
		gmsec.disconnect();
		// Sleep for a brief time
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			log.LogMessage_High("WATCHDOG: GMSEC loop sleep error!");
			e.printStackTrace();
		}
		if(gmsec.connect())
			return true;
		else
		{
			System.out.println("GMSEC Error");
			log.LogMessage_High("WATCHDOG: Unable to connect to GMSEC Bolt");
			return false;
		}
	}
}

