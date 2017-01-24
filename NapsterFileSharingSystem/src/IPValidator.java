import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPValidator {
	private static Pattern pat;
	private static Matcher match;

	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	
	public static boolean validate(final String ip) {
		pat = Pattern.compile(IPADDRESS_PATTERN);
		match = pat.matcher(ip);
		boolean flag,flag1=false;
		flag =match.matches();
		if(ip.equalsIgnoreCase("localhost"))
		{
			flag1=true;
		}
		if(flag==true || flag1==true)
		{
			return true;
		}
		else{
			return false;	
		}
		
	}
	
}