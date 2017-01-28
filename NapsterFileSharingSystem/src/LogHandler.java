import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class LogHandler {

	private String logFileName = "";
	private BufferedWriter bufOut = null;
	private final String logFileLocation = "logs/";


	public LogHandler(String logType) {
		try {
			if (logType.equalsIgnoreCase("Peer")) {
				logFileName = "peerdownload.log";
			} else if (logType.equalsIgnoreCase("Server")) {
				logFileName = "server.log";
			}
			
			File file = new File(logFileLocation);
			
			if (!file.exists()){
				file.mkdir();
			}
			
			bufOut = new BufferedWriter(new FileWriter(logFileLocation + logFileName, true));
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public boolean writeLog(String logMsg) {
		boolean writeFlag = false;
		try {
			String timeLog = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
			if (bufOut != null) {
				logMsg = String.format("%s => %s", timeLog, logMsg);
				bufOut.write(logMsg);
				String newline = System.getProperty("line.separator");
				bufOut.write(newline);
				writeFlag = true;
				bufOut.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writeFlag;
	}
	
	public void closeLogFile() {
		try {
			if (bufOut != null) {
				String lineSeparator = System.getProperty("line.separator");
				bufOut.write(lineSeparator);
				bufOut.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	protected void finalize() throws Throwable {
		if (bufOut != null) {
			bufOut.close();
		}
		super.finalize();
	}
	
}