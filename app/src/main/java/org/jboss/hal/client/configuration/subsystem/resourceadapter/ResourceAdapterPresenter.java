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
package org.jboss.hal.client.configuration.subsystem.resourceadapter;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;

import static org.jboss.hal.client.configuration.subsystem.resourceadapter.AddressTemplates.RESOURCE_ADAPTER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.resourceadapter.AddressTemplates.SELECTED_RESOURCE_ADAPTER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOURCE_ADAPTERS;
import static org.jboss.hal.meta.token.NameTokens.RESOURCE_ADAPTER;

public class ResourceAdapterPresenter
        extends MbuiPresenter<ResourceAdapterPresenter.MyView, ResourceAdapterPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private String resourceAdapter;

    @Inject
    public ResourceAdapterPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> resourceAdapter);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        resourceAdapter = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_RESOURCE_ADAPTER_TEMPLATE.resolve(statementContext);
    }

    String getResourceAdapter() {
        return resourceAdapter;
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(RESOURCE_ADAPTERS)
                .append(Ids.RESOURCE_ADAPTER, Ids.resourceAdapter(resourceAdapter),
                        Names.RESOURCE_ADAPTER, resourceAdapter);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_RESOURCE_ADAPTER_TEMPLATE.resolve(statementContext);
        crud.readRecursive(address, result -> getView().update(new ResourceAdapter(resourceAdapter, result)));
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(RESOURCE_ADAPTER)
    @Requires(RESOURCE_ADAPTER_ADDRESS)
    public interface MyProxy extends ProxyPlace<ResourceAdapterPresenter> {
    }

    public interface MyView extends MbuiView<ResourceAdapterPresenter> {
        void update(ResourceAdapter resourceAdapter);
    }
    // @formatter:on
}
