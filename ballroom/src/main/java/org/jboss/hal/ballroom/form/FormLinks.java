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
package org.jboss.hal.ballroom.form;

import com.google.gwt.core.client.GWT;
import org.gwtproject.safehtml.shared.SafeHtml;
import elemental2.dom.*;
import org.jboss.elemento.Elements;
import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.HASH;

/**
 * Links for commons form {@linkplain Form.Operation operations} placed above the actual form. Depending on the
 * {@linkplain Form.State state} links are displayed or hidden.
 * <p>
 * The following links are part of this element:
 * <ol>
 * <li>Edit: Visible in the {@linkplain Form.State#READONLY read-only} state. Switches to the {@linkplain
 * Form.State#EDITING editing} state</li>
 * <li>Reset: Visible in the {@linkplain Form.State#READONLY read-only} state. Resets the form's model. If a {@link
 * org.jboss.hal.ballroom.form.Form.PrepareReset} callback is defined, the callback is called. Otherwise {@link
 * Form#reset()} is called.</li>
 * <li>Remove: Visible in the {@linkplain Form.State#READONLY read-only} state. Removes the form's model. If a {@link
 * org.jboss.hal.ballroom.form.Form.PrepareRemove} callback is defined, the callback is called, otherwise {@link
 * Form#remove()} is called.</li>
 * <li>Help: Visible in the {@linkplain Form.State#READONLY read-only} and the {@linkplain Form.State#EDITING editing}
 * states. Provides access to the help texts.</li>
 * </ol>
 */
public class FormLinks<T> implements IsElement {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final LinkedHashMap<String, SafeHtml> helpTexts;
    private final HTMLElement root;
    private HTMLElement editLink;
    private HTMLElement resetLink;
    private HTMLElement removeLink;
    private HTMLElement helpLink;

    FormLinks(AbstractForm<T> form,
            StateMachine stateMachine,
            LinkedHashMap<String, SafeHtml> helpTexts,
            EventCallbackFn<MouseEvent> onEdit,
            EventCallbackFn<MouseEvent> onReset,
            EventCallbackFn<MouseEvent> onRemove) {
        this.helpTexts = helpTexts;

        String linksId = Ids.build(form.getId(), "links");
        String helpId = Ids.build(form.getId(), "help");

        HTMLUListElement links;
        HTMLDivElement helpContent;
        HtmlContentBuilder<HTMLDivElement> rootBuilder = div().css(CSS.form, formHorizontal)
                .add(links = ul().id(linksId).css(formLinks, clearfix).element())
                .add(div().id(helpId).css(formHelpContent, collapse)
                        .add(helpContent = div().element()));

        if (stateMachine.supports(EDIT)) {
            editLink = link(EDIT, CONSTANTS.edit(), pfIcon("edit"), onEdit);
            links.appendChild(editLink);
        }
        if (stateMachine.supports(RESET)) {
            resetLink = link(RESET, CONSTANTS.reset(), fontAwesome("undo"), onReset);
            resetLink.dataset.set(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            resetLink.dataset.set(UIConstants.PLACEMENT, "right"); //NON-NLS
            resetLink.title = CONSTANTS.formResetDesc();
            links.appendChild(resetLink);
        }
        if (stateMachine.supports(REMOVE)) {
            removeLink = link(REMOVE, CONSTANTS.remove(), CSS.pfIcon("remove"), onRemove);
            links.appendChild(removeLink);
        }
        if (!helpTexts.isEmpty()) {
            helpLink = li()
                    .add(a(HASH + helpId)
                            .data(UIConstants.TOGGLE, UIConstants.COLLAPSE)
                            .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                            .aria(UIConstants.CONTROLS, helpId)
                            .add(i().css(pfIcon("help")))
                            .add(span().css(formLinkLabel).textContent(CONSTANTS.help()))).element();
            for (Map.Entry<String, SafeHtml> entry : helpTexts.entrySet()) {
                helpContent.appendChild(help(entry.getKey(), entry.getValue()));
            }
            links.appendChild(helpLink);
        }
        root = rootBuilder.element();
    }

    private HTMLLIElement link(Form.Operation operation, String text, String css, EventCallbackFn<MouseEvent> onclick) {
        return li()
                .add(a().css(clickable)
                        .data("operation", operation.name().toLowerCase())
                        .on(click, onclick)
                        .add(i().css(css))
                        .add(span().css(formLinkLabel).textContent(text))).element();
    }

    private HTMLDivElement help(String label, SafeHtml description) {
        return div().css(formGroup)
                .add(label()
                        .css(controlLabel, halFormLabel)
                        .textContent(label))
                .add(div().css(halFormInput)
                        .add(p().css(formControlStatic).innerHtml(description))).element();
    }

    @Override
    public HTMLElement element() {
        if (editLink == null && resetLink == null && removeLink == null && helpTexts.isEmpty()) {
            Elements.setVisible(root, false);
        }
        return root;
    }

    public void setVisible(boolean edit, boolean reset, boolean remove, boolean help) {
        Elements.setVisible(editLink, edit);
        Elements.setVisible(resetLink, reset);
        Elements.setVisible(removeLink, remove);
        Elements.setVisible(helpLink, help && !helpTexts.isEmpty());

        Elements.setVisible(root, Elements.isVisible(editLink) ||
                Elements.isVisible(resetLink) ||
                Elements.isVisible(removeLink) ||
                Elements.isVisible(helpLink));
    }

    public void setVisible(Form.Operation operation, boolean visible) {
        switch (operation) {
            case EDIT:
                Elements.setVisible(editLink, visible);
                break;
            case RESET:
                Elements.setVisible(resetLink, visible);
                break;
            case REMOVE:
                Elements.setVisible(removeLink, visible);
                break;
            default:
                break;
        }
    }
}
