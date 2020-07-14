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

import org.gwtproject.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.ResponseHeader;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class FilterPresenter
        extends MbuiPresenter<FilterPresenter.MyView, FilterPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public FilterPresenter(EventBus eventBus,
            FilterPresenter.MyView view,
            FilterPresenter.MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return FILTER_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(UNDERTOW)
                .append(Ids.UNDERTOW_SETTINGS, Ids.asId(Names.FILTERS),
                        resources.constants().settings(), Names.FILTERS);
    }

    @Override
    protected void reload() {
        crud.readRecursive(FILTER_TEMPLATE, result -> getView().update(result));
    }

    void addResponseHeader() {
        Metadata metadata = metadataRegistry.lookup(RESPONSE_HEADER_TEMPLATE);
        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNDERTOW_RESPONSE_HEADER_ADD, metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .build();

        List<String> responseHeader = Arrays.stream(ResponseHeader.values())
                .map(ResponseHeader::header)
                .collect(toList());
        form.getFormItem(HEADER_NAME).registerSuggestHandler(new StaticAutoComplete(responseHeader));

        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.RESPONSE_HEADER), form,
                (name, model) -> {
                    //noinspection ConstantConditions
                    SafeHtml successMessage = resources.messages()
                            .addResourceSuccess(Names.RESPONSE_HEADER, model.get(HEADER_NAME).asString());
                    crud.add(name, RESPONSE_HEADER_TEMPLATE, model, successMessage, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveResponseHeader(Form<NamedNode> form, Map<String, Object> changedValues) {
        SafeHtml successMessage = resources.messages()
                .modifyResourceSuccess(Names.RESPONSE_HEADER, form.getModel().get(HEADER_NAME).asString());
        crud.save(form.getModel().getName(), RESPONSE_HEADER_TEMPLATE, changedValues, successMessage, this::reload);
    }

    void resetResponseHeader(Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(RESPONSE_HEADER_TEMPLATE);
        SafeHtml successMessage = resources.messages()
                .resetResourceSuccess(Names.RESPONSE_HEADER, form.getModel().get(HEADER_NAME).asString());
        crud.reset(Names.RESPONSE_HEADER, form.getModel().getName(), RESPONSE_HEADER_TEMPLATE, form, metadata,
                successMessage, new Form.FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(Form<NamedNode> form) {
                        reload();
                    }
                });
    }

    void removeResponseHeader(NamedNode responseHeader) {
        ResourceAddress address = RESPONSE_HEADER_TEMPLATE.resolve(statementContext, responseHeader.getName());
        String name = responseHeader.get(HEADER_NAME).asString();

        DialogFactory.showConfirmation(
                resources.messages().removeConfirmationTitle(Names.RESPONSE_HEADER),
                resources.messages().removeConfirmationQuestion(name),
                () -> {
                    Operation operation = new Operation.Builder(address, REMOVE).build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(getEventBus(), Message.success(
                                resources.messages().removeResourceSuccess(Names.RESPONSE_HEADER, name)));
                        reload();
                    });
                });
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires(FILTER_ADDRESS)
    @NameToken(NameTokens.UNDERTOW_FILTER)
    public interface MyProxy extends ProxyPlace<FilterPresenter> {
    }

    public interface MyView extends MbuiView<FilterPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on
}
