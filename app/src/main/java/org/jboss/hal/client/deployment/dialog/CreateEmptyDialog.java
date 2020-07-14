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

import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItemValidation;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import java.util.function.Consumer;

import static org.jboss.elemento.Elements.p;

public class CreateEmptyDialog {

    private final Dialog dialog;
    private final Form<ModelNode> form;
    private final NameItem nameItem;

    public CreateEmptyDialog(Resources resources, Consumer<String> callback) {
        nameItem = new NameItem();
        form = new ModelNodeForm.Builder<>(Ids.DEPLOYMENT_EMPTY_FORM, Metadata.empty())
                .unboundFormItem(nameItem, 0)
                .onSave((f, changedValues) -> callback.accept(nameItem.getValue()))
                .addOnly()
                .build();
        dialog = new Dialog.Builder(resources.constants().deploymentEmptyCreate())
                .add(p().textContent(resources.messages().deploymentEmptyCreate()).element())
                .add(form.element())
                .closeOnEsc(true)
                .closeIcon(true)
                .primary(resources.constants().add(), form::save)
                .secondary(() -> {
                    form.cancel();
                    return true;
                })
                .build();
        dialog.registerAttachable(form);
    }

    public void addValidationHandlerForNameItem(FormItemValidation<String> validationHandler) {
        nameItem.addValidationHandler(validationHandler);
    }

    public void show() {
        dialog.show();
        form.edit(new ModelNode());
    }
}
