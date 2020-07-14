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
package org.jboss.hal.js;

import java.util.HashMap;
import java.util.Map;

import elemental2.core.JsRegExp;
import elemental2.core.RegExpResult;
import elemental2.dom.DragEvent;
import elemental2.dom.HTMLElement;
import jsinterop.base.JsPropertyMap;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.event.shared.HandlerRegistrations;
import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.EventType;
import org.jboss.hal.resources.CSS;

import static elemental2.core.Global.decodeURIComponent;
import static elemental2.dom.DomGlobal.window;

public final class JsHelper {

    public static Map<String, Object> asMap(JsPropertyMap<Object> jsMap) {
        Map<String, Object> map = new HashMap<>();
        jsMap.forEach(key -> map.put(key, jsMap.get(key)));
        return map;
    }

    public static <T> JsPropertyMap<Object> asJsMap(Map<String, T> map) {
        JsPropertyMap<Object> jsMap = JsPropertyMap.of();
        map.forEach(jsMap::set);
        return jsMap;
    }

    public static String requestParameter(String name) {
        String validName = name.replaceAll("[\\[\\]]", "\\$&");
        JsRegExp valueRegExp = new JsRegExp("[?&]" + validName + "(=([^&#]*)|&|#|$)");
        RegExpResult results = valueRegExp.exec(window.location.search);
        if (results == null || results.getLength() < 2) {
            return null;
        }
        if (results.getAt(2) == null) {
            return null;
        }
        return decodeURIComponent(results.getAt(2).replace('+', ' '));
    }

    public static native boolean supportsAdvancedUpload() /*-{
        var div = document.createElement('div');
        return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) &&
            'FormData' in window && 'FileReader' in window;
    }-*/;

    public static HandlerRegistration addDropHandler(HTMLElement element, EventCallbackFn<DragEvent> handler) {
        EventCallbackFn<DragEvent> noop = event -> {
            event.preventDefault();
            event.stopPropagation();
        };
        EventCallbackFn<DragEvent> addDragIndicator = event -> {
            noop.onEvent(event);
            element.classList.add(CSS.ondrag);
        };
        EventCallbackFn<DragEvent> removeDragIndicator = event -> {
            noop.onEvent(event);
            element.classList.remove(CSS.ondrag);
        };

        return HandlerRegistrations.compose(
                EventType.bind(element, EventType.drag, noop),
                EventType.bind(element, EventType.dragstart, noop),
                EventType.bind(element, EventType.dragenter, addDragIndicator),
                EventType.bind(element, EventType.dragover, addDragIndicator),
                EventType.bind(element, EventType.dragleave, removeDragIndicator),
                EventType.bind(element, EventType.dragend, removeDragIndicator),
                EventType.bind(element, EventType.drop, event -> {
                    noop.onEvent(event);
                    removeDragIndicator.onEvent(event);
                    handler.onEvent(event);
                }));
    }

    private JsHelper() {
    }
}
