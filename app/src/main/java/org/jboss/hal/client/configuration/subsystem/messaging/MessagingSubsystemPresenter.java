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

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.MESSAGING_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.MESSAGING_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MESSAGING_ACTIVEMQ;

public class MessagingSubsystemPresenter
        extends MbuiPresenter<MessagingSubsystemPresenter.MyView, MessagingSubsystemPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public MessagingSubsystemPresenter(EventBus eventBus,
            MessagingSubsystemPresenter.MyView view,
            MessagingSubsystemPresenter.MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return MESSAGING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.MESSAGING_GLOBAL_SETTINGS,
                        resources.constants().category(), resources.constants().globalSettings());
    }

    @Override
    protected void reload() {
        crud.read(MESSAGING_SUBSYSTEM_TEMPLATE, result -> getView().update(result));
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.MESSAGING)
    @Requires(value = MESSAGING_SUBSYSTEM_ADDRESS, recursive = false)
    public interface MyProxy extends ProxyPlace<MessagingSubsystemPresenter> {
    }

    public interface MyView extends MbuiView<MessagingSubsystemPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on
}
