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
package org.jboss.hal.client.configuration;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class PathsPresenter extends MbuiPresenter<PathsPresenter.MyView, PathsPresenter.MyProxy> {

    private final CrudOperations crud;

    @Inject
    public PathsPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            CrudOperations crud) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public FinderPath finderPath() {
        return new FinderPath()
                .append(Ids.CONFIGURATION, Ids.asId(Names.PATHS), Names.CONFIGURATION, Names.PATHS);
    }

    @Override
    protected void reload() {
        crud.readChildren(ResourceAddress.root(), PATH, children -> getView().update(asNamedNodes(children)));
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires("/path=*")
    @NameToken(NameTokens.PATH)
    public interface MyProxy extends ProxyPlace<PathsPresenter> {
    }

    public interface MyView extends MbuiView<PathsPresenter> {
        void update(List<NamedNode> paths);
    }
    // @formatter:on
}
