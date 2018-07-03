package msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CMPP_SUBMIT_RESP extends Message_Header{
private long Msg_Id;
private byte Result;

	public CMPP_SUBMIT_RESP() {
		super();
	}

	public CMPP_SUBMIT_RESP(byte[] data){
		ByteArrayInputStream bins=new ByteArrayInputStream(data);
		DataInputStream dins=new DataInputStream(bins);
		try {
			this.setTotalLength(data.length+4);
			this.setCommandId(dins.readInt());
			this.setSequenceId(dins.readInt());
			this.Msg_Id=dins.readLong();
			this.Result=dins.readByte();
			dins.close();
			bins.close();
		} catch (IOException e){}
	}

	public byte[] toByteArray() {
		ByteArrayOutputStream bous=new ByteArrayOutputStream();
		DataOutputStream dous=new DataOutputStream(bous);
		try {
			dous.writeInt(this.getTotalLength());
			dous.writeInt(this.getCommandId());
			dous.writeInt(this.getSequenceId());
			dous.writeLong(this.Msg_Id);
			dous.writeByte(this.getResult());
			dous.close();
		} catch (IOException e) {
			System.out.print("CMPP_SUBMIT_RESP封装链接二进制数组失败。");
		}
		return bous.toByteArray();
	}
	
	public long getMsg_Id() {
		return Msg_Id;
	}


	public void setMsg_Id(long msgId) {
		Msg_Id = msgId;
	}


	public byte getResult() {
		return Result;
	}


	public void setResult(byte result) {
		Result = result;
	}
	
	
}
