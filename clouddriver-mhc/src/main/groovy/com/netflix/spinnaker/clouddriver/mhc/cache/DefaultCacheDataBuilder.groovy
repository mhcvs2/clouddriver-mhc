package com.netflix.spinnaker.clouddriver.mhc.cache

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.netflix.spinnaker.cats.cache.DefaultCacheData

class DefaultCacheDataBuilder {

    String id = ''
    int ttlSeconds = -1
    Map<String, Object> attributes = [:]
    Map<String, Collection<String>> relationships = [:].withDefault({ _ -> [] as Set })

    DefaultCacheDataBuilder(String id) {
        this.id = id
    }

    @JsonCreator
    DefaultCacheDataBuilder(@JsonProperty("id") String id,
                            @JsonProperty("attributes") Map<String, Object> attributes,
                            @JsonProperty("relationships") Map<String, Collection<String>> relationships) {
        this(id)
        this.attributes.putAll(attributes)
        this.relationships.putAll(relationships)
    }

    DefaultCacheData build() {
        new DefaultCacheData(id, ttlSeconds, attributes, relationships)
    }

    static Map<String, DefaultCacheDataBuilder> defaultCacheDataBuilderMap() {
        return [:].withDefault { String id -> new DefaultCacheDataBuilder(id) }
    }

}
