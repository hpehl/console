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
package org.jboss.hal.client.configuration.subsystem.undertow;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused", "WeakerAccess"})
public abstract class HandlerView extends MbuiViewImpl<HandlerPresenter>
        implements HandlerPresenter.MyView {

    public static HandlerView create(final MbuiContext mbuiContext) {
        return new Mbui_HandlerView(mbuiContext);
    }

    @MbuiElement("undertow-handler-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("undertow-file-handler-table") Table<NamedNode> fileHandlerTable;
    @MbuiElement("undertow-file-handler-form") Form<NamedNode> fileHandlerForm;
    @MbuiElement("undertow-reverse-proxy-table") Table<NamedNode> reverseProxyTable;
    @MbuiElement("undertow-reverse-proxy-form") Form<NamedNode> reverseProxyForm;

    HandlerView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        clearHostFields();
    }

    private void clearHostFields() {
        fileHandlerForm.getFormItem(HOSTS).setEnabled(false);
        reverseProxyForm.getFormItem(HOSTS).setEnabled(false);
    }

    @Override
    public void attach() {
        super.attach();
        fileHandlerTable.onSelectionChange(t -> updateHostRefs(t, fileHandlerForm));
        reverseProxyTable.onSelectionChange(t -> updateHostRefs(t, reverseProxyForm));
    }

    @SuppressWarnings("ConstantConditions")
    private void updateHostRefs(final Table<NamedNode> table, final Form<NamedNode> form) {
        FormItem<String> formItem = form.getFormItem(HOSTS);
        if (formItem != null) {
            if (table.hasSelection()) {
                ResourceAddress address = HOST_TEMPLATE.append(LOCATION + "=*").resolve(mbuiContext.statementContext());
                Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION).build();
                mbuiContext.dispatcher().execute(operation,
                        result -> {
                            String hosts = result.asList()
                                    .stream()
                                    .filter(node -> table.selectedRow().getName().equals(
                                            node.get(RESULT).get(HANDLER).asString()))
                                    .map(node -> {
                                        ResourceAddress adr = new ResourceAddress(node.get(ADDRESS));
                                        ResourceAddress host = adr.getParent();
                                        ResourceAddress server = host.getParent();
                                        return server.lastValue() + "/" + host.lastValue();
                                    })
                                    .distinct()
                                    .sorted()
                                    .collect(Collectors.joining(", "));
                            formItem.setValue(hosts);
                        },
                        (op1, failure) -> formItem.clearValue());
            } else {
                formItem.clearValue();
            }
        }
    }

    @Override
    public void update(final ModelNode modelNode) {
        clearHostFields();

        fileHandlerForm.clear();
        fileHandlerTable.update(asNamedNodes(failSafePropertyList(modelNode, "file")));
        reverseProxyForm.clear();
        reverseProxyTable.update(asNamedNodes(failSafePropertyList(modelNode, "reverse-proxy")));
    }
}
