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
package org.jboss.hal.client.configuration.subsystem.ee;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.elemento.Elements.*;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.ee.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.*;

public class EEView extends HalViewImpl implements EEPresenter.MyView {

    // local names
    private static final String DEFAULT_BINDINGS_NAME = "Default Bindings";
    private static final String CONTEXT_SERVICE_NAME = "Context Service";
    private static final String MANAGED_EXECUTOR_NAME = "Executor";
    private static final String MANAGED_EXECUTOR_SCHEDULED_NAME = "Scheduled Executor";
    private static final String SERVICES_NAME = "Services";
    private static final String MANAGED_THREAD_FACTORY_NAME = "Thread Factories";

    private final MetadataRegistry metadataRegistry;
    private final TableButtonFactory tableButtonFactory;
    private final Resources resources;
    private final VerticalNavigation navigation;
    private final Map<String, ModelNodeForm> forms;
    private final Table<ModelNode> globalModulesTable;
    private final Map<String, Table<NamedNode>> tables;

    private EEPresenter presenter;

    @Inject
    public EEView(MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory,
            Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.tableButtonFactory = tableButtonFactory;
        this.resources = resources;

        this.forms = new HashMap<>();
        this.tables = new HashMap<>(4);
        this.navigation = new VerticalNavigation();
        registerAttachable(navigation);

        // ============================================
        // attributes - deployments
        Metadata eeMetadata = metadataRegistry.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);

        ModelNodeForm<ModelNode> eeAttributesForm = new ModelNodeForm.Builder<>(EE_ATTRIBUTES_FORM, eeMetadata)
                .onSave((f, changedValues) -> presenter.save(AddressTemplates.EE_SUBSYSTEM_TEMPLATE, changedValues,
                        eeMetadata, resources.messages()
                                .modifyResourceSuccess(Names.EE, resources.constants().deploymentAttributes())))
                .prepareReset(f -> presenter.resetSingleton(resources.constants().deploymentAttributes(),
                        AddressTemplates.EE_SUBSYSTEM_TEMPLATE, f, eeMetadata))
                .build();
        forms.put(EE_ATTRIBUTES_FORM, eeAttributesForm);
        registerAttachable(eeAttributesForm);

        HTMLElement navigationElement = div()
                .add(h(1).textContent(Names.DEPLOYMENTS))
                .add(p().textContent(eeMetadata.getDescription().getDescription()))
                .add(eeAttributesForm).element();
        navigation.addPrimary(EE_ATTRIBUTES_ITEM, Names.CONFIGURATION, pfIcon("settings"), navigationElement);

        // ============================================
        // global modules
        Metadata globalModulesMetadata = eeMetadata.forComplexAttribute(GLOBAL_MODULES);

        globalModulesTable = new ModelNodeTable.Builder<>(Ids.EE_GLOBAL_MODULES_TABLE, globalModulesMetadata)
                .columns(NAME, "slot", "annotations", "services", "meta-inf")
                .button(tableButtonFactory.add(EE_SUBSYSTEM_TEMPLATE,
                        table -> presenter.launchAddDialogGlobalModule()))
                .button(tableButtonFactory.remove(EE_SUBSYSTEM_TEMPLATE,
                        table -> presenter.removeGlobalModule(table.selectedRow())))
                .build();
        registerAttachable(globalModulesTable);

        navigationElement = div()
                .add(h(1).textContent(Names.GLOBAL_MODULES))
                .add(p().textContent(globalModulesMetadata.getDescription().getDescription()))
                .add(globalModulesTable).element();
        navigation.addPrimary(EE_GLOBAL_MODULES_ITEM, Names.GLOBAL_MODULES, fontAwesome("cubes"), navigationElement);

        // ============================================
        // service=default-bindings
        Metadata defaultBindingsMetadata = metadataRegistry.lookup(AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE);

        ModelNodeForm<ModelNode> defaultBindingsForm = new ModelNodeForm.Builder<>(EE_DEFAULT_BINDINGS_FORM,
                defaultBindingsMetadata)
                .onSave((form, changedValues) -> presenter.save(AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE,
                        changedValues, defaultBindingsMetadata,
                        resources.messages().modifyResourceSuccess(Names.EE, DEFAULT_BINDINGS_NAME)))
                .prepareReset(f -> presenter.resetSingleton(DEFAULT_BINDINGS_NAME,
                        AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE, f, defaultBindingsMetadata))
                .build();
        forms.put(EE_DEFAULT_BINDINGS_FORM, defaultBindingsForm);
        registerAttachable(defaultBindingsForm);

        navigationElement = div()
                .add(h(1).textContent(DEFAULT_BINDINGS_NAME))
                .add(p().textContent(defaultBindingsMetadata.getDescription().getDescription()))
                .add(defaultBindingsForm).element();
        navigation.addPrimary(EE_DEFAULT_BINDINGS_ITEM, DEFAULT_BINDINGS_NAME, fontAwesome("link"),
                navigationElement);

        // ============================================
        // services
        navigation.addPrimary(EE_SERVICES_ITEM, SERVICES_NAME, pfIcon("service"));

