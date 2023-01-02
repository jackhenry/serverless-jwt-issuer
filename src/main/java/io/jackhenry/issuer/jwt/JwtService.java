package io.jackhenry.issuer.jwt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.jackhenry.issuer.credentials.dto.JwtTokenDto;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.build.Jwt;

@ApplicationScoped
public class JwtService {
  private final int ACCESS_EXPIRATION = 3600;
  private final int RENEWAL_EXPIRATION = 18000;
  @Inject
  JWTParser parser;

  public JwtTokenDto generate(String clientid) {
    String accessToken = Jwt.upn(clientid)
        .claim("clientid", clientid)
        .claim("type", "access")
        .expiresIn(ACCESS_EXPIRATION)
        .sign();

    String renewalToken = Jwt.upn(clientid)
        .claim("clientid", clientid)
        .claim("type", "refresh")
        .expiresIn(RENEWAL_EXPIRATION)
        .sign();

    var token = new JwtTokenDto(ACCESS_EXPIRATION, accessToken, renewalToken);
    return token;
  }

  public JwtTokenDto renew(String token) throws Exception {
    JsonWebToken parsedToken = parser.parse(token);
    var type = parsedToken.claim("type");
    if (!type.isPresent())
      throw new Exception("Invalid token type");
    if (!type.get().equals("refresh"))
      throw new Exception("Invalid token type");
    return this.generate(parsedToken.getClaim("upn"));
  }
}
