import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
			ServerSocket serverSocket=new ServerSocket(INDEX_SERVER_PORT);;
			System.out.println("Indexing Server Started!!!");
		
			System.out.println("Waiting for new connection from peer!!!");
			System.out.println();
			while(true)
			{
				new IndexServer(serverSocket.accept()).start();
			}
	}

	private static class IndexServer extends Thread {
		
		Socket clientSocket;
		String ipAddress;
		ObjectOutputStream serveroutput=null;
		ObjectInputStream serverinput=null;
		
		public IndexServer(Socket clientSocket) throws IOException
		{	
			this.clientSocket=clientSocket;
			System.out.println("Connection Estaiblished with Peer having IP "
			+clientSocket.getInetAddress());
		}
		
		public void run()
		{	
			
			try {
				serveroutput = new ObjectOutputStream(clientSocket.getOutputStream());
				serveroutput.flush();
				serverinput=new ObjectInputStream(clientSocket.getInputStream());
				Communicator res=new Communicator();
				
				res.setCommunicatorInfo("Welcome to the P2P File Sharing System!! Please below options of your choice");
				serveroutput.writeObject(res);
				serveroutput.flush();
			
				while(true){
					String choice=null;
					res=(Communicator)serverinput.readObject();
					choice = res.getCommunicatorType().toString();
					serveroutput.writeObject(res);
					
					System.out.println("in while loop:-"+res.getCommunicatorType());
					switch(choice){
						case "REGISTERPEER":
							//System.out.println("in case: REGISTERPEER:");
							peerConnection();
							break;
						default:
							break;
					}
					
				}//end while(true)
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void peerConnection()
		{
			int flag=0;
			String dirCheck=null;
			try {
					//ObjectOutputStream serveroutput=new ObjectOutputStream(clientSocket.getOutputStream());
					//serveroutput.flush();
					//ObjectInputStream serverinput=new ObjectInputStream(clientSocket.getInputStream());
					Communicator res=new Communicator();
					/*res.setCommunicatorInfo("Welcome to the P2P File Sharing System!! Please below options of your choice");
					serveroutput.writeObject(res);
					serveroutput.flush();*/
					ipAddress=clientSocket.getInetAddress().getHostAddress();
					res=(Communicator)serverinput.readObject();
					System.out.println("Info :"+res.getCommunicatorType());
					if(res.getCommunicatorType().equals("Peercheck"))
					{
						dirCheck=res.getCommunicatorInfo().toString();
						System.out.println(dirCheck);
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
						System.out.println("New Peer If");
					}
					else{
						
						serveroutput.writeObject(res);
					}
					
					res=(Communicator)serverinput.readObject();
					System.out.println(res.getCommunicatorType());
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
					
				} catch (Exception e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
		
		private void peerRegister(Communicator comm)
		{
			Communicator res=new Communicator();
			ArrayList<FileInfo> fileInfo=(ArrayList<FileInfo>)comm.getCommunicatorInfo();
			//String peerInfo=peerId+"#"+clientSocket.getInetAddress().getHostAddress()+"#"+clientSocket.getPort();
			fileList.put(peerId, fileInfo);
			System.out.println("All the files are registered for Peer #"+peerId);
			System.out.println("-------Peer List-----");
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
			}
			res.setCommunicatorType("registrationSuccessfull");
			res.setCommunicatorInfo("Peer registered with given files successfully...!!!");
			try {
				serveroutput.writeObject(res);
				serveroutput.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	
}
