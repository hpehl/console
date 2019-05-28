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
package org.jboss.hal.client.tools;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.ExtensionStorage;
import org.jboss.hal.core.extension.InstalledExtension;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExternalMode;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;

/** Presenter to manage JavaScript extensions. */
public class ExtensionManagerPresenter
        extends ApplicationPresenter<ExtensionManagerPresenter.MyView, ExtensionManagerPresenter.MyProxy>
        implements HasTitle, SupportsExternalMode {

    private final ExtensionRegistry extensionRegistry;
    private final ExtensionStorage extensionStorage;
    private final Resources resources;

    @Inject
    public ExtensionManagerPresenter(EventBus eventBus, MyView view, MyProxy proxy,
            ExtensionRegistry extensionRegistry, ExtensionStorage extensionStorage,
            Resources resources) {
        super(eventBus, view, proxy);
        this.extensionRegistry = extensionRegistry;
        this.extensionStorage = extensionStorage;
        this.resources = resources;
    }

    @Override
    public String getTitle() {
        return resources.constants().extensionError();
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setExtensions(extensionStorage.list());
    }


    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.EXTENSION_MANAGER)
    public interface MyProxy extends ProxyPlace<ExtensionManagerPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<ExtensionManagerPresenter> {
        void setExtensions(Iterable<InstalledExtension> extensions);
    }
    // @formatter:on
}
