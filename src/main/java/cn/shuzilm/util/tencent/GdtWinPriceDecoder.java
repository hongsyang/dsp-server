package cn.shuzilm.util.tencent;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class GdtWinPriceDecoder {

	private String AES128Decode(byte[] encoded_price, String token)
			throws UnsupportedEncodingException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		byte[] raw = token.getBytes("utf-8");
		SecretKeySpec key_spec = new SecretKeySpec(raw, "AES");

		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, key_spec);
		byte[] decoded_price = cipher.doFinal(encoded_price);
		return new String(decoded_price, "utf-8");
	}

	private byte[] Base64Decode(String encoded_price) {
		return Base64.getUrlDecoder().decode(encoded_price);
	}

	public String DecodePrice(String encoded_price, String token)
			throws InvalidKeyException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		byte[] after_base64_decode = Base64Decode(encoded_price);
		return AES128Decode(after_base64_decode, token);
	}

	public static void main(String[] args) {
		GdtWinPriceDecoder decoder = new GdtWinPriceDecoder();
		try {
			String decoded_str = decoder.DecodePrice(
					"O-0mVdLfTGAnt3TClMitSg==", "NDAzMzY3LDE0MjI4");
			String price_str = decoded_str.trim();
			System.out.println("price_str:"+price_str);
			int price = Integer.parseInt(price_str);
			assert price == 2301;

			decoded_str = decoder.DecodePrice("VwSSFElLxs3wWy0LMsTy5Q==",
					"NDAzMzY3LDE0MjI4");
			System.out.println("decoded_str:"+decoded_str);
			price_str = decoded_str.trim();
			price = Integer.parseInt(price_str);
			System.out.println(price);
			assert price == 2201;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
	}
}
