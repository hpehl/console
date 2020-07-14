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
package org.jboss.hal.ballroom;

import com.google.common.collect.Iterables;
import org.gwtproject.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.Elements;
import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * Element to be used when a view is empty because no objects exists and you want to guide the user to perform specific
 * actions.
 * <p>
 * {@linkplain Constraint Constraints} for the primary and secondary actions are encoded as {@code data-constraint}
 * attributes. Please make sure to call {@link ElementGuard#processElements(AuthorisationDecision,
 * String)} when the empty state element is added to the DOM.
 *
 * @see <a href="https://www.patternfly.org/pattern-library/communication/empty-state/">https://www.patternfly.org/pattern-library/communication/empty-state/</a>
 */
public class EmptyState implements IsElement<HTMLElement> {

    private final HTMLElement root;
    private final HTMLElement icon;
    private final HTMLElement header;
    private final HTMLElement paragraphsDiv;
    private final HTMLElement primaryActionDiv;

    private EmptyState(Builder builder) {
        HtmlContentBuilder<HTMLDivElement> rb = div().id(builder.id).css(blankSlatePf);
        if (builder.icon != null) {
            rb.add(div().css(blankSlatePfIcon).add(icon = i().css(builder.icon).element()).element());
        } else {
            icon = null;
        }
        rb.add(header = h(1).textContent(builder.title).element());
        rb.add(paragraphsDiv = div().element());
        for (HTMLElement element : builder.elements) {
            paragraphsDiv.appendChild(element);
        }
        rb.add(primaryActionDiv = div().css(blankSlatePfMainAction).element());
        if (builder.primaryAction != null) {
            if (builder.primaryAction.constraint != null) {
                primaryActionDiv.dataset.set(UIConstants.CONSTRAINT, builder.primaryAction.constraint.data());
            }
            primaryActionDiv.appendChild(button()
                    .css(btn, btnPrimary, btnLg)
                    .textContent(builder.primaryAction.title)
                    .on(click, event -> builder.primaryAction.callback.execute()).element());
        }
        HTMLElement secondaryActionsDiv;
        rb.add(secondaryActionsDiv = div().css(blankSlatePfSecondaryAction).element());
        if (!builder.secondaryActions.isEmpty()) {
            for (Action a : builder.secondaryActions) {
                HtmlContentBuilder<HTMLButtonElement> bb = button()
                        .css(btn, btnDefault)
                        .textContent(a.title)
                        .on(click, event -> a.callback.execute());
                if (a.constraint != null) {
                    bb.data(UIConstants.CONSTRAINT, a.constraint.data());
                }
                secondaryActionsDiv.appendChild(bb.element());
            }
        }
        root = rb.element();
        Elements.setVisible(primaryActionDiv, builder.primaryAction != null);
        Elements.setVisible(secondaryActionsDiv, !builder.secondaryActions.isEmpty());
    }

    public void setIcon(String icon) {
        if (this.icon != null) {
            this.icon.className = icon;
        }
    }

    public void setHeader(String header) {
        this.header.textContent = header;
    }

    public void setDescription(SafeHtml description) {
        Elements.removeChildrenFrom(paragraphsDiv);
        if (description != null) {
            paragraphsDiv.appendChild(p().innerHtml(description).element());
        }
    }

    public void setPrimaryAction(String title, Callback callback) {
        Elements.removeChildrenFrom(primaryActionDiv);
        HTMLElement element = button()
                .css(btn, btnPrimary, btnLg).on(click, event -> callback.execute())
                .textContent(title).element();
        primaryActionDiv.appendChild(element);
        Elements.setVisible(primaryActionDiv, true);
    }

    public void showPrimaryAction(boolean visible) {
        Elements.setVisible(primaryActionDiv, visible);
    }

    @Override
    public HTMLElement element() {
        return root;
    }


    private static class Action {

        public final String title;
        public final Callback callback;
        private final Constraint constraint;

        Action(String title, Callback callback, Constraint constraint) {
            this.title = title;
            this.callback = callback;
            this.constraint = constraint;
        }
    }


    public static class Builder {

        private final String id;
        private final String title;
        private final List<HTMLElement> elements;
        private final List<Action> secondaryActions;
        private String icon;
        private Action primaryAction;

        public Builder(String id, String title) {
            this.id = id;
            this.title = title;
            this.elements = new ArrayList<>();
            this.secondaryActions = new ArrayList<>();
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder description(String description) {
            elements.add(p().textContent(description).element());
            return this;
        }

        public Builder description(SafeHtml description) {
            elements.add(p().innerHtml(description).element());
            return this;
        }

        public Builder add(HTMLElement element) {
            elements.add(element);
            return this;
        }

        public Builder addAll(Iterable<HTMLElement> elements) {
            Iterables.addAll(this.elements, elements);
            return this;
        }

        public Builder primaryAction(String title, Callback callback) {
            return primaryAction(title, callback, null);
        }

        public Builder primaryAction(String title, Callback callback, Constraint constraint) {
            this.primaryAction = new Action(title, callback, constraint);
            return this;
        }

        public Builder secondaryAction(String title, Callback callback) {
            return secondaryAction(title, callback, null);
        }

        public Builder secondaryAction(String title, Callback callback, Constraint constraint) {
            this.secondaryActions.add(new Action(title, callback, constraint));
            return this;
        }

        public EmptyState build() {
            return new EmptyState(this);
        }
    }
}
