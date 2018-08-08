package com.netflix.spinnaker.clouddriver.mhc.provider.agent

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.cats.agent.AccountAware
import com.netflix.spinnaker.cats.agent.AgentDataType
import com.netflix.spinnaker.cats.agent.AgentIntervalAware
import com.netflix.spinnaker.cats.agent.CacheResult
import com.netflix.spinnaker.cats.agent.CachingAgent
import com.netflix.spinnaker.cats.agent.DefaultCacheResult
import com.netflix.spinnaker.cats.cache.CacheData
import com.netflix.spinnaker.cats.cache.DefaultCacheData
import com.netflix.spinnaker.cats.provider.ProviderCache
import com.netflix.spinnaker.clouddriver.cache.OnDemandAgent
import com.netflix.spinnaker.clouddriver.cache.OnDemandMetricsSupport
import com.netflix.spinnaker.clouddriver.mhc.MhcCloudProvider
import com.netflix.spinnaker.clouddriver.mhc.cache.DefaultCacheDataBuilder
import com.netflix.spinnaker.clouddriver.mhc.cache.Keys
import com.netflix.spinnaker.clouddriver.mhc.provider.MhcProvider
import com.netflix.spinnaker.clouddriver.mhc.security.MhcCredentials


import static com.netflix.spinnaker.clouddriver.core.provider.agent.Namespace.*

import java.util.concurrent.TimeUnit

import static java.util.Collections.unmodifiableSet

class MhcCachingAgent implements CachingAgent, AccountAware, AgentIntervalAware, OnDemandAgent {

    static final Set<AgentDataType> types = unmodifiableSet([
            AgentDataType.Authority.INFORMATIVE.forType(Keys.Namespace.MHC_TEST.ns)
    ] as Set)

    private MhcCredentials credentials
    private MhcCloudProvider mhcCloudProvider
    private String accountName
    private final int index
    private final int threadCount
    private final long interval
    private ObjectMapper objectMapper

    MhcCachingAgent(MhcCredentials credentials, MhcCloudProvider mhcCloudProvider, String accountName, int index, int threadCount, Long interval) {
        this.credentials = credentials
        this.mhcCloudProvider = mhcCloudProvider
        this.accountName = accountName
        this.index = index
        this.threadCount = threadCount
        this.interval = TimeUnit.SECONDS.toMillis(interval)
        this.objectMapper = new ObjectMapper()
    }

    @Override
    String getAccountName() {
        return accountName
    }

    @Override
    Long getAgentInterval() {
        return interval
    }

    @Override
    Collection<AgentDataType> getProvidedDataTypes() {
        return types
    }

    @Override
    CacheResult loadData(ProviderCache providerCache) {
        Long start = System.currentTimeMillis()
        List<String> names = []
        List<Integer> is = []
        for(int i=0; i<10; i++) {
            names << "some-${i}".toString()
            is[i] = i
        }

        List<CacheData> evictFromOnDemand = []
        List<CacheData> keepInOnDemand = []

        providerCache.getAll(ON_DEMAND.ns,
            names.collect {
                Keys.getSomeKey(accountName, it)
            }
        ).each {
            CacheData onDemandEntry ->
                if(onDemandEntry.attributes.cacheTime < start && onDemandEntry.attributes.processedCount > 0) {
                    evictFromOnDemand << onDemandEntry
                } else {
                    keepInOnDemand << onDemandEntry
                }
        }
        buildCacheResult(is, keepInOnDemand.collectEntries {CacheData ondemandEntry ->
            [(ondemandEntry.id): ondemandEntry]}, evictFromOnDemand*.id, start)
    }

