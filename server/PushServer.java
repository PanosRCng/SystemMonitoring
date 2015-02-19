import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import com.google.gson.Gson;

public class PushServer implements Runnable
{
	//  the server's keystore file
	private final static String SERVER_KEYSTORE = "keys/server.private";
	
	// the keystore file tha holds the clients' public keys
	private final static String CLIENTS_KEYSTORE = "keys/clients.public"; 

	// passphrase for accessing the server's keystore
  	private final static String SERVER_KEYSTORE_PASS = "serverPass";

	// passphrase for accessing the clients' keystore
	private final static String CLIENTS_KEYSTORE_PASS = "public";

        // the enabled protocols
        private final static String[] protocols = {
                                                   "TLSv1.2"
                                                  };

        // the enabled cipher suites
        private final static String[] suites = {
                                                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256"
                                               };

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

	// the object that implements the PushServerListener interface
	private static PushServerListener pushServerListener;


	/*
	 * constructor 
	 * gets a reference to the object that implements the PushServerListener interface
	 * gets the port where the server listens
	 * initializes the secure random numbers
	 */
	public PushServer(PushServerListener pushServerListener, int server_port)
	{
		this.pushServerListener = pushServerListener;
		this.server_port = server_port;

		initSecureRandom();
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

                                // set the enables protocols
                                server.setEnabledProtocols(protocols);

                                // set the enabled cipher suites
                                server.setEnabledCipherSuites(suites);

                                // require client authentication
                                server.setNeedClientAuth( true );

				pushServerListener.onPushServerStatusChanged("Listening on port "+ server_port);

				while(true)
				{
					// the Socket object for the client connection
					Socket connection = null;

					try
					{
						// block and wait for a client to connect and get a Socket
						connection = server.accept();

						pushServerListener.onPushServerStatusChanged("Got connection from: " +
										connection.getInetAddress().getHostAddress());

						// get an input stream for reading bytes from socket (byte stream)
						InputStream in = connection.getInputStream();

						StringBuilder jsonPush = new StringBuilder();

						// get an input stream reader with utf-8 charset
						// reads bytes and decodes them into characters using a utf-8 charset
						InputStreamReader reader = new InputStreamReader(in, "UTF-8");

						// read from socket and build the string
						for(int c=reader.read(); c!=-1; c=reader.read())
						{
							jsonPush.append((char) c);
						}

						Gson gson = new Gson();

						// construct the report Push from the json string
						Push push = gson.fromJson(jsonPush.toString(), Push.class);   

						// inform PushServerListener about the Push
						pushServerListener.onPushReceived(push);
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

								pushServerListener.onPushServerStatusChanged("Close connection with: " 												+connection.getInetAddress().getHostAddress());
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

						pushServerListener.onPushServerStatusChanged("Server socket closed");
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
		pushServerListener.onPushServerStatusChanged("secure random numbers are initialized");

		// construct a secure random number generator, implementing the default random number algorithm
    		secureRandom = new SecureRandom();
    		secureRandom.nextInt();

		pushServerListener.onPushServerStatusChanged("secure random numbers initialized ok");
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

}
