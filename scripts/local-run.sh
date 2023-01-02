quarkus build
java -jar java-function-invoker-1.1.1.jar \
  --classpath target/deployment/oauth2-serverless-1.0.0-SNAPSHOT-runner.jar \
  --target io.quarkus.gcp.functions.http.QuarkusHttpFunction