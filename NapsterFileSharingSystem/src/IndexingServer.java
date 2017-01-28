import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class IndexingServer {
	
	static int peerId=0;
	private static HashMap<Integer, PeerInfo> peerList=new HashMap<Integer,PeerInfo>();
	private static HashMap<Integer, ArrayList<FileInfo>> fileList=new HashMap<Integer,ArrayList<FileInfo>>();
	private static final int INDEX_SERVER_PORT=10000;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
			//int peerId=1;
			ServerSocket serverSocket=new ServerSocket(INDEX_SERVER_PORT);
			System.out.println("Indexing Server Started!!!");
			System.out.println("Waiting for new connection from peer!!!");
			System.out.println();
			try{
				new Index2PeerFileSync().start();
					
				while(true)
				{
					new IndexServer(serverSocket.accept()).start();
				}
			}
			finally{
				serverSocket.close();
			}
	}
	
	private static class Index2PeerFileSync extends Thread {
		
		public void run()
		{
			while(true)
			{	
				
				String syncIp=null,syncLocation=null;
				Communicator res=new Communicator();
				try{
					Index2PeerFileSync.sleep(60000);
					for(int pi : peerList.keySet()) {
						PeerInfo peerinfo=peerList.get(pi);
						syncIp=peerinfo.getIp();
						syncLocation=peerinfo.getDirectory();
						Socket clientsyncsocket = new Socket(syncIp, Peer.PEER_LISTEN_PORT);
						ObjectOutputStream serversyncoutput = new ObjectOutputStream(clientsyncsocket.getOutputStream());
						ObjectInputStream serversyncinput = new ObjectInputStream(clientsyncsocket.getInputStream());
						res.setCommunicatorType("FileSync");
						res.setCommunicatorInfo(peerinfo);
						serversyncoutput.writeObject(res);
						
						res = (Communicator) serversyncinput.readObject();
						ArrayList<FileInfo> afl=(ArrayList<FileInfo>)res.getCommunicatorInfo();
						if(afl.isEmpty())
						{
							//peerList.remove(pi);
							fileList.remove(pi);
						}
						else
						{
							fileList.put(pi, afl);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}	
		}	
	}

	private static class IndexServer extends Thread {
		
		Socket clientSocket;
		String ipAddress;
		ObjectOutputStream serveroutput=null;
		ObjectInputStream serverinput=null;
		LogHandler log;
		Communicator res;
		public IndexServer(Socket clientSocket) throws IOException
		{	
			this.clientSocket=clientSocket;
			ipAddress=clientSocket.getInetAddress().getHostAddress();
			System.out.println("Connection Estaiblished with Peer having IP "
			+clientSocket.getInetAddress());
			
			printLog("Connection Estaiblished with Peer having IP "
			+clientSocket.getInetAddress());
			
		}
		

		public void printLog(String msg)
		{
			log=new LogHandler("server");
			log.writeLog(msg);
			log.closeLogFile();
		}
		
		
		public void run()
		{	
			
			try {
				serveroutput = new ObjectOutputStream(clientSocket.getOutputStream());
				serveroutput.flush();
				serverinput=new ObjectInputStream(clientSocket.getInputStream());
				res=new Communicator();
				
				res.setCommunicatorInfo("Welcome to the P2P File Sharing System!! Please choose from below options of your choice");
				serveroutput.writeObject(res);
				serveroutput.flush();
				
				while(true){
					String choice=null;
					res=(Communicator)serverinput.readObject();
					choice = res.getCommunicatorType().toString();
					if(!choice.equals("UNREGISTER") && !choice.equals("CLOSE"))
					{	
						serveroutput.writeObject(res);
						serveroutput.flush();
					}
					//System.out.println("in while loop:-"+res.getCommunicatorType());
					switch(choice){
						case "REGISTERPEER":
							peerConnection();
							break;
						case "LOOKUPFILE":
							peerFileLookup();
							break;
						case "LOOKUPPRINTFILE":
							peerFileLookupPrint();
							break;
						case "UNREGISTER":
							peerUnregister(res);
							break;	
						case "CLOSE" :
							peerClose(res);
							break;
						default:
							break;
					}
					
				}//end while(true)
				
			}
			catch(SocketException e)
			{
				System.out.println("Connection with IP: "+ipAddress+" terminated\n");
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void peerConnection()
		{
			int flag=0;
			String dirCheck=null;
			try {
					Communicator res=new Communicator();
					printLog("Peer with IP: "+ipAddress+" connected.");
					res=(Communicator)serverinput.readObject();
					//System.out.println("Info :"+res.getCommunicatorType());
					if(res.getCommunicatorType().equals("Peercheck"))
					{
						dirCheck=res.getCommunicatorInfo().toString();
						//System.out.println(dirCheck);
						for (int pi : peerList.keySet()) {
							PeerInfo peerInfo=peerList.get(pi);
							if(peerInfo.getIp().equals(ipAddress) && peerInfo.getDirectory().equals(dirCheck))
							{
								flag=1;
								break;
							}
						} 
						
					}
					if(flag==0)
					{
						PeerInfo peer=new PeerInfo();
						peerId++;
						peer.setPeerId(peerId);
						peer.setIp(ipAddress);
						peer.setDirectory(dirCheck);
						peerList.put(peerId, peer);
						serveroutput.writeObject(res);
						//System.out.println("New Peer If");
					}
					else{
						
						serveroutput.writeObject(res);
					}
					
					res=(Communicator)serverinput.readObject();
					//System.out.println(res.getCommunicatorType());
					switch(res.getCommunicatorType().toString())
					{
						case "Register":
							//System.out.println("before register");
							peerRegister(res);
							//System.out.println("after register");
							break;
						default :
							break;
					}
					
				}
				catch(SocketException e)
				{
					System.out.println("Connection with IP: "+ipAddress+" terminated\n");
					printLog("Connection with IP: "+ipAddress+" terminated");
				}
				catch (Exception e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
		
		private void peerRegister(Communicator comm)
		{
			printLog("Register request from "+ipAddress);
			Communicator res=new Communicator();
			ArrayList<FileInfo> fileInfo=(ArrayList<FileInfo>)comm.getCommunicatorInfo();
			//String peerInfo=peerId+"#"+clientSocket.getInetAddress().getHostAddress()+"#"+clientSocket.getPort();
			fileList.put(peerId, fileInfo);
			System.out.println("All the files are registered for Peer #"+peerId);
			/*System.out.println("-------Peer List-----");
			for (int pi : peerList.keySet()) {
				PeerInfo peerInfo=peerList.get(pi);
				System.out.println("PeerId #"+peerInfo.getPeerId()+" IP: "+peerInfo.getIp()+" Directory: "+peerInfo.getDirectory());
			} 
			
			System.out.println("-------File-Peer List-----");
			for (int pi : fileList.keySet()) {
				ArrayList<FileInfo> fi=fileList.get(pi);
				System.out.println("PeerId: #"+pi);
				for(FileInfo fInfo : fi)
				{
					System.out.println("File Name : "+fInfo.getFileName()+" File Location: "+fInfo.getFileLocation());
			
				} 
			}*/
			res.setCommunicatorType("registrationSuccessfull");
			res.setCommunicatorInfo("Peer registered with given files successfully...!!!");
			printLog("Files registered succesfully.");
			try {
				serveroutput.writeObject(res);
				serveroutput.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private void peerFileLookup()
		{
			//Communicator res=new Communicator();
			String lookupFileName=null;
			ArrayList<PeerInfo> alf=new ArrayList<PeerInfo>();
			HashMap<PeerInfo, ArrayList<FileInfo>> lookMap=new HashMap<PeerInfo,ArrayList<FileInfo>>();
			int flag=0;
		try {
			res = (Communicator)serverinput.readObject();			
			if(res.getCommunicatorType().equals("lookupFileName"))
			{
				lookupFileName=res.getCommunicatorInfo().toString();
				printLog("Lookup request for File: " +lookupFileName);
			}
			
			for (int pi : fileList.keySet()) {
				ArrayList<FileInfo> fi=fileList.get(pi);
				ArrayList<FileInfo> resArl=new ArrayList<FileInfo>();
				for(FileInfo fInfo : fi)
				{
					if(fInfo.getFileName().equals(lookupFileName))
					{
						resArl.add(fInfo);
						flag=1;
					}
				} 
				if(flag==1)
				{
					PeerInfo peer=peerList.get(pi);
					lookMap.put(peer, resArl);
				}
			}
			
			/*System.out.println("------------------Lookup HashMap--------------------------");
			for (PeerInfo peerinfo : lookMap.keySet()) {
				ArrayList<FileInfo> fi=lookMap.get(peerinfo);
				System.out.println("PeerId: #"+peerinfo.getPeerId()+" with IP "+peerinfo.getIp());
				for(FileInfo fInfo : fi)
				{
					System.out.println("File Name : "+fInfo.getFileName()+" File Location: "+fInfo.getFileLocation());
				} 
			}*/
			
			if(lookMap.isEmpty())
			{
				printLog("File: "+lookupFileName+" Not Found.");
			}
			//System.out.println("before resp----");
			serveroutput.flush();
			res.setCommunicatorType("LookupMap");
			
			res.setCommunicatorInfo(lookMap);
			//System.out.println("After Resp-----write");
			serveroutput.writeObject(res);
			serveroutput.flush();
			//System.out.println("After Resp-----");
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private void peerFileLookupPrint()
		{
			Communicator res;
			String lookupFileName=null;
			ArrayList<PeerInfo> alf=new ArrayList<PeerInfo>();
			HashMap<PeerInfo, ArrayList<FileInfo>> lookMap=new HashMap<PeerInfo,ArrayList<FileInfo>>();
			int flag=0;
		try {
			res = (Communicator)serverinput.readObject();			
			if(res.getCommunicatorType().equals("lookupPrintFileName"))
			{
				lookupFileName=res.getCommunicatorInfo().toString();
				printLog("Lookup request for File: " +lookupFileName);
			}
			
			for (int pi : fileList.keySet()) {
				ArrayList<FileInfo> fi=fileList.get(pi);
				ArrayList<FileInfo> resArl=new ArrayList<FileInfo>();
				for(FileInfo fInfo : fi)
				{
					if(fInfo.getFileName().equals(lookupFileName))
					{
						resArl.add(fInfo);
						flag=1;
					}
				} 
				if(flag==1)
				{
					PeerInfo peer=peerList.get(pi);
					lookMap.put(peer, resArl);
				}
			}
			
			/*System.out.println("------------------Lookup HashMap--------------------------");
			for (PeerInfo peerinfo : lookMap.keySet()) {
				ArrayList<FileInfo> fi=lookMap.get(peerinfo);
				System.out.println("PeerId: #"+peerinfo.getPeerId()+" with IP "+peerinfo.getIp());
				for(FileInfo fInfo : fi)
				{
					System.out.println("File Name : "+fInfo.getFileName()+" File Location: "+fInfo.getFileLocation());
				} 
			}*/
			res.setCommunicatorType("LookupPrintMap");
			res.setCommunicatorInfo(lookMap);
			if(lookMap.isEmpty())
			{
				printLog("File: "+lookupFileName+" Not Found.");
			}
			serveroutput.writeObject(res);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
		public void peerUnregister(Communicator res)
		{
			printLog("Unregister request from "+ipAddress);
			try{
				int isShared=1;
				serveroutput.flush();
				
					for (int pi : peerList.keySet())
					{
						PeerInfo peerinfo=peerList.get(pi);
						if(peerinfo.getIp().equals(ipAddress))
						{
							//peerList.remove(pi);
							fileList.remove(pi);
							isShared=0;
						}
					}
					if(isShared==0) {
						res.setCommunicatorType("UnregisterSuccessfull");
						res.setCommunicatorInfo((String)"Files unregistered successfully!!!\nPlease select options 1 if you want to share any files.");
						serveroutput.writeObject(res);
						printLog("Files unregister successfully.");
						
					}
					else{
						res.setCommunicatorType("NotShared");
						res.setCommunicatorInfo((String)"Sorry you haven't shared any files! Select to option 1 to register/share any files");
						serveroutput.writeObject(res);
						printLog("No files found for unregistration");
					}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		
		public void peerClose(Communicator res)
		{
			printLog("Peer closing request from "+ipAddress);
			try{
				int isShared=1;
				serveroutput.flush();
				
					for (int pi : peerList.keySet())
					{
						PeerInfo peerinfo=peerList.get(pi);
						if(peerinfo.getIp().equals(ipAddress))
						{
							peerList.remove(pi);
							fileList.remove(pi);
							isShared=0;
						}
					}
					
					res.setCommunicatorType("CloseSuccessfull");
					res.setCommunicatorInfo((String)"Thankyou for Using Napster");
					serveroutput.writeObject(res);
					printLog("Peer closing request completed for "+ipAddress);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	
	
}
