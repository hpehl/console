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
package org.jboss.hal.client.deployment.dialog;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.SwitchBridge;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.InputType.checkbox;
import static org.jboss.hal.resources.CSS.marginTopLarge;

/** Dialog used to deploy one or several one content items to one server group. */
public class DeployContentDialog2 {

    private final String serverGroup;
    private final List<Content> content;
    private final DeployCallback deployCallback;
    private final Alert noContentSelected;
    private final Table<Content> table;
    private final HTMLInputElement enable;
    private final Dialog dialog;

    public DeployContentDialog2(String serverGroup, List<Content> content, Resources resources,
            DeployCallback deployCallback) {
        this.serverGroup = serverGroup;
        this.content = content.stream()
                .sorted(comparing(Content::getName))
                .collect(toList());
        this.deployCallback = deployCallback;

        noContentSelected = new Alert(Icons.ERROR, resources.messages().noContentSelected());

        Options<Content> options = new OptionsBuilder<Content>()
                .checkboxColumn()
                .column(resources.constants().content(), (cell, type, row, meta) -> row.getName())
                .keys(false)
                .paging(false)
                .searching(false)
                .multiselect()
                .options();
        table = new DataTable<>(Ids.SERVER_GROUP_DEPLOYMENT_TABLE, options);

        Iterable<HTMLElement> elements = collect()
                .add(div().add(noContentSelected))
                .add(p().innerHtml(resources.messages().chooseContentToDeploy(serverGroup)))
                .add(table)
                .add(div().css(marginTopLarge)
                        .add(enable = input(checkbox).id(Ids.SERVER_GROUP_DEPLOYMENT_ENABLE).element())
                        .add(label().css(CSS.marginLeft5)
                                .apply(l -> l.htmlFor = Ids.SERVER_GROUP_DEPLOYMENT_ENABLE)
                                .textContent(resources.constants().enableDeployment()))).elements();

        dialog = new Dialog.Builder(resources.constants().deployContent())
                .add(elements)
                .primary(resources.constants().deploy(), this::finish)
                .cancel()
                .build();
        dialog.registerAttachable(table);
    }

    private boolean finish() {
        boolean hasSelection = table.hasSelection();
        Elements.setVisible(noContentSelected.element(), !hasSelection);
        if (hasSelection) {
            List<Content> content = table.selectedRows();
            deployCallback.deploy(serverGroup, content, SwitchBridge.Api.element(enable).getValue());
        }
        return hasSelection;
    }

    public void show() {
        dialog.show();
        Elements.setVisible(noContentSelected.element(), false);
        table.update(content);
        SwitchBridge.Api.element(enable).setValue(true);
    }


    @FunctionalInterface
    public interface DeployCallback {

        void deploy(String serverGroup, List<Content> content, boolean enable);
    }
}
