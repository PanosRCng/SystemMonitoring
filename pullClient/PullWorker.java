import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.TimerTask;

import com.google.gson.Gson;


public class PullWorker extends TimerTask
{
	// the client's key pair alias
	private final static String CLIENT_KEYPAIR_ALIAS = "testClientPrivateKey"; 

	//  the client's keystore file
	private final static String CLIENT_KEYSTORE = "keys/testClient.private"; 

	// the keystore file tha holds the server's public keys
	private final static String SERVER_KEYSTORE = "keys/server.public";

	// passphrase for accessing the client's keystore
	private final static String CLIENT_KEYSTORE_PASS = "testClientPass";
	
	// passphrase for accessing the server's keystore
  	private final static String SERVER_KEYSTORE_PASS = "public";

	// keyStore for storing the client's key pair
	private KeyStore clientKeyStore;
  
	// keyStore for storing the server's public key
	private KeyStore serverKeyStore;

	// used to generate a SocketFactory
	private SSLContext sslContext;
  
	// a source of secure random numbers
	static private SecureRandom secureRandom;

	// holds the remote server ip
	private String remote_server;

	// holds the remote server port
	private int remote_port;

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

		pullWorkerListener.onPullWorkerStatusChanged("secure random numbers are initialized");
		secureRandom = new SecureRandom();
		secureRandom.nextInt();
		pullWorkerListener.onPullWorkerStatusChanged("secure random number initialized ok");
	}


	@Override
	public void run()
	{
		try
		{
      			setupServerKeystore();
      			setupClientKeyStore();
      			setupSSLContext();

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


	private void setupClientKeyStore() throws GeneralSecurityException, IOException
	{
		// get a Keystore object for the JKS keystore type
		clientKeyStore = KeyStore.getInstance( "JKS" );
    
		// load the client's keystore file
		clientKeyStore.load( new FileInputStream( CLIENT_KEYSTORE ), CLIENT_KEYSTORE_PASS.toCharArray() );
	}


	private void setupServerKeystore() throws GeneralSecurityException, IOException
	{
		// get a Keystore object for the JKS keystore type
		serverKeyStore = KeyStore.getInstance( "JKS" );
    
		// load the server's keystore file
		serverKeyStore.load( new FileInputStream( SERVER_KEYSTORE ), SERVER_KEYSTORE_PASS.toCharArray() );
	}


	private void setupSSLContext() throws GeneralSecurityException, IOException
	{
		// get a TrustManagerFactory for that key manager algorithm
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		
		// initialize this factory with the server's public key keystore as the trust material for the secure sockets
		tmf.init( serverKeyStore );

		// get a KeyManagerFactory for that key manager algorithm
		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );

		// initialize this factory with the client key pair keystore as the key material for the secure sockets
	   	kmf.init( clientKeyStore, CLIENT_KEYSTORE_PASS.toCharArray() );

		// get a SSLContext object that implements the TLS secure socket protocol
		sslContext = SSLContext.getInstance( "TLS" );
    
		// initialize this SSLContext
		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom );
	}

}
