
import java.io.Serializable;

public class Response implements Serializable {
	
	private String responseType;
	private Object responseInfo;
	
	public String getResponseType() {
		return responseType;
	}
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}
	public Object getResponseInfo() {
		return responseInfo;
	}
	public void setResponseInfo(Object responseInfo) {
		this.responseInfo = responseInfo;
	}
	
}