package security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Order for encrypt: <i>getBytes, encrypt, encode, toString</i>
 * <p>
 * Order for decrypt: <i>getBytes, decode, decrypt, toString</i>
 * <p>
 * Use to encrypt and decrypt for high scores file
 * AES Algorithm (Advanced Encryption Standard)
 */
public class EncrypterDecrypter {
	Cipher eCipher, dCipher;
	public static final String keyValue = "I'm awe-handsome"; // must be 16 bytes
	public static SecretKeySpec keySpec = new SecretKeySpec(keyValue.getBytes(), "AES");

	public EncrypterDecrypter() {
		try {
			// Can use AlgorithmParameterSpec instead of IvParameterSpec
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(keyValue.getBytes());
			eCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			dCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			eCipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec);
			dCipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
		} catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException
				| NoSuchAlgorithmException e) {
			System.out.println(e.toString() + ": 1");
		}
	}

	/**
	 * encrypt() inputs a string and returns an encrypted version of that String
	 */
	public String encrypt(String valueToEnc) {
		String encryptedValue = null;
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = valueToEnc.getBytes("UTF8");
			// Encrypt
			byte[] encValue = eCipher.doFinal(utf8);
			// Encode bytes to base64 to get a string
			encryptedValue = Base64.encode(encValue);
		} catch (BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException e) {
			System.out.println(e.toString() + ": 2");
		}
		return encryptedValue;
	}

	/**
	 * decrypt() inputs a string and returns an encrypted version of that String
	 */
	public String decrypt(String valueToDec) {
		String decryptedValue = null;
		try {
			// Decode base64 to get bytes
			byte[] decValue = Base64.decode(valueToDec);
			// Decrypt
			byte[] utf8 = dCipher.doFinal(decValue);
			// Decode using utf-8
			decryptedValue = new String(utf8, "UTF8");
		} catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println(e.toString() + ": 3");
		}
		return decryptedValue;
	}
}