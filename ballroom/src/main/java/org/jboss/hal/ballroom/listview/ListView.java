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
package org.jboss.hal.ballroom.listview;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.Elements;
import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.ballroom.dataprovider.Display;
import org.jboss.hal.ballroom.dataprovider.PageInfo;
import org.jboss.hal.ballroom.dataprovider.SelectionInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.resources.CSS.*;

/**
 * PatternFly list view. The list view does not manage data by itself. Instead you have to use a {@link DataProvider}
 * and add the list view as a display to the data provider:
 *
 * <pre>
 * DataProvider dataProvider = ...;
 * ListView listView = ...;
 *
 * dataProvider.addDisplay(listView);
 * dataProvider.setItems(...);
 * </pre>
 *
 * @see <a href="https://www.patternfly.org/pattern-library/content-views/list-view/">https://www.patternfly.org/pattern-library/content-views/list-view/</a>
 */
public class ListView<T> implements Display<T>, IsElement<HTMLElement> {

    private final DataProvider<T> dataProvider;
    private final ItemRenderer<T> itemRenderer;
    private final boolean multiSelect;
    private final String[] contentWidths;
    private final HTMLElement root;
    private final Map<String, ListItem<T>> currentListItems;

    public ListView(String id, DataProvider<T> dataProvider, ItemRenderer<T> itemRenderer,
            boolean stacked, boolean multiSelect) {
        this(id, dataProvider, itemRenderer, stacked, multiSelect, new String[]{"60%", "40%"});
    }

    @SuppressWarnings("WeakerAccess")
    public ListView(String id, DataProvider<T> dataProvider, ItemRenderer<T> itemRenderer,
            boolean stacked, boolean multiSelect, String[] contentWidths) {
        this.dataProvider = dataProvider;
        this.itemRenderer = itemRenderer;
        this.multiSelect = multiSelect;
        this.contentWidths = contentWidths;
        this.currentListItems = new HashMap<>();

        HtmlContentBuilder<HTMLDivElement> div = div().id(id).css(listPf);
        if (stacked) {
            div.css(listPfStacked);
        }
        this.root = div.element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void showItems(Iterable<T> items, PageInfo pageInfo) {
        currentListItems.clear();
        Elements.removeChildrenFrom(root);
        for (T item : items) {
            ItemDisplay<T> display = itemRenderer.render(item);
            ListItem<T> listItem = new ListItem<>(this, item, multiSelect, display, contentWidths);
            currentListItems.put(listItem.id, listItem);
            root.appendChild(listItem.element());
        }
    }

    @Override
    public void updateSelection(SelectionInfo<T> selectionInfo) {
        for (ListItem<T> item : currentListItems.values()) {
            if (selectionInfo.isSelected(item.item)) {
                item.element().classList.add(active);
                if (item.checkbox != null) {
                    item.checkbox.checked = true;
                }
            } else {
                item.element().classList.remove(active);
                if (item.checkbox != null) {
                    item.checkbox.checked = false;
                }
            }
        }
    }

    void selectListItem(ListItem<T> listItem, boolean select) {
        dataProvider.select(listItem.item, select);
    }

    public void enableAction(T item, String actionId) {
        ListItem<T> listItem = currentListItems.get(dataProvider.getId(item));
        if (listItem != null) {
            listItem.enableAction(actionId);
        }
    }

    public void disableAction(T item, String actionId) {
        ListItem<T> listItem = currentListItems.get(dataProvider.getId(item));
        if (listItem != null) {
            listItem.disableAction(actionId);
        }
    }

    protected List<ItemAction<T>> allowedActions(List<ItemAction<T>> actions) {
        return actions;
    }
}
