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
package org.jboss.hal.client.runtime.subsystem.jpa;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.Strings;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;

import static org.jboss.hal.client.runtime.subsystem.jpa.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.token.NameTokens.JPA_RUNTIME;

// TODO Support sub-deployments!
public class JpaPresenter extends ApplicationFinderPresenter<JpaPresenter.MyView, JpaPresenter.MyProxy> {

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private String deployment;
    private String subdeployment;
    private String resourceName;
    private String persistenceUnit;

    @Inject
    public JpaPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        deployment = request.getParameter(DEPLOYMENT, null);
        subdeployment = request.getParameter(SUBDEPLOYMENT, null);
        resourceName = request.getParameter(NAME, null);
        persistenceUnit = resourceName != null ? Strings.substringAfterLast(resourceName, "#") : Names.NOT_AVAILABLE;
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, JPA, resources.constants().monitor(), Names.JPA)
                .append(Ids.JPA_RUNTIME, Ids.jpaStatistic(deployment, subdeployment, resourceName), Names.JPA,
                        persistenceUnit);
    }

    @Override
    protected void reload() {
        ResourceAddress address = jobAddress();
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> getView().update(new JpaStatistic(address, result)));
    }

    private ResourceAddress jobAddress() {
        ResourceAddress address;
        if (subdeployment == null) {
            address = JPA_DEPLOYMENT_TEMPLATE.resolve(statementContext, deployment, resourceName);
        } else {
            address = HPU_SUBDEPLOYMENT_TEMPLATE.resolve(statementContext, deployment, subdeployment, resourceName);
        }
        return address;
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(JPA_RUNTIME)
    @Requires(JPA_DEPLOYMENT_ADDRESS)
    public interface MyProxy extends ProxyPlace<JpaPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<JpaPresenter> {
        void update(JpaStatistic statistic);
    }
    // @formatter:on
}
