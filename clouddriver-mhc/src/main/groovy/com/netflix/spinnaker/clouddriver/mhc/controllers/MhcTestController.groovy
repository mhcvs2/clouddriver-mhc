package com.netflix.spinnaker.clouddriver.mhc.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.cats.cache.Cache
import com.netflix.spinnaker.cats.cache.CacheData
import com.netflix.spinnaker.cats.cache.RelationshipCacheFilter
import com.netflix.spinnaker.clouddriver.mhc.cache.Keys
import com.netflix.spinnaker.clouddriver.mhc.config.MhcConfigurationProperties
import com.netflix.spinnaker.clouddriver.mhc.security.MhcNamedAccountCredentials
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mhc/test")
class MhcTestController {

    @Autowired
    private final Cache cacheView

    @Autowired
    AccountCredentialsProvider accountCredentialsProvider

    @Autowired
    MhcConfigurationProperties mhcConfigurationProperties

    @RequestMapping(value = "/1", method = RequestMethod.GET)
    Set<MhcNamedAccountCredentials> t1(){
        def res = accountCredentialsProvider.getAll().findAll {
            it instanceof  MhcNamedAccountCredentials
        } as Set<MhcNamedAccountCredentials>
        println("===========================")
        println(res.size())
        res
    }

    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    MhcConfigurationProperties properties() {
        mhcConfigurationProperties
    }

    @RequestMapping(value = "/c1", method = RequestMethod.GET)
    Set<CacheData> c1() {
        Set<CacheData> res = []
        def credentials = accountCredentialsProvider.getAll().findAll {
            it instanceof  MhcNamedAccountCredentials
        } as Set<MhcNamedAccountCredentials>
        credentials.each {
            def key = Keys.getSomeKey(it.accountName, '*')
            println("it.accountName: ${it.accountName}")
            println("key: ${key}")
            def identifiers = cacheView.filterIdentifiers(Keys.Namespace.MHC_TEST.ns, key)
            Set<CacheData> cacheDatas = cacheView.getAll(Keys.Namespace.MHC_TEST.ns, identifiers)
            if(cacheDatas == null) {
                println("null-----------------")
            } else {
                println("have")
                res.addAll(cacheDatas)
            }
        }
        res
    }

    @RequestMapping(value = "/c2", method = RequestMethod.GET)
    Set<CacheData> c2(@RequestParam('account') String account, @RequestParam('name') String name) {
        Set<CacheData> res = []
        def credentials = accountCredentialsProvider.getCredentials(account).findAll {
            it instanceof  MhcNamedAccountCredentials
        } as Set<MhcNamedAccountCredentials>
        MhcNamedAccountCredentials it = credentials.first()
        def key = Keys.getSomeKey(it.accountName, name)
        println("it.accountName: ${it.accountName}")
        println("key: ${key}")
        Set<CacheData> cacheDatas = cacheView.getAll(Keys.Namespace.MHC_TEST.ns, key)
        if(cacheDatas == null) {
            println("null-----------------")
        } else {
            println("have")
            res.addAll(cacheDatas)
        }
        res
    }

    @RequestMapping(value = "/c3", method = RequestMethod.GET)
    Set<CacheData> c3(@RequestParam('account') String account, @RequestParam('name') String name){
        Set<CacheData> res = []
        def credentials = accountCredentialsProvider.getCredentials(account).findAll {
            it instanceof  MhcNamedAccountCredentials
        } as Set<MhcNamedAccountCredentials>
        MhcNamedAccountCredentials it = credentials.first()
        def key = Keys.getSomeKey(it.accountName, name)
        CacheData someData = cacheView.get(Keys.Namespace.MHC_TEST.ns, key)
        Collection<String> anotherKeys = someData.relationships[Keys.Namespace.ANOTHER_TEST.ns]
        Collection<CacheData> anotherDatas = anotherKeys ? cacheView.getAll(Keys.Namespace.ANOTHER_TEST.ns, anotherKeys) : []
        res << someData
        res.addAll(anotherDatas)
        res
    }

}
