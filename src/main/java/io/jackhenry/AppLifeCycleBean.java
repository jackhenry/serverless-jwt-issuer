package io.jackhenry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.jackhenry.issuer.client.ClientService;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class AppLifeCycleBean {
  @Inject
  ClientService clientService;

  @ConfigProperty(name = "default.admin.clientid")
  String adminClientId;

  @ConfigProperty(name = "default.admin.secret")
  String adminSecret;

  void onStart(@Observes StartupEvent ev) {
    System.out.println("App startup...");
    this.clientService.createAdminClient(adminClientId, adminSecret).await().indefinitely();
    System.out.println("Finished startup");
  }
}
