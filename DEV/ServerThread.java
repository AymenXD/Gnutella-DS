import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;

public class ServerThread extends Thread {
String FileDir;
int port_no;
ServerSocket serverSocket=null;
Socket socket=null;
int peer_id;
static ArrayList<String> msg;
	ServerThread(int port,String SharedDir,int peer_id)
	{
		port_no=port;
		FileDir=SharedDir;
		this.peer_id=peer_id;
		msg=new ArrayList<String>();
	}
	
	public void run()
	{	
		try
		{	
			serverSocket=new ServerSocket(port_no);
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
		while(true)
		{
			try{
					socket=serverSocket.accept();
					System.out.println("Connected to client at "+socket.getRemoteSocketAddress()+" with peer "+peer_id);
					new Download(socket,FileDir,peer_id,msg).start();
				}
			catch(IOException io)
			{
				io.printStackTrace();
			}
		}
	}
}


class Download extends Thread
{
	protected Socket socket;
	String FileDirectory;
	int port;
	String fname;
	int peer_id;
	//Peer p=new Peer();
	ArrayList<String> peermsg; 
	ArrayList<Thread> thread=new ArrayList<Thread>();
	ArrayList<ClientThread> peerswithfiles=new ArrayList<ClientThread>();
	int[] peersArray_list=new int[20];
	int[] a=new int[20];
	int countofpeers=0;
	int messageId;
	int set=0;
	int TTL_value;
	MsgModel MF=new MsgModel();
	Download(Socket socket,String FileDirectory,int peer_id,ArrayList<String> peermsg)
	{
		this.socket=socket;
		this.FileDirectory=FileDirectory;
		this.peer_id=peer_id;
		this.peermsg=peermsg;
	}
	
	public void run()
	{
		try{
			System.out.println("server thread for peer"+peer_id);
			
			InputStream is=socket.getInputStream();
			ObjectInputStream ois=new ObjectInputStream(is);
			OutputStream os=socket.getOutputStream();
			ObjectOutputStream oos=new ObjectOutputStream(os);
			boolean peerduplicate;
			
			MF=(MsgModel)ois.readObject();					
		
			System.out.println("got request from "+MF.fromPeerId);
			
			
			 peerduplicate=this.peermsg.contains(MF.msgId);
			 if(peerduplicate==false)
			 {
				 this.peermsg.add(MF.msgId);
			 }
			 else
			 {
				 System.out.println("duplicate");
			 }
			
			 fname=MF.fileName;
			 System.out.println("Found: "+fname);

			
			if(!peerduplicate)
			{
				File newfind;
				File directoryObj = new File(FileDirectory);
				String[] filesList = directoryObj.list();
				for (int j = 0; j < filesList.length; j++)
				{ 
					newfind = new File(filesList[j]);
					if(newfind.getName().equals(fname))
					{
						peersArray_list[countofpeers++]=peer_id;
						break;
					}
				}
				System.out.println("Local Search Completed");
				Properties prop = new Properties();
				Main M=new Main();
				String fileName = M.fn;
				is = new FileInputStream(fileName);
				prop.load(is);
				String temp=prop.getProperty("peer"+peer_id+".next");
		    	if(temp!=null && MF.ttlValue>0)
	    		{
	    			
	    			String[] neighbours=temp.split(",");
		  	
	    			for(int i=0;i<neighbours.length;i++)
	    			{   
	    				if(MF.fromPeerId==Integer.parseInt(neighbours[i]))	//creat client thread for all neighbouring peers
	    				{
	    					continue;
	    				}
	    				int connectingport=Integer.parseInt(prop.getProperty("peer"+neighbours[i]+".port"));
	    				int neighbouringpeer=Integer.parseInt(neighbours[i]);
	    				
	    				System.out.println("sending to "+neighbouringpeer);
	    				ClientThread cp=new ClientThread(connectingport,neighbouringpeer,fname,MF.msgId,peer_id,MF.ttlValue--);
	    				Thread t=new Thread(cp);
	    				t.start();
	    				thread.add(t);
	    				peerswithfiles.add(cp);
	    				
	    			}
		    	}
			  	//oos.writeObject(p);
	  			for(int i=0;i<thread.size();i++)
  				{
  					((Thread) thread.get(i)).join();
  				}
	  			for(int i=0;i<peerswithfiles.size();i++)
  				{
  					a=((ClientThread)peerswithfiles.get(i)).getarray();
  					for(int j=0;j<a.length;j++)
  					{	if(a[j]==0)
  						break;
  						peersArray_list[countofpeers++]=a[j];
  					}
  				}
			}
			oos.writeObject(peersArray_list);
			
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
			
		}
	}
}