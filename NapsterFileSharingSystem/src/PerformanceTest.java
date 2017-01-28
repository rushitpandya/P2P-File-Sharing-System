import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PerformanceTest {

	private final static int TEST_COUNT = 1000;
	
	public static void main(String[] args) {
		
		BufferedReader input = null;
        
		try {
			input = new BufferedReader(new InputStreamReader(System.in));
			String hostAddress, fileName;
			
			// Display different choices to the user
			while(true){
				System.out.println("\nPlease choose below option for test:");
				System.out.println("1.Lookup and Download:");
				System.out.println("2.Register File:");
				System.out.println("3.Exit.");
				System.out.print("Enter choice and press ENTER:");
				
				int option = 0;
	
				// Check if the user has entered only numbers.
				try {
					option = Integer.parseInt(input.readLine());
				} catch (NumberFormatException e) {
					System.out.println("Wrong choice. Try again!!!");
					System.exit(0);
				}
	
				switch (option) {
				case 1:
					/*System.out.println("\nEnter server address and name of the file you want to search and Download:");
					hostAddress = input.readLine();
					fileName = input.readLine();*/
					(new LookupDownloadTest("localhost", "123-9MB.txt")).start();
					(new LookupDownloadTest("localhost", "123-9MB.txt")).start();
					(new LookupDownloadTest("localhost", "123-9MB.txt")).start();
					break;
	
				case 2:
					System.out.println("\nEnter peer address and two file names you want to download:");
					hostAddress = input.readLine();
					String file1 = input.readLine();
					String file2 = input.readLine();
					
					/*(new DownloadTest(hostAddress, file1)).start();
					(new DownloadTest(hostAddress, file2)).start();*/
					break;
					
				case 3:
					System.out.println("Thanks for using this system.");
					System.exit(0);
					break;
				default:
					System.out.println("Wrong choice. Try again!!!");
					break;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class LookupDownloadTest extends Thread {
		private String indexServerIP;
		private String fileName;
		
		public LookupDownloadTest(String host, String file) {
			this.indexServerIP = host;
			this.fileName = file;
		}
		
		public void run() {
			ObjectInputStream serverinput = null;
			ObjectOutputStream serveroutput = null;
			HashMap<PeerInfo, ArrayList<FileInfo>> lookMap=null;
			HashMap<Integer, DownloadInfo> indexMap=new HashMap<Integer,DownloadInfo>();
			Socket clientsocket  = null;
			
			int indexCount=0;
			long lookupStartTime, lookEndTime, lookupTotalTime = 0;
			long downloadStartTime, downloadEndTime, downloadTotalTime = 0;
			double avgLookTime, avgDownloadTime;
			
			
			try {
				clientsocket = new Socket(indexServerIP, 10000);
				serveroutput = new ObjectOutputStream(clientsocket.getOutputStream());
				serveroutput.flush();
				serverinput = new ObjectInputStream(clientsocket.getInputStream());
				
				//BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
				Communicator comm=new Communicator();
				comm = (Communicator) serverinput.readObject();
				//System.out.println((String) comm.getCommunicatorInfo());
				
				for (int i = 0; i < TEST_COUNT; i++) {
					lookupStartTime = System.currentTimeMillis();
			        //System.out.println("i="+i);
					comm.setCommunicatorType("LOOKUPFILE");
					serveroutput.writeObject(comm);
					serveroutput.flush();
					comm=(Communicator)serverinput.readObject();
					
					/*System.out.println("Enter the file name to lookup and download it:");
					String lookupFileName=input.readLine();*/
					
					String lookupFileName= fileName;
					comm.setCommunicatorType("lookupFileName");
					comm.setCommunicatorInfo(lookupFileName);
					serveroutput.writeObject(comm);
					serveroutput.flush();
					
					comm=(Communicator)serverinput.readObject();
					
					//System.out.println("Type : - "+ comm.getCommunicatorType());
					
					if(comm.getCommunicatorType().equals("LookupMap"))
					{
						//System.out.println("In if lookup===");
						lookMap=(HashMap<PeerInfo, ArrayList<FileInfo>>)comm.getCommunicatorInfo();
					}
					
					lookEndTime = System.currentTimeMillis();
					lookupTotalTime += (lookEndTime - lookupStartTime);
					//System.out.println("RITEN---END LOOKUP");
					for (PeerInfo peerinfo : lookMap.keySet()) {
						ArrayList<FileInfo> fi=lookMap.get(peerinfo);
						for(FileInfo fInfo : fi)
						{
							indexCount++;
							//System.out.println(indexCount+"\t|"+peerinfo.getPeerId()+"\t|"+fInfo.getFileName()+"\t\t|"+fInfo.getFileLocation());
							DownloadInfo di=new DownloadInfo();
							di.setPeerInfo(peerinfo);
							di.setFileInfo(fInfo);
							/*if(di==null)
								System.out.println("di is null...");*/
							indexMap.put(indexCount,di);
						} 
					}
					
					
					//System.out.println();
					if(!lookMap.isEmpty())
					{
						downloadStartTime = System.currentTimeMillis();
						
				        
						//System.out.println("\nEnter the index number from above table to download respective file:");
							//String regex="[0-9]+";
							//String sindexNumber=null;
							//sindexNumber=input.readLine();
								
							int indexNumber=1;				
							DownloadInfo downloadObject=indexMap.get(indexNumber);
							PeerClient p =new PeerClient();
							p.peerServerConnection(downloadObject);
							
							downloadEndTime = System.currentTimeMillis();
							downloadTotalTime += (downloadEndTime - downloadStartTime);
							
					}
					else{
						System.out.println("Sorry file not found on any peer.");
					}
				}
				avgLookTime = (double) Math.round(lookupTotalTime / (double) TEST_COUNT) / 1000;

				avgDownloadTime = (double) Math.round(downloadTotalTime / (double) TEST_COUNT) / 1000;
				
				File file = new File(FileHandler.downloadLocation+File.separator+fileName);
				
				System.out.println("Average lookup time for " + TEST_COUNT + " lookup requests is " + avgLookTime + " seconds.");
				System.out.println("Average Download time for " + TEST_COUNT + " lookup requests is " + avgDownloadTime + " seconds.");
				
				System.out.println("AVG LookUp Time\tAvg Download Time\tNo of Request/Thread\tTotal Lookup time\tTotal Download time\t Download Speed(MB/Sec)");
				System.out.println(avgLookTime+"\t\t"+avgDownloadTime+"\t\t"+TEST_COUNT+"\t"+lookupTotalTime+"\t"+downloadTotalTime+"\t"+(file.length()/1000000)/avgDownloadTime);
				//input.readLine();
				//this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					// Closing all streams. Close the stream only if it is initialized 
					if (serveroutput != null)
						serveroutput.close();
					
					if (serverinput != null)
						serverinput.close();
					
					if (clientsocket != null)
						clientsocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}