import java.io.Serializable;

public class PeerInfo implements Serializable{
	
	private String Ip;
	private int peerId;
	private String directory;
	public String getIp() {
		return Ip;
	}
	public void setIp(String ip) {
		Ip = ip;
	}
	public int getPeerId() {
		return peerId;
	}
	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	

}
