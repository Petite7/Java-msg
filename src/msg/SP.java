package msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Petite7
 *
 */

class SP {
	public int port = 23333;
	public int request;
	public String source;
	public String destination;
	public String msgR;
	public Map<Integer, String> Service = new HashMap<Integer, String>();
	public static Socket socket;
	public static String IP="localhost";
	public static DataInputStream in;
	public static DataOutputStream out;
	
	public void addService(int id, String ret) {
		Service.put(id, ret);
	}

	public int responCONNECT(int toport, int thisport) throws IOException {
		System.out.println("[Info] [" + thisport + "] Sending CONNECT requeset to ISMG: ["+ toport + "]\n");
		CMPP_CONNECT connect = new CMPP_CONNECT();
		connect.setTotalLength(3*4+6+16+1+4);
		connect.setCommandId(0x00000001);
		connect.setSequenceId(MsgUtils.getSequence());
		connect.setSourceAddr(Integer.toString(port));
		String timestamp=MsgUtils.getTimestamp();
		connect.setAuthenticatorSource(MsgUtils.getAuthenticatorSource(Integer.toString(port), "passwd", timestamp));
		connect.setTimestamp(Integer.parseInt(timestamp));
		connect.setVersion((byte)0x20);
		List<byte[]> dataList=new ArrayList<byte[]>();
		dataList.add(connect.toByteArray());
		socket = new Socket(IP,toport);
		socket.setKeepAlive(true);
		socket.setSoTimeout(10000);
		out = new DataOutputStream(socket.getOutputStream());
		if(out != null && null != dataList){
			for(byte[]data:dataList){
				out.write(data);
				out.flush();
				System.out.println("[Info] [" + thisport + "] CONNECT packge to ISMG: [" + toport + "] send.\n");
			}
		}
		CMPP_CONNECT_RESP resp = null;
		in = new DataInputStream(socket.getInputStream());
		int len=in.readInt();
		List<byte[]> getData=new ArrayList<byte[]>();
		if(null != in && 0 != len){
			byte[] data=new byte[len-4];
			in.read(data);
			getData.add(data);
			for(byte[] returnData:getData){
				resp = new CMPP_CONNECT_RESP(returnData);
				System.out.println("[Info] [" + thisport + "] Get ISMG respond, status : " + resp.getStatus() + "\n");
			}
		}
		in.close();
		out.close();
		socket.close();
		return resp.getStatus();
	}

	public void sendSUBMIT(String msg, int service, String sour, String dest, int toport, int thisport) throws IOException {
		CMPP_SUBMIT submit = new CMPP_SUBMIT();
		submit.setMsg_Content(msg);
		submit.setMsg_Length((byte)msg.length());
		submit.setSrc_Id(dest);
		submit.setDest_terminal_Id(sour);
		submit.setService_Id(String.valueOf(service));
		submit.setMsg_src(String.valueOf(thisport));
		submit.setTotalLength(159+msg.getBytes().length);
		submit.setCommandId(0x00000004);
		submit.setSequenceId(MsgUtils.getSequence());
		socket = new Socket(IP,toport);
		socket.setKeepAlive(true);
		socket.setSoTimeout(10000);
		out = new DataOutputStream(socket.getOutputStream());
		List<byte[]> dataList=new ArrayList<byte[]>();
		dataList.add(submit.toByteArray());
		if(out != null && null != dataList){
			for(byte[]data:dataList){
				out.write(data);
				out.flush();
				System.out.println("[Info] [" + thisport + "] SUBMIT package to ISMG: [" + toport + "] send.\n");
			}
		}		
		in = new DataInputStream(socket.getInputStream());
		int len=in.readInt();
		List<byte[]> getData=new ArrayList<byte[]>();
		if(null != in && 0 != len){
			byte[] data=new byte[len-4];
			in.read(data);
			getData.add(data);
			for(byte[] returnData:getData){
				readSUBMIT_RESP(returnData);
			}
		}
		in.close();
		out.close();
		socket.close();
	}
	
