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

import elemental2.dom.HTMLElement;
import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * Breadcrumb element as specified by <a href="https://www.patternfly.org/pattern-library/widgets/#breadcrumbs">PatternFly</a>
 *
 * @see <a href="https://www.patternfly.org/pattern-library/widgets/#breadcrumbs>https://www.patternfly.org/pattern-library/widgets/#breadcrumbs</a>
 */
public class Breadcrumb implements IsElement<HTMLElement> {

    private final HTMLElement root;

    @SuppressWarnings("WeakerAccess")
    public Breadcrumb() {
        root = ol().css(breadcrumb).element();
    }

    public void clear() {
        Elements.removeChildrenFrom(root);
    }

    public Breadcrumb append(String segment, SegmentHandler handler) {
        root.appendChild(li()
                .add(a().css(clickable)
                        .textContent(segment)
                        .on(click, e -> handler.onClick())).element());
        return this;
    }

    public Breadcrumb append(String segment) {
        root.appendChild(li().css(active).textContent(segment).element());
        return this;
    }

    @Override
    public HTMLElement element() {
        return root;
    }


    @FunctionalInterface
    public interface SegmentHandler {

        void onClick();
    }
}
