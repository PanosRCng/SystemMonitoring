import java.security.cert.Certificate;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;


public class ClientKeyManager extends KeyManager
{
	public final static String TAG = "CLIENT_KEY_MANAGER";

	// the server's public keystore, holds the server's trusted certificate
	protected final static String SERVER_KEYSTORE = "server.trustore";


	// constructor
	public ClientKeyManager()
	{
		super("push_client_private_pass");
	}


	public SSLContext getSSLContext() throws GeneralSecurityException, IOException
	{
		initSecureRandom();

		KeyStore server_keyStore = loadKeyStore(SERVER_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());
		KeyStore private_keyStore = loadKeyStore(PRIVATE_KEYSTORE, private_keystore_pass.toCharArray());

		// get a TrustManagerFactory for that key manager algorithm
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		
		// initialize this factory with the server's certificate keystore as the trust material for the secure sockets
		tmf.init( server_keyStore );

		// get a KeyManagerFactory for that key manager algorithm
		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );

		// initialize this factory with the client key pair keystore as the key material for the secure sockets
	   	kmf.init( private_keyStore, private_keystore_pass.toCharArray() );

		// get a SSLContext object that implements the TLSv1.2 secure socket protocol
		SSLContext sslContext = SSLContext.getInstance( "TLSv1.2" );

		// initialize this SSLContext
		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom );

		return sslContext;
  	}


	/*
	 * exports to file the certificate, that is stored to the private keystore
	 */
	public boolean exportCertificate(String cert_filename)
	{
		try
		{
			KeyStore private_keyStore = loadKeyStore(PRIVATE_KEYSTORE, private_keystore_pass.toCharArray());

      			// get the certificate associated with the given alias
      			Certificate cert = private_keyStore.getCertificate(PRIVATE_KEY_ALIAS);

			saveCertificate(cert, cert_filename);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}

}
