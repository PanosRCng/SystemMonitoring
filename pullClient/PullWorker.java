import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.TimerTask;

import com.google.gson.Gson;


public class PullWorker extends TimerTask
{
	// holds the remote server ip
	private String remote_server;

	// holds the remote server port
	private int remote_port;

	// holds the Keymanager object
	private ClientKeyManager clientKeyManager;

	// the object that implements the PullWorkerListener interface
	private static PullWorkerListener pullWorkerListener;

	private final static int CONN_TIMEOUT = 15000;


	/*
	 * constructor 
	 * gets a reference to the object that implements the PullWorkerListener interface
	 * gets the remote server ip and port where the PullServer listens
	 * initializes the secure random numbers
	 */
	public PullWorker(PullWorkerListener pullWorkerListener, String remote_server, int remote_port)
	{
		this.pullWorkerListener = pullWorkerListener;
		this.remote_server = remote_server;
		this.remote_port = remote_port;

		this.clientKeyManager = new ClientKeyManager();	
	}


	@Override
	public void run()
	{
		try
		{
			SSLContext sslContext = clientKeyManager.getSSLContext();

      			SSLSocketFactory sf = sslContext.getSocketFactory();

			// the client Socket
			SSLSocket socket = null;

			try
			{
				// create socket and connect to remote server ip and port 
				socket = (SSLSocket)sf.createSocket( remote_server, remote_port );

				pullWorkerListener.onPullWorkerStatusChanged("Connecting to "+ remote_server + ":" + remote_port );

				// get an input stream for reading bytes from socket (byte stream)
				InputStream in = socket.getInputStream();

				StringBuilder jsonPull = new StringBuilder();

				// get an input stream reader with utf-8 charset
				// reads bytes and decodes them into characters using a utf-8 charset
				InputStreamReader reader = new InputStreamReader(in, "UTF-8");

				// read from socket and build the string
				for(int c=reader.read(); c!=-1; c=reader.read())
				{
					jsonPull.append((char) c);
				}

				Gson gson = new Gson();

				// construct the Pull from the json string
				Pull pull = gson.fromJson(jsonPull.toString(), Pull.class);   

				// inform the PullWorkerListener about the Pull
				pullWorkerListener.onPullReceived(pull);

				// disconnect from server 
				socket.close();

				pullWorkerListener.onPullWorkerStatusChanged("Connection closed");

			}
			catch(IOException ex)
			{
				pullWorkerListener.onPullWorkerStatusChanged("Could not connect to remote server");
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
