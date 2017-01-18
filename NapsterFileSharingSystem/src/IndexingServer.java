import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class IndexingServer {

	private static HashMap<Integer, String> peerList=new HashMap<Integer,String>();
	
	private static final int INDEX_SERVER_PORT=10000;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
			int peerId=1;
			ServerSocket serverSocket=new ServerSocket(INDEX_SERVER_PORT);;
			System.out.println("Indexing Server Started!!!");
			System.out.println("Waiting for new connection from peer!!!");
			while(true)
			{
				new IndexServer(peerId++,serverSocket.accept()).start();
			}
	}

	private static class IndexServer extends Thread {
		int peerId;
		Socket clientSocket;
		
		public IndexServer(int peerId,Socket clientSocket) throws IOException
		{	
			this.peerId=peerId;
			this.clientSocket=clientSocket;
			System.out.println("Connection Estaiblished with Peer having IP "
			+clientSocket.getInetAddress());
		}
		
		public void run()
		{	
			peerConnection();
		}
		
		public void peerConnection()
		{
			try {
					ObjectOutputStream serveroutput=new ObjectOutputStream(clientSocket.getOutputStream());
					serveroutput.flush();
					ObjectInputStream serverinput=new ObjectInputStream(clientSocket.getInputStream());
					
					Communicator res=new Communicator();
					res.setCommunicatorInfo("Welcome to the P2P File Sharing System!! Please below options of your choice");
					serveroutput.writeObject(res);
					//String peerValue=peerId+"#"+clientSocket.getInetAddress().getHostAddress()+"#"+clientSocket.getPort();
					//peerList.put(peerId, peerValue);
				} catch (Exception e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
	}
}
