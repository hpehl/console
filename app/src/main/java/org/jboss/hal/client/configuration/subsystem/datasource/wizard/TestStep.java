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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import org.gwtproject.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.*;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import rx.Completable;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.configuration.subsystem.datasource.wizard.DataSourceWizard.addOperation;
import static org.jboss.hal.core.runtime.TopologyTasks.runningServers;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.properties;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.CSS.*;

class TestStep extends WizardStep<Context, State> {

    private static final String WIZARD_TITLE = "wizard-title";
    private static final String WIZARD_TEXT = "wizard-text";

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final HTMLElement root;

    TestStep(Dispatcher dispatcher,
            StatementContext statementContext,
            Environment environment,
            Provider<Progress> progress,
            Resources resources) {

        super(resources.constants().testConnection());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.environment = environment;
        this.progress = progress;
        this.resources = resources;

        String testConnection = resources.constants().testConnection();
        SafeHtml description = environment.isStandalone()
                ? resources.messages().testConnectionStandalone(testConnection)
                : resources.messages().testConnectionDomain(testConnection);

        root = div()
                .add(div().innerHtml(description))
                .add(div().css(blankSlatePf)
                        .add(button(resources.constants().testConnection())
                                .id(Ids.DATA_SOURCE_TEST_CONNECTION)
                                .css(btn, btnLg, btnPrimary)
                                .on(click, event -> testConnection()))).element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private void testConnection() {
        Context context = wizard().getContext();

        List<Task<FlowContext>> tasks = new ArrayList<>();
        if (!context.isCreated()) {
            // add data source
            tasks.add(flowContext -> dispatcher.execute(addOperation(context, statementContext))
                    .doOnSuccess((CompositeResult result) -> context.setCreated(true))
                    .doOnError(throwable -> {
                        flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                        flowContext.set(WIZARD_TEXT, resources.messages().dataSourceAddError());
                    })
                    .toCompletable());
        }

        // check running server(s)
        tasks.addAll(runningServers(environment, dispatcher,
                properties(PROFILE_NAME, statementContext.selectedProfile())));

        // test connection
        tasks.add(flowContext -> {
            List<Server> servers = flowContext.get(TopologyTasks.SERVERS);
            ResourceAddress address;
            if (!servers.isEmpty()) {
                Server server = servers.get(0);
                address = server.getServerAddress();
            } else if (environment.isStandalone()) {
                address = ResourceAddress.root();
            } else {
                flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                flowContext.set(WIZARD_TEXT,
                        SafeHtmlUtils.fromString(resources.constants().noRunningServers()));
                return Completable.error(new FlowException(resources.messages().testConnectionErrorDomain(),
                        flowContext));
            }
            address.add(SUBSYSTEM, DATASOURCES)
                    .add(context.dataSource.isXa() ? XA_DATA_SOURCE : DATA_SOURCE, context.dataSource.getName());
            Operation operation = new Operation.Builder(address, TEST_CONNECTION_IN_POOL).build();
            return dispatcher.execute(operation).doOnError(throwable -> {
                flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                flowContext.set(WIZARD_TEXT,
                        resources.messages().testConnectionError(context.dataSource.getName()));
            }).toCompletable();
        });

        series(new FlowContext(progress.get()), tasks)
                .subscribe(new Outcome<FlowContext>() {
                    @Override
                    public void onError(FlowContext flowContext, Throwable error) {
                        String title;
                        SafeHtml text;
                        if (flowContext == null) {
                            title = resources.constants().unknownError();
                            text = resources.messages().unknownError();
                        } else {
                            title = flowContext.get(WIZARD_TITLE);
                            text = flowContext.get(WIZARD_TEXT);
                        }
                        wizard().showError(title, text, error.getMessage(), false);
                    }

                    @Override
                    public void onSuccess(FlowContext flowContext) {
                        wizard().showSuccess(resources.constants().testConnectionSuccess(),
                                resources.messages().testConnectionSuccess(context.dataSource.getName()), false);
                    }
                });
    }
}
