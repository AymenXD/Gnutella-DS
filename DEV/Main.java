import java.net.*;
import java.util.Properties;
import java.io.*;
import java.util.*;

public class Main {
	static String fn;
	public static void main(String[] args)  {
		int id;
		int ports;
		int portserver;
		Scanner scan = new Scanner(System.in);
		String serverName = "localhost";
		int peer;
		int count=0;
		int TTL_Value;
		String msgid;
		String sharedDir;
		ArrayList<Thread> thread=new ArrayList<Thread>();				
		ArrayList<ClientThread> peers=new ArrayList<ClientThread>();
		try {
			System.out.println("Welcome to the distributed file sharing system!");
			System.out.println("----------------------------------");
			System.out.println("initializing nodes...");
			int peer_id=Integer.parseInt(args[1]);
			sharedDir=args[2];
			System.out.println("Peer "+peer_id+" has joined the network. \n Linking media on : "+ sharedDir);
			Properties prop = new Properties();	
		    fn = args[0];
		    System.out.println("Adopted topology : "+fn);
		    InputStream is = new FileInputStream(fn);
		    prop.load(is);
		    ports=Integer.parseInt(prop.getProperty("peer"+peer_id+".serverport"));
		    ServerDownload sd=new ServerDownload(ports,sharedDir);
		    sd.start();
		    portserver=Integer.parseInt(prop.getProperty("peer"+peer_id+".port"));
			ServerThread cs=new ServerThread(portserver,sharedDir,peer_id);
			cs.start();
			System.out.println("----------------------------------");
			System.out.println("\n     File downloader (1) \n");
			System.out.println("----------------------------------");
			System.out.println(">> ");
			int ch=scan.nextInt();
			scan.nextLine();
			if(ch==1)
			{
					System.out.println("Search :");
			}
					String f_name=scan.nextLine();
					
					++count;
					msgid=peer_id+"."+count;
				    String[] neighbours=prop.getProperty("peer"+peer_id+".next").split(","); 	//Creating a client thread for every neighbouring peer
				    TTL_Value=neighbours.length;
				    for(int i=0;i<neighbours.length;i++)
				    {
				    	int connectingport=Integer.parseInt(prop.getProperty("peer"+neighbours[i]+".port"));
				    	int neighbouringpeer=Integer.parseInt(neighbours[i]);
				    	ClientThread cp=new ClientThread(connectingport,neighbouringpeer,f_name,msgid,peer_id,TTL_Value);
				    	Thread t=new Thread(cp);
				    	t.start();
				    	thread.add(t);
				    	peers.add(cp);
				    }
				    for(int i=0;i<thread.size();i++)
				    {
				    	try {
							((Thread) thread.get(i)).join();		
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				    }
				    int[] peerswithfiles;
				    if(ch==1)
				    {
				    	System.out.println("Peers containing the file are: ");
					    for(int i=0;i<peers.size();i++)
					    {

					    	peerswithfiles=((ClientThread)peers.get(i)).getarray();			
					    	for(int j=0;j<peerswithfiles.length;j++)
					    	{	if(peerswithfiles[j]==0)
					    		break;
					    		System.out.println(peerswithfiles[j]);
					    	}
					    }
					    System.out.println("Enter the peer from where to download the file: ");
					    int peerfromdownload=scan.nextInt();
					    int porttodownload=Integer.parseInt(prop.getProperty("peer"+peerfromdownload+".serverport"));
					    ClientasServer(peerfromdownload,porttodownload,f_name,sharedDir);
					    System.out.println("File: "+f_name+" downloaded from Peer "+peerfromdownload+" to Peer "+peer_id);
				    }

			
			}catch(IOException io)
				{
						io.printStackTrace();
				}
			}

	public static void ClientasServer(int cspeerid,int csportno,String fn,String sharedDir)
	{																															
		try{
			Socket clientasserversocket=new Socket("localhost",csportno);
			ObjectOutputStream ooos=new ObjectOutputStream(clientasserversocket.getOutputStream());
			ooos.flush();
			ObjectInputStream oois=new ObjectInputStream(clientasserversocket.getInputStream());
			ooos.writeObject(fn);
			int readbytes=(int)oois.readObject();
			System.out.println("bytes transferred: "+readbytes);
			byte[] b=new byte[readbytes];
			oois.readFully(b);
			OutputStream fileos=new FileOutputStream(sharedDir+"//"+fn);
			BufferedOutputStream bos=new BufferedOutputStream(fileos);
			bos.write(b, 0,(int) readbytes);
	        System.out.println(fn+" Successfully downloaded ! "+sharedDir);
	        bos.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void BroadcastInvaliedMsg(int cspeerid,int csportno,String fn)
	{
		try{
			Socket clientasserversocket=new Socket("localhost",csportno);
			ObjectOutputStream ooos=new ObjectOutputStream(clientasserversocket.getOutputStream());
			ooos.flush();
			ooos.writeObject("Invalied File "+fn);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