        navigation.addSecondary(EE_SERVICES_ITEM, EE_CONTEXT_SERVICE, CONTEXT_SERVICE_NAME,
                buildServicePanel(EE_CONTEXT_SERVICE, CONTEXT_SERVICE_TEMPLATE, CONTEXT_SERVICE_NAME));
        navigation.addSecondary(EE_SERVICES_ITEM, EE_MANAGED_EXECUTOR, MANAGED_EXECUTOR_NAME,
                buildServicePanel(EE_MANAGED_EXECUTOR, MANAGED_EXECUTOR_TEMPLATE, MANAGED_EXECUTOR_NAME));
        navigation.addSecondary(EE_SERVICES_ITEM, EE_MANAGED_EXECUTOR_SCHEDULED, MANAGED_EXECUTOR_SCHEDULED_NAME,
                buildServicePanel(EE_MANAGED_EXECUTOR_SCHEDULED, MANAGED_EXECUTOR_SCHEDULED_TEMPLATE,
                        MANAGED_EXECUTOR_SCHEDULED_NAME));
        navigation.addSecondary(EE_SERVICES_ITEM, EE_MANAGED_THREAD_FACTORY, MANAGED_THREAD_FACTORY_NAME,
                buildServicePanel(EE_MANAGED_THREAD_FACTORY, MANAGED_THREAD_FACTORY_TEMPLATE,
                        MANAGED_THREAD_FACTORY_NAME));

        // ============================================
        // main layout
        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void attach() {
        super.attach();
        tables.forEach((id, table) -> {
            if (forms.containsKey(id)) {
                table.bindForm(forms.get(id));
            }
        });
    }

    @Override
    public void setPresenter(EEPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(ModelNode eeData) {
        // update the attributes - deployments tab
        Form<ModelNode> formDeployments = forms.get(EE_ATTRIBUTES_FORM);
        formDeployments.view(eeData);

        // update the global modules tab
        globalModulesTable.clear();
        if (eeData.hasDefined(GLOBAL_MODULES)) {
            List<ModelNode> globalModulesList = eeData.get(GLOBAL_MODULES).asList();
            globalModulesTable.update(globalModulesList);
        }

        // update the default-bindings tab
        if (eeData.hasDefined(SERVICE)) {
            ModelNode defaultBindings = eeData.get(SERVICE).get(ModelDescriptionConstants.DEFAULT_BINDINGS);
            Form<ModelNode> formDefaultBindings = forms.get(EE_DEFAULT_BINDINGS_FORM);
            formDefaultBindings.view(defaultBindings);
        }
        // update the context-service table
        update(eeData, CONTEXT_SERVICE, EE_CONTEXT_SERVICE);

        // update the managed-executor-service table
        update(eeData, MANAGED_EXECUTOR_SERVICE, EE_MANAGED_EXECUTOR);

        // update the managed-scheduled-executor-service table
        update(eeData, MANAGED_SCHEDULED_EXECUTOR_SERVICE, EE_MANAGED_EXECUTOR_SCHEDULED);

        // update the managed-thread-factory table
        update(eeData, MANAGED_THREAD_FACTORY, EE_MANAGED_THREAD_FACTORY);

    }

    @SuppressWarnings("unchecked")
    private void update(ModelNode eeData, String resourceType, String navigationId) {
        if (eeData.hasDefined(resourceType)) {
            List<NamedNode> models = asNamedNodes(eeData.get(resourceType).asPropertyList());
            Form form = forms.get(resourceType);
            form.clear();
            Table<NamedNode> table = tables.get(resourceType);
            table.update(models);
            navigation.updateBadge(navigationId, models.size());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private HTMLElement buildServicePanel(String baseId, AddressTemplate template, String type) {

        Metadata metadata = metadataRegistry.lookup(template);

        Table<NamedNode> table = new ModelNodeTable.Builder<NamedNode>(Ids.build(baseId, Ids.TABLE),
                metadata)
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .button(tableButtonFactory.add(Ids.build(baseId, Ids.ADD), type, template,
                        (name, address) -> presenter.reload()))
                .button(tableButtonFactory.remove(type, template, (api) -> api.selectedRow().getName(),
                        () -> presenter.reload()))

                .build();
        registerAttachable(table);
        tables.put(template.lastName(), table);

        ModelNodeForm<NamedNode> form = new ModelNodeForm.Builder<NamedNode>(Ids.build(baseId, Ids.FORM),
                metadata)
                .onSave((f, changedValues) -> {
                    AddressTemplate fullyQualified = template.replaceWildcards(table.selectedRow().getName());
                    presenter.save(fullyQualified, changedValues, metadata,
                            resources.messages().modifyResourceSuccess(Names.EE, template.lastName()));
                })
                .prepareReset(f -> {
                    String name = table.selectedRow().getName();
                    AddressTemplate fullyQualified = template.replaceWildcards(name);
                    presenter.reset(type, name, fullyQualified, f, metadata,
                            resources.messages().modifyResourceSuccess(Names.EE, template.lastName()));
                })
                .build();

        forms.put(template.lastName(), form);
        registerAttachable(form);

        return section()
                .add(h(1).textContent(type))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();
    }
}
