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
import java.util.Iterator;

import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.MODCLUSTER_BALANCER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.Strings.substringAfterLast;

@AsyncColumn(Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER_NODE)
public class ModclusterBalancerNodeColumn extends FinderColumn<NamedNode> {

    @Inject
    public ModclusterBalancerNodeColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            Dispatcher dispatcher,
            StatementContext statementContext) {

        super(new Builder<NamedNode>(finder, Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER_NODE, Names.NODE)
                .columnAction(columnActionFactory.refresh(Ids.UNDERTOW_MODCLUSTER_BALANCER_NODE_REFRESH))
                .itemsProvider((context, callback) -> {

                    String modcluster = "";
                    String balancer = "";
                    for (Iterator<FinderSegment> iter = context.getPath().iterator(); iter.hasNext(); ) {
                        FinderSegment finderSegment = iter.next();
                        if ("undertow-runtime-modcluster".equals(finderSegment.getColumnId())) {
                            modcluster = substringAfterLast(finderSegment.getItemId(), "undertow-modcluster-");
                        }
                        if ("undertow-runtime-modcluster-balancer".equals(finderSegment.getColumnId())) {
                            balancer = substringAfterLast(finderSegment.getItemId(), "undertow-modcluster-balancer-");
                        }
                    }
                    ResourceAddress address = MODCLUSTER_BALANCER_TEMPLATE.resolve(statementContext, modcluster,
                            balancer);
                    Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, NODE)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    dispatcher.execute(operation, result -> callback.onSuccess(asNamedNodes(result.asPropertyList())));


                })
                .itemRenderer(item -> new ItemDisplay<NamedNode>() {
                    @Override
                    public String getId() {
                        return Ids.build(UNDERTOW, MODCLUSTER, BALANCER, NODE, item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    /*@Override
                    public String nextColumn() {
                        return Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER_NODE_CONTEXT;
                    }*/
                })
                .onPreview(ModclusterBalancerNodePreview::new)
        );
    }
}
