/**
 * 
 */
package msg;

/**
 * @author Petite7
 *
 */
public class LoginSMSC {
	
	public static void send(String sour, String dest, int type, String msg) {
		new Thread(new SMSCSend(sour, dest, type, msg)).start();
	}
	
	public static void main(String[] args) {
		
		new Thread(new SMSCListen()).start();
	}

}
