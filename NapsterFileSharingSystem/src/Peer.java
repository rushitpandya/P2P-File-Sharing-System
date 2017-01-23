import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

	Socket socket;
	ObjectInputStream peerServerInput;
	ObjectOutputStream peerServerOutput;
	public PeerServer(Socket socket)
	{
		this.socket=socket;
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
					System.out.println("Download request accepted");
					downloadInfo=(DownloadInfo)comm.getCommunicatorInfo();
					PeerInfo downloadPeerInfo=downloadInfo.getPeerInfo();
					FileInfo downloadFileInfo=downloadInfo.getFileInfo();
					String fileName=downloadFileInfo.getFileName();
					String fileLocation=downloadFileInfo.getFileLocation();
					File file=new File(fileLocation+File.separator+fileName);
					byte[] filebytesArray=new byte[(int)file.length()];
					BufferedInputStream buf=new BufferedInputStream(new FileInputStream(file));
					buf.read(filebytesArray,0,filebytesArray.length);
					out = socket.getOutputStream();
					out.write(filebytesArray, 0, filebytesArray.length);
					out.flush();
					//comm.setCommunicatorType("FileContent");
					//comm.setCommunicatorInfo(filebytesArray);
					//peerServerOutput.writeObject(comm);
					System.out.println("finish sending");
				}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}



class PeerClient extends Thread {

	String serverIp = null;
	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	ObjectOutputStream serveroutput=null;
	ObjectInputStream serverinput=null;
	int choice;
	public void run() {

		connection();
		while(true)
		{
			System.out.println("\n\n1. Register Files with the server");
			System.out.println("2. Lookup Files with the server");
			System.out.println("3. Unregister Files with the server");
			System.out.println("4. Lookup and Print the file content");
			System.out.println("5. Exit");
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
			Socket clientsocket = new Socket(serverIp, Peer.PEER_REQUEST_PORT);
			serveroutput = new ObjectOutputStream(clientsocket.getOutputStream());
			serveroutput.flush();
			serverinput = new ObjectInputStream(clientsocket.getInputStream());
			Communicator res = (Communicator) serverinput.readObject();
			System.out.println((String) res.getCommunicatorInfo());// displaying welcome message from server!

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void unregisterPeerFiles()
	{
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Are you sure want to unregister all files that you have share?Y/N");
		try {
			String toUnRegister=input.readLine();
			Communicator comm=new Communicator();
			comm.setCommunicatorType("UNREGISTER");
			comm.setCommunicatorInfo(toUnRegister);
			serveroutput.writeObject(comm);
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
				System.out.println(comm.getCommunicatorInfo().toString());
				System.out.println("------------------------------------------");
				
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
			
			System.out.println("Enter the file name for lookup and download it:");
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
			
			System.out.println("Given File is found on following Peers: ");
			System.out.println("Index\t|PeerId\t|FileName\t\t|FileLocation");
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
			
			System.out.println("Enter the index number from above table to download respective file:");
			int indexNumber=Integer.parseInt(input.readLine());
			DownloadInfo downloadObject=indexMap.get(indexNumber);
			peerServerConnection(downloadObject);
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
			System.out.println("Please Only Enter .txt file for Printing...!!!");
			System.out.println("Enter the file name for lookup and Print it:");
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
				
				
				if(!lookMap.isEmpty())
				{
					System.out.println("Given File is found on following Peers: ");
					System.out.println("Index\t|PeerId\t|FileName\t\t|FileLocation");
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
					
					System.out.println("Enter the index number from above table to Print respective file Content:");
					int indexNumber=Integer.parseInt(input.readLine());
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
			
			System.out.println("Requesting a File for Printing............");
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
			in.read(byteArray);
			System.out.println("\nFile Name:"+downloadFileInfo.getFileName());
			System.out.println("Host Ip & Peer_ID:"+downloadPeerInfo.getIp()+" & "+downloadPeerInfo.getPeerId());
			System.out.println("***********File Content**************\n");
			System.out.write(byteArray);
			
			//System.out.println("getData");
			
			System.out.println("\nFile Printing Successfully!!!!!!");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	private void peerServerConnection(DownloadInfo downloadObject)
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
			
			System.out.println("Requesting a File............");
			res.setCommunicatorType("DownloadRequest");
			res.setCommunicatorInfo(downloadObject);
			serveroutput1.writeObject(res);
			serveroutput1.flush();
			/*if(res.getCommunicatorType().equals("FileContent"))
			{
				res=(Communicator)serverinput1.readObject();
			}*/	
			System.out.println("Downloading a File...........");
			File file=new File(FileHandler.downloadLocation);
			if(!file.exists())
			{
				file.mkdirs();
			}
			
			in=clientsocket1.getInputStream();
			bufOut=new BufferedOutputStream(new FileOutputStream(FileHandler.downloadLocation+downloadFileInfo.getFileName()));
			int numByteRead;
			
			/*while((numByteRead = in.read(byteArray,0,byteArray.length))>0)
			{
				System.out.println("getData1");
				bufOut.write(byteArray,0,numByteRead);
			}*/
			numByteRead = in.read(byteArray);
			bufOut.write(byteArray);
			//System.out.println("getData");
			
			System.out.println("File Downloaded Successfully!!!!!!");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
