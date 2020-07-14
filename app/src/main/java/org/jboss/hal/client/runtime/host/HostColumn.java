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
package org.jboss.hal.client.runtime.host;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.*;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.host.*;
import org.jboss.hal.core.runtime.host.HostActionEvent.HostActionHandler;
import org.jboss.hal.core.runtime.host.HostResultEvent.HostResultHandler;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.*;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.configurationchanges.ConfigurationChangesPresenter.CONFIGURATION_CHANGES_ADDRESS;
import static org.jboss.hal.client.runtime.host.AddressTemplates.HOST_CONNECTION_TEMPLATE;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.core.runtime.TopologyTasks.hosts;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.CSS.pfIcon;

@Column(Ids.HOST)
@Requires(value = {
        "/core-service=management/host-connection=*",
        "host=*/subsystem=core-management/service=configuration-changes",
        "host=*/core-service=management/service=management-operations"}, recursive = false)
public class HostColumn extends FinderColumn<Host> implements HostActionHandler, HostResultHandler {

    static AddressTemplate hostTemplate(Host host) {
        return AddressTemplate.of("/host=" + host.getAddressName());
    }

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public HostColumn(Finder finder,
            Environment environment,
            Dispatcher dispatcher,
            CrudOperations crud,
            EventBus eventBus,
            StatementContext statementContext,
            @Footer Provider<Progress> progress,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            HostActions hostActions,
            Resources resources) {

        super(new Builder<Host>(finder, Ids.HOST, Names.HOST)
                .onItemSelect(host -> eventBus.fireEvent(new HostSelectionEvent(host.getAddressName())))
                .onPreview(item -> new HostPreview(hostActions, item, resources))
                .pinnable()
                .showCount()
                // Unlike other columns the host column does not have a custom breadcrumb item handler.
                // It makes no sense to replace the host in a finder path like
                // "host => master / server => server-one / subsystem => logging / log-file => server.log"
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .filterDescription(resources.messages().hostColumnFilterDescription())
        );
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.resources = resources;

        addColumnAction(columnActionFactory.refresh(Ids.HOST_REFRESH));
        List<ColumnAction<Host>> pruneActions = new ArrayList<>();
        pruneActions.add(new ColumnAction.Builder<Host>(Ids.HOST_PRUNE_EXPIRED)
                .title(resources.constants().pruneExpired())
                .handler(column -> pruneExpired())
                .constraint(Constraint.executable(HOST_CONNECTION_TEMPLATE, PRUNE_EXPIRED))
                .build());
        pruneActions.add(new ColumnAction.Builder<Host>(Ids.HOST_PRUNE_DISCONNECTED)
                .title(resources.constants().pruneDisconnected())
                .handler(column -> pruneDisconnected())
                .constraint(Constraint.executable(HOST_CONNECTION_TEMPLATE, PRUNE_DISCONNECTED))
                .build());
        addColumnActions(Ids.HOST_PRUNE_ACTIONS, pfIcon("remove"), resources.constants().prune(), pruneActions);

        ItemsProvider<Host> itemsProvider = (context, callback) -> series(new FlowContext(progress.get()),
                hosts(environment, dispatcher))
                .subscribe(new Outcome<FlowContext>() {
                    @Override
                    public void onError(FlowContext context, Throwable error) {
                        callback.onFailure(error);
                    }

                    @Override
                    public void onSuccess(FlowContext context) {
                        List<Host> hosts = context.get(TopologyTasks.HOSTS);
                        callback.onSuccess(hosts);

                        // Restore pending visualization
                        hosts.stream()
                                .filter(hostActions::isPending)
                                .forEach(host -> ItemMonitor.startProgress(Ids.host(host.getAddressName())));
                    }
                });
        setItemsProvider(itemsProvider);

        setBreadcrumbItemsProvider((context, callback) -> itemsProvider.get(context, new AsyncCallback<List<Host>>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(List<Host> result) {
                // only connected hosts which are not booting please!
                callback.onSuccess(result.stream()
                        .filter(Host::isAlive)
                        .collect(toList()));
            }
        }));

