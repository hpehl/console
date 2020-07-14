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

import com.google.common.base.Joiner;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.*;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

@AsyncColumn(Ids.MAIL_SESSION)
@Requires({MAIL_ADDRESS, MAIL_SESSION_ADDRESS, SERVER_ADDRESS})
public class MailSessionColumn extends FinderColumn<MailSession> {

    @Inject
    protected MailSessionColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            EventBus eventBus,
            Dispatcher dispatcher,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry,
            Places places,
            Resources resources) {

        super(new Builder<MailSession>(finder, Ids.MAIL_SESSION, Names.MAIL_SESSION)
                .withFilter()
                .filterDescription(resources.messages().mailColumnFilterDescription())
                .useFirstActionAsBreadcrumbHandler());

        setItemsProvider((context, callback) -> {
            ResourceAddress mailAddress = MAIL_TEMPLATE.resolve(statementContext);
            Operation op = new Operation.Builder(mailAddress, READ_RESOURCE_OPERATION)
                    .param(RECURSIVE, true).build();

            dispatcher.execute(op, result -> {
                List<MailSession> mailSessions = result.get(MAIL_SESSION).asPropertyList().stream()
                        .map(MailSession::new).collect(toList());
                callback.onSuccess(mailSessions);
            });
        });

        addColumnAction(columnActionFactory.add(Ids.MAIL_SESSION_ADD, Names.MAIL_SESSION, MAIL_SESSION_TEMPLATE,
                column -> {
                    Metadata metadata = metadataRegistry.lookup(AddressTemplates.MAIL_SESSION_TEMPLATE);
                    AddResourceDialog dialog = new AddResourceDialog(Ids.MAIL_SESSION_DIALOG,
                            resources.messages().addResourceTitle(Names.MAIL_SESSION), metadata,
                            Arrays.asList(JNDI_NAME, FROM, "debug"), //NON-NLS
                            (name, modelNode) -> {
                                if (modelNode != null) {
                                    ResourceAddress address = AddressTemplates.MAIL_SESSION_TEMPLATE
                                            .resolve(statementContext, name);
                                    Operation operation = new Operation.Builder(address, ADD)
                                            .param(MAIL_SESSION, name)
                                            .payload(modelNode)
                                            .build();
                                    dispatcher.execute(operation, result -> {
                                        MessageEvent.fire(eventBus,
                                                Message.success(resources.messages()
                                                        .addResourceSuccess(Names.MAIL_SESSION, name)));
                                        column.refresh(Ids.mailSession(name));
                                    });
                                }
                            });
                    dialog.getForm().<String>getFormItem(NAME).addValidationHandler(createUniqueValidation());
                    dialog.show();
                }));
        addColumnAction(columnActionFactory.refresh(Ids.MAIL_SESSION_REFRESH));

        setItemRenderer(mailSession -> new ItemDisplay<MailSession>() {
            @Override
            public String getId() {
                return Ids.mailSession(mailSession.getName());
            }

            @Override
            public String getTitle() {
                return mailSession.getName();
            }

            @Override
            public HTMLElement element() {
                if (!mailSession.getServers().isEmpty()) {
                    return ItemDisplay
                            .withSubtitle(mailSession.getName(), Joiner.on(", ").join(mailSession.getServers()));
                }
                return null;
            }

            @Override
            public String getFilterData() {
                List<String> data = new ArrayList<>();
                data.add(mailSession.getName());
                data.addAll(mailSession.getServers());
                return String.join(" ", data);
            }

            @Override
            public List<ItemAction<MailSession>> actions() {
                List<ItemAction<MailSession>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(places.selectedProfile(NameTokens.MAIL_SESSION)
                        .with(NAME, mailSession.getName()).build()));
                actions.add(itemActionFactory.remove(Names.MAIL_SESSION, mailSession.getName(),
                        AddressTemplates.MAIL_SESSION_TEMPLATE, MailSessionColumn.this));
                return actions;
            }
        });

        setPreviewCallback(mailSession -> new MailSessionPreview(mailSession, resources));
    }
}
