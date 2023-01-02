package io.jackhenry.issuer.client.dto;

public class ClientVerificationResult {
  protected boolean authenticated;
  protected String message;

  public boolean getAuthenticated() {
    return this.authenticated;
  }

  public String getMessage() {
    return this.message;
  }

  public static class VerificationResultSuccess extends ClientVerificationResult {
    public VerificationResultSuccess(boolean authenticated) {
      super();
      this.authenticated = authenticated;
    }
  }

  public static class VerificationResultFailure extends ClientVerificationResult {
    public VerificationResultFailure(boolean authenticated, String message) {
      super();
      this.authenticated = authenticated;
      this.message = message;
    }
  }

  public static VerificationResultSuccess success() {
    return new VerificationResultSuccess(true);
  }

  public static VerificationResultFailure failure(String message) {
    return new VerificationResultFailure(false, message);
  }
}
