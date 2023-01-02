package io.jackhenry.crypto;

import java.security.SecureRandom;
import com.password4j.Password;

import io.jackhenry.issuer.client.dto.ClientVerificationResult;

public class Crypto {
  private static final int SECRET_LENGTH = 64;
  private static SecureRandom random = new SecureRandom();

  public static ClientVerificationResult check(String password, String actualHash) {
    boolean verified = Password.check(password, actualHash).withBcrypt();
    if (!verified)
      return ClientVerificationResult.failure("Secret is not valid.");
    return ClientVerificationResult.success();
  }

  public static String createSecret() {
    StringBuffer buffer = new StringBuffer();
    while (buffer.length() < SECRET_LENGTH) {
      buffer.append(String.format("%08x", random.nextInt()));
    }
    return buffer.toString().substring(0, SECRET_LENGTH);
  }
}