        setItemRenderer(item -> new HostDisplay(item, hostActions, resources) {
            @Override
            public String nextColumn() {
                return item.isAlive() ? SERVER : null;
            }

            @Override
            public List<ItemAction<Host>> actions() {
                if (item.isAlive()) {
                    PlaceRequest placeRequest = new PlaceRequest.Builder()
                            .nameToken(NameTokens.HOST_CONFIGURATION)
                            .with(HOST, item.getAddressName()).build();
                    List<ItemAction<Host>> actions = new ArrayList<>();
                    actions.add(itemActionFactory.viewAndMonitor(Ids.host(item.getAddressName()), placeRequest));
                    if (!hostActions.isPending(item)) {
                        if (ManagementModel.supportsConfigurationChanges(item.getManagementVersion())) {
                            PlaceRequest ccPlaceRequest = new PlaceRequest.Builder()
                                    .nameToken(NameTokens.CONFIGURATION_CHANGES)
                                    .with(HOST, item.getAddressName())
                                    .build();
                            actions.add(itemActionFactory.placeRequest(resources.constants().configurationChanges(),
                                    ccPlaceRequest,
                                    Constraint.executable(hostTemplate(item).append(CONFIGURATION_CHANGES_ADDRESS),
                                            ADD)));
                        }
                        // TODO Add additional operations like :reload(admin-mode=true), :clean-obsolete-content or :take-snapshot
                        actions.add(ItemAction.separator());
                        actions.add(new ItemAction.Builder<Host>()
                                .title(resources.constants().reload())
                                .handler(hostActions::reload)
                                .constraint(Constraint.executable(hostTemplate(item), RELOAD))
                                .build());
                        actions.add(new ItemAction.Builder<Host>()
                                .title(resources.constants().restart())
                                .handler(hostActions::restart)
                                .constraint(Constraint.executable(hostTemplate(item), SHUTDOWN))
                                .build());
                    }
                    return actions;
                } else {
                    return emptyList();
                }
            }
        });

        eventBus.addHandler(HostActionEvent.getType(), this);
        eventBus.addHandler(HostResultEvent.getType(), this);
    }

    @Override
    public void onHostAction(HostActionEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.startProgress(Ids.host(host.getAddressName()));
            event.getServers().forEach(server -> ItemMonitor.startProgress(server.getId()));
        }
    }

    @Override
    public void onHostResult(HostResultEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.stopProgress(Ids.host(host.getAddressName()));
            event.getServers().forEach(server -> ItemMonitor.stopProgress(server.getId()));
            refresh(RESTORE_SELECTION);
        }
    }

    private void pruneExpired() {
        DialogFactory.showConfirmation(resources.constants().pruneExpired(),
                resources.messages().pruneExpiredQuestion(),
                () -> prune(PRUNE_EXPIRED));
    }

    private void pruneDisconnected() {
        DialogFactory.showConfirmation(resources.constants().pruneDisconnected(),
                resources.messages().pruneDisconnectedQuestion(),
                () -> prune(PRUNE_DISCONNECTED));
    }

    private void prune(String operation) {
        ResourceAddress address = new ResourceAddress().add(CORE_SERVICE, MANAGEMENT);
        crud.readChildren(address, HOST_CONNECTION, children -> {
            List<Operation> operations = children.stream()
                    .map(property -> {
                        ResourceAddress hcAddress = HOST_CONNECTION_TEMPLATE.resolve(statementContext,
                                property.getName());
                        return new Operation.Builder(hcAddress, operation).build();
                    })
                    .collect(toList());
            dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                MessageEvent.fire(eventBus, Message.success(resources.messages().pruneSuccessful()));
                refresh(RESTORE_SELECTION);
            });
        });
    }
}
