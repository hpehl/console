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
package org.jboss.hal.client.runtime.subsystem.undertow;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.core.finder.*;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

@AsyncColumn(Ids.UNDERTOW_RUNTIME_DEPLOYMENT)
@Requires(WEB_DEPLOYMENT_ADDRESS)
public class DeploymentColumn extends FinderColumn<DeploymentResource> {

    @Inject
    public DeploymentColumn(Finder finder,
            FinderPathFactory finderPathFactory,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Dispatcher dispatcher,
            Places places,
            StatementContext statementContext,
            Environment environment,
            ServerActions serverActions,
            Resources resources) {

        super(new Builder<DeploymentResource>(finder, Ids.UNDERTOW_RUNTIME_DEPLOYMENT, Names.DEPLOYMENT)
                .columnAction(columnActionFactory.refresh(Ids.UNDERTOW_RUNTIME_REFRESH))
                .itemsProvider((context, callback) -> {
                    ResourceAddress addressDeploy = WEB_DEPLOYMENT_TEMPLATE.resolve(statementContext);
                    Operation operationDeploy = new Operation.Builder(addressDeploy, READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    ResourceAddress addressSubdeploy = WEB_SUBDEPLOYMENT_TEMPLATE.resolve(statementContext);
                    Operation operationSubDeploy = new Operation.Builder(addressSubdeploy, READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    dispatcher.execute(new Composite(operationDeploy, operationSubDeploy), (CompositeResult result) -> {
                        List<DeploymentResource> deployments = new ArrayList<>();
                        result.step(0).get(RESULT).asList().forEach(r -> {
                            ResourceAddress _address = new ResourceAddress(r.get(ADDRESS));
                            deployments.add(new DeploymentResource(_address, r.get(RESULT)));
                        });
                        result.step(1).get(RESULT).asList().forEach(r -> {
                            ResourceAddress _address = new ResourceAddress(r.get(ADDRESS));
                            deployments.add(new DeploymentResource(_address, r.get(RESULT)));
                        });
                        callback.onSuccess(deployments);
                    });
                })
                .itemRenderer(item -> new ItemDisplay<DeploymentResource>() {
                    @Override
                    public String getId() {
                        return Ids.asId(item.getPath());
                    }

                    @Override
                    public String getTitle() {
                        return item.getPath();
                    }

                    @Override
                    public List<ItemAction<DeploymentResource>> actions() {
                        List<ItemAction<DeploymentResource>> actions = new ArrayList<>();
                        actions.add(itemActionFactory.view(
                                places.selectedProfile(NameTokens.UNDERTOW_RUNTIME_DEPLOYMENT_VIEW)
                                        .with(DEPLOYMENT, item.getDeployment())
                                        .with(SUBDEPLOYMENT, item.getSubdeployment())
                                        .build()));
                        return actions;
                    }
                })
                .onPreview(item -> new DeploymentPreview(item, finderPathFactory, places, resources, environment,
                        dispatcher, statementContext, serverActions))
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .showCount()
        );
    }
}
