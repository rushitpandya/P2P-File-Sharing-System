
import java.io.Serializable;

public class Communicator implements Serializable {
	
	private String communicatorType;
	private Object communicatorInfo;
	
	public String getCommunicatorType() {
		return communicatorType;
	}
	public void setCommunicatorType(String communicatorType) {
		this.communicatorType = communicatorType;
	}
	public Object getCommunicatorInfo() {
		return communicatorInfo;
	}
	public void setCommunicatorInfo(Object communicatorInfo) {
		this.communicatorInfo = communicatorInfo;
	}
	
}