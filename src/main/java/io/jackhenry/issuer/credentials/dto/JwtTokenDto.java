package io.jackhenry.issuer.credentials.dto;

public class JwtTokenDto {
  private int expiresIn;
  private String token;
  private String refresh;

  public JwtTokenDto(int expiresIn, String token, String refresh) {
    this.expiresIn = expiresIn;
    this.token = token;
    this.refresh = refresh;
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getRefresh() {
    return refresh;
  }

  public void setRefresh(String refresh) {
    this.refresh = refresh;
  }

}
