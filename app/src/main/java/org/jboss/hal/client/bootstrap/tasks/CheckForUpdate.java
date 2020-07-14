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

import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.XMLHttpRequest;
import org.jboss.hal.config.Build;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;

public class CheckForUpdate implements InitializedTask {

    private static final String METADATA_URL = "https://raw.githubusercontent.com/hal/console/master/version.txt";
    private static Logger logger = LoggerFactory.getLogger(CheckForUpdate.class);

    private final Environment environment;
    private final EventBus eventBus;

    @Inject
    public CheckForUpdate(Environment environment, EventBus eventBus) {
        this.environment = environment;
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        // only check for community updates
        if (environment.getHalBuild() == Build.PRODUCT) {
            logger.debug("Version update check skipped for EAP");
        } else {
            logger.debug("Check for update");
            XMLHttpRequest xhr = new XMLHttpRequest();
            xhr.onload = event -> {
                String versionText = xhr.responseText;
                try {
                    logger.debug("Online version {}", versionText);
                    Version version = Version.parseVersion(versionText);
                    eventBus.fireEvent(new VersionUpdateEvent(version));
                } catch (Throwable t) {
                    logger.warn("Cannot parse version from {}: {}", versionText, t.getMessage());
                }
            };
            xhr.addEventListener("error", event -> logger.warn("Cannot read version from {}: Status {} {}",
                    METADATA_URL, xhr.status, xhr.statusText));
            xhr.open(GET.name(), METADATA_URL, true);
            xhr.send();
        }
    }
}
