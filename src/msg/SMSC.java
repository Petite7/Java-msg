/**
 * 
 */
package msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Petite7
 *
 */
class SMSCListen implements Runnable {
	
	public static int port = 23331;
	public static String IP = "localhost";
	public static HashMap<String, String> user = new HashMap<String, String>();
	public static Socket socket;
	public static DataInputStream in;
	public static DataOutputStream out;
	
	public SMSCListen() {
		
	}
	
	public int routeISMG(int serviceType) {
		if(serviceType >= 0 && serviceType <= 3)
			return 23332;
		else if(serviceType == 4 || serviceType == 5)
			return 23333;
		else if(serviceType == 6 || serviceType == 7)
			return 23334;
		
		return -1;
	}


	public void SMPPdisplay(SMPP smpp) {
		System.out.println("[Package]");
		System.out.println("<--------Get SMPP Response packege-------->");
		System.out.println("TotalLength: " + smpp.getTotalLength());
		System.out.println("CommadnID: " + smpp.getCommandId());
		System.out.println("Sequence: " + smpp.getSequenceId());
		System.out.println("Source: " + smpp.getSourceAddr());
		System.out.println("Dest  : " + smpp.getDestAddr());
		System.out.println("ServiceType: " + smpp.getServiceType());
		System.out.println("MsgResponse: " + smpp.getMsgContent());
		System.out.println("<----------------------------------------->\n");
		if(smpp.getSourceAddr().equals("*")) {
			for (HashMap.Entry<String, String> entry : user.entrySet()) { 
				entry.setValue(smpp.getMsgContent());
			} 
		} else {
			user.put(smpp.getSourceAddr(), smpp.getMsgContent());
		}
	}
	
	@SuppressWarnings("resource")
	public void run(){
		ServerSocket SMSC = null;
		try {
			SMSC = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    while(true) {
	    	System.out.println("[Listen] SMSC [" + port + "] Waiting ISMG response...\n");
	    	try {
			    Socket socketismg = SMSC.accept();
	    		in = new DataInputStream(socketismg.getInputStream());
	    		int len = in.readInt();
	    		if(null != in && 0 != len){
    				byte[] data = new byte[len-4];
    				in.read(data);
    				SMPP smpp = new SMPP(data);
    				SMPPdisplay(smpp);
    			}
    			socketismg.close();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
	}

}

class SMSCSend extends SMSCListen implements Runnable{
	
	public String source;
	public String destination;
	public int service;
	public String msg;
	
	public void toISMG(String phoneNumber, String dest, int serviceType, String msg) throws IOException {
		SMPP smpp = new SMPP();
		smpp.setSourceAddr(phoneNumber);
		smpp.setDestAddr(dest);
		smpp.setServiceType(serviceType);
		smpp.setMsgContent(msg);
		smpp.setTotalLength(3*4 + 94);
		smpp.setCommandId(0x00000008);
		smpp.setSequenceId(MsgUtils.getSequence());
//		System.out.println("[DEBUG]");
//		SMPPdisplay(smpp);
		List<byte[]> dataList=new ArrayList<byte[]>();
		dataList.add(smpp.toByteArray());
		System.out.println("[Info] SMPP Byte Data send : [" + MsgUtils.bytesToHex(dataList.get(0)) + "],  Data Length:" + dataList.get(0).length + "\n");
		socket = new Socket(IP, routeISMG(serviceType));
		socket.setKeepAlive(true);
		socket.setSoTimeout(10000);
		out = new DataOutputStream(socket.getOutputStream());
		if(out != null && null != dataList){
			for(byte[]data : dataList){
				out.write(data);
				out.flush();
				System.out.println("[Info] Sending SMPP request to ISMG: [" + routeISMG(serviceType) + "] complete.\n");
			}
		}
		out.close();
		socket.close();
	}
	
	public SMSCSend(String sour, String dest, int type, String msg) {
		this.source = sour;
		this.destination = dest;
		this.service = type;
		this.msg = msg;
	}
	
	public void run() {
		try {
			toISMG(source, destination, service, msg);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("[Info] Message to " + destination + " send, Please wait...\n" );
	}
}

class SMSCgetUser extends SMSCListen implements Runnable{
	
	public void run() {
		for (HashMap.Entry<String, String> entry : user.entrySet()) { 
			System.out.println("Phone = " + entry.getKey() + ", Message = " + entry.getValue()); 
		}
	}
	
}
