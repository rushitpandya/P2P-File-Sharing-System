import java.io.Serializable;

public class FileInfo implements Serializable{

	private String fileName;
	private String fileLocation;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileLocation() {
		return fileLocation;
	}
	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}
	
	
	
}
