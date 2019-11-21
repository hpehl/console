package org.jboss.hal.client.runtime.managementinterface;

import java.util.List;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;

class HeadersElement
        implements HasPresenter<ConstantHeadersPresenter>, Attachable, IsElement<HTMLElement> {

    private final HTMLElement root;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private ConstantHeadersPresenter presenter;
    private int pathIndex;

    HeadersElement(Metadata metadata, AddressTemplate template, Resources resources) {
        LabelBuilder labelBuilder = new LabelBuilder();
        Constraint constraint = Constraint.writable(template, CONSTANT_HEADERS);
        table = new ModelNodeTable.Builder<NamedNode>(Ids.CONSTANT_HEADERS_HEADER_TABLE, metadata)
                .button(resources.constants().add(), t -> presenter.addHeader(pathIndex, metadata), constraint)
                .button(resources.constants().remove(),
                        t -> {
                            ModelNode row = t.selectedRow();
                            presenter.removeHeader(pathIndex, row.get(HAL_INDEX).asInt(), row.get(NAME).asString());
                        },
                        Scope.SELECTED, constraint)
                .column(NAME)
                .column(VALUE)
                .build();
        form = new ModelNodeForm.Builder<NamedNode>(Ids.CONSTANT_HEADERS_HEADER_FORM, metadata)
                .include(NAME, VALUE)
                .onSave((f, changedValues) -> presenter.saveHeader(pathIndex, f.getModel().get(HAL_INDEX).asInt(),
                        f.getModel().get(NAME).asString(), metadata, changedValues))
                .build();

        root = section()
                .add(h(1).textContent(labelBuilder.label(HEADERS)).get())
                .add(p().textContent(metadata.getDescription().getDescription()).get())
                .add(table)
                .add(form)
                .get();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
    }

    @Override
    public void detach() {
        table.detach();
        form.detach();
    }

    @Override
    public void setPresenter(ConstantHeadersPresenter presenter) {
        this.presenter = presenter;
    }

    void update(int pathIndex, List<NamedNode> headers) {
        storeIndex(headers);
        this.pathIndex = pathIndex;
        this.form.clear();
        this.table.update(headers);
    }
}