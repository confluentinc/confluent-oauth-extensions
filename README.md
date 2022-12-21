# confluent-oauth-extensions

Apache Kafka client library providing additional integrations relating to OAuth/OIDC integrations with Confluent Cloud and Apache Kafka.

## Authenticating to Confluent Cloud via OAuth, using Azure Managed Identites

Example Kafka client config and JAAS config:

```
bootstrap.servers=pkc-xxxxx.ap-southeast-2.aws.confluent.cloud:9092
security.protocol=SASL_SSL
sasl.oauthbearer.token.endpoint.url=http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01&resource=https%3A%2F%2Fxxxxxxxx.onmicrosoft.com%2Fxxxxxxxxxx%2Fxxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx&client_id=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxx
sasl.login.callback.handler.class=io.confluent.oauth.azure.managedidentity.OAuthBearerLoginCallbackHandler
sasl.mechanism=OAUTHBEARER
sasl.jaas.config= \
	org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required \
		clientId='ignored' \
		clientSecret='ignored' \
		extension_logicalCluster='lkc-xxxxxx' \
		extension_identityPoolId='pool-xxxx';
```
