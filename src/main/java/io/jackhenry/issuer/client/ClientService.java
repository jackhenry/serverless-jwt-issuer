package io.jackhenry.issuer.client;

import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import com.password4j.Password;
import io.jackhenry.crypto.Crypto;
import io.jackhenry.issuer.client.dto.ClientDto;
import io.jackhenry.issuer.client.dto.ClientVerificationResult;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ClientService {

  @Inject
  SessionFactory sf;

  public Uni<Client> createNonAdminClient() {
    var uuid = UUID.randomUUID().toString();
    String secret = Crypto.createSecret();
    var hash = Password.hash(secret).withBcrypt();
    Client credential = new Client(uuid, hash.getResult(), false);

    return this.sf.withTransaction(session -> session.persist(credential))
        .onItem()
        .transform(item -> new Client(uuid, secret, false));
  }

  public Uni<Client> createAdminClient(String clientid, String secret) {
    var hash = Password.hash(secret).withBcrypt();
    Client credential = new Client(clientid, hash.getResult(), true);
    return this.sf.withTransaction(session -> session.persist(credential))
        .onItem()
        .transform(item -> credential);
  }

  public Uni<Boolean> verifyAdminCredential(String clientid, String secret) {
    return this.sf.withTransaction(session -> session.find(Client.class, clientid))
        .onItem()
        .ifNull()
        .fail()
        .onItem()
        .transform(credential -> {
          var verification = Crypto.check(secret, credential.getSecret());
          if (verification.getAuthenticated() && credential.getHasAdminRole())
            return true;
          return false;
        })
        .onFailure()
        .recoverWithItem(false);
  }

  public Uni<ClientVerificationResult> verifyCredential(ClientDto dto) {
    return this.sf.withTransaction(session -> session.find(Client.class, dto.getId()))
        .onItem()
        .ifNull()
        .failWith(() -> new Exception("Client not found."))
        .onItem()
        .transform(credential -> Crypto.check(dto.getSecret(), credential.getSecret()))
        .onFailure()
        .recoverWithItem(failure -> ClientVerificationResult.failure(failure.getMessage()));
  }
}
