import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Peer {
	public static final int PEER_LISTEN_PORT = 2000;
	public static final int PEER_REQUEST_PORT = 10000;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Peer Client Started!!");
		new PeerClient().start();
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
			System.out.println("1. Register Files with the server");
			System.out.println("2. Lookup Files with the server");
			System.out.println("3. Unregister Files with the server");
			System.out.println("4. Exit");
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
					break;
				case 3:
					break;
				case 4:
					break;
				default:
					break;
			}
		}
	}
	
	private void registerPeerFiles()
	{
		String path=null;
		System.out.println("Enter the path of directory/file that you want to share with others peer:");
		try {
			path=input.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Wrong path! Enter valid path");
		}
	}

	private void connection() {
		try {
			System.out.println("Enter the index server ip address!!");
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
	
	private void register()
	{
		
	}

}
