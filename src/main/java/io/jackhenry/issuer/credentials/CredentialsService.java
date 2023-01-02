package io.jackhenry.issuer.credentials;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;

import io.jackhenry.crypto.Crypto;
import io.jackhenry.issuer.client.Client;
import io.jackhenry.issuer.client.dto.ClientVerificationResult;
import io.jackhenry.issuer.credentials.dto.JwtTokenDto;
import io.jackhenry.issuer.jwt.JwtService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

@ApplicationScoped
public class CredentialsService {

  @Inject
  SessionFactory sf;

  @Inject
  JwtService jwt;

  public Uni<ClientVerificationResult> verifyCredentials(String clientid, String secret) {
    return this.sf.withTransaction(session -> session.find(Client.class, clientid))
        .onItem()
        .ifNull()
        .failWith(() -> new Exception("Client not found."))
        .onItem()
        .transform(credential -> Crypto.check(secret, credential.getSecret()))
        .onFailure()
        .recoverWithItem(failure -> ClientVerificationResult.failure(failure.getMessage()));
  }

  public Uni<JwtTokenDto> issue(String clientid, String secret) {
    return this.verifyCredentials(clientid, secret)
        .onItem()
        .transform(result -> {
          if (!result.getAuthenticated()) {
            throw new Error("Invalid credentials");
          } else {
            return jwt.generate(clientid);
          }
        });
  }

  public Uni<JwtTokenDto> renew(String token) {
    return Uni
        .createFrom()
        .voidItem()
        .onItem()
        .transform(Unchecked.function(i -> {
          try {
            return jwt.renew(token);
          } catch (Exception e) {
            throw new Exception("Invalid token");
          }
        }));
  }
}
