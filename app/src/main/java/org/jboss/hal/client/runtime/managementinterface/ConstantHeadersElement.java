package org.jboss.hal.client.runtime.managementinterface;

import java.util.List;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;

public class ConstantHeadersElement
        implements HasPresenter<ConstantHeadersPresenter>, Attachable, IsElement<HTMLElement> {

    private Form<ModelNode> form;
    private Table<ModelNode> table;
    private HeadersElement headersElement;
    private Pages pages;
    private ConstantHeadersPresenter presenter;
    private String selectedPath;

    public ConstantHeadersElement(MetadataRegistry metadataRegistry, AddressTemplate template,
            Resources resources) {
        LabelBuilder labelBuilder = new LabelBuilder();
        Metadata metadata = metadataRegistry.lookup(template).forComplexAttribute(CONSTANT_HEADERS);
        Constraint constraint = Constraint.writable(template, CONSTANT_HEADERS);
        table = new ModelNodeTable.Builder<>(Ids.build(Ids.CONSTANT_HEADERS, Ids.TABLE), metadata)
                .button(resources.constants().add(), t -> presenter.addConstantHeaderPath(metadata), constraint)
                .button(resources.constants().remove(),
                        t -> {
                            ModelNode row = t.selectedRow();
                            presenter.removeConstantHeaderPath(row.get(PATH).asString(), row.get(HAL_INDEX).asInt());
                        },
                        Scope.SELECTED, constraint)
                .column(PATH)
                .column(new InlineAction<>(labelBuilder.label(HEADERS), row -> {
                    selectedPath = row.get(PATH).asString();
                    List<NamedNode> headers = failSafeList(row, HEADERS).stream()
                            .map(node -> new NamedNode(node.get(NAME).asString(), node))
                            .collect(toList());
                    headersElement.update(headers);
                    pages.showPage(Ids.CONSTANT_HEADERS_PATH_PAGE);
                }))
                .build();

        form = new ModelNodeForm.Builder<>(Ids.build(Ids.CONSTANT_HEADERS, Ids.FORM), metadata)
                .include(PATH)
                .onSave((f, changedValues) -> {
                    int index = f.getModel().get(HAL_INDEX).asInt();
                    String path = String.valueOf(changedValues.get(PATH));
                    presenter.saveConstantHeaderPath(path, index);
                })
                .build();

        HTMLElement section = section()
                .add(h(1).textContent(labelBuilder.label(CONSTANT_HEADERS)).get())
                .add(p().textContent(metadata.getDescription().getDescription()).get())
                .add(table)
                .add(form)
                .get();

        headersElement = new HeadersElement(metadata.forComplexAttribute(HEADERS), resources);

        pages = new Pages(Ids.CONSTANT_HEADERS_PAGES, Ids.CONSTANT_HEADERS_PAGE, section);
        pages.addPage(Ids.CONSTANT_HEADERS_PAGE, Ids.CONSTANT_HEADERS_PATH_PAGE,
                () -> labelBuilder.label(PATH) + " " + selectedPath,
                () -> labelBuilder.label(HEADERS),
                headersElement);
    }

    @Override
    public HTMLElement element() {
        return pages.element();
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
        headersElement.attach();
    }

    @Override
    public void detach() {
        table.detach();
        form.detach();
        headersElement.detach();
    }

    @Override
    public void setPresenter(ConstantHeadersPresenter presenter) {
        this.presenter = presenter;
        headersElement.setPresenter(presenter);
    }

    public void update(List<ModelNode> constantHeaders) {
        storeIndex(constantHeaders);
        form.clear();
        table.update(constantHeaders, modelNode -> modelNode.get(PATH).asString());
    }
}
