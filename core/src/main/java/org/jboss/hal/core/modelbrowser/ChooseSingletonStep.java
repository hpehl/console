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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.elemento.InputType;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.click;

class ChooseSingletonStep extends WizardStep<SingletonContext, SingletonState> {

    private final HTMLElement root;
    private final HTMLInputElement firstRadio;

    ChooseSingletonStep(Node<Context> parent, List<String> children, Resources resources) {
        super(resources.constants().chooseSingleton());

        this.root = div().element();
        SortedSet<String> singletons = new TreeSet<>(parent.data.getSingletons());
        SortedSet<String> existing = new TreeSet<>(children);
        singletons.removeAll(existing);

        for (String singleton : singletons) {
            HTMLInputElement input;
            root.appendChild(div().css(CSS.radio)
                    .add(label()
                            .add(input = input(InputType.radio)
                                    .attr("name", "singleton") //NON-NLS
                                    .attr("value", singleton).element())
                            .add(span().textContent(singleton))).element());
            bind(input, click, event -> wizard().getContext().singleton = input.value);
        }

        firstRadio = (HTMLInputElement) root.querySelector("input[type=radio]"); //NON-NLS
        firstRadio.checked = true;
    }

    @Override
    public void reset(SingletonContext context) {
        context.singleton = firstRadio.value;
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