	public void readSUBMIT_RESP(byte[] data) {
		CMPP_SUBMIT_RESP resp = new CMPP_SUBMIT_RESP(data);
		System.out.println("[Package]");
		System.out.println("[" + port + "]<--------Get CMPP_SUBMIT_RESP packege-------->");
		System.out.println("TotalLength: " + resp.getTotalLength());
		System.out.println("CommadnID: " + resp.getCommandId());
		System.out.println("Sequence: " + resp.getSequenceId());
		System.out.println("Msg_Id : " + resp.getMsg_Id());
		System.out.println("Status : " + resp.getResult());
		System.out.println("[" + port + "]<-------------------------------------------->\n");
	}
	
	public int routeISMG(int port) {
		if(port == 23335 || port == 23336)
			return 23332;
		else if(port == 23337 || port == 23338)
			return 23333;
		else if(port == 23339)
			return 23334;
		
		return -1;
	}
}

class SPListen extends SP implements Runnable{
	
	public  int port = 23333;
	
	public SPListen(int port) {
		this.port = port;
	}
	
	public void getDeliver(byte[] data) {
		CMPP_DELIVER deliver = new CMPP_DELIVER(data);
		System.out.println("[Package]");
		System.out.println("[" + port + "]<--------Get CMPP_DELIVER packege-------->");
		System.out.println("TotalLength: " + deliver.getTotalLength());
		System.out.println("CommadnID: " + deliver.getCommandId());
		System.out.println("Sequence: " + deliver.getSequenceId());
		System.out.println("Msg_Id: " + deliver.getMsg_Id());
		System.out.println("Source_ID: " + deliver.getDest_terminal_Id());
		System.out.println("Dest_Id: " + deliver.getDest_Id());
		System.out.println("Service_ID: " + deliver.getService_Id());
		System.out.println("Msg_Length: " + deliver.getMsg_Length());
		System.out.println("Msg_Content: " + deliver.getMsg_Content());
		System.out.println("[" + port + "]<---------------------------------------->\n");
		this.request = MsgUtils.toInt(deliver.getService_Id());
		this.source = deliver.getDest_terminal_Id();
		this.destination = deliver.getDest_Id();
		this.msgR = deliver.getMsg_Content();
	}
	
	public String msgServe(int Port, int requestId, String disc) {
		/*if(Port > 23335)*/ return new String("You got Service [" + requestId + "]: " + disc);
		//return new String("You got Service [" + disc + "]: " + Service.get(requestId));
	}
	
	@SuppressWarnings("resource")
	public void run() {
		/*Scanner cin = new Scanner(System.in);
		if(this.port == 23335) {
			System.out.println("[Info] Before start, You need to set your Service: [ServiceID] + [ReturnMessage]");
			int type = cin.nextInt();
			String serv = cin.next();
			addService(type, serv);
		}
		cin.close();*/
		ServerSocket SP = null;
		try {
			SP = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    while(true) {
	    	System.out.println("[Listen] [" + port + "] SP is waiting ISMG deliver...\n");
	    	try {
	    		Socket socketsp = SP.accept();
	    		in = new DataInputStream(socketsp.getInputStream());
	    		int len = in.readInt();
	    		//System.out.println("[DEBUG] Get socket byte stream, length : " + len);
	    		int connectStatus = 0;
				if(null != in && 0 != len){
					byte[] data=new byte[len-4];
					in.read(data);
					if(len == 3*4 + 8 + 4) {
						readSUBMIT_RESP(data);
					} else {
	    				getDeliver(data);
	    				connectStatus = responCONNECT(routeISMG(this.port), this.port);
	    				if(connectStatus == 0) {
	    					sendSUBMIT(msgServe(this.port, request, msgR), request, destination, source, routeISMG(this.port), this.port);
	    				} else {
	    					System.out.println("[Error] Incorrect response code : " + connectStatus + "\n");
	    				}
					}
				}
				socketsp.close();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
}

class SPsend extends SP implements Runnable{
	
	public int port = 2333;
	public int service;
	
	public SPsend(int port, String sour, String dest, int type, String msg) {
		this.port = port;
		this.source = sour;
		this.destination = dest;
		this.service = type;
		this.msgR = msg;
	}
	
	public void run() {		
		try {
			int status = responCONNECT(routeISMG(this.port), this.port);
			if(status == 0) {
				sendSUBMIT(msgR, service, destination, source, routeISMG(this.port), this.port);
				System.out.println("[Info] Message to " + destination + " send." );
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

