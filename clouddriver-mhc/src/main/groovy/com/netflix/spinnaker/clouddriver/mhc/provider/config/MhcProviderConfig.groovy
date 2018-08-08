package com.netflix.spinnaker.clouddriver.mhc.provider.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spectator.api.Registry
import com.netflix.spinnaker.cats.agent.Agent
import com.netflix.spinnaker.cats.provider.ProviderSynchronizerTypeWrapper
import com.netflix.spinnaker.clouddriver.mhc.MhcCloudProvider
import com.netflix.spinnaker.clouddriver.mhc.provider.MhcProvider
import com.netflix.spinnaker.clouddriver.mhc.provider.agent.MhcCachingAgent
import com.netflix.spinnaker.clouddriver.mhc.security.MhcNamedAccountCredentials
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository
import com.netflix.spinnaker.clouddriver.security.ProviderUtils
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Scope

import java.util.concurrent.ConcurrentHashMap

@Configuration
class MhcProviderConfig {

    @Bean
    @DependsOn('mhcNamedAccountCredentials')
    MhcProvider mhcProvider(MhcCloudProvider mhcCloudProvider,
                            AccountCredentialsRepository accountCredentialsRepository,
                            ObjectMapper objectMapper,
                            Registry registry) {
        def mhcProvider = new MhcProvider(mhcCloudProvider, Collections.newSetFromMap(new ConcurrentHashMap<Agent, Boolean>()))
        synchronizerMhcProvider(mhcProvider, mhcCloudProvider, accountCredentialsRepository, objectMapper, registry)
        mhcProvider
    }

    @Bean
    MhcProviderSynchronizerTypeWrapper mhcProviderSynchronizerTypeWrapper() {
        new MhcProviderSynchronizerTypeWrapper()
    }

    class MhcProviderSynchronizerTypeWrapper implements ProviderSynchronizerTypeWrapper {
        @Override
        Class getSynchronizerType() {
            return MhcProviderSynchronizer
        }
    }

    class MhcProviderSynchronizer {}

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    MhcProviderSynchronizer synchronizerMhcProvider(MhcProvider mhcProvider,
                                                    MhcCloudProvider mhcCloudProvider,
                                                    AccountCredentialsRepository accountCredentialsRepository,
                                                    ObjectMapper objectMapper,
                                                    Registry registry){
        def scheduledAccounts = ProviderUtils.getScheduledAccounts(mhcProvider)
        def allAccounts = ProviderUtils.buildThreadSafeSetOfAccounts(accountCredentialsRepository, MhcNamedAccountCredentials)

        allAccounts.each {
            MhcNamedAccountCredentials credentials ->
                if(!scheduledAccounts.contains(credentials.accountName)) {
                    def newlyAddedAgents = []
                    newlyAddedAgents << new MhcCachingAgent(credentials.credentials, mhcCloudProvider, credentials.accountName, 0 , 1, 15)
                    if(mhcProvider.agentScheduler) {
                        ProviderUtils.rescheduleAgents(mhcProvider, newlyAddedAgents)
                    }
                    println("add agents ${newlyAddedAgents.size()}")
                    mhcProvider.agents.addAll(newlyAddedAgents)
                }
        }
        new MhcProviderSynchronizer()
    }
}
