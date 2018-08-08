package com.netflix.spinnaker.clouddriver.mhc.security

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netflix.spinnaker.clouddriver.mhc.api.client.MhcClient
import com.netflix.spinnaker.clouddriver.security.AccountCredentials

class MhcNamedAccountCredentials implements AccountCredentials<MhcCredentials> {

    static class Builder {
        String accountName
        String environment
        String accountType
        String address
        String username
        String password
        File passwordFile

        Builder() {}

        Builder accountName(String accountName) {
            this.accountName = accountName
            return this
        }

        Builder environment(String environment) {
            this.environment = environment
            return this
        }

        Builder accountType(String accountType) {
            this.accountType = accountType
            return this
        }

        Builder address(String address) {
            this.address = address
            return this
        }

        Builder username(String username) {
            this.username = username
            return this
        }

        Builder password(String address) {
            this.password = address
            return this
        }

        Builder passwordFile(String passwordFile) {
            if (passwordFile) {
                this.passwordFile = new File(passwordFile)
            } else {
                this.passwordFile = null
            }
            return this
        }

        MhcNamedAccountCredentials build() {
            return new MhcNamedAccountCredentials(accountName,
                                                environment,
                                                accountName,
                                                address,
                                                username,
                                                password,
                                                passwordFile,
                                                null)
        }

    }

    private static final String CLOUD_PROVIDER = "mhc"
    final String accountName
    final String environment
    final String accountType
    final String address
    @JsonIgnore
    final String username
    @JsonIgnore
    final String password
    final File passwordFile
    @JsonIgnore
    final MhcCredentials credentials
    final List<String> requiredGroupMembership

    @Override
    String getCloudProvider() {
        return CLOUD_PROVIDER
    }

    @Override
    String getName() {
        return accountName
    }

    MhcNamedAccountCredentials(String accountName, String environment, String accountType, String address, String username, String password, File passwordFile, List<String> requiredGroupMembership) {
        this.accountName = accountName
        this.environment = environment
        this.accountType = accountType
        this.address = address
        this.username = username
        this.password = password
        this.passwordFile = passwordFile
        this.requiredGroupMembership = requiredGroupMembership == null ? Collections.emptyList() : Collections.unmodifiableList(requiredGroupMembership)
        this.credentials = buildCredentials()
    }

    private MhcCredentials buildCredentials() {
        MhcClient client = (new MhcClient.Builder())
                .address(address)
                .username(username)
                .password(password)
                .passwordFile(passwordFile)
                .build()
        return new MhcCredentials(client)
    }

}
