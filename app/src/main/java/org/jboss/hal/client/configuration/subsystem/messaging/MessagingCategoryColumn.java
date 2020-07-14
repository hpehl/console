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
package org.jboss.hal.client.configuration.subsystem.messaging;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.*;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import javax.inject.Inject;

import static java.util.Arrays.asList;
import static org.jboss.hal.resources.Ids.JMS_BRIDGE_ITEM;

@AsyncColumn(Ids.MESSAGING_CATEGORY)
public class MessagingCategoryColumn extends StaticItemColumn {

    @Inject
    public MessagingCategoryColumn(Finder finder,
            ItemActionFactory itemActionFactory,
            CrudOperations crud,
            Places places,
            Resources resources) {

        super(finder, Ids.MESSAGING_CATEGORY, resources.constants().category(), (context, callback) ->
                callback.onSuccess(asList(
                        new StaticItem.Builder(resources.constants().globalSettings())
                                .id(Ids.MESSAGING_GLOBAL_SETTINGS)
                                .action(itemActionFactory.view(places.selectedProfile(NameTokens.MESSAGING).build()))
                                .onPreview(new MessagingSubsystemPreview(crud, resources))
                                .build(),
                        new StaticItem.Builder(resources.constants().remoteActiveMQServer())
                                .id(Ids.MESSAGING_REMOTE_ACTIVEMQ)
                                .action(itemActionFactory.view(places.selectedProfile(NameTokens.MESSAGING_REMOTE_ACTIVEMQ).build()))
                                .onPreview(new PreviewContent<>(Names.MESSAGING_REMOTE_ACTIVEMQ,
                                        resources.previews().configurationMessagingRemoteActiveMQ()))
                                .build(),
                        new StaticItem.Builder(Names.SERVER)
                                .nextColumn(Ids.MESSAGING_SERVER_CONFIGURATION)
                                .onPreview(new PreviewContent<>(Names.SERVER,
                                        resources.previews().configurationMessagingServer()))
                                .build(),
                        new StaticItem.Builder(Names.JMS_BRIDGE)
                                .id(JMS_BRIDGE_ITEM)
                                .nextColumn(Ids.JMS_BRIDGE)
                                .onPreview(new PreviewContent<>(Names.JMS_BRIDGE,
                                        resources.previews().configurationMessagingJmsBridge()))
                                .build()
                ))
        );
    }
}
