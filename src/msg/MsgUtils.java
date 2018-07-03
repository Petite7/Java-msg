package msg;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




public class MsgUtils {
	private static int sequenceId=0;//序列编号
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray(); 
	/**
	 * 序列 自增
	 */
	public synchronized static int getSequence(){
		++sequenceId;
		if(sequenceId > 255){
			sequenceId=0;
		}
		return sequenceId;
	}
	/**
	 * 时间戳的明文,由客户端产生,格式为MMDDHHMMSS，即月日时分秒，10位数字的整型，右对齐 。
	 */
	public static String  getTimestamp(){
		DateFormat format=new SimpleDateFormat("MMddhhmmss");
		return format.format(new Date());
	}
	/**
	 * 用于鉴别源地址。其值通过单向MD5 hash计算得出，表示如下：
	 * AuthenticatorSource =
	 * MD5（Source_Addr+9 字节的0 +shared secret+timestamp）
	 * Shared secret 由中国移动与源地址实体事先商定，timestamp格式为：MMDDHHMMSS，即月日时分秒，10位。
	 * @return
	 */
	public static byte[] getAuthenticatorSource(String spId,String secret, String timestamp){
		try {
			MessageDigest md5=MessageDigest.getInstance("MD5");
			byte[] data=(spId+"\0\0\0\0\0\0\0\0\0"+secret+timestamp).getBytes();
			return md5.digest(data);
		} catch (NoSuchAlgorithmException e) {
			//log.error("SP链接到ISMG拼接AuthenticatorSource失败："+e.getMessage());
			return null;
		}
	}
	/**
	 * 用于鉴别ISMG地址。其值通过单向MD5 hash计算得出，表示如下：
	 * AuthenticatorSource =
	 * MD5（Status+AuthenticatorSource +shared secret）
	 * AuthenticatorSource为源地址实体发送给ISMG的对应消息CMPP_Connect中的值Shared secret由中国移动与源地址实体事先商定。
	 * @return
	 */
	public static byte[] getAuthenticatorISMG(int status, byte[] AuthenticatorSource,String secretp){
		try {
			MessageDigest md5=MessageDigest.getInstance("MD5");
			String str = new String(AuthenticatorSource);
			byte[] data=(status+str+secretp).getBytes();
			return md5.digest(data);
		} catch (NoSuchAlgorithmException e) {
			//log.error("SP链接到ISMG拼接AuthenticatorSource失败："+e.getMessage());
			return null;
		}
	}
	/**
	 * 向流中写入指定字节长度的字符串，不足时补0
	 * @param dous:要写入的流对象
	 * @param s:要写入的字符串
	 * @param len:写入长度,不足补0
	 */
	public static void writeString(DataOutputStream dous, String s, int len){
		if(s.length() > len) {
			//System.out.println("[DEBUG] String lenght exceed, msg : " + s + ", String length: " + s.length() + ",Restricked length : " + len);
			s = s.substring(0, len-1);
		}
		try {
			while(s.length() < len) {
				s += '\0';
			}
			byte[] data = s.getBytes();
			dous.write(data);
		} catch (IOException e) {
			//log.error("向流中写入指定字节长度的字符串失败："+e.getMessage());
		}
	}
	
	/**
	 * 从流中读取指定长度的字节，转成字符串返回
	 * @param ins:要读取的流对象
	 * @param len:要读取的字符串长度
	 * @return:读取到的字符串
	 */
	public static String readString(java.io.DataInputStream ins,int len){
		byte[] b=new byte[len];
		try {
			ins.read(b);
			String s=new String(b);
			s=s.trim();
			return s;
		} catch (IOException e) {
			return "";
		}
	}
	
	public static String bytesToHex(byte[] bytes) {  
	    char[] hexChars = new char[bytes.length * 2];  
	    for ( int j = 0; j < bytes.length; j++ ) {  
	        int v = bytes[j] & 0xFF;  
	        hexChars[j * 2] = hexArray[v >>> 4];  
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];  
	    }  
	    return new String(hexChars);  
	}  
	
	/***
	 * 长连接链路检测
	 * @throws IOException 
	 */
	public static void CMPP_ACTIVE_TEST(DataOutputStream out) throws IOException{
		Message_Header header=new Message_Header();
		header.setTotalLength(12);
		header.setCommandId(0x00000008);
		header.setSequenceId(MsgUtils.getSequence());
		List<byte[]> dataList=new ArrayList<byte[]>();
		dataList.add(header.toByteArray());
		if(out != null&&null!=dataList){
			for(byte[]data:dataList){
				out.write(data);
				out.flush();
				System.out.println("长连接链路检测中......");
			}
		}
	}	
	
	/***
	 * 转换字符串
	 *@param s: 要转换成int 的字符串
	 */
	
	public static int toInt(String s) {
		int ret = 0;
		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i) == '\0')
				return ret;
			ret = ret*10 + (s.charAt(i) - '0');
		}
		return ret;
	}
	
}
