import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Peer {

	private static final int PEER_LISTEN_PORT=2000;
	private static final int PEER_REQUEST_PORT=10000;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			System.out.println("Peer Client Started!!");
			new PeerClient().start();
	}

	static class PeerClient extends Thread{
		
		String serverIp=null;
		public void run()
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			try {
				System.out.println("Enter the index server ip address!!");
				serverIp=input.readLine();
				Socket clientsocket=new Socket(serverIp,PEER_REQUEST_PORT);
				ObjectOutputStream serveroutput=new ObjectOutputStream(clientsocket.getOutputStream());
				serveroutput.flush();
				ObjectInputStream serverinput=new ObjectInputStream(clientsocket.getInputStream());
				Response res=(Response)serverinput.readObject();
				System.out.println((String)res.getResponseInfo());
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
