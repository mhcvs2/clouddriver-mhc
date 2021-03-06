/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.security.resources;

import com.netflix.frigga.Names;

/**
 * Convenience trait for parsing the application name out of a description with a "serverGroupName"
 * property.
 */
public interface ServerGroupNameable extends ApplicationNameable {
    String getServerGroupName();

    @Override
    default String getApplication() {
        return Names.parseName(getServerGroupName()).getApp();
    }
}
