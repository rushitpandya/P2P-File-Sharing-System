
import java.io.Serializable;

public class Request implements Serializable {
	
	private String requestType;
	private Object requestInfo;
	
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public Object getRequestInfo() {
		return requestInfo;
	}
	public void setRequestInfo(Object requestInfo) {
		this.requestInfo = requestInfo;
	}
	
}