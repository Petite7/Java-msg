/**
 * 
 */
package msg;

/**
 * @author Petite7
 *
 */
public class LoginISMG {
	
	public static void main(String[] args) {
		new Thread(new ISMG(23332)).start();
		new Thread(new ISMG(23333)).start();
		new Thread(new ISMG(23334)).start();
	}

}
