package msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Petite7
 *
 */
public class ISMG implements Runnable{

	public int port=23332;	
	public static int portSMSC = 23331;
	public static Socket socket;
	public static String IP="localhost";
	public static DataInputStream in;
	public static DataOutputStream out;

	public ISMG(int port) {
		this.port = port;
	}
	
	public void showCMPP_CONNECT(CMPP_CONNECT connect) {
		System.out.println("[Package]");
		System.out.println("[" + port + "]<--------Get CMPP_CONNECT packege-------->");
		System.out.println("TotalLength: " + connect.getTotalLength());
		System.out.println("CommadnID: " + connect.getCommandId());
		System.out.println("Sequence: " + connect.getSequenceId());
		System.out.println("Source: " + connect.getSourceAddr());
		System.out.println("Authorized(MD5): " + MsgUtils.bytesToHex(connect.getAuthenticatorSource()));
		System.out.println("Version: " + connect.getVersion());
		System.out.println("Timestamp: " + connect.getTimestamp());
		System.out.println("[" + port + "]<---------------------------------------->\n");
	}
	
	public void showSMPP(SMPP smpp) {
		System.out.println("[Package]");
		System.out.println("[" + port + "]<--------Get SMPP Request packege-------->");
		System.out.println("TotalLength: " + smpp.getTotalLength());
		System.out.println("CommadnID: " + smpp.getCommandId());
		System.out.println("Sequence: " + smpp.getSequenceId());
		System.out.println("Source: " + smpp.getSourceAddr());
		System.out.println("Dest  : " + smpp.getDestAddr());
		System.out.println("ServiceType: " + smpp.getServiceType());
		System.out.println("MsgContent: " + smpp.getMsgContent());
		System.out.println("[" + port + "]<---------------------------------------->\n");
	}
	
	public void showDeliver(CMPP_DELIVER deliver) {
		System.out.println("[Package]");
		System.out.println("[" + port + "]<-------- CMPP_DELIVER packege-------->");
		System.out.println("TotalLength: " + deliver.getTotalLength());
		System.out.println("CommadnID: " + deliver.getCommandId());
		System.out.println("Sequence: " + deliver.getSequenceId());
		System.out.println("Msg_Id: " + deliver.getMsg_Id());
		System.out.println("Dest_Id: " + deliver.getDest_Id());
		System.out.println("Service_ID: " + deliver.getService_Id());
		System.out.println("Msg_Length: " + deliver.getMsg_Length());
		System.out.println("Msg_Content: " + deliver.getMsg_Content());
		System.out.println("[" + port + "]<------------------------------------->\n");
	}
	
	
	public int routeSP(int service) {
		if(service == 0 || service == 1)
			return 23335;
		else if(service == 2 || service == 3)
			return 23336;
		else if(service == 4)
			return 23337;
		else if(service == 5)
			return 23338;
		else if(service == 6 || service == 7)
			return 23339;
		
		return -1;
	}
	
	public int readSPConnect(byte[] data, Socket SP) throws IOException {
		int status = 1;
		try {
			List<byte[]> getData = new ArrayList<byte[]>();
			getData.add(data);
			byte[] auth = null;
			for(byte[] returnData:getData){
				CMPP_CONNECT connect = new CMPP_CONNECT(returnData);
				auth = connect.getAuthenticatorSource();
	    		showCMPP_CONNECT(connect);
			}
			status = 0;
			CMPP_CONNECT_RESP resp = new CMPP_CONNECT_RESP();
			resp.setTotalLength(3*4+1+16+1);
			resp.setCommandId(0x80000001);
			resp.setSequenceId(MsgUtils.getSequence());
			resp.setStatus(status);
			resp.setAuthenticatorISMG(MsgUtils.getAuthenticatorISMG(resp.getStatus(), auth, "passwd"));
			resp.setVersion((byte)0x20);
			
			List<byte[]> dataList=new ArrayList<byte[]>();
			dataList.add(resp.toByteArray());
			out = new DataOutputStream(SP.getOutputStream());
			if(out != null && null != dataList){
				for(byte[]returndata:dataList){
					out.write(returndata);
					out.flush();
					System.out.println("[Info] [" + port + "]Authorization to SP Complete, return status：" + status + " ,clear to get SUBMMIT...\n");
				}
			}
			out.close();
		} catch (Exception e) {
			//TODO : 异常处理，当SP没有返回信息(socket closed)时处理返回，例如：Destination Unreachable
			e.printStackTrace();
		}
		return 0;
	}
	
