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

import org.jboss.hal.config.Build;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.resources.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import javax.inject.Inject;

import static org.jboss.hal.config.Settings.Key.*;

/**
 * Loads the settings. Please make sure this is one of the last bootstrap function. This function loads the run-as role
 * which is then used by the dispatcher. But all previous bootstrap functions must not have a run-as role in the
 * dispatcher.
 */
public class LoadSettings implements BootstrapTask {

    private static final Logger logger = LoggerFactory.getLogger(LoadSettings.class);

    private final Environment environment;
    private final Settings settings;

    @Inject
    public LoadSettings(Environment environment, Settings settings) {
        this.environment = environment;
        this.settings = settings;
    }

    @Override
    public Completable call(FlowContext context) {
        settings.load(TITLE, Names.BROWSER_DEFAULT_TITLE);
        settings.load(COLLECT_USER_DATA, environment.getHalBuild() == Build.COMMUNITY);
        settings.load(LOCALE, Settings.DEFAULT_LOCALE);
        settings.load(PAGE_SIZE, Settings.DEFAULT_PAGE_SIZE);
        settings.load(POLL, true);
        settings.load(POLL_TIME, Settings.DEFAULT_POLL_TIME);
        settings.load(RUN_AS, null);
        logger.debug("Load settings: {}", settings);
        return Completable.complete();
    }
}
