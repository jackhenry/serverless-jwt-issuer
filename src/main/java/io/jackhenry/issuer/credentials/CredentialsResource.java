package io.jackhenry.issuer.credentials;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.jackhenry.issuer.credentials.dto.RefreshDto;
import io.jackhenry.issuer.credentials.dto.RequestIssueDto;
import io.jackhenry.json.JSendResponse;
import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
@RouteBase(path = "credentials")
public class CredentialsResource {

  @Inject
  CredentialsService service;

  @Route(path = "/issue", methods = Route.HttpMethod.POST)
  public Uni<JSendResponse> issue(@Body RequestIssueDto credentials) {
    return service.issue(credentials.getId(), credentials.getSecret())
        .onItem()
        .transform(token -> JSendResponse.success(token).build())
        .onFailure()
        .recoverWithItem(failure -> JSendResponse.fail(failure.getMessage()).build());
  }

  @Route(path = "/refresh", methods = Route.HttpMethod.POST)
  public Uni<JSendResponse> renew(@Body RefreshDto dto) {
    return service.renew(dto.getToken())
        .onItem()
        .transform(token -> JSendResponse.success(token).build())
        .onFailure()
        .recoverWithItem(failure -> JSendResponse.fail("Invalid token").build());
  }

}
