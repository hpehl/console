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

import elemental2.dom.HTMLElement;
import jsinterop.annotations.*;
import jsinterop.base.Js;
import org.jboss.hal.ballroom.JQuery;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Subset of the DataTables API.
 * <p>
 * This class and every member of this class is considered to be an internal API and should not be used outside of
 * package {@code org.jboss.hal.ballroom.table}.
 *
 * @see <a href="https://datatables.net/reference/api/">https://datatables.net/reference/api/</a>
 */
@JsType(isNative = true)
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
class Api<T> {

    // ------------------------------------------------------ initialization

    @JsMethod(namespace = GLOBAL, name = "$")
    static native <T> Api<T> select(String selector);

    @JsMethod(name = "DataTable")
    native Api<T> dataTable(Options options);


    // ------------------------------------------------------ properties

    // We cannot have both a property and a method named equally.
    // That's why the API defines the property "row" and the method "rows"
    @JsProperty Row<T> row;


    // ------------------------------------------------------ API a-z

    native Api<T> button(int index);

    native Api<T> clear();

    native Api<T> data();

    native Api<T> draw(String paging);

    /**
     * Disables or enables the button selected with {@link #button(int)}
     */
    native Api<T> enable(boolean enable);

    /**
     * Returns the jQuery object for the button selected with {@link #button(int)}
     */
    native JQuery node();

    /**
     * Adds a callback. Currently restricted to the "select", "deselect" and "draw" event.
     *
     * @param event    must be "select", "deselect" or "draw"
     * @param callback the callback
     */
    native Api<T> on(String event, CallbackUnionType<T> callback);

    /**
     * Select all rows, but apply the specified modifier (e.g. to return only selected rows). Chain the {@link #data()}
     * to get the actual data.
     */
    native Api<T> rows(SelectorModifier selectorModifier);

    /**
     * Select rows by tr element. Chain the {@link #data()} to get the actual data.
     */
    native Api<T> rows(HTMLElement tr);

    /**
     * Select rows by using a function. Chain the {@link #data()} to get the actual data.
     */
    native Api<T> rows(RowSelection<T> selection);

    /**
     * Selects the row(s) that have been found by the {@link #rows(RowSelection)}, {@link #rows(HTMLElement)} or {@link
     * #rows(SelectorModifier)} selector methods.
     */
    native Api<T> select();

    native T[] toArray();


    // ------------------------------------------------------ overlay methods

    @JsOverlay
    final Api<T> add(Iterable<T> data) {
        if (data != null) {
            for (T d : data) {
                row.add(d);
            }
        }
        return this;
    }

    @JsOverlay
    final T selectedRow() {
        List<T> rows = selectedRows();
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    @JsOverlay
    final List<T> selectedRows() {
        SelectorModifier selectorModifier = new SelectorModifierBuilder().selected().build();
        T[] selection = rows(selectorModifier).data().toArray();
        if (selection == null || selection.length == 0) {
            return Collections.emptyList();
        }
        return asList(selection);
    }


    // ------------------------------------------------------ button(s)


    /**
     * Custom data tables button.
     *
     * @author Harald Pehl
     * @see <a href="https://datatables.net/extensions/buttons/custom">https://datatables.net/extensions/buttons/custom</a>
     */
    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Button<T> {

        String text;
        ActionHandler<T> action;
        String extend;
        String titleAttr;
        String constraint;
        // not part of the DataTables API, but used to have a reference back to the table in ActionHandler
        Table<T> table;


        /**
         * Action handler for a custom button.
         *
         * @see <a href="https://datatables.net/reference/option/buttons.buttons.action">https://datatables.net/reference/option/buttons.buttons.action</a>
         */
        @JsFunction
        interface ActionHandler<T> {

            void action(Object event, Object api, Object node, Button<T> btn);
        }
    }


    /**
     * Buttons options.
     *
     * @param <T> the row type
     * @author Harald Pehl
     * @see <a href="https://datatables.net/reference/option/#buttons">https://datatables.net/reference/option/#buttons</a>
     */
    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Buttons<T> {

        Button<T>[] buttons;
        Dom dom;


        @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
        static class Dom {

            Factory container;
            Factory button;


            @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
            static class Factory {

                public String tag;
                public String className;
            }
        }
    }


    // ------------------------------------------------------ rows


    /**
     * Represents the {@code row} property in a data table.
     *
     * @param <T> the row type
     * @author Harald Pehl
     */
    @JsType(isNative = true)
    static class Row<T> {

        /**
         * Adds a new row to the table.
         */
        native Api<T> add(T data);
    }


    /**
     * Function to be used as a row selector in {@link Api#rows(RowSelection)}.
     *
     * @author Harald Pehl
     * @see <a href="https://datatables.net/reference/type/row-selector#Function">https://datatables.net/reference/type/row-selector#Function</a>
     */
    @JsFunction
    interface RowSelection<T> {

        boolean select(int index, T data, HTMLElement tr);
    }


    // ------------------------------------------------------ selection


    /**
     * Select options.
     *
     * @author Harald Pehl
     * @see <a href="https://datatables.net/reference/option/#select">https://datatables.net/reference/option/#select</a>
     */
    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Select {

        @JsOverlay
        @SuppressWarnings("HardCodedStringLiteral")
        static Select build(boolean multiselect) {
            Select select = new Select();
            select.info = false;
            select.items = "row";
            select.style = multiselect ? "multi" : "single";
            return select;
        }

        boolean info;
        String items;
        String style;
    }


    /**
     * Callback used for all kind of "select" and "deselect" events.
     *
     * @param <T> the row type
     * @see <a href="https://datatables.net/reference/event/select">https://datatables.net/reference/event/select</a>
     * @see <a href="https://datatables.net/reference/event/deselect">https://datatables.net/reference/event/deselect</a>
     */
    @JsFunction
    interface SelectCallback<T> {

        void onSelect(Object event, Api<T> api, String type);
    }


    // ------------------------------------------------------ draw


    @JsFunction
    interface DrawCallback {

        void afterDraw(Object event, Object settings);
    }


    // ------------------------------------------------------ callback union type


    @JsType(isNative = true, namespace = GLOBAL, name = "?")
    interface CallbackUnionType<T> {

        @JsOverlay
        static <T> CallbackUnionType<T> of(Object o) {
            return Js.cast(o);
        }
    }
}