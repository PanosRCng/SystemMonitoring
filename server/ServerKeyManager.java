import java.io.File;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.security.cert.Certificate;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;


public class ServerKeyManager extends KeyManager
{
	public final static String TAG = "SERVER_KEY_MANAGER";

	// the clients keystore, holds the clients's trusted certificates
	protected final static String CLIENTS_KEYSTORE = "clients.trustore";

	// the public keystore file
	// holds the "trusted certificate", a signed certificate for the public key
	protected static String PUBLIC_KEYSTORE = "server.trustore";


	// constructor
	public ServerKeyManager()
	{
		super("server_private_pass");
	}



	/*
	 * creates the keystore that holds the clients's trusted certificates
	 */ 
	public boolean setClientsKeystore()
	{
		try
		{
			// get a KeyStore object of the "JKS" keystore type, to store the trusted certificate
			KeyStore clients_keyStore = KeyStore.getInstance("JKS");  
			clients_keyStore.load(null, null); 

			saveKeyStore(clients_keyStore, CLIENTS_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}


	public SSLContext getSSLContext() throws GeneralSecurityException, IOException
	{
		initSecureRandom();

		KeyStore clients_keyStore = loadKeyStore(CLIENTS_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());
		KeyStore private_keyStore = loadKeyStore(PRIVATE_KEYSTORE, private_keystore_pass.toCharArray());

		// get a TrustManagerFactory for that key manager algorithm
    		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );

		// initialize this factory with the clients' certificates keystore as the trust material for the secure sockets
    		tmf.init( clients_keyStore );

		// get a KeyManagerFactory for that key manager algorithm
    		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );

		// initialize this factory with the server's private keystore as the key material for the secure sockets
    		kmf.init( private_keyStore, private_keystore_pass.toCharArray() );

		// get a SSLContext object that implements the TLS secure socket protocol
    		SSLContext sslContext = SSLContext.getInstance( "TLS" );

		// initialize this SSLContext
    		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom );

		return sslContext;
  	}


	/*
	 * imports to the clients keystore a client's certificate
	 */
	public boolean addClientCertificate(String cert_filepath, String certificate_alias)
	{
		try
		{
			KeyStore clients_keyStore = loadKeyStore(CLIENTS_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());

			if( clients_keyStore.isCertificateEntry(certificate_alias) )
			{
				return false;
			}

			Certificate cert = loadCertificate(cert_filepath);
			
			clients_keyStore.setCertificateEntry(certificate_alias, cert);

			saveKeyStore(clients_keyStore, CLIENTS_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}


	/*
	 * deletes from the clients keystore the client certificate identified by the given alias
	 */
	public boolean removeClientCertificate(String certificate_alias)
	{
		try
		{
			KeyStore clients_keyStore = loadKeyStore(CLIENTS_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());
			
			if( !clients_keyStore.isCertificateEntry(certificate_alias) )
			{
				return false;
			}

			clients_keyStore.deleteEntry(certificate_alias);

			saveKeyStore(clients_keyStore, CLIENTS_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}


	/*
	 * returns the imported clients' aliases as a list of Strings
	 */
	public List<String> getClients()
	{
		List<String> clients = new ArrayList<String>();

		try
		{
			KeyStore clients_keyStore = loadKeyStore(CLIENTS_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());

			for(Enumeration<String> clients_aliases = clients_keyStore.aliases(); clients_aliases.hasMoreElements();)
       			{
				clients.add( clients_aliases.nextElement() );
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return clients;
	}


	/*
	 * exports the signed certificate from the private keystore, to the public keystore
	 */
	public boolean exportCertificate()
	{
		try
		{
			KeyStore private_keyStore = loadKeyStore(PRIVATE_KEYSTORE, private_keystore_pass.toCharArray());

			// get a KeyStore object of the "JKS" keystore type, to store the trusted certificate
			KeyStore public_keyStore = KeyStore.getInstance("JKS");  
			public_keyStore.load(null, null); 

      			// get the certificate associated with the given alias
      			Certificate cert = private_keyStore.getCertificate(PRIVATE_KEY_ALIAS);

			// assign the certificate to the given alias
			public_keyStore.setCertificateEntry(CERTIFICATE_ALIAS, cert);

			saveKeyStore(public_keyStore, PUBLIC_KEYSTORE, PUBLIC_KEYSTORE_PASS.toCharArray());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}

	
	/*
	 * checks if the necessary keystores exist
	 */
	public static boolean checkSetup()
	{		
		// (!) do a path combiner here
		File priv_ketstore = new File(KEYS_DIRECTORY + "/" + PRIVATE_KEYSTORE);
		if( !priv_ketstore.exists() )
		{
			return false;
		}

		// (!) do a path combiner here
		File pub_ketstore = new File(KEYS_DIRECTORY + "/" + PUBLIC_KEYSTORE);
		if( !pub_ketstore.exists() )
		{
			return false;
		}

		// (!) do a path combiner here
		File cl_ketstore = new File(KEYS_DIRECTORY + "/" + CLIENTS_KEYSTORE);
		if( !cl_ketstore.exists() )
		{
			return false;
		}

		return true;
	}

}
