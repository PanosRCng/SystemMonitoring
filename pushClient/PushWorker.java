import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.ArrayList;
import com.google.gson.Gson;

public class PushWorker implements Runnable
{
	// holds the remote server ip
	private String remote_server;

	// holds the remote server port
	private int remote_port;

	// holds the Keymanager object
	private ClientKeyManager clientKeyManager;

	// the object that implements the PushWorkerListener interface
	private static PushWorkerListener pushWorkerListener;

	private final static int CONN_TIMEOUT = 15000;

	// the ArrayList that holds the reports data to send
	private ArrayList<Report> reports;	


	/*
	 * constructor 
	 * gets a reference to the object that implements the PushWorkerListener interface
	 * gets the remote server ip and port where the PushServer listens
	 * gets a header and the Push report data, constructs the Push object and converts it to json string
	 * initializes the secure random numbers
	 */
	public PushWorker(PushWorkerListener pushWorkerListener, String remote_server, int remote_port, ArrayList<Report> reports)
	{
		this.pushWorkerListener = pushWorkerListener;
		this.remote_server = remote_server;
		this.remote_port = remote_port;
		this.reports = reports;

		this.clientKeyManager = new ClientKeyManager();	
	}

	public void run()
	{
		try
		{
			SSLContext sslContext = clientKeyManager.getSSLContext(); 

      			SSLSocketFactory sf = sslContext.getSocketFactory();

			Utils utils = new Utils(); 
			//String header = utils.getHeader(clientKeyStore, CLIENT_KEYPAIR_ALIAS);
			String header = "test";

			// create the report Push, and convert it to json string
			Push push = new Push(header, reports);
			Gson gson = new Gson();
			String jsonPush = gson.toJson(push); 

			// the client Socket
			SSLSocket socket = null;

			try
			{
				// create socket and connect to remote server ip and port 
				socket = (SSLSocket)sf.createSocket( remote_server, remote_port );

				pushWorkerListener.onPushWorkerStatusChanged("Connecting to "+ remote_server + ":" + remote_port );

				// get an output stream for writing to socket (byte stream)
				OutputStream out = socket.getOutputStream();

				// get an output stream writer with utf-8 charset (characters written to it are encoded into bytes)
				Writer writer = new OutputStreamWriter(out, "UTF-8");

				// writes text to a character-output stream
				writer = new BufferedWriter(writer);

				pushWorkerListener.onPushWorkerStatusChanged("Sending data to server");

				// write to socket
				writer.write(jsonPush);

				// make sure to send over the network
				writer.flush();

				// disconnect from server 
				socket.close();

				pushWorkerListener.onPushWorkerStatusChanged("Connection closed");

			}
			catch(IOException ex)
			{
				pushWorkerListener.onPushWorkerStatusChanged("Could not connect to remote server");
			}
			finally
			{
				// make sure that you close the Socket
				try
				{
					if(socket!=null)
					{
						socket.close();
					}
				}
				catch(IOException ex)
				{
					ex.printStackTrace();
				}
			}

		}
		catch( GeneralSecurityException gse )
		{
	        	gse.printStackTrace();
    		}
		catch( IOException ie )
		{
	        	ie.printStackTrace();
    		}

	}

}
