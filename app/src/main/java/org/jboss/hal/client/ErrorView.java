/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client;

import elemental2.dom.HTMLDivElement;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.elemento.Elements.nav;
import static org.jboss.elemento.Elements.*;
import static org.jboss.hal.resources.CSS.*;

public class ErrorView extends HalViewImpl implements ErrorPresenter.MyView {

    public ErrorView() {
        HTMLDivElement root = div()
                .add(nav().css(navbar, navbarDefault, navbarFixedTop, navbarPf)
                        .attr(UIConstants.ROLE, "navigation")
                        .add(div().css(navbarHeader)
                                .add(a().css(navbarBrand, logo)
                                        .add(span().css(logoText, logoTextFirst)
                                                .textContent("Management "))
                                        .add(span().css(logoText, logoTextLast)
                                                .textContent("Console")))))
                .add(div().css(containerFluid)
                        .add(div().css(row)
                                .add(div().css(column(12, columnLg, columnMd, columnSm))
                                        .add(h(1, "Page Not Found"))
                                        .add(div().css(alert, alertWarning, marginTopLarge)
                                                .add(span().css(pfIcon(warningTriangleO)))
                                                .add(span().textContent("The page could not be found.")))
                                        .add(div()
                                                .add(a("javascript:history.back()").textContent("Go Back"))))))
                .element();
        initElement(root);
    }
}