	public void readSMSCRequest(byte[] data) {
		try {
			SMPP smpp = new SMPP(data);
    		showSMPP(smpp);
    		int serviceType = smpp.getServiceType();
    		String sour = smpp.getSourceAddr();
    		String dest = smpp.getDestAddr();
    		String msg = smpp.getMsgContent();
    		int SPport = routeSP(serviceType);
    		CMPP_DELIVER deliver = new CMPP_DELIVER();
    		deliver.setDest_Id(dest);
    		deliver.setDest_terminal_Id(sour);
    		deliver.setService_Id(String.valueOf(serviceType));
    		deliver.setMsg_Content(msg);
    		deliver.setMsg_Length(msg.length());
    		deliver.setTotalLength(3*4 + 136);
    		deliver.setCommandId(0x00000007);
    		deliver.setSequenceId(MsgUtils.getSequence());
    		List<byte[]> dataList=new ArrayList<byte[]>();
    		dataList.add(deliver.toByteArray());
    		Socket toSP = new Socket(IP, SPport);
    		toSP.setKeepAlive(true);
    		toSP.setSoTimeout(10000);
    		out = new DataOutputStream(toSP.getOutputStream());
    		if(out != null && null != dataList){
    			for(byte[]datal : dataList){
    				out.write(datal);
    				out.flush();
    				System.out.println("[Info] [" + port + "] DELIVER to SP: [" + SPport + "] send. Waiting Response...\n");
    			}
    		}
    		toSP.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readSUBMIT(byte[] data, Socket socket) throws IOException {
		CMPP_SUBMIT submit = new CMPP_SUBMIT(data);
		CMPP_SUBMIT_RESP resp = new CMPP_SUBMIT_RESP();
		resp.setMsg_Id(0);
		resp.setResult((byte)0);
		resp.setTotalLength(3*4 + 8 + 4);
		resp.setCommandId(0x00000010);
		resp.setSequenceId(MsgUtils.getSequence());
		List<byte[]> dataList=new ArrayList<byte[]>();
		dataList.add(resp.toByteArray());
		out = new DataOutputStream(socket.getOutputStream());
		if(out != null && null != dataList){
			for(byte[]datal : dataList){
				out.write(datal);
				out.flush();
				System.out.println("[Info] [" + port + "] SUBMIT_RESP to SP: " + submit.getMsg_src() + " send. \n");
			}
		}
		out.close();
		socket.close();
		SMPP smpp = new SMPP();
		smpp.setSourceAddr(submit.getSrc_Id());
		smpp.setDestAddr(submit.getDest_terminal_Id());
		smpp.setServiceType(MsgUtils.toInt(submit.getService_Id()));
		smpp.setMsgContent(new String(submit.getMsg_Content()));
		smpp.setTotalLength(3*4 + 94);
		smpp.setCommandId(0x00000011);
		smpp.setSequenceId(MsgUtils.getSequence());
		List<byte[]> dataL=new ArrayList<byte[]>();
		dataL.add(smpp.toByteArray());
		Socket toSMSC = new Socket(IP, portSMSC);
		toSMSC.setKeepAlive(true);
		toSMSC.setSoTimeout(10000);
		out = new DataOutputStream(toSMSC.getOutputStream());
		if(out != null && null != dataL){
			for(byte[]datal : dataL){
				out.write(datal);
				out.flush();
				System.out.println("[Info] [" + port + "] SMPP Response to SMSC send. \n");
			}
		}
		out.close();
		toSMSC.close();
	}
	
	@SuppressWarnings("resource")
	public void run() {
		ServerSocket ISMG = null;
		try {
			ISMG = new ServerSocket(port);
		}catch(Exception e) {
			e.printStackTrace();
		}
	    while(true) {
	    	System.out.println("[Listen] [" + port +"] ISMG Wating Message from SMSC & SP...\n");
	    	try {
	    		socket = ISMG.accept();
	    		in = new DataInputStream(socket.getInputStream());
				int len = in.readInt();
				byte[] getData = new byte[len - 4];
				in.read(getData);
				if(len == 3*4 + 27) {
					readSPConnect(getData, socket);
				} else if(len == 3*4 + 94) {
					readSMSCRequest(getData);
				} else if(len >= 159) {
					readSUBMIT(getData, socket);
				}
				in.close();
			    socket.close();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
	}

}
