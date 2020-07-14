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
package org.jboss.hal.ballroom.table;

import org.gwtproject.safehtml.shared.SafeHtmlUtils;

/**
 * Builder for a {@link Column}.
 *
 * @param <T> the row type
 */
public class ColumnBuilder<T> {

    private final String name;
    private final String title;
    private final Column.RenderCallback<T, String> render;

    private boolean orderable;
    private boolean searchable;
    private boolean safeHtml;
    private String type;
    private String width;
    private String className;

    public ColumnBuilder(String name, String title, Column.RenderCallback<T, String> render) {
        this.name = name;
        this.title = title;
        this.render = render;
        this.orderable = true;
        this.searchable = true;
        this.safeHtml = false;
    }

    public ColumnBuilder<T> orderable(boolean orderable) {
        this.orderable = orderable;
        return this;
    }

    public ColumnBuilder<T> searchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public ColumnBuilder<T> safeHtml() {
        this.safeHtml = true;
        return this;
    }

    public ColumnBuilder<T> type(String type) {
        this.type = type;
        return this;
    }

    public ColumnBuilder<T> width(String width) {
        this.width = width;
        return this;
    }

    public ColumnBuilder<T> className(String className) {
        this.className = className;
        return this;
    }

    public Column<T> build() {
        Column.RenderCallback<T, String> effectiveRender;
        if (safeHtml) {
            // use render callback as-is
            effectiveRender = render;
        } else {
            // make sure the render escapes HTML
            effectiveRender = (cell, type, row, meta) -> {
                String value = ColumnBuilder.this.render.render(cell, type, row, meta);
                if (value != null) {
                    value = SafeHtmlUtils.htmlEscape(value);
                }
                return value;
            };
        }

        Column<T> column = new Column<>();
        column.name = name;
        column.title = title;
        column.render = effectiveRender;
        column.orderable = orderable;
        column.searchable = searchable;
        if (type != null) {
            column.type = type;
        }
        if (width != null) {
            column.width = width;
        }
        if (className != null) {
            column.className = className;
        }
        return column;
    }
}
