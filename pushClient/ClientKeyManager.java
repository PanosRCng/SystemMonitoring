import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.asn1.x509.*;
import java.util.Date;
import java.math.BigInteger;

import java.io.Writer;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class ClientKeyManager
{
	public final static String TAG = "CLIENT_KEY_MANAGER";

	// the directory that keeps the keystores
	private final static String KEYS_DIRECTORY = "keys";

	// the pushClient's private keystore file
	// holds the privateKey accompanied by a signed certificate for the public key
	private final static String CLIENT_KEYSTORE = "testClient.private";

	// passphrase for accessing the server's keystore
  	private final static String CLIENT_KEYSTORE_PASS = "testClientPass";

	private final static String CLIENT_CERTIFICATE_FILE = "testClientPublicKey.cert";

	// the public-key cryptosystem algorithm
	private final static String CRYPTOSYSTEM_ALGORITHM = "RSA";

	// the size of the key in bits
	private final static int KEY_SIZE = 2048;

	// the signing algorithm for the "trusted certificate"
	private final static String CERTIFICATE_SIGNING_ALGORITHM = "SHA1WithRSAEncryption";

	// a source of secure random numbers
	private static SecureRandom secureRandom;


	// constructor
	public ClientKeyManager()
	{
		if( checkKeysDir() )
		{
			System.out.println("keys folder is ready");
		}
	}


	/*
	 * exports to file the certificate, that is stored to the private keystore
	 */
	public boolean exportCertificate()
	{
		try
		{
			// get a KeyStore object of the "JKS" keystore type, and load the private KeyStore
			KeyStore private_keyStore = KeyStore.getInstance("JKS"); 
			FileInputStream readStream = new FileInputStream(KEYS_DIRECTORY + "/" + CLIENT_KEYSTORE);
			private_keyStore.load(readStream, CLIENT_KEYSTORE_PASS.toCharArray());

      			// get the certificate associated with the given alias
      			Certificate cert = private_keyStore.getCertificate("testClientPrivateKey");

			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + CLIENT_CERTIFICATE_FILE);

			// write the certificate's bytes to file		
			writeStream.write(cert.getEncoded());

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
			keyStore.setKeyEntry("testClientPrivateKey", (Key)keyPair.getPrivate(), CLIENT_KEYSTORE_PASS.toCharArray(), certChain); 

			// (!) do a path combiner here
			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + CLIENT_KEYSTORE);

			// store the keystore to the the output stream, protected with a password
			keyStore.store(writeStream, CLIENT_KEYSTORE_PASS.toCharArray());

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
