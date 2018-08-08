package com.netflix.spinnaker.clouddriver.mhc

import com.netflix.spinnaker.clouddriver.core.CloudProvider
import org.springframework.stereotype.Component

import java.lang.annotation.Annotation

@Component
class MhcCloudProvider implements CloudProvider {

    final static String MHC = "mhc"
    final String id = MHC
    final String displayName = "mhc cloud driver"
    final Class<Annotation> operationAnnotationType = Annotation

}