    CacheResult buildCacheResult(List<Integer> is, Map<String, CacheData> onDemandKeep, List<String> onDemandEvict, Long start) {
        Map<String, DefaultCacheDataBuilder> cachedData = DefaultCacheDataBuilder.defaultCacheDataBuilderMap()
        Map<String, DefaultCacheDataBuilder> cachedData2 = DefaultCacheDataBuilder.defaultCacheDataBuilderMap()
        for(int i: is) {
            def someKey = Keys.getSomeKey(accountName, "some-${i}")
            def anotherKey = Keys.getSomeKey(accountName, "another-${i}")

            def onDemandData = onDemandKeep ? onDemandKeep[someKey] : null
            if (onDemandData && onDemandData.attributes.cacheTime >= start) {
                Map<String, List<CacheData>> cacheResults = objectMapper.readValue(onDemandData.attributes.cacheResults as String,
                new TypeReference<Map<String, List<DefaultCacheData>>>() {})
                cacheResults[Keys.Namespace.MHC_TEST.ns].each {
                    println("00000000000000000000000")
                    println(it)
                    cachedData[it.id].with {
                        attributes.putAll(it.attributes)
                        relationships[Keys.Namespace.ANOTHER_TEST.ns].add(anotherKey)
                    }
                }
            } else {
                cachedData[someKey].with {
                    attributes.account = accountName
                    attributes.some = "some-${i}".toString()
                    relationships[Keys.Namespace.ANOTHER_TEST.ns].add(anotherKey)
                }
            }
            cachedData2[anotherKey].with {
                attributes.account = accountName
                attributes.some = "another-${i}".toString()
                relationships[Keys.Namespace.MHC_TEST.ns].add(someKey)
            }
        }
        println("Caching ${cachedData.size()} in type ${agentType}")
        def res = new DefaultCacheResult([
                (Keys.Namespace.MHC_TEST.ns) : cachedData.values().collect({ builder -> builder.build() }),
                (Keys.Namespace.ANOTHER_TEST.ns): cachedData2.values().collect({ builder -> builder.build() }),
                (ON_DEMAND.ns): onDemandKeep.values()
        ], [
                (ON_DEMAND.ns): onDemandEvict,
        ])
        return res
    }

    @Override
    String getAgentType() {
        return "${accountName}/${MhcCachingAgent.simpleName}[${index + 1}/${threadCount}]"
    }

    @Override
    String getProviderName() {
        return MhcProvider.PROVIDER_NAME
    }

    //----------------------------------------------------------------
    @Override
    String getOnDemandAgentType() {
        return "${getAgentType()}-OnDemand"
    }

    @Override
    OnDemandMetricsSupport getMetricsSupport() {
        return null
    }

    @Override
    boolean handles(OnDemandType type, String cloudProvider) {
        println("handles=====================================")
        println(OnDemandType.ServerGroup)
        return type == OnDemandType.ServerGroup && cloudProvider == mhcCloudProvider.id
    }

    @Override
    OnDemandResult handle(ProviderCache providerCache, Map<String, ?> data) {
        println("data: ")
        println(data)
        if (!data.containsKey("name")) {
            return null
        }
        if(data.account != accountName) {
            return null
        }
        String name = data.name
        List<Integer> is = [name.split('-').last().toInteger()]
        println(is)
        CacheResult result = buildCacheResult(is, [:], [], Long.MAX_VALUE)
        def jsonResult = objectMapper.writeValueAsString(result.cacheResults)
        if(result.cacheResults.values().flatten().isEmpty()) {
            providerCache.evictDeletedItems(ON_DEMAND.ns, [Keys.getSomeKey(accountName, name)])
        } else {
            def cacheData = new DefaultCacheData(
                    Keys.getSomeKey(accountName, name),
                    10 * 60,
                    [
                            cacheTime: System.currentTimeMillis(),
                            cacheResults: jsonResult,
                            processedCount: 0,
                            processedTime: null
                    ],
                    [:]
            )
            providerCache.putCacheData(ON_DEMAND.ns, cacheData)
        }
        println("On demand cahce refresh (data: ${data} succeeded.)")
        return new OnDemandResult(
                sourceAgentType: getOnDemandAgentType(),
                cacheResult: result,
                evictions: [:]
        )
    }

    @Override
    Collection<Map> pendingOnDemandRequests(ProviderCache providerCache) {
        return null
    }
}
