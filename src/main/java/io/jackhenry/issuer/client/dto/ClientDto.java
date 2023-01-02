package io.jackhenry.issuer.client.dto;

public class ClientDto {

  private String id;
  private String secret;

  public ClientDto(String id, String secret) {
    this.id = id;
    this.secret = secret;
  }

  public String getId() {
    return id;
  }

  public String getSecret() {
    return secret;
  }

}
