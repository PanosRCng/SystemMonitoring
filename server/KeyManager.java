import java.io.File;
import java.io.FileOutputStream;
import java.security.*;
import java.security.cert.Certificate;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.asn1.x509.*;
import java.util.Date;
import java.math.BigInteger;


public class KeyManager
{
	public final static String TAG = "KEY_MANAGER";

	// the directory that keeps the keystores
	private final static String KEYS_DIRECTORY = "keys";

	//  the server's keystore file
	private final static String SERVER_KEYSTORE = "server.private";

	// the public key encryption algorithm
	private final static String PUBLIC_KEY_ALGORITHM = "RSA";

	// the size of the key in bits
	private final static int KEY_SIZE = 2048;

	// passphrase for accessing the server's keystore
  	private final static String SERVER_KEYSTORE_PASS = "serverPass";


	// a source of secure random numbers
	private static SecureRandom secureRandom;


	// constructor
	public KeyManager()
	{
		if( checkKeysDir() )
		{
			System.out.println("keys folder is ready");
		}
	}

	
	/*
	 * generate a private/public key pair and stores it to keystore
	 */
	public boolean getKeyPair()
	{
		initSecureRandom();

		try
		{
			// get a KeyPairGenerator object that generates public/private key pairs for the specified algorithm
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(PUBLIC_KEY_ALGORITHM);

			// initialize the key pair generator with a keysize and a secure source of randomness
			keyGen.initialize(KEY_SIZE, secureRandom);

			// geenerate a pair of private/public keys
			KeyPair keyPair = keyGen.generateKeyPair();

			// generate a certificate and put it to a certificate chain
			X509Certificate certificate = generateCertificate(keyPair);
			Certificate[] certChain = new Certificate[1];  
			certChain[0] = certificate; 

			KeyStore keyStore = KeyStore.getInstance("JKS");  
			
			keyStore.load(null, null);  
 
			keyStore.setKeyEntry("serverPrivateKey", (Key)keyPair.getPrivate(), SERVER_KEYSTORE_PASS.toCharArray(), certChain); 

			// (!) do a path combiner here
			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + SERVER_KEYSTORE);

			keyStore.store(writeStream, SERVER_KEYSTORE_PASS.toCharArray());

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
			X509V3CertificateGenerator cert = new X509V3CertificateGenerator();   
			cert.setSerialNumber(BigInteger.valueOf(1));
			cert.setSubjectDN(new X500Principal("CN=server")); 
			cert.setIssuerDN(new X500Principal("CN=server"));
			cert.setPublicKey(keyPair.getPublic());  
			cert.setNotBefore(new Date(System.currentTimeMillis() - 10000));  
			cert.setNotAfter(new Date(System.currentTimeMillis() + 10000));  
			cert.setSignatureAlgorithm("SHA1WithRSAEncryption");   
			PrivateKey signingKey = keyPair.getPrivate();    
		
			return cert.generate(signingKey, "BC");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}


}
