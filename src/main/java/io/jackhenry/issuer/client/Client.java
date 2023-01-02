package io.jackhenry.issuer.client;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "clients")
public class Client {
  @Id
  private String id;
  private String secret;
  private boolean hasAdminRole;

  public Client() {
  }

  public Client(String id, String secret, boolean hasAdminRole) {
    this.id = id;
    this.secret = secret;
    this.hasAdminRole = hasAdminRole;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public boolean getHasAdminRole() {
    return hasAdminRole;
  }

  public void setHasAdminRole(boolean hasAdminRole) {
    this.hasAdminRole = hasAdminRole;
  }
}
