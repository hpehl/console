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
package org.jboss.hal.client.configuration.subsystem.microprofile;

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
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;

import static org.jboss.hal.client.configuration.subsystem.microprofile.AddressTemplates.MICRO_PROFILE_CONFIG_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.microprofile.AddressTemplates.MICRO_PROFILE_CONFIG_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MICROPROFILE_CONFIG_SMALLRYE;

public class MicroProfileConfigPresenter
        extends MbuiPresenter<MicroProfileConfigPresenter.MyView, MicroProfileConfigPresenter.MyProxy>
        implements SupportsExpertMode {

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public MicroProfileConfigPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Dispatcher dispatcher,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
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
        return MICRO_PROFILE_CONFIG_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(MICROPROFILE_CONFIG_SMALLRYE);
    }

    @Override
    protected void reload() {
        crud.readRecursive(MICRO_PROFILE_CONFIG_TEMPLATE.resolve(statementContext),
                result -> getView().update(result));
    }

    void add() {
        AddConfigSourceWizard wizard = new AddConfigSourceWizard(this, dispatcher,
                metadataRegistry, statementContext, resources);
        wizard.show();
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.MICRO_PROFILE_CONFIG)
    @Requires(MICRO_PROFILE_CONFIG_ADDRESS)
    public interface MyProxy extends ProxyPlace<MicroProfileConfigPresenter> {
    }

    public interface MyView extends MbuiView<MicroProfileConfigPresenter> {
        void update(ModelNode modelNode);
    }
    // @formatter:on
}
