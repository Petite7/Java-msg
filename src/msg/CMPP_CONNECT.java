package msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;



public class CMPP_CONNECT extends Message_Header{
	private String sourceAddr;//
	private byte[] authenticatorSource;//
	private byte version;//
	private int timestamp;//
	
	public CMPP_CONNECT() {
		super();
	}
	
	public CMPP_CONNECT(byte[] data) {
		if(data.length==8+6+16+1+4){
			ByteArrayInputStream bins=new ByteArrayInputStream(data);
			DataInputStream dins=new DataInputStream(bins);
			try {
				this.setTotalLength(data.length + 4);
				this.setCommandId(dins.readInt());
				this.setSequenceId(dins.readInt());
				byte[] sourceaddr = new byte[6];
				dins.read(sourceaddr);
				this.setSourceAddr(new String(sourceaddr));
				byte[] aiByte=new byte[16];
				dins.read(aiByte);
				this.authenticatorSource=aiByte;
				this.version=dins.readByte();
				this.setTimestamp(dins.readInt());
				dins.close();
				bins.close();
			} catch (IOException e){}
		}else{
			System.out.println("链接至IMSP,解析数据包出错，包长度不一致。长度为:"+data.length);
		}
	}
	
	public byte[] toByteArray(){
		ByteArrayOutputStream bous=new ByteArrayOutputStream();
		DataOutputStream dous=new DataOutputStream(bous);
		try {
			dous.writeInt(this.getTotalLength());
			dous.writeInt(this.getCommandId());
			dous.writeInt(this.getSequenceId());
			MsgUtils.writeString(dous,this.sourceAddr,6);
			dous.write(authenticatorSource);
			dous.writeByte(version);
			dous.writeInt(timestamp);
			dous.close();
		} catch (IOException e) {
			System.out.print("封装链接二进制数组失败。");
		}
		return bous.toByteArray();
	}
	
	public String getSourceAddr() {
		return sourceAddr;
	}
	public void setSourceAddr(String sourceAddr) {
		this.sourceAddr = sourceAddr;
	}
	public byte[] getAuthenticatorSource() {
		return authenticatorSource;
	}
	public void setAuthenticatorSource(byte[] authenticatorSource) {
		this.authenticatorSource = authenticatorSource;
	}
	public byte getVersion() {
		return version;
	}
	public void setVersion(byte version) {
		this.version = version;
	}
	public int getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

}
