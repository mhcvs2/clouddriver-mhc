/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.orchestration

import com.netflix.spinnaker.clouddriver.deploy.DescriptionValidator
import com.netflix.spinnaker.clouddriver.security.ProviderVersion
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * This class is to support older naming convention for operations. Do NOT use this for new
 * operations - this is just used until older operations are migrated to new scheme
 *
 */
class ApplicationContextAtomicOperationsRegistry implements AtomicOperationsRegistry {

    @Autowired
    ApplicationContext applicationContext

    @Override
    AtomicOperationConverter getAtomicOperationConverter(String description, String cloudProvider, ProviderVersion version) {
        def result = (AtomicOperationConverter) applicationContext.getBean(description)
        if (!result.acceptsVersion(version)) {
            throw new AtomicOperationConverterNotFoundException("Converter version mismatch. Converter '$description' not applicable for '$version'")
        }

        return result
    }

    @Override
    DescriptionValidator getAtomicOperationDescriptionValidator(String validator, String cloudProvider, ProviderVersion version) {
        def result = (DescriptionValidator) applicationContext.getBean(validator)
        if (!result.acceptsVersion(version)) {
            throw new AtomicOperationConverterNotFoundException("Validator version mismatch. Validator '$validator' not applicable for '$version'")
        }

        return result
    }
}
