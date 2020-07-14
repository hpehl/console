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
package org.jboss.hal.client.configuration.subsystem.elytron;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;
import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;

import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.HASH;

class PolicyElement implements IsElement<HTMLElement>, Attachable, HasPresenter<OtherSettingsPresenter> {

    private final Metadata metadata;
    private final Resources resources;
    private final HTMLElement header;
    private final HTMLElement description;
    private final EmptyState emptyState;
    private final Form<ModelNode> customPolicyForm;
    private final Form<ModelNode> jaccPolicyForm;
    private final HTMLElement root;
    private OtherSettingsPresenter presenter;
    private String policyName;

    PolicyElement(Metadata metadata, Resources resources) {
        this.metadata = metadata;
        this.resources = resources;

        emptyState = new EmptyState.Builder(Ids.ELYTRON_CUSTOM_POLICY_EMPTY, resources.constants().noPolicy())
                .add(div().innerHtml(resources.messages().noPolicy()).element())
                .secondaryAction(resources.messages().addResourceTitle(Names.CUSTOM_POLICY),
                        () -> presenter.addPolicy(CUSTOM_POLICY, Names.CUSTOM_POLICY))
                .secondaryAction(resources.messages().addResourceTitle(Names.JACC_POLICY),
                        () -> presenter.addPolicy(JACC_POLICY, Names.JACC_POLICY))
                .build();

        // custom policy
        Metadata customPolicyMetadata = metadata.forComplexAttribute(ModelDescriptionConstants.CUSTOM_POLICY);
        customPolicyForm = new ModelNodeForm.Builder<>(Ids.ELYTRON_CUSTOM_POLICY_FORM, customPolicyMetadata)
                .onSave(((form, changedValues) -> presenter.savePolicy(policyName, CUSTOM_POLICY, Names.CUSTOM_POLICY,
                        changedValues)))
                .unsorted()
                .build();
        injectRemove(customPolicyForm, () -> presenter.removePolicy(policyName, Names.CUSTOM_POLICY));
        Elements.setVisible(customPolicyForm.element(), false);

        // jacc policy
        Metadata jaccPolicyMetadata = metadata.forComplexAttribute(ModelDescriptionConstants.JACC_POLICY);
        jaccPolicyForm = new ModelNodeForm.Builder<>(Ids.ELYTRON_JACC_POLICY_FORM, jaccPolicyMetadata)
                .onSave(((form, changedValues) -> presenter.savePolicy(policyName, JACC_POLICY, Names.JACC_POLICY,
                        changedValues)))
                .prepareReset(form -> presenter.resetPolicy(policyName, JACC_POLICY, Names.JACC_POLICY, form))
                .unsorted()
                .build();
        injectRemove(jaccPolicyForm, () -> presenter.removePolicy(policyName, Names.JACC_POLICY));
        Elements.setVisible(jaccPolicyForm.element(), false);

        root = section()
                .add(header = h(1).element())
                .add(description = p().element())
                .add(emptyState)
                .add(customPolicyForm)
                .add(jaccPolicyForm).element();
        Elements.setVisible(description, false);
    }

    private void injectRemove(Form<ModelNode> form, Callback callback) {
        // hacky way to inject the remove link into the form tools,  depends on FormLink internals!
        String linksId = Ids.build(form.getId(), "links");
        Element formLinks = form.element().querySelector(HASH + linksId);
        if (formLinks != null) {
            HTMLLIElement removeLink = li().add(a().css(clickable).on(click, event -> callback.execute())
                    .add(i().css(pfIcon("remove")))
                    .data(OPERATION, REMOVE)
                    .add(span().css(formLinkLabel).textContent(resources.constants().remove()))).element();
            formLinks.insertBefore(removeLink, formLinks.lastElementChild);
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        customPolicyForm.attach();
        jaccPolicyForm.attach();
    }

    @Override
    public void setPresenter(OtherSettingsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(NamedNode policy) {
        Elements.setVisible(emptyState.element(), policy == null);
        if (policy == null) {
            policyName = null;
            Elements.setVisible(header, false);
            Elements.setVisible(description, false);
            Elements.setVisible(customPolicyForm.element(), false);
            Elements.setVisible(jaccPolicyForm.element(), false);

        } else {
            policyName = policy.getName();
            Elements.setVisible(header, true);
            Elements.setVisible(description, true);
            Elements.setVisible(customPolicyForm.element(), policy.hasDefined(CUSTOM_POLICY));
            Elements.setVisible(jaccPolicyForm.element(), policy.hasDefined(JACC_POLICY));
            if (policy.hasDefined(CUSTOM_POLICY)) {
                header.textContent = Names.CUSTOM_POLICY;
                description.textContent = metadata.forComplexAttribute(CUSTOM_POLICY).getDescription().getDescription();
                customPolicyForm.view(policy.get(CUSTOM_POLICY));

            } else if (policy.hasDefined(JACC_POLICY)) {
                header.textContent = Names.JACC_POLICY;
                description.textContent = metadata.forComplexAttribute(JACC_POLICY).getDescription().getDescription();
                jaccPolicyForm.view(policy.get(JACC_POLICY));
            }
        }
    }
}
