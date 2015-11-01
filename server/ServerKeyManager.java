import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.asn1.x509.*;
import java.util.Date;
import java.math.BigInteger;

import java.io.Writer;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class ServerKeyManager
{
	public final static String TAG = "SERVER_KEY_MANAGER";

	// the directory that keeps the keystores
	private final static String KEYS_DIRECTORY = "keys";

	// the server's private keystore file
	// holds the privateKey accompanied by a signed certificate for the public key
	private final static String SERVER_KEYSTORE = "server.private";

	// the server's public keystore file
	// holds the "trusted certificate", a signed certificate for the public key
	private final static String SERVER_PUBLIC_KEYSTORE = "server.public";

	// the server's clients keystore, holds the clients' public keys
	private final static String SERVER_CLIENTS_KEYSTORE = "clients.public";

	private final static String CLIENT_CERTIFICATE_FILE = "testClientPublicKey.cert";

	// passphrase for accessing the server's keystore
  	private final static String SERVER_PRI_KEYSTORE_PASS = "serverPass";

	// passphrase for accessing the server's keystore
  	private final static String SERVER_PUB_KEYSTORE_PASS = "public";

	// the public-key cryptosystem algorithm
	private final static String CRYPTOSYSTEM_ALGORITHM = "RSA";

	// the size of the key in bits
	private final static int KEY_SIZE = 2048;

	// the signing algorithm for the "trusted certificate"
	private final static String CERTIFICATE_SIGNING_ALGORITHM = "SHA1WithRSAEncryption";

	// a source of secure random numbers
	private static SecureRandom secureRandom;


	// constructor
	public ServerKeyManager()
	{
		if( checkKeysDir() )
		{
			System.out.println("keys folder is ready");
		}
	}


	/*
	 * creates the keystore that holds the clients's trusted certificates
	 */ 
	public boolean setClientsKeystore()
	{
		try
		{
			// get a KeyStore object of the "JKS" keystore type, to store the trusted certificate
			KeyStore public_keyStore = KeyStore.getInstance("JKS");  
			public_keyStore.load(null, null); 

			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + SERVER_CLIENTS_KEYSTORE);

			// store the keystore to the the output stream, protected with a password
			public_keyStore.store(writeStream, SERVER_PUB_KEYSTORE_PASS.toCharArray());

			writeStream.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}


	/*
	 * imports to the clients keystore a client's certificate
	 */
	public boolean addClientCertificate()
	{
		try
		{
			// get a KeyStore object of the "JKS" keystore type, and load the server's clients KeyStore
			KeyStore clients_keyStore = KeyStore.getInstance("JKS"); 
			FileInputStream readStream = new FileInputStream(KEYS_DIRECTORY + "/" + SERVER_CLIENTS_KEYSTORE);
			clients_keyStore.load(readStream, SERVER_PUB_KEYSTORE_PASS.toCharArray());

			FileInputStream certReadStream = new FileInputStream(KEYS_DIRECTORY + "/" + CLIENT_CERTIFICATE_FILE);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			Certificate cert = cf.generateCertificate(certReadStream);
			clients_keyStore.setCertificateEntry("2", cert);

			readStream.close();

			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + SERVER_CLIENTS_KEYSTORE);

			// store the keystore to the the output stream, protected with a password
			clients_keyStore.store(writeStream, SERVER_PUB_KEYSTORE_PASS.toCharArray());

			certReadStream.close();
			writeStream.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}


	/*
	 * exports the signed certificate from the private keystore, to the public keystore
	 */
	public boolean exportCertificate()
	{
		try
		{
			// get a KeyStore object of the "JKS" keystore type, and load the private KeyStore
			KeyStore private_keyStore = KeyStore.getInstance("JKS"); 
			FileInputStream readStream = new FileInputStream(KEYS_DIRECTORY + "/" + SERVER_KEYSTORE);
			private_keyStore.load(readStream, SERVER_PRI_KEYSTORE_PASS.toCharArray());

			// get a KeyStore object of the "JKS" keystore type, to store the trusted certificate
			KeyStore public_keyStore = KeyStore.getInstance("JKS");  
			public_keyStore.load(null, null); 

      			// get the certificate associated with the given alias
      			Certificate cert = private_keyStore.getCertificate("serverPrivateKey");

			// assign the certificate to the given alias
			public_keyStore.setCertificateEntry("serverPublicKey", cert);

			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + SERVER_PUBLIC_KEYSTORE);

			// store the keystore to the the output stream, protected with a password
			public_keyStore.store(writeStream, SERVER_PUB_KEYSTORE_PASS.toCharArray());

			writeStream.close();
			readStream.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}

	
	/*
	 * generates a private/public key pair and a singed certificate for the public key
	 * stores the generated private key and the certificate to the private keystore
	 */
	public boolean getKeyPair()
	{
		initSecureRandom();

		try
		{
			// get a KeyPairGenerator object that generates public/private key pairs for the specified algorithm
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(CRYPTOSYSTEM_ALGORITHM);

			// initialize the key pair generator with a keysize and a secure source of randomness
			keyGen.initialize(KEY_SIZE, secureRandom);

			// generate a pair of private/public keys
			KeyPair keyPair = keyGen.generateKeyPair();

			// generate a signed certificate for the public key, and put it to a certificate chain
			X509Certificate certificate = generateCertificate(keyPair);
			Certificate[] certChain = new Certificate[1];  
			certChain[0] = certificate; 

			// get a KeyStore object of the "JKS" keystore type
			KeyStore keyStore = KeyStore.getInstance("JKS");  
			keyStore.load(null, null);  
 
			// assign the private key to the given alias, accompanied by a signed certificate for the public key
			keyStore.setKeyEntry("serverPrivateKey", (Key)keyPair.getPrivate(), SERVER_PRI_KEYSTORE_PASS.toCharArray(), certChain); 

			// (!) do a path combiner here
			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + SERVER_KEYSTORE);

			// store the keystore to the the output stream, protected with a password
			keyStore.store(writeStream, SERVER_PRI_KEYSTORE_PASS.toCharArray());

			writeStream.close();

		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
		}

		return true;
	}


	private void initSecureRandom()
	{
		System.out.println(TAG + " : " + "secure random numbers are initialized");

		// construct a secure random number generator, implementing the default random number algorithm
    		secureRandom = new SecureRandom();
    		secureRandom.nextInt();

		System.out.println(TAG + " : " + "secure random numbers initialized ok");
	}


	/*
	 * checks if the keys directory exists, if not it creates it
	 */
	private boolean checkKeysDir()
	{
		File keys_dir = new File(KEYS_DIRECTORY);

		if ( !keys_dir.isDirectory() )
		{
			try
			{
				keys_dir.mkdir();
			}
			catch(SecurityException ex)
			{
				ex.printStackTrace();

				return false;
			} 
		}

		return true;
	}


	/*
	 * generates a certificate for a KeyPair, using the BouncyCastle library
	 */
	public X509Certificate generateCertificate(KeyPair keyPair)
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		try
		{
			// instantiate an X.509 cert. generator
			X509V3CertificateGenerator cert = new X509V3CertificateGenerator(); 
  
			// set some infos to the generator about the certificate
			cert.setSerialNumber(BigInteger.valueOf(1));
			cert.setSubjectDN(new X500Principal("CN=server")); 
			cert.setIssuerDN(new X500Principal("CN=server")); 
			cert.setNotBefore(new Date(System.currentTimeMillis() - 10000));  
			cert.setNotAfter(new Date(System.currentTimeMillis() + 10000));  

			// set the public key of the key pair and the signing algorithm to the certificate generator
			cert.setPublicKey(keyPair.getPublic()); 
			cert.setSignatureAlgorithm(CERTIFICATE_SIGNING_ALGORITHM);   

			PrivateKey signingKey = keyPair.getPrivate();    
			
			// generate the certificate
			return cert.generate(signingKey, "BC");
			// X509Certificate PKCertificate = v3CertGen.generateX509Certificate(KPair.getPrivate());

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}


}
