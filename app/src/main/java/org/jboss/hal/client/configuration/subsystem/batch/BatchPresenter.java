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
package org.jboss.hal.client.configuration.subsystem.batch;

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
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.client.configuration.subsystem.batch.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BATCH_JBERET;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

public class BatchPresenter
        extends MbuiPresenter<BatchPresenter.MyView, BatchPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;

    @Inject
    public BatchPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return BATCH_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(BATCH_JBERET);
    }

    @Override
    protected void reload() {
        crud.readRecursive(BATCH_SUBSYSTEM_TEMPLATE, result -> {
            // @formatter:off
            getView().updateConfiguration(result);
            getView().updateInMemoryJobRepository(asNamedNodes(failSafePropertyList(result, IN_MEMORY_JOB_REPO_TEMPLATE.lastName())));
            getView().updateJdbcJobRepository(asNamedNodes(failSafePropertyList(result, JDBC_JOB_REPO_TEMPLATE.lastName())));
            getView().updateThreadFactory(asNamedNodes(failSafePropertyList(result, THREAD_FACTORY_TEMPLATE.lastName())));
            getView().updateThreadPool(asNamedNodes(failSafePropertyList(result, THREAD_POOL_TEMPLATE.lastName())));
            // @formatter:on
        });
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.BATCH_CONFIGURATION)
    @Requires(value = {BATCH_SUBSYSTEM_ADDRESS,
            IN_MEMORY_JOB_REPO_ADDRESS,
            JDBC_JOB_REPO_ADDRESS,
            THREAD_FACTORY_ADDRESS,
            THREAD_POOL_ADDRESS},
            recursive = false)
    public interface MyProxy extends ProxyPlace<BatchPresenter> {
    }

    public interface MyView extends MbuiView<BatchPresenter> {
        void updateConfiguration(ModelNode conf);
        void updateInMemoryJobRepository(List<NamedNode> items);
        void updateJdbcJobRepository(List<NamedNode> items);
        void updateThreadFactory(List<NamedNode> items);
        void updateThreadPool(List<NamedNode> items);
    }
    // @formatter:on
}
