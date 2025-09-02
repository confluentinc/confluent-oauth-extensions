/*
MIT License

Copyright (c) 2023 Nikolai Seip

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

This file was copied from https://github.com/nniikkoollaaii/workload-identity-kafka-sasl-oauthbearer/blob/main/src/main/java/io/github/nniikkoollaaii/kafka/workload_identity/WorkloadIdentityUtils.java
 */
package io.confluent.oauth.azure.managedidentity.utils;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.WorkloadIdentityCredential;
import com.azure.identity.WorkloadIdentityCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkloadIdentityUtils {

        private static final Logger log = LoggerFactory.getLogger(WorkloadIdentityUtils.class);


        // ENV vars set by AzureAD Workload Identity Mutating Admission Webhook: https://azure.github.io/azure-workload-identity/docs/installation/mutating-admission-webhook.html
        public static final String AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_FEDERATED_TOKEN_FILE = "AZURE_FEDERATED_TOKEN_FILE";
        public static final String AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_AUTHORITY_HOST = "AZURE_AUTHORITY_HOST";
        public static final String AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_TENANT_ID = "AZURE_TENANT_ID";
        public static final String AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_CLIENT_ID = "AZURE_CLIENT_ID";


        public static String getTenantId() {
                String tenantId = System.getenv(WorkloadIdentityUtils.AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_TENANT_ID);
                if (tenantId == null || tenantId.equals(""))
                        throw new WorkloadIdentityKafkaClientOAuthBearerAuthenticationException(String.format("Missing environment variable %s", WorkloadIdentityUtils.AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_TENANT_ID));
                log.debug("Config: Tenant Id " + tenantId);
                return tenantId;
        }

        public static String getClientId() {
                String clientId = System.getenv(WorkloadIdentityUtils.AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_CLIENT_ID);
                if (clientId == null || clientId.equals(""))
                        throw new WorkloadIdentityKafkaClientOAuthBearerAuthenticationException(String.format("Missing environment variable %s", WorkloadIdentityUtils.AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_CLIENT_ID));
                log.debug("Config: Client Id " + clientId);
                return clientId;
        }

        public static WorkloadIdentityCredential createWorkloadIdentityCredentialFromEnvironment() {
                String federatedTokeFilePath = System.getenv(WorkloadIdentityUtils.AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_FEDERATED_TOKEN_FILE);
                if (federatedTokeFilePath == null || federatedTokeFilePath.equals(""))
                        throw new WorkloadIdentityKafkaClientOAuthBearerAuthenticationException(String.format("Missing environment variable %s", WorkloadIdentityUtils.AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_FEDERATED_TOKEN_FILE));
                log.debug("Config: Federated Token File Path " + federatedTokeFilePath);

                String authorityHost = System.getenv(WorkloadIdentityUtils.AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_AUTHORITY_HOST);
                if (authorityHost == null || authorityHost.equals(""))
                        throw new WorkloadIdentityKafkaClientOAuthBearerAuthenticationException(String.format("Missing environment variable %s", WorkloadIdentityUtils.AZURE_AD_WORKLOAD_IDENTITY_MUTATING_ADMISSION_WEBHOOK_ENV_AUTHORITY_HOST));
                log.debug("Config: Authority host " + authorityHost);
                
                String tenantId = getTenantId(); 
                String clientId = getClientId();


                WorkloadIdentityCredential workloadIdentityCredential  = new WorkloadIdentityCredentialBuilder()
                        .tokenFilePath(federatedTokeFilePath)
                        .authorityHost(authorityHost)
                        .clientId(clientId)
                        .tenantId(tenantId)
                        .build();

                return workloadIdentityCredential;
        }


        public static TokenRequestContext createTokenRequestContextFromEnvironment(String scope) {

                String tenantId = getTenantId(); 
                String clientId = getClientId();

                //Construct a TokenRequestContext to be used be requsting a token at runtime.
                String usedScope =  clientId + "/.default";
                if(scope != null && !scope.isEmpty()) {
                        usedScope = scope;
                }
                log.debug("Config: Scope " + usedScope);
                TokenRequestContext tokenRequestContext = new TokenRequestContext() // TokenRequestContext: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/TokenRequestContext.java
                        .addScopes(usedScope)
                        .setTenantId(tenantId);

                return tokenRequestContext;
        }
}