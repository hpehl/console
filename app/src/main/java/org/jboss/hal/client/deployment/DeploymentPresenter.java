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
package org.jboss.hal.client.deployment;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.Elements;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.FinderPresenter;
import org.jboss.hal.core.mvp.FinderView;
import org.jboss.hal.js.JsHelper;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;

import static elemental2.dom.DomGlobal.document;

public class DeploymentPresenter extends FinderPresenter<DeploymentPresenter.MyView, DeploymentPresenter.MyProxy> {

    private final Environment environment;
    private final Resources resources;

    @Inject
    public DeploymentPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            Environment environment,
            Resources resources) {
        super(eventBus, view, proxy, finder, resources);
        this.environment = environment;
        this.resources = resources;
    }

    @Override
    protected String initialColumn() {
        return environment.isStandalone() ? Ids.DEPLOYMENT : Ids.DEPLOYMENT_BROWSE_BY;
    }

    @Override
    protected PreviewContent initialPreview() {
        return new InitialPreview();
    }


    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.DEPLOYMENTS)
    public interface MyProxy extends ProxyPlace<DeploymentPresenter> {
    }

    public interface MyView extends FinderView {
    }
    // @formatter:on


    private class InitialPreview extends PreviewContent<Void> {

        InitialPreview() {
            super(Names.DEPLOYMENTS, environment.isStandalone()
                    ? resources.previews().deploymentsStandalone()
                    : resources.previews().deploymentsDomain());
        }

        @Override
        public void update(Void whatever) {
            if (environment.isStandalone()) {
                Elements.setVisible((HTMLElement) document.getElementById(Ids.DRAG_AND_DROP_DEPLOYMENT),
                        JsHelper.supportsAdvancedUpload());
            }
        }
    }
}
