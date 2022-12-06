import java.net.Socket;
import java.io.IOException;
import java.io.*;

public class ClientThread extends Thread {

int portofconnection;
int peertoconnect;
String filetodownload;
Socket socket=null;
int[] peersArray;
MsgModel MF=new MsgModel();
String msgid;
int frompeer_id;
int TTL_value;

	public ClientThread(int portofconnection,int peertoconnect,String filetodownload,String msgid,int frompeer_id,int TTL_value)
	{
		this.portofconnection=portofconnection;
		this.peertoconnect=peertoconnect;
		this.filetodownload=filetodownload;
		this.msgid=msgid;
		this.frompeer_id=frompeer_id;
		this.TTL_value=TTL_value;
	}

	public void run()
	{
		try{
				socket=new Socket("localhost",portofconnection);		
				OutputStream os=socket.getOutputStream();
				ObjectOutputStream oos=new ObjectOutputStream(os);
				InputStream is=socket.getInputStream();
				ObjectInputStream ois=new ObjectInputStream(is);
				MF.fileName=filetodownload;							 
				MF.msgId=msgid;
				MF.fromPeerId=frompeer_id;
				MF.ttlValue=TTL_value;
				oos.writeObject(MF);
		
				peersArray=(int[])ois.readObject();
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
			catch(ClassNotFoundException cp)
			{
				cp.printStackTrace();
			}
	}
	
	public int[] getarray()
	{
		return peersArray;
	}
}

