/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.runtime.runner.opts;

import org.apache.isis.applib.maybe.Maybe;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;

public final class OptionValidatorForPersistor implements OptionValidator {
    private final OptionHandlerPersistor optionHandlerPersistor;

    public OptionValidatorForPersistor(final OptionHandlerPersistor optionHandlerPersistor) {
        this.optionHandlerPersistor = optionHandlerPersistor;
    }

    @Override
    public Maybe<String> validate(final DeploymentType deploymentType) {
        final String objectPersistorName = optionHandlerPersistor.getPersistorName();
        final boolean fail =
            (!StringUtils.isNullOrEmpty(objectPersistorName)) && !deploymentType.canSpecifyObjectStore();
        final String failMsg =
            String.format("Error: cannot specify an object store (persistor) for deployment type %s\n", deploymentType
                .name().toLowerCase());
        return Maybe.setIf(fail, failMsg);
    }
}