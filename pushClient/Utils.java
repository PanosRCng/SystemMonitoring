import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.PublicKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utils
{

	/*
	 * constructor
	 */
	public Utils()
	{
		//
	}


	/*
	 * returns a header for the Push object
	 * the sha256 hash of the client's public key bytes, as hexadecimal in string representation
	 */
	public String getHeader(KeyStore keystore, String alias)
	{
		// get the client public key as byte array
		byte[] publicKey = getPublicKey(keystore, alias);

		if(publicKey != null)
		{
			// get the sha256 hash of public key's bytes
			byte[] sha256 = sha256Hash(publicKey);

			// get the hexadecimal in string representation of the sha256 hash bytes
			String publicKey_str = getHexStr(sha256);

			return publicKey_str;
		}
		
		return null;
	}


	/*
	 * returns the public key of the alias from a key pair keystore 
	 * as a byte array
	 */
	public byte[] getPublicKey(KeyStore keystore, String alias)
	{
		try
		{
			// get certificate of public key
      			Certificate cert = keystore.getCertificate(alias);

			// get public key
			PublicKey publicKey = cert.getPublicKey();

			return publicKey.getEncoded();
		}
		catch(KeyStoreException ex)
		{
			ex.printStackTrace();
		}

		return null;
	}


	/*
	 * returns the sha256 hash of the input byte array
	 * as a byte array, (length 32 bytes -> 8x32=256 bits)
	 */
	public byte[] sha256Hash(byte[] input)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.reset();

			md.update(input);
			byte[] digest = md.digest();

			return digest;
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

		return null;
	}


	/*
         * returns the hexadecimal in string representation of the input byte array
	 * (length 64)
         */
	public String getHexStr(byte input[])
	{
		String hexStr = "";

		for (int i = 0; i < input.length; i++)
		{
			hexStr += Integer.toString( ( input[i] & 0xff ) + 0x100, 16).substring( 1 );
		}

		return hexStr;
	}
}
