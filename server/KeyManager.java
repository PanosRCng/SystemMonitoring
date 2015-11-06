import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.math.BigInteger;
import java.security.Security;
import java.security.KeyStore;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;



public abstract class KeyManager
{
	public final static String TAG = "KEY_MANAGER";

	// the directory that keeps the keystores
	protected final static String KEYS_DIRECTORY = "keys";

	// the private keystore file
	// holds the privateKey accompanied by a signed certificate for the public key
	protected final static String PRIVATE_KEYSTORE = "private.keystore";

	// the private key alias for the private keystore
	protected final static String PRIVATE_KEY_ALIAS = "private_key";

	// the public keystore file
	// holds the "trusted certificate", a signed certificate for the public key
	protected final static String PUBLIC_KEYSTORE = "public.keystore";

	// the certificate alias for the public keystore
	protected final static String CERTIFICATE_ALIAS = "certificate_alias";

	// passphrase for accessing the public keystores
  	protected final static String PUBLIC_KEYSTORE_PASS = "public_pass";

	// the public-key cryptosystem algorithm
	protected final static String CRYPTOSYSTEM_ALGORITHM = "RSA";

	// the size of the key in bits
	protected final static int KEY_SIZE = 2048;

	// the signing algorithm for the "trusted certificate"
	protected final static String CERTIFICATE_SIGNING_ALGORITHM = "SHA1WithRSAEncryption";

	// a source of secure random numbers
	protected SecureRandom secureRandom;

	// passphrase for accessing the private keystore
  	protected String private_keystore_pass = "private_Pass";



	public KeyManager(String private_keystore_pass)
	{
		this.private_keystore_pass = private_keystore_pass;

		if( !checkKeysDir() )
		{
			System.out.println(TAG + " : " + "keys folder is not ready");
		}
	}



	/*
	 * loads a JKS keystore from a file using the given password
	 */
	protected KeyStore loadKeyStore(String filename, char[] pass) throws GeneralSecurityException, IOException
	{
		// get a KeyStore object of the "JKS" keystore type, and load the keystore from the file
		KeyStore keyStore = KeyStore.getInstance("JKS"); 

		keyStore.load( new FileInputStream(KEYS_DIRECTORY + "/" + filename), pass);

		return keyStore;
	}


	/*
	 * stores a JKS keystore to file, protected by the given password
	 */
	protected boolean saveKeyStore(KeyStore keyStore, String filename, char[] pass)
	{
		try
		{
			// (!) do a path combiner here
			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + filename);

			// store the keystore to the the output stream, protected with a password
			keyStore.store(writeStream, pass);

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
	 * loads a X.509 certificate from file
	 */
	protected Certificate loadCertificate(String filepath)
	{
		Certificate cert = null;

		try
		{
			FileInputStream readStream = new FileInputStream(filepath);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			cert = cf.generateCertificate(readStream);

			readStream.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return cert;
	}


	/*
	 * stores a certificate to file 
	 */
	protected boolean saveCertificate(Certificate cert, String filename)
	{
		try
		{
			FileOutputStream writeStream = new FileOutputStream(KEYS_DIRECTORY + "/" + filename);

			// write the certificate's bytes to file		
			writeStream.write(cert.getEncoded());

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
			keyStore.setKeyEntry(PRIVATE_KEY_ALIAS, (Key)keyPair.getPrivate(), private_keystore_pass.toCharArray(), certChain); 

			saveKeyStore(keyStore, PRIVATE_KEYSTORE, private_keystore_pass.toCharArray());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();

			return false;
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
			cert.setSubjectDN(new X500Principal("CN=my_auth")); 
			cert.setIssuerDN(new X500Principal("CN=my_auth")); 
			cert.setNotBefore(new Date(System.currentTimeMillis() - 10000));  
			cert.setNotAfter(new Date(System.currentTimeMillis() + 10000));  

			// set the public key of the key pair and the signing algorithm to the certificate generator
			cert.setPublicKey(keyPair.getPublic()); 
			cert.setSignatureAlgorithm(CERTIFICATE_SIGNING_ALGORITHM);   
  			
			// generate the certificate
			return cert.generate(keyPair.getPrivate(), "BC");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}


	protected void initSecureRandom()
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
	protected boolean checkKeysDir()
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

}
