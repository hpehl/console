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
package org.jboss.hal.core.modelbrowser;

import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.elemento.Elements.div;

class CreateSingletonStep extends WizardStep<SingletonContext, SingletonState> {

    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final EventBus eventBus;
    private final Resources resources;
    private final HTMLElement root;
    private Form<ModelNode> form;

    CreateSingletonStep(Node<Context> parent, MetadataProcessor metadataProcessor,
            Provider<Progress> progress, EventBus eventBus, Resources resources) {
        super(resources.messages().addResourceTitle(parent.text));
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.eventBus = eventBus;
        this.resources = resources;
        this.root = div().element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(SingletonContext context) {
        Elements.removeChildrenFrom(root);
        Node<Context> parent = wizard().getContext().parent;
        ResourceAddress singletonAddress = parent.data.getAddress().getParent()
                .add(parent.text, wizard().getContext().singleton);
        AddressTemplate template = ModelBrowser.asGenericTemplate(parent, singletonAddress);
        metadataProcessor.lookup(template, progress.get(), new MetadataProcessor.MetadataCallback() {
            @Override
            public void onError(Throwable error) {
                MessageEvent.fire(eventBus, Message.error(resources.messages().metadataError(), error.getMessage()));
            }

            @Override
            public void onMetadata(Metadata metadata) {
                String id = Ids.build(Ids.MODEL_BROWSER_CREATE_SINGLETON_FORM, Ids.FORM);
                form = new ModelNodeForm.Builder<>(id, metadata)
                        .fromRequestProperties()
                        .onSave((f, changedValues) -> wizard().getContext().modelNode = f.getModel())
                        .build();
                root.appendChild(form.element());
                PatternFly.initComponents();
                form.attach();
                form.edit(new ModelNode());
            }
        });
    }

    @Override
    protected boolean onNext(SingletonContext context) {
        return form != null && form.save();
    }
}
