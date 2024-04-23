package io.confluent.oauth.azure.managedidentity;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.schemaregistry.client.security.bearerauth.BearerAuthCredentialProvider;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.types.Password;
import org.apache.kafka.common.security.JaasContext;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.internals.secured.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static io.confluent.oauth.azure.managedidentity.OAuthBearerLoginCallbackHandler.createAccessTokenRetriever;

public class RegistryBearerAuthCredentialProvider implements BearerAuthCredentialProvider {

    private static final Logger log = LoggerFactory.getLogger(RegistryBearerAuthCredentialProvider.class);
    public static final String SASL_IDENTITY_POOL_CONFIG = "extension_identityPoolId";

    private String targetSchemaRegistry;
    private String targetIdentityPoolId;
    private Map<String, Object> moduleOptions;

    private AccessTokenRetriever accessTokenRetriever;
    private AccessTokenValidator accessTokenValidator;
    private boolean isInitialized;


    @Override
    public void configure(Map<String, ?> configs) {
        // from SaslOauthCredentialProvider
        Map<String, Object> updatedConfigs = getConfigsForJaasUtil(configs);
        JaasContext jaasContext = JaasContext.loadClientContext(updatedConfigs);
        List<AppConfigurationEntry> appConfigurationEntries = jaasContext.configurationEntries();
        Map<String, ?> jaasconfig;
        if (Objects.requireNonNull(appConfigurationEntries).size() == 1
                && appConfigurationEntries.get(0) != null) {
            jaasconfig = Collections.unmodifiableMap(
                    ((AppConfigurationEntry) appConfigurationEntries.get(0)).getOptions());
        } else {
            throw new ConfigException(
                    String.format("Must supply exactly 1 non-null JAAS mechanism configuration (size was %d)",
                            appConfigurationEntries.size()));
        }

        // make sure we have scope and sub set
        Map<String, Object> myConfigs = new HashMap<>(configs);
        myConfigs.put(SaslConfigs.SASL_OAUTHBEARER_SCOPE_CLAIM_NAME, "scope");
        myConfigs.put(SaslConfigs.SASL_OAUTHBEARER_SUB_CLAIM_NAME, "sub");


        ConfigurationUtils cu = new ConfigurationUtils(myConfigs);
        JaasOptionsUtils jou = new JaasOptionsUtils((Map<String, Object>) jaasconfig);

        targetSchemaRegistry = cu.validateString(
                SchemaRegistryClientConfig.BEARER_AUTH_LOGICAL_CLUSTER, false);

        // if the schema registry oauth configs are set it is given higher preference
        targetIdentityPoolId = cu.get(SchemaRegistryClientConfig.BEARER_AUTH_IDENTITY_POOL_ID) != null
                ? cu.validateString(SchemaRegistryClientConfig.BEARER_AUTH_IDENTITY_POOL_ID)
                : jou.validateString(SASL_IDENTITY_POOL_CONFIG, false);

        String saslMechanism = cu.validateString(SaslConfigs.SASL_MECHANISM);
        moduleOptions = JaasOptionsUtils.getOptions(saslMechanism, appConfigurationEntries);
        AccessTokenRetriever accessTokenRetriever = createAccessTokenRetriever(myConfigs, saslMechanism, moduleOptions);

        AccessTokenValidator accessTokenValidator = AccessTokenValidatorFactory.create(myConfigs, saslMechanism);
        init(accessTokenRetriever, accessTokenValidator);
    }

    /*
     * Package-visible for testing.
     */

    void init(AccessTokenRetriever accessTokenRetriever, AccessTokenValidator accessTokenValidator) {
        this.accessTokenRetriever = accessTokenRetriever;
        this.accessTokenValidator = accessTokenValidator;

        try {
            this.accessTokenRetriever.init();
        } catch (IOException e) {
            throw new KafkaException("The OAuth login configuration encountered an error when initializing the AccessTokenRetriever", e);
        }

        isInitialized = true;
    }


    @Override
    public String getBearerToken(URL url) {
        try {
            String accessToken = accessTokenRetriever.retrieve();
            OAuthBearerToken token = accessTokenValidator.validate(accessToken);
            return accessToken;
        } catch (ValidateException | IOException e) {
            log.warn(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public String getTargetIdentityPoolId() {
        return targetIdentityPoolId;
    }

    @Override
    public String getTargetSchemaRegistry() {
        return targetSchemaRegistry;
    }

    // from SaslOauthCredentialProvider
    Map<String, Object> getConfigsForJaasUtil(Map<String, ?> configs) {
        Map<String, Object> updatedConfigs = new HashMap<>(configs);
        if (updatedConfigs.containsKey(SaslConfigs.SASL_JAAS_CONFIG)) {
            Object saslJaasConfig = updatedConfigs.get(SaslConfigs.SASL_JAAS_CONFIG);
            if (saslJaasConfig instanceof String) {
                updatedConfigs.put(SaslConfigs.SASL_JAAS_CONFIG, new Password((String) saslJaasConfig));
            }
        }
        return updatedConfigs;
    }

}
