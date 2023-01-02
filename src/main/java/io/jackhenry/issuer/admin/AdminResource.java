package io.jackhenry.issuer.admin;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.jackhenry.issuer.client.ClientService;
import io.jackhenry.issuer.client.dto.ClientDto;
import io.jackhenry.json.JSendResponse;
import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
@RouteBase(path = "admin")
public class AdminResource {

  @Inject
  ClientService service;

  @Route(path = "/client/create", methods = Route.HttpMethod.POST)
  public Uni<JSendResponse> create(@Body ClientDto client) {
    return service.verifyAdminCredential(client.getId(), client.getSecret())
        .onItem()
        .transform(isVerified -> {
          if (!isVerified)
            throw new Error("Invalid credentials");
          return isVerified;
        })
        .chain(item -> service.createNonAdminClient())
        .onItem()
        .transform(item -> {
          return JSendResponse.success(item).build();
        })
        .onFailure()
        .recoverWithItem(failure -> {
          return JSendResponse.fail(failure.getMessage()).build();
        });
  }
}
