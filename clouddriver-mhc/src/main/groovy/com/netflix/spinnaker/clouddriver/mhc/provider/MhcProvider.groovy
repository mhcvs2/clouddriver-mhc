package com.netflix.spinnaker.clouddriver.mhc.provider

import com.netflix.spinnaker.cats.agent.Agent
import com.netflix.spinnaker.cats.agent.AgentSchedulerAware
import com.netflix.spinnaker.clouddriver.cache.SearchableProvider
import com.netflix.spinnaker.clouddriver.mhc.MhcCloudProvider

class MhcProvider extends AgentSchedulerAware implements SearchableProvider {

    public static final String PROVIDER_NAME = MhcProvider.name

    final Set<String> defaultCaches = Collections.emptySet()

    final Map<String, String> urlMappingTemplates = Collections.emptyMap()
    final Collection<Agent> agents
    final MhcCloudProvider cloudProvider
    final Map<SearchableResource, SearchableProvider.SearchResultHydrator> searchResultHydrators = Collections.emptyMap()

    MhcProvider(MhcCloudProvider cloudProvider, Collection<Agent> agents) {
        println("init=============================================")
        this.agents = agents
        this.cloudProvider = cloudProvider
    }

    @Override
    Map<String, String> parseKey(String key) {
        return [:]
    }

    @Override
    String getProviderName() {
        return PROVIDER_NAME
    }

}
