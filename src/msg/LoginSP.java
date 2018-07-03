/**
 * 
 */
package msg;

/**
 * @author Petite7
 *
 */
public class LoginSP {
	
	public static void send(int port, String sour, String dest, int type, String msg) {
		new Thread(new SPsend(port, sour, dest, type, msg)).start();
	}
	
	public static void main(String[] args) {
		new Thread(new SPListen(23335)).start();
		new Thread(new SPListen(23336)).start();
		new Thread(new SPListen(23337)).start();
		new Thread(new SPListen(23338)).start();
		new Thread(new SPListen(23339)).start();
	}

}
