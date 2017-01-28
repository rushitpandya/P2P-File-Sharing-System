import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class Peer {
	public static final int PEER_LISTEN_PORT = 20000;
	public static final int PEER_REQUEST_PORT = 10000;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Peer Server Started!!");
		ServerSocket serverSocket=null;
		try {
			serverSocket =new ServerSocket(PEER_LISTEN_PORT);
			System.out.println("Peer Client Started!!");
			new PeerClient().start();
			
			while(true)
			{
				 new PeerServer(serverSocket.accept()).start();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			serverSocket.close();
		}
		
	}
}



class PeerServer extends Thread {

	public void printLog(String msg)
	{
		peerserverlog = new LogHandler("peer");
		peerserverlog.writeLog(msg);
		peerserverlog.closeLogFile();
	}
	
	Socket socket;
	ObjectInputStream peerServerInput;
	ObjectOutputStream peerServerOutput;
	private LogHandler peerserverlog ;
	public PeerServer(Socket socket)
	{
		this.socket=socket;
	}
	
	@Override
	public void interrupt() {
		peerserverlog.closeLogFile();
		super.interrupt();
	}

	
	public void run() {
		OutputStream out;
		try{
				DownloadInfo downloadInfo;
				
				peerServerOutput = new ObjectOutputStream(socket.getOutputStream());
				peerServerOutput.flush();
				peerServerInput=new ObjectInputStream(socket.getInputStream());
				Communicator comm=(Communicator)peerServerInput.readObject();
				
				if(comm.getCommunicatorType().equals("DownloadRequest"))
				{
					String Ip=socket.getInetAddress().getHostAddress();
					printLog("File downloading request from "+socket.getInetAddress().getHostAddress()+" accepted and processing initiated .");
					printLog("Locating file requested by "+socket.getInetAddress().getHostAddress());
					//System.out.println("Download request accepted........");
					downloadInfo=(DownloadInfo)comm.getCommunicatorInfo();
					PeerInfo downloadPeerInfo=downloadInfo.getPeerInfo();
					FileInfo downloadFileInfo=downloadInfo.getFileInfo();
					String fileName=downloadFileInfo.getFileName();
					String fileLocation=downloadFileInfo.getFileLocation();
					printLog("Sending file "+fileName+" requested by "+Ip);
					
					File file=new File(fileLocation+File.separator+fileName);
					byte[] filebytesArray=new byte[(int)file.length()];
					
					BufferedInputStream buf=new BufferedInputStream(new FileInputStream(file));
					buf.read(filebytesArray,0,filebytesArray.length);
					out = socket.getOutputStream();
					out.write(filebytesArray, 0, filebytesArray.length);
					out.flush();
					printLog("File sent to the peer");
					buf.close();
					out.close();
					
				//	System.out.println("finish sending");
				}
				else if (comm.getCommunicatorType().equals("FileSync"))
				{
					String Ip=socket.getInetAddress().getHostAddress();
					printLog("File Syncing request from"+Ip+" accepted and processing initiated.");
					PeerInfo peerinfo=(PeerInfo)comm.getCommunicatorInfo();
					FileHandler fh=new FileHandler();
					ArrayList<FileInfo> afl=fh.getFiles(peerinfo.getDirectory());
					comm.setCommunicatorType("UpdatedFiles");
					comm.setCommunicatorInfo(afl);
					peerServerOutput.writeObject(comm);
				}
			
		}
		catch(FileNotFoundException e){
			System.out.println("Either File Deleted and Not Found on Specified Path.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}



class PeerClient extends Thread {

	int connectionFlag=0;
	String serverIp = null;
	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	ObjectOutputStream serveroutput=null;
	ObjectInputStream serverinput=null;
	int choice;
	public void run() {

		connection();	
		while(connectionFlag==1) {	
			connection();
		}	
			while(true)
			{
				System.out.println("\nWhat do you want to do?");
				System.out.println("1. Register Files with the server");
				System.out.println("2. Lookup Files with the server");
				System.out.println("3. Unregister Files with the server");
				System.out.println("4. Lookup and Print the file content");
				System.out.println("5. Exit\n");
				System.out.print("Enter Your Choice: ");
				try {
					choice=Integer.parseInt(input.readLine());
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					System.out.println("Wrong Choice!!Enter valid choice");
					continue;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				
				switch(choice)
				{
					case 1:
						registerPeerFiles();
						break;
					case 2:
						lookupFile();
						break;
					case 3:
						unregisterPeerFiles();
						break;
					case 4:
						lookupPrintFile();
						break;
					case 5:
						closePeerConnection();
					
						break;
					default:
						break;
				}
			}
			
	}
	
	private void connection() {
		
		try {
			System.out.println("Enter the Indexing Server IP Address:");
			serverIp = input.readLine();
			while(serverIp.trim().length() == 0 || !IPValidator.validate(serverIp)) {
				System.out.println("Invalid Server IP Address.");
				System.out.println();
				System.out.println("Enter Valid Indexing Server IP Address:");
				serverIp = input.readLine();
			}
			Socket clientsocket = new Socket(serverIp, Peer.PEER_REQUEST_PORT);
			serveroutput = new ObjectOutputStream(clientsocket.getOutputStream());
			serveroutput.flush();
			serverinput = new ObjectInputStream(clientsocket.getInputStream());
			Communicator res = (Communicator) serverinput.readObject();
			System.out.println((String) res.getCommunicatorInfo());// displaying welcome message from server!
			connectionFlag=0;
		}
		catch(ConnectException e)
		{
			System.out.println("Connection Timed Out: Unable to find server.\n");
			connectionFlag=1;
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void unregisterPeerFiles()
	{
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Are you sure want to unregister all files that you have share?(Y/N)");
		
		try {
			String toUnRegister=input.readLine();
			if(toUnRegister.equalsIgnoreCase("Y"))
			{
				Communicator comm=new Communicator();
				comm.setCommunicatorType("UNREGISTER");
				comm.setCommunicatorInfo(toUnRegister);
				serveroutput.writeObject(comm);
				
				comm=(Communicator)serverinput.readObject();
				if(comm.getCommunicatorType().equals("UnregisterSuccessfull"))
				{
					System.out.println(comm.getCommunicatorInfo().toString());
				}
				if(comm.getCommunicatorType().equals("NotShared"))
				{
					System.out.println(comm.getCommunicatorInfo().toString());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void closePeerConnection()
	{
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Note: This will remove all data from Indexing Server");
		System.out.println("Are you sure want to terminate the connection?(Y/N)");
		
		try {
			String toUnRegister=input.readLine();
			if(toUnRegister.equalsIgnoreCase("Y"))
			{
				Communicator comm=new Communicator();
				comm.setCommunicatorType("CLOSE");
				comm.setCommunicatorInfo(toUnRegister);
				serveroutput.writeObject(comm);
				
				comm=(Communicator)serverinput.readObject();
				if(comm.getCommunicatorType().equals("CloseSuccessfull"))
				{
					System.out.println(comm.getCommunicatorInfo().toString());
				}
				System.exit(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void registerPeerFiles()
	{
		String path=null,fileCheck=null;
		Communicator comm=new Communicator();
		FileHandler fh=new FileHandler();
		
		try {
			comm.setCommunicatorType("REGISTERPEER");
			//comm.setCommunicatorInfo(fileCheck);
			serveroutput.writeObject(comm);
			
			
			comm=(Communicator)serverinput.readObject();
			
		
			System.out.println("Enter the path of directory/file that you want to share with others peer:");
			path=input.readLine();
			File fp=new File(path);
			while(!fp.exists())
			{
				System.out.println("Invalid Path.");
				System.out.println("Enter the valid path of directory/file that you want to share with others peer:");
				path=input.readLine();
				fp=new File(path);
			}
			if(fp.isFile())
			{
				fileCheck=fp.getAbsolutePath().substring(0,fp.getAbsolutePath().lastIndexOf(File.separator));
			}
			else if (fp.isDirectory())
			{
				fileCheck=fp.getAbsolutePath();
			}
			serveroutput.flush();	
			comm.setCommunicatorType("Peercheck");
			comm.setCommunicatorInfo(fileCheck);
			serveroutput.writeObject(comm);
			
			comm=(Communicator)serverinput.readObject();
			
			serveroutput.flush();
			ArrayList<FileInfo> filesList=fh.getFiles(path);
			comm.setCommunicatorType("Register");
			comm.setCommunicatorInfo(filesList);		
			serveroutput.writeObject(comm);
			
			comm = (Communicator)serverinput.readObject();
			if(comm.getCommunicatorType().equals("registrationSuccessfull")){
				System.out.println("\n-------------------------------------------------");
				System.out.println(comm.getCommunicatorInfo().toString());
				System.out.println("---------------------------------------------------");
				
				for(FileInfo fi : filesList){
					System.out.println("File Name : "+fi.getFileName()+" File Location: "+fi.getFileLocation());
				}
			}
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void lookupFile()
	{
		Communicator comm=new Communicator();
		FileHandler fh=new FileHandler();
		HashMap<PeerInfo, ArrayList<FileInfo>> lookMap=null;
		HashMap<Integer, DownloadInfo> indexMap=new HashMap<Integer,DownloadInfo>();
		int indexCount=0;
		try {
			comm.setCommunicatorType("LOOKUPFILE");
			serveroutput.writeObject(comm);
			
			comm=(Communicator)serverinput.readObject();
			
			System.out.println("Enter the file name to lookup and download it:");
			String lookupFileName=input.readLine();
			comm.setCommunicatorType("lookupFileName");
			comm.setCommunicatorInfo(lookupFileName);
			serveroutput.writeObject(comm);
			serveroutput.flush();
			comm=(Communicator)serverinput.readObject();
			
			if(comm.getCommunicatorType().equals("LookupMap"))
			{
				lookMap=(HashMap<PeerInfo, ArrayList<FileInfo>>)comm.getCommunicatorInfo();
			}
			
			System.out.println();
			if(!lookMap.isEmpty())
			{
				System.out.println("Given File is found on following Peers \n");
				System.out.println("------------------------------------------");
				System.out.println("Index\t|PeerId\t|FileName\t\t|FileLocation");
				System.out.println("------------------------------------------");
				for (PeerInfo peerinfo : lookMap.keySet()) {
					ArrayList<FileInfo> fi=lookMap.get(peerinfo);
					for(FileInfo fInfo : fi)
					{
						indexCount++;
						System.out.println(indexCount+"\t|"+peerinfo.getPeerId()+"\t|"+fInfo.getFileName()+"\t\t|"+fInfo.getFileLocation());
						DownloadInfo di=new DownloadInfo();
						di.setPeerInfo(peerinfo);
						di.setFileInfo(fInfo);
						if(di==null)
							System.out.println("di is null...");
						indexMap.put(indexCount,di);
					} 
				}
				
				System.out.println("\nEnter the index number from above table to download respective file:");
					String regex="[0-9]+";
					String sindexNumber=null;
					sindexNumber=input.readLine();
					while(!sindexNumber.matches(regex))
					{	
						System.out.println("Please Enter Proper Index Number:");
						sindexNumber=input.readLine();
					}	
					int indexNumber=Integer.parseInt(sindexNumber);				
					DownloadInfo downloadObject=indexMap.get(indexNumber);
					peerServerConnection(downloadObject);
			}
			else{
				System.out.println("Sorry file not found on any peer.");
			}
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void lookupPrintFile()
	{
		Communicator comm=new Communicator();
		FileHandler fh=new FileHandler();
		HashMap<PeerInfo, ArrayList<FileInfo>> lookMap=null;
		HashMap<Integer, DownloadInfo> indexMap=new HashMap<Integer,DownloadInfo>();
		int indexCount=0;
		try {
			comm.setCommunicatorType("LOOKUPPRINTFILE");
			serveroutput.writeObject(comm);
			
			comm=(Communicator)serverinput.readObject();
			System.out.println("Note: Please Only Enter '.txt' file for Printing.\n");
			System.out.println("Enter the File Name for Lookup and Print it:");
			String lookupFileName=input.readLine();
			
			if(lookupFileName.endsWith(".txt")){
				comm.setCommunicatorType("lookupPrintFileName");
				comm.setCommunicatorInfo(lookupFileName);
				serveroutput.writeObject(comm);
				serveroutput.flush();
				comm=(Communicator)serverinput.readObject();
				
				if(comm.getCommunicatorType().equals("LookupPrintMap"))
				{
					lookMap=(HashMap<PeerInfo, ArrayList<FileInfo>>)comm.getCommunicatorInfo();
				}
				
				System.out.println();
				if(!lookMap.isEmpty())
				{
					System.out.println("Given File is found on following Peers: ");
					System.out.println("------------------------------------------");
					System.out.println("Index\t|PeerId\t|FileName\t\t|FileLocation");
					System.out.println("------------------------------------------");
					for (PeerInfo peerinfo : lookMap.keySet()) {
						ArrayList<FileInfo> fi=lookMap.get(peerinfo);
						for(FileInfo fInfo : fi)
						{
							indexCount++;
							System.out.println(indexCount+"\t|"+peerinfo.getPeerId()+"\t|"+fInfo.getFileName()+"\t\t|"+fInfo.getFileLocation());
							DownloadInfo di=new DownloadInfo();
							di.setPeerInfo(peerinfo);
							di.setFileInfo(fInfo);
							/*if(di==null)
								System.out.println("di is null...");*/
							indexMap.put(indexCount,di);
						} 
					}
					
					System.out.println("\nEnter the index number from above table to Print respective file Content:");
					String regex="[0-9]+";
					String sindexNumber=null;
					sindexNumber=input.readLine();
					while(!sindexNumber.matches(regex))
					{	
						System.out.println("Please Enter Proper Index Number:");
						sindexNumber=input.readLine();
					}	
					
					int indexNumber=Integer.parseInt(sindexNumber);
					DownloadInfo downloadObject=indexMap.get(indexNumber);
					printFileContent(downloadObject);
				}
				else{
					System.out.println("Sorry File Not Found...");
				}
			}		
			else{
				System.out.println("Sorry...!!Please Enter '.txt' file only for Printing...");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void printFileContent(DownloadInfo downloadObject)
	{
		PeerInfo downloadPeerInfo=downloadObject.getPeerInfo();
		FileInfo downloadFileInfo=downloadObject.getFileInfo();
		byte[] byteArray=new byte[1024*64];
		InputStream in=null;
		BufferedOutputStream bufOut;
		try {
			String serverIp1 = downloadPeerInfo.getIp();
			Socket clientsocket1 = new Socket(serverIp1, Peer.PEER_LISTEN_PORT);
			ObjectOutputStream serveroutput1 = new ObjectOutputStream(clientsocket1.getOutputStream());
			serveroutput1.flush();
			ObjectInputStream serverinput1 = new ObjectInputStream(clientsocket1.getInputStream());
			Communicator res = new Communicator();
			
			System.out.println("\nRequesting a File for Printing............");
			res.setCommunicatorType("DownloadRequest");
			res.setCommunicatorInfo(downloadObject);
			serveroutput1.writeObject(res);
			serveroutput1.flush();
			/*if(res.getCommunicatorType().equals("FileContent"))
			{
				res=(Communicator)serverinput1.readObject();
			}*/	
			System.out.println("Printing a File...........");
			
			in=clientsocket1.getInputStream();
			bufOut=new BufferedOutputStream(new FileOutputStream(FileHandler.downloadLocation+downloadFileInfo.getFileName()));
			int numByteRead;
			in.read(byteArray,0,64000);
			System.out.println("\nFile Name:"+downloadFileInfo.getFileName());
			System.out.println("Host Ip & Peer_ID:"+downloadPeerInfo.getIp()+" & "+downloadPeerInfo.getPeerId());
			System.out.println("File Content Only print upto 64KB...");
			System.out.println("\n***********File Content**************\n");
			System.out.println("-----------------------------------------");
			System.out.write(byteArray);
			System.out.println();
			System.out.println("-----------------------------------------");
			//System.out.println("getData");
			
			System.out.println("\nFile Printing Successfully Completed");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void peerServerConnection(DownloadInfo downloadObject)
	{
		PeerInfo downloadPeerInfo=downloadObject.getPeerInfo();
		FileInfo downloadFileInfo=downloadObject.getFileInfo();
		byte[] byteArray=new byte[1024*64];
		InputStream in=null;
		BufferedOutputStream bufOut;
		try {
			String serverIp1 = downloadPeerInfo.getIp();
			Socket clientsocket1 = new Socket(serverIp1, Peer.PEER_LISTEN_PORT);
			ObjectOutputStream serveroutput1 = new ObjectOutputStream(clientsocket1.getOutputStream());
			serveroutput1.flush();
			ObjectInputStream serverinput1 = new ObjectInputStream(clientsocket1.getInputStream());
			Communicator res = new Communicator();
			
			//System.out.println("Requesting a File............");
			res.setCommunicatorType("DownloadRequest");
			res.setCommunicatorInfo(downloadObject);
			serveroutput1.writeObject(res);
			serveroutput1.flush();
			/*if(res.getCommunicatorType().equals("FileContent"))
			{
				res=(Communicator)serverinput1.readObject();
			}*/	
			//System.out.println("Downloading a File...........");
			File file=new File(FileHandler.downloadLocation);
			if(!file.exists())
			{
				file.mkdirs();
			}
			
			in=clientsocket1.getInputStream();
			bufOut=new BufferedOutputStream(new FileOutputStream(FileHandler.downloadLocation+downloadFileInfo.getFileName()));
			int numByteRead=0;
			int count=0;
			while((numByteRead = in.read(byteArray))> 0)
			{
				count++;
				//System.out.println(count+"----"+numByteRead);
				bufOut.write(byteArray,0,numByteRead);
				bufOut.flush();
				if(numByteRead <= 0)
					break;
			}
			//System.out.println("out of loop");
			/*numByteRead = in.read(byteArray);
			bufOut.write(byteArray);*/
			bufOut.close();
			in.close();
			
				
			System.out.println("File Downloaded Successfully!!!!!!");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
