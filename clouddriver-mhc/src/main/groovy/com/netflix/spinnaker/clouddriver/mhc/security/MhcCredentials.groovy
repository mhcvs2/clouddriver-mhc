package com.netflix.spinnaker.clouddriver.mhc.security

import com.netflix.spinnaker.clouddriver.mhc.api.client.MhcClient

class MhcCredentials {

    private final MhcClient client

    MhcCredentials(MhcClient client) {
        this.client = client
    }

    MhcClient getClient() {
        return client
    }
}
