import java.io.Serializable;

public class DownloadInfo implements Serializable{
private PeerInfo peerInfo;
private FileInfo fileInfo;
public PeerInfo getPeerInfo() {
	return peerInfo;
}
public void setPeerInfo(PeerInfo peerInfo) {
	this.peerInfo = peerInfo;
}
public FileInfo getFileInfo() {
	return fileInfo;
}
public void setFileInfo(FileInfo fileInfo) {
	this.fileInfo = fileInfo;
}

}
