package com.netflix.spinnaker.clouddriver.mhc

import com.netflix.spinnaker.clouddriver.mhc.config.MhcConfigurationProperties
import com.netflix.spinnaker.clouddriver.mhc.security.MhcCredentialsInitializer
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.EnableScheduling


@Configuration
@EnableConfigurationProperties
@EnableScheduling
@ConditionalOnProperty('mhc.enabled')
@ComponentScan(["com.netflix.spinnaker.clouddriver.mhc"])
@Import([ MhcCredentialsInitializer ])
class MhcConfiguration {

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    @ConfigurationProperties('mhc')
    MhcConfigurationProperties mhcConfigurationProperties() {
        new MhcConfigurationProperties()
    }


}
