/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.bootstrap.tasks;

import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.ExtensionStorage;
import org.jboss.hal.core.extension.InstalledExtension;
import org.jboss.hal.flow.FlowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import javax.inject.Inject;

public class ReadExtensions implements BootstrapTask {

    private static final Logger logger = LoggerFactory.getLogger(ReadExtensions.class);

    private final ExtensionRegistry extensionRegistry;
    private final ExtensionStorage extensionStorage;

    @Inject
    public ReadExtensions(ExtensionRegistry extensionRegistry, ExtensionStorage extensionStorage) {
        this.extensionRegistry = extensionRegistry;
        this.extensionStorage = extensionStorage;
    }

    @Override
    public Completable call(FlowContext context) {
        // TODO Load server side extensions from /core-service=management/console-extension=*
        for (InstalledExtension extension : extensionStorage.list()) {
            logger.debug("Read extension {}", extension.getName());
            extensionRegistry.inject(extension.getFqScript(), extension.getFqStylesheets());
        }
        return Completable.complete();
    }
}
