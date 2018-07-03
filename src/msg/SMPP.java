/**
 * 
 */
package msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Petite7
 *
 */
public class SMPP extends Message_Header{

	private String sourceAddr;		//20 Bytes
	private String destAddr;		//20 Bytes
	private int serviceType;		//4  Bytes
	private String msgContent;		//50 Bytes

	public SMPP() {
		super();
	}
	
	public void showSMPP(SMPP smpp) {
		System.out.println("[$$]<--------SMPP  packege-------->[$$]");
		System.out.println("TotalLength: " + smpp.getTotalLength());
		System.out.println("CommadnID: " + smpp.getCommandId());
		System.out.println("Sequence: " + smpp.getSequenceId());
		System.out.println("Source: " + smpp.getSourceAddr());
		System.out.println("Dest:   " + smpp.getDestAddr());
		System.out.println("ServiceType: " + smpp.getServiceType());
		System.out.println("MsgContent: " + smpp.getMsgContent());
		System.out.println("[$$]<----------------------------->[$$]");
	}
	
	public SMPP(byte[] data) throws IOException {
		ByteArrayInputStream bins=new ByteArrayInputStream(data);
		DataInputStream dins=new DataInputStream(bins);
		if(data.length == 94 + 4*2) {
			this.setTotalLength(data.length + 4);
			this.setCommandId(dins.readInt());
			this.setSequenceId(dins.readInt());
			byte[] addr = new byte[20];
			dins.read(addr);
			this.setSourceAddr(new String(addr));
			byte[] daddr = new byte[20];
			dins.read(daddr);
			this.setDestAddr(new String(daddr));
			this.setServiceType(dins.readInt());
			byte[] msg = new byte[50];
			dins.read(msg);
			this.setMsgContent(new String(msg));
			dins.close();
			bins.close();
			//showSMPP(this);
		} else {
			System.out.println("解析SMPP数据包出错，包长度不一致。长度为:"+data.length);
		}
	}
	
	public byte[] toByteArray() {
		//System.out.println("[DEBUG]SMPP toByteArray msglength : " +  this.msgContent.length());
		//showSMPP(this);
		ByteArrayOutputStream bous=new ByteArrayOutputStream();
		DataOutputStream dous=new DataOutputStream(bous);
		try{
			dous.writeInt(this.getTotalLength());
			dous.writeInt(this.getCommandId());
			dous.writeInt(this.getSequenceId());
			MsgUtils.writeString(dous,this.sourceAddr,20);
			MsgUtils.writeString(dous,this.destAddr,20);
			dous.writeInt(this.getServiceType());
			MsgUtils.writeString(dous,this.msgContent,50);
			dous.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return bous.toByteArray();
	}
	
	public void setSourceAddr(String addr) {
		this.sourceAddr = addr;
	}
	
	public void setMsgContent(String msg) {
		this.msgContent = msg;
	}
	
	public void setDestAddr(String addr) {
		this.destAddr = addr;
	}
	
	public void setServiceType(int type) {
		this.serviceType = type;
	}
	
	public String getSourceAddr() {
		return this.sourceAddr;
	}
	
	public String getDestAddr() {
		return this.destAddr;
	}
	
	public String getMsgContent() {
		return this.msgContent;
	}
	
	public int getServiceType() {
		return this.serviceType;
	}

}
