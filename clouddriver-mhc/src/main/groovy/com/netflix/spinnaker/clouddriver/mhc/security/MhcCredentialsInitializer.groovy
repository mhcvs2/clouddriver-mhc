package com.netflix.spinnaker.clouddriver.mhc.security

import com.netflix.spinnaker.cats.module.CatsModule
import com.netflix.spinnaker.cats.provider.ProviderSynchronizerTypeWrapper
import com.netflix.spinnaker.clouddriver.mhc.config.MhcConfigurationProperties
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository
import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable
import com.netflix.spinnaker.clouddriver.security.ProviderUtils
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Slf4j
@Component
@Configuration
class MhcCredentialsInitializer implements CredentialsInitializerSynchronizable {

    @Bean
    List<? extends MhcNamedAccountCredentials> mhcNamedAccountCredentials(MhcConfigurationProperties mhcConfigurationProperties,
                                                                          AccountCredentialsRepository accountCredentialsRepository,
                                                                          ApplicationContext applicationContext,
                                                                          List<ProviderSynchronizerTypeWrapper> providerSynchronizerTypeWrappers) {
        synchronizeMhcAccounts(mhcConfigurationProperties, accountCredentialsRepository, null, applicationContext, providerSynchronizerTypeWrappers)
    }

    @Override
    String getCredentialsSynchronizationBeanName() {
        return "synchronizeMhcAccounts"
    }

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    List<? extends MhcNamedAccountCredentials> synchronizeMhcAccounts(MhcConfigurationProperties mhcConfigurationProperties, AccountCredentialsRepository accountCredentialsRepository,
                                                                      CatsModule catsModule,
                                                                      ApplicationContext applicationContext,
                                                                      List<ProviderSynchronizerTypeWrapper> providerSynchronizerTypeWrappers) {
        println("synchronizeMhcAccounts================================================")
        def (ArrayList<MhcConfigurationProperties.ManagedAccount> accountsToAdd, List<String> namesOfDeletedAccounts) =
        ProviderUtils.calculateAccountDeltas(accountCredentialsRepository, MhcNamedAccountCredentials, mhcConfigurationProperties.accounts)
        accountsToAdd.each {
            MhcConfigurationProperties.ManagedAccount managedAccount ->
                try {
                    def mhcAccount = (new MhcNamedAccountCredentials.Builder())
                    .accountName(managedAccount.name)
                    .environment(managedAccount.environment ?: managedAccount.name)
                    .accountType(managedAccount.accountType ?: managedAccount.name)
                    .password(managedAccount.password)
                    .address(managedAccount.address)
                    .username(managedAccount.username)
                    .passwordFile(managedAccount.passwordFile)
                    .build()

                    accountCredentialsRepository.save(managedAccount.name, mhcAccount)
                } catch (e) {
                    e.printStackTrace()
                    log.info "Could not load account ${managedAccount.name} for Mhc"
                }
        }

        accountCredentialsRepository.all.findAll {
            it instanceof MhcNamedAccountCredentials
        } as List<MhcNamedAccountCredentials>
    }

}
