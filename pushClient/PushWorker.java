import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.ArrayList;
import com.google.gson.Gson;

public class PushWorker implements Runnable
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

		pushWorkerListener.onPushWorkerStatusChanged("secure random numbers are initialized");
		secureRandom = new SecureRandom();
		secureRandom.nextInt();
		pushWorkerListener.onPushWorkerStatusChanged("secure random number initialized ok");
	}

	public void run()
	{
		try
		{
      			setupServerKeystore();
      			setupClientKeyStore();
      			setupSSLContext();

      			SSLSocketFactory sf = sslContext.getSocketFactory();

			Utils utils = new Utils(); 
			String header = utils.getHeader(clientKeyStore, CLIENT_KEYPAIR_ALIAS);

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
