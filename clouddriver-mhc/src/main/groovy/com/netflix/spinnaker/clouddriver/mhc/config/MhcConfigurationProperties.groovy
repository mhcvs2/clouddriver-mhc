package com.netflix.spinnaker.clouddriver.mhc.config

import groovy.transform.ToString


@ToString(includeNames = true)
class MhcConfigurationProperties {

    @ToString(includeNames = true)
    static class ManagedAccount {
        String name
        String environment
        String address
        String accountType
        String username
        String password
        String passwordFile
    }

    List<ManagedAccount> accounts = []

}
