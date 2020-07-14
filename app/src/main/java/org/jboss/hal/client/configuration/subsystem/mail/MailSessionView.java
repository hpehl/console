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
package org.jboss.hal.client.configuration.subsystem.mail;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.elytron.CredentialReference;
import org.jboss.hal.core.elytron.CredentialReference.AlternativeValidation;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.elemento.Elements.*;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Implementation note: Not based on MBUI XML due to special handling of mail servers. Each mail session can define
 * three different servers: SMTP, IMAP and POP3.
 */
public class MailSessionView extends HalViewImpl implements MailSessionPresenter.MyView {

    private final Form<MailSession> mailSessionForm;
    private final Table<NamedNode> serverTable;
    private final Form<NamedNode> serverForm;
    private Form<ModelNode> crForm;

    private MailSessionPresenter presenter;

    @Inject
    public MailSessionView(MetadataRegistry metadataRegistry,
            Dispatcher dispatcher,
            StatementContext statementContext,
            TableButtonFactory tableButtonFactory,
            CredentialReference cr,
            Resources resources) {

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        // ============================================
        // mail-session attributes
        Metadata mailSessionMetadata = metadataRegistry.lookup(MAIL_SESSION_TEMPLATE);
        mailSessionForm = new ModelNodeForm.Builder<MailSession>(
                Ids.MAIL_SESSION_FORM, mailSessionMetadata)
                .onSave((form, changedValues) -> presenter.saveMailSession(changedValues))
                .prepareReset(form -> presenter.resetMailSession(form))
                .build();
        registerAttachable(mailSessionForm);

        HTMLElement mailSessionElement = section()
                .add(p().textContent(mailSessionMetadata.getDescription().getDescription()))
                .add(mailSessionForm.element()).element();
        navigation.addPrimary(Ids.MAIL_SESSION_ITEM, resources.constants().attributes(),
                fontAwesome("envelope"), mailSessionElement);

        // ============================================
        // mail-server: smtp, pop, imap
        Metadata serverMetadata = metadataRegistry.lookup(SERVER_TEMPLATE);

        //noinspection ConstantConditions
        serverTable = new ModelNodeTable.Builder<NamedNode>(Ids.MAIL_SERVER_TABLE, serverMetadata)
                .button(tableButtonFactory.add(SERVER_TEMPLATE, table -> presenter.launchAddServer()))
                .button(tableButtonFactory.remove(SERVER_TEMPLATE,
                        table -> presenter.removeServer(table.selectedRow())))
                .column(new ColumnBuilder<NamedNode>(TYPE, resources.constants().type(),
                        (cell, type, row, meta) -> row.getName().toUpperCase()).build())
                .column(new ColumnBuilder<NamedNode>(OUTBOUND_SOCKET_BINDING_REF, "Outbound Socket Binding", //NON-NLS
                        (cell, type, row, meta) -> row.get(OUTBOUND_SOCKET_BINDING_REF).asString()).build())
                .build();
        registerAttachable(serverTable);

        serverForm = new ModelNodeForm.Builder<NamedNode>(Ids.MAIL_SERVER_FORM, serverMetadata)
                .include(OUTBOUND_SOCKET_BINDING_REF, USERNAME, PASSWORD, "ssl", "tls")
                .unsorted()
                .onSave((f, changedValues) -> presenter.saveServer(f.getModel().getName(), changedValues))
                .prepareReset(f -> presenter.resetServer(f.getModel().getName(), f))
                .build();
        serverForm.addFormValidation(new AlternativeValidation<>(PASSWORD, () -> crForm.getModel(), resources));
        serverForm.getFormItem(OUTBOUND_SOCKET_BINDING_REF).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, SOCKET_BINDING_TEMPLATE));
        registerAttachable(serverForm);

        crForm = cr.form(Ids.MAIL_SERVER, serverMetadata, PASSWORD,
                () -> serverForm.<String>getFormItem(PASSWORD).getValue(),
                () -> presenter.credentialReferenceTemplate(
                        serverTable.hasSelection() ? serverTable.selectedRow().getName() : null),
                () -> presenter.reload());
        registerAttachable(crForm);

        Tabs serverTabs = new Tabs(Ids.MAIL_SERVER_TAB_CONTAINER);
        serverTabs.add(Ids.build(Ids.MAIL_SERVER, SERVER, Ids.TAB), resources.constants().attributes(),
                serverForm.element());
        serverTabs.add(Ids.build(Ids.MAIL_SERVER, CREDENTIAL_REFERENCE, Ids.TAB), Names.CREDENTIAL_REFERENCE,
                crForm.element());

        mailSessionElement = section()
                .add(p().textContent(serverMetadata.getDescription().getDescription()))
                .addAll(serverTable, serverTabs).element();
        navigation.addPrimary(Ids.MAIL_SERVER_ITEM, Names.SERVER, pfIcon("server"), mailSessionElement);

        // ============================================
        // main layout
        initElement(row()
                .add(column()
                        .add(h(1).textContent(Names.MAIL_SESSION))
                        .addAll(navigation.panes())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void attach() {
        super.attach();
        serverTable.bindForm(serverForm);
        serverTable.onSelectionChange(table -> {
            if (table.hasSelection()) {
                crForm.view(failSafeGet(table.selectedRow(), CREDENTIAL_REFERENCE));
            } else {
                crForm.clear();
            }
        });
    }

    @Override
    public void setPresenter(MailSessionPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(MailSession mailSession) {
        mailSessionForm.view(mailSession);

        List<NamedNode> servers = asNamedNodes(failSafePropertyList(mailSession, SERVER));
        serverForm.clear();
        crForm.clear();
        serverTable.update(servers);
        serverTable.enableButton(0, servers.size() != 3);
    }

    @Override
    public void select(NamedNode mailServer) {
        serverTable.select(mailServer);
    }
}
