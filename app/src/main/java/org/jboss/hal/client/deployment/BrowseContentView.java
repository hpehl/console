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
package org.jboss.hal.client.deployment;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;

public class BrowseContentView extends HalViewImpl implements BrowseContentPresenter.MyView {

    private final BrowseContentElement browseContent;

    @Inject
    public BrowseContentView(Dispatcher dispatcher, EventBus eventBus, Environment environment,
            MetadataRegistry metadataRegistry, Resources resources) {
        Metadata metadata = metadataRegistry.lookup(ContentColumn.CONTENT_TEMPLATE);
        browseContent = new BrowseContentElement(dispatcher, environment, eventBus, metadata, resources);
        registerAttachable(browseContent);
        initElement(browseContent);
    }

    @Override
    public void setContent(Content content) {
        browseContent.setContent(content);
    }
}
