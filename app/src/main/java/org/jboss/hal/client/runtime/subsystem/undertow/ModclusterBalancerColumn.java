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

import org.jboss.hal.core.finder.*;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;

import javax.inject.Inject;

import static java.util.Collections.emptyList;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.MODCLUSTER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER)
public class ModclusterBalancerColumn extends FinderColumn<NamedNode> {

    @Inject
    public ModclusterBalancerColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            Dispatcher dispatcher,
            StatementContext statementContext) {

        super(new Builder<NamedNode>(finder, Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER, Names.BALANCER)
                .columnAction(columnActionFactory.refresh(Ids.UNDERTOW_MODCLUSTER_BALANCER_REFRESH))
                .itemsProvider((context, callback) -> {

                    FinderSegment segment = context.getPath().findColumn(Ids.UNDERTOW_RUNTIME_MODCLUSTER);
                    if (segment != null) {
                        String modcluster = Ids.extractUndertowModcluster(segment.getItemId());
                        ResourceAddress address = MODCLUSTER_TEMPLATE.resolve(statementContext, modcluster);
                        Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                                .param(CHILD_TYPE, BALANCER)
                                .param(INCLUDE_RUNTIME, true)
                                .build();
                        dispatcher.execute(operation,
                                result -> callback.onSuccess(asNamedNodes(result.asPropertyList())));
                    } else {
                        callback.onSuccess(emptyList());
                    }
                })
                .itemRenderer(item -> new ItemDisplay<NamedNode>() {
                    @Override
                    public String getId() {
                        return Ids.build(UNDERTOW, MODCLUSTER, BALANCER, item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public String nextColumn() {
                        return Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER_NODE;
                    }
                })
                .onPreview(ModclusterBalancerPreview::new)
        );
    }
}
