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

import jsinterop.annotations.JsType;

import java.util.Map;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Options for a data table.
 *
 * @param <T> the row type
 *
 * @see <a href="https://datatables.net/reference/option/">https://datatables.net/reference/option/</a>
 */
@SuppressWarnings("WeakerAccess")
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Options<T> {

    public Api.Buttons<T> buttons;
    public Column<T>[] columns;
    public String dom;
    public boolean keys;
    public boolean paging;
    public int pageLength;
    public boolean searching;
    public Api.Select select;
    // not part of the DataTables API, but used internally
    String id;
    Map<String, InlineActionHandler<T>> columnActionHandler;
    public Map<Integer, String> buttonConstraints;
}
