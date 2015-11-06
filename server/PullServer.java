import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import com.google.gson.Gson;

public class PullServer implements Runnable
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


	public void setPull(Pull pull)
	{
		this.pull = pull;
	}

}
