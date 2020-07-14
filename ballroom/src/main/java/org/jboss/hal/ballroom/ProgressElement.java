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
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.flow.Progress;

import static elemental2.dom.DomGlobal.setTimeout;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.MEDIUM_TIMEOUT;
import static org.jboss.hal.resources.UIConstants.ROLE;

public class ProgressElement implements IsElement, Progress {

    public enum Size {
        NORMAL(""), SM(progressSm), XS(progressXs);

        final String css;

        Size(String css) {
            this.css = css;
        }
    }


    public enum Label {
        NONE, INLINE, LEFT
    }


    private static final String ARIA_VALUENOW = "aria-valuenow";

    private final boolean reverse;
    private final HTMLElement root;
    private final HTMLElement valueElement;
    private final HTMLElement progressBarElement;

    private int value;
    private int max;
    private boolean determinate;

    @SuppressWarnings("HardCodedStringLiteral")
    public ProgressElement(Size size, Label label, boolean reverse) {
        this.reverse = reverse;

        value = 0;
        max = 100;
        determinate = true;
        root = div().css(progress)
                .add(progressBarElement = div().css(progressBar)
                        .attr(ROLE, "progress-bar") //NON-NLS
                        .aria("valuenow", "0")
                        .aria("valuemin", "0")
                        .aria("valuemax", "100")
                        .add(valueElement = span().innerHtml(SafeHtmlUtils.EMPTY_SAFE_HTML).element()).element())
                .element();

        if (size != Size.NORMAL) {
            valueElement.classList.add(srOnly);
            root.classList.add(size.css);
        }
        if (label == Label.LEFT) {
            root.classList.add(progressLabelLeft);
        }
        Elements.setVisible(root, false);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void reset() {
        reset(0);
    }

    @Override
    public void reset(int mx, String label) {
        value = 0;
        max = mx;
        determinate = max > 1; // if there's just one step, choose none-determinate state

        if (determinate) {
            progressBarElement.classList.remove(progressBarStriped);
            progressBarElement.classList.remove(active);
            progressBarElement.setAttribute(ARIA_VALUENOW, "0");
            if (reverse) {
                progressBarElement.style.width = width("100%"); //NON-NLS
            } else {
                progressBarElement.style.width = width(0);
            }
        } else {
            progressBarElement.classList.add(progressBarStriped);
            progressBarElement.classList.add(active);
            progressBarElement.setAttribute(ARIA_VALUENOW, "100");
            progressBarElement.style.width = width("100%");
        }
        if (label != null) {
            valueElement.textContent = label;
        } else {
            valueElement.textContent = String.valueOf(value);
        }
        Elements.setVisible(root, true);
    }

    @Override
    public void tick(String label) {
        if (determinate) {
            if (value < max) {
                value++;
                double percent = min(round(((double) value / (double) max) * 100.0), 100.0);
                progressBarElement.setAttribute(ARIA_VALUENOW, String.valueOf(percent));
                progressBarElement.style.width = width(reverse ? (100 - percent) + "%" : String.valueOf(percent));
                if (label != null) {
                    valueElement.textContent = label;
                } else {
                    valueElement.textContent = String.valueOf(value);
                }
            }
        }
    }

    @Override
    public void finish() {
        // give the user a chance to see that we're finished
        setTimeout((o) -> Elements.setVisible(root, false), MEDIUM_TIMEOUT);
    }
}
