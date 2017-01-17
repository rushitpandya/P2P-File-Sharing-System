import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class IndexingServer {

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
			System.out.println("Connection Estaiblished with Peer #"+peerId+" having IP "
			+clientSocket.getInetAddress());
		}
		
		public void run()
		{	
			try {
				ObjectOutputStream serveroutput=new ObjectOutputStream(clientSocket.getOutputStream());
				serveroutput.flush();
				ObjectInputStream serverinput=new ObjectInputStream(clientSocket.getInputStream());
				Communicator res=new Communicator();
				res.setCommunicatorInfo("Welcome to the Napster!! Your peer id is #"+peerId);
				serveroutput.writeObject(res);
				//System.out.println("mdls");
				} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			
		}
	}
}
