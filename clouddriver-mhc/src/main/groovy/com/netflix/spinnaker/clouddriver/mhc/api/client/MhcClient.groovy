package com.netflix.spinnaker.clouddriver.mhc.api.client

import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Slf4j
class MhcClient {

    static class Builder {
        String address
        String username
        String password
        File passwordFile

        Builder address(String address) {
            this.address = address
            return this
        }

        Builder username(String username) {
            this.username = username
            return this
        }

        Builder password(String password){
            this.password = password
            return this
        }

        Builder passwordFile(File passwordFile) {
            this.passwordFile = passwordFile
            return this
        }

        MhcClient build() {
            if (password && passwordFile) {
                throw new IllegalArgumentException('Error, at most one of "password", "passwordFile" can be specified')
            }
            if(password) {
                return new MhcClient(address, username, password)
            } else if (passwordFile) {
                return new MhcClient(address, username, new BufferedReader(new FileReader(passwordFile)).getText())
            } else {
                return new MhcClient(address, "default", "default")
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(MhcClient)

    String address
    String username
    String password

    MhcClient(String address, String username, String password) {
        this.address = address
        this.username = username
        this.password = password
    }
}
