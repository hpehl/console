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
package org.jboss.hal.client.tools;

import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.macro.MacroOptions;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.macro.MacroOptions.*;

/** Dialog to record a new macro. */
public class MacroOptionsDialog {

    private final Dialog dialog;
    private final Form<MacroOptions> form;

    public MacroOptionsDialog(Macros macros, Resources resources, MacroOptionsCallback callback) {
        Metadata metadata = Metadata.staticDescription(RESOURCES.macroOptions());

        form = new ModelNodeForm.Builder<MacroOptions>(Ids.MACRO_OPTIONS, metadata)
                .include(NAME, DESCRIPTION, OMIT_READ_OPERATIONS, OPEN_IN_EDITOR)
                .unsorted()
                .onSave((form, changedValues) -> callback.onOptions(form.getModel()))
                .build();
        FormItem<String> nameItem = form.getFormItem(NAME);
        nameItem.addValidationHandler(name -> macros.get(name) != null
                ? ValidationResult.invalid(resources.constants().duplicateMacro()) : ValidationResult.OK);

        dialog = new Dialog.Builder(resources.constants().startMacro())
                .add(form.element())
                .primary(resources.constants().ok(), form::save)
                .cancel()
                .build();
        dialog.registerAttachable(form);
    }

    public void show() {
        dialog.show();
        form.edit(new MacroOptions());
    }


    public interface MacroOptionsCallback {

        void onOptions(MacroOptions options);
    }
}
