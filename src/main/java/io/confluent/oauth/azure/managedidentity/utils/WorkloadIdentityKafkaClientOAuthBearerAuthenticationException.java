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

This file was copied from https://github.com/nniikkoollaaii/workload-identity-kafka-sasl-oauthbearer/blob/main/src/main/java/io/github/nniikkoollaaii/kafka/workload_identity/WorkloadIdentityKafkaClientOAuthBearerAuthenticationException.java
 */
package io.confluent.oauth.azure.managedidentity.utils;

/**
 * Custom runtime exception to signal errors when using Workload Identity to fetch a token from AzureAD.
 */
public class WorkloadIdentityKafkaClientOAuthBearerAuthenticationException extends RuntimeException {

    public WorkloadIdentityKafkaClientOAuthBearerAuthenticationException(String message) {
        super(message);
    }
}