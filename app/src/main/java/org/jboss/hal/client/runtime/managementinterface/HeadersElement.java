package org.jboss.hal.client.runtime.managementinterface;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;

class HeadersElement
        implements HasPresenter<ConstantHeadersPresenter>, Attachable, IsElement<HTMLElement> {

    private final HTMLElement root;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private ConstantHeadersPresenter presenter;

    HeadersElement(Metadata metadata, Resources resources) {
        LabelBuilder labelBuilder = new LabelBuilder();
        table = new ModelNodeTable.Builder<NamedNode>(Ids.CONSTANT_HEADERS_HEADERS_TABLE, metadata)
                .column(NAME)
                .column(VALUE)
                .build();
        form = new ModelNodeForm.Builder<NamedNode>(Ids.CONSTANT_HEADERS_HEADERS_FORM, metadata)
                .include(NAME, VALUE)
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

    void update(Iterable<NamedNode> headers) {
        form.clear();
        table.update(headers);
    }
}