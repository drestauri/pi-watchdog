package home.gmsec;

// IMPORTANT:
// Add the location of the .../GMSEC_API/bin folder to your PATH environment variable (Windows)

// NOTE: running GMSEC on Raspberry Pi requires a special build of the GMSEC API! This must be obtained
//   from NASA: https://gmsec.gsfc.nasa.gov/
// Unpack the tgz on the Raspberry Pi:
//		tar -xvzf filename.tgz
// Raspberry Pi command for running your app is something like:
// 		sudo java -jar -Djava.library.path=/home/pi/Desktop/opt/GMSEC_API-4.4.2/bin app.jar ttyACM0 subscribe mw-id=... server=...

import gov.nasa.gsfc.gmsec.api.Config;
import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.api.mist.ConnectionManager;
import gov.nasa.gsfc.gmsec.api.util.Log;

public class GMSECConnection {

	private ConnectionManager connMgr = null;
	private Config config = null;
	
	public GMSECConnection(String[] my_args)
	{
		// Example config:
		// String my_args[] = {"subscribe", "mw-id=activemq394", "server=tcp://localhost:61616"};
		// Alternatively, add like config.addValue("server", "tcp://localhost:61616");
		config = new Config(my_args);


		// If it was not specified in the command-line arguments, set LOGLEVEL
		// to 'INFO' and LOGFILE to 'stdout' to allow the program report output
		// on the terminal/command line
		initializeLogging(config);

		// Print the GMSEC API version number using the GMSEC Logging
		// interface
		// This is useful for determining which version of the API is
		// configured within the environment
		Log.info(ConnectionManager.getAPIVersion());

	}
	
	public Config getConfig()
	{
		return config;
	}
	
	public ConnectionManager getConnMgr()
	{
		return connMgr;
	}
	
	public boolean connect()
	{
		try{
			// Create a ConnectionManager object
			// This is the linchpin for all communications between the
			// GMSEC API and the middleware server
			connMgr = new ConnectionManager(config);
	
			// Open the connection to the middleware
			Log.info("Opening the connection to the middleware server");
			connMgr.initialize();
	
			// Output middleware client library version
			Log.info(connMgr.getLibraryVersion());
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
			return false;
		}
		return true;
	}
	
	public void disconnect()
	{
		// Disconnect from the middleware and clean up the Connection
		try {
			connMgr.cleanup();
		} catch (GMSEC_Exception e) {
			e.printStackTrace();
		}
		
		Log.info("Disconnected from the message bus");
	}
	
	public static void initializeLogging(Config config)
	{
		// If it was not specified in the command-line arguments, set
		// LOGLEVEL to 'INFO' and LOGFILE to 'stdout' to allow the
		// program report output on the terminal/command line
		try
		{
			String logLevel = config.getValue("LOGLEVEL");
			String logFile = config.getValue("LOGFILE");

			if (logLevel == null)
			{
				config.addValue("LOGLEVEL", "INFO");
			}
			if (logFile == null)
			{
				config.addValue("LOGFILE", "STDOUT");
			}
		}
		catch(Exception e)
		{
			Log.error(e.toString());
		}
	}
}

