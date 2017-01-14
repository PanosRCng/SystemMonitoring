import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;


public abstract class Server implements Runnable
{
        // the enabled protocols
        private final static String[] protocols = {
                                                   "TLSv1.2"
                                                  };

        // the enabled cipher suites
        private final static String[] suites = {
                                                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256"
                                               };

	// holds the Keymanager object
	private ServerKeyManager serverKeyManager;

	// the port where the server listens
	private int server_port;


	/*
	 * constructor 
	 * gets the port where the server listens
	 * initializes the secure random numbers
	 */
	public Server(int server_port)
	{
		this.server_port = server_port;

		this.serverKeyManager = new ServerKeyManager();
	}


	public void run()
	{
		try
		{
			SSLContext sslContext = serverKeyManager.getSSLContext();   			

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

				statusChanged("Listening on port "+ server_port);

				while(true)
				{
					// the Socket object for the client connection
					Socket connection = null;

					try
					{
						// block and wait for a client to connect and get a Socket
						connection = server.accept();

						statusChanged("Got connection from: " +
										connection.getInetAddress().getHostAddress());
						
						serverLogic(connection);
		
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

								statusChanged("Close connection with: " 												+connection.getInetAddress().getHostAddress());
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

						statusChanged("Server socket closed");
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


	// the server's logic - to implement in a subclass
	protected abstract void serverLogic(Socket connection);

	// to implement in a subclass
	protected abstract void statusChanged(String msg);

}
