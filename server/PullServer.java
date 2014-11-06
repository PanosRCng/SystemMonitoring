import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import com.google.gson.Gson;

public class PullServer implements Runnable
{
	//  the server's keystore file
	private final static String SERVER_KEYSTORE = "keys/server.private";
	
	// the keystore file tha holds the clients' public keys
	private final static String CLIENTS_KEYSTORE = "keys/clients.public"; 

	// passphrase for accessing the server's keystore
  	private final static String SERVER_KEYSTORE_PASS = "serverPass";

	// passphrase for accessing the clients' keystore
	private final static String CLIENTS_KEYSTORE_PASS = "public";

	// keyStore for storing the server's key pair
	private KeyStore serverKeyStore;

	// keyStore for storing the clients' public key
  	private KeyStore clientKeyStore;

	// used to generate a SocketFactory
  	private SSLContext sslContext;

	// a source of secure random numbers
	static private SecureRandom secureRandom;


	// the port where the server listens
	private int server_port;

	// the object that implements the PullServerListener interface
	private static PullServerListener pullServerListener;

	// the Pull
	private Pull pull;


	/*
	 * constructor 
	 * gets a reference to the object that implements the PullServerListener interface
	 * gets the port where the server listens
	 * initializes the secure random numbers
	 */
	public PullServer(PullServerListener pullServerListener, int server_port)
	{
		this.pullServerListener = pullServerListener;
		this.server_port = server_port;

		pullServerListener.onPullServerStatusChanged("secure random numbers are initialized");
		secureRandom = new SecureRandom();
		secureRandom.nextInt();
		pullServerListener.onPullServerStatusChanged("secure random number initialized ok");
	}


	public void run()
	{
		try
		{
      			setupClientsKeyStore();
      			setupServerKeystore();
      			setupSSLContext();

      			SSLServerSocketFactory sf = sslContext.getServerSocketFactory();

			// the ServerSocket
			SSLServerSocket server = null;

			try
			{
				// create a ServerSocket object and start listening for clients
      				server = (SSLServerSocket)sf.createServerSocket( server_port );

      				// require client authorization
      				server.setNeedClientAuth( true );

				pullServerListener.onPullServerStatusChanged("Listening on port "+ server_port);

				while(true)
				{
					// the Socket object for the client connection
					Socket connection = null;

					try
					{
						// block and wait for a client to connect and get a Socket
						connection = server.accept();

						pullServerListener.onPullServerStatusChanged("Got connection from: " +
										connection.getInetAddress().getHostAddress());

						// get an output stream for writing to socket (byte stream)
						OutputStream out = connection.getOutputStream();

						// get an output stream writer with utf-8 charset 
						//(characters written to it are encoded into bytes)
						Writer writer = new OutputStreamWriter(out, "UTF-8");

						// writes text to a character-output stream
						writer = new BufferedWriter(writer);

						pullServerListener.onPullServerStatusChanged("Sending data to client");

						Gson gson = new Gson();

						// convert the Pull to the json string
						String jsonPull = gson.toJson(pull);  

						// write to socket
						writer.write(jsonPull);

						// make sure to send over the network
						writer.flush();

						// disconnect from server 
						connection.close();

						pullServerListener.onPullServerStatusChanged("Connection closed");

					}
					catch(IOException ex)
					{
						ex.printStackTrace();
					}
					finally
					{
						// make sure that you close the Socket for the client connection
						try
						{
							if( connection != null )
							{
								connection.close();

								pullServerListener.onPullServerStatusChanged("Close connection with: " 												+connection.getInetAddress().getHostAddress());
							}
						}
						catch(IOException ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				// make sure that you close the ServerSocket
				try
				{
					if(server!=null)
					{
						server.close();

						pullServerListener.onPullServerStatusChanged("Server socket closed");
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


	private void initSecureRandom()
	{
		pullServerListener.onPullServerStatusChanged("secure random numbers are initialized");

		// construct a secure random number generator, implementing the default random number algorithm
    		secureRandom = new SecureRandom();
    		secureRandom.nextInt();

		pullServerListener.onPullServerStatusChanged("secure random numbers initialized ok");
	}


  	private void setupServerKeystore() throws GeneralSecurityException, IOException
	{
		// get a Keystore object for the JKS keystore type
    		serverKeyStore = KeyStore.getInstance( "JKS" );

		// load the server's keystore file
    		serverKeyStore.load( new FileInputStream( SERVER_KEYSTORE ), SERVER_KEYSTORE_PASS.toCharArray() );
  	}

	
	private void setupClientsKeyStore() throws GeneralSecurityException, IOException
	{
		// get a Keystore object for the JKS keystore type
		clientKeyStore = KeyStore.getInstance( "JKS" );

		// load the clients' keystore file
    		clientKeyStore.load( new FileInputStream( CLIENTS_KEYSTORE ), CLIENTS_KEYSTORE_PASS.toCharArray() );
  	}


	private void setupSSLContext() throws GeneralSecurityException, IOException
	{
		// get a TrustManagerFactory for that key manager algorithm
    		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );

		// initialize this factory with the clients' public keys keystore as the trust material for the secure sockets
    		tmf.init( clientKeyStore );

		// get a KeyManagerFactory for that key manager algorithm
    		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );

		// initialize this factory with the server key pair keystore as the key material for the secure sockets
    		kmf.init( serverKeyStore, SERVER_KEYSTORE_PASS.toCharArray() );

		// get a SSLContext object that implements the TLS secure socket protocol
    		sslContext = SSLContext.getInstance( "TLS" );

		// initialize this SSLContext
    		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom );
  	}


	public void setPull(Pull pull)
	{
		this.pull = pull;
	}

}
