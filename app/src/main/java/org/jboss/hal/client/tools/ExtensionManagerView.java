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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.ballroom.listview.ItemRenderer;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.core.extension.InstalledExtension;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.resources.CSS.noMacros;

public class ExtensionManagerView extends HalViewImpl implements ExtensionManagerPresenter.MyView {

    private final Resources resources;
    private final EmptyState empty;
    private final DataProvider<InstalledExtension> dataProvider;
    private final ListView<InstalledExtension> extensions;
    private final HTMLElement row;
    private ExtensionManagerPresenter presenter;

    @Inject
    public ExtensionManagerView(Resources resources) {
        this.resources = resources;

        dataProvider = new DataProvider<>(InstalledExtension::getName, false);
        // dataProvider.onSelect(this::loadMacro);

        empty = new EmptyState.Builder(Ids.EXTENSION_MANAGER_EMPTY, resources.constants().noExtensions())
                .icon(CSS.fontAwesome("cubes"))
                .description(resources.messages().noExtensions())
                .build();
        empty.element().classList.add(noMacros);

        ItemRenderer<InstalledExtension> itemRenderer = ie -> ie::getName;
        extensions = new ListView<>(Ids.MACRO_LIST, dataProvider, itemRenderer, true, false);
        dataProvider.addDisplay(extensions);

        row = row()
                .add(column().add(extensions))
                .get();
        initElements(asList(empty.element(), row));
    }

    @Override
    public void setPresenter(ExtensionManagerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setExtensions(Iterable<InstalledExtension> extensions) {
        if (extensions.iterator().hasNext()) {
            Elements.setVisible(empty.element(), false);
            Elements.setVisible(row, true);
            dataProvider.update(extensions);
        } else {
            Elements.setVisible(empty.element(), true);
            Elements.setVisible(row, false);
        }
    }
}
