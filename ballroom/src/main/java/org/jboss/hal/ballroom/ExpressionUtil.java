package org.jboss.hal.ballroom;

import org.gwtproject.safehtml.shared.SafeHtml;
import elemental2.dom.*;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;

public class ExpressionUtil {

    public static void replaceExpression(HTMLElement context, String expression, Object value) {
        SafeHtml safeValue;
        if (value instanceof SafeHtml) {
            safeValue = (SafeHtml) value;
        } else {
            safeValue = SafeHtmlUtils.fromString(String.valueOf(value));
        }
        // Text nodes are automatically made safe by the browser, so we need to use value here instead of safeValue to avoid escaping the string twice.
        replaceNestedExpressionInText(context, expression, String.valueOf(value));
        replaceNestedExpressionInAttributes(context, expression, safeValue.asString());
        // The call above does not catch the attributes in 'context', we need to replace them explicitly.
        replaceExpressionInAttributes(context, expression, safeValue.asString());
    }

    private static void replaceNestedExpressionInText(HTMLElement context, String expression, String value) {
        // We would normally pass a NodeFilter object (containing an acceptNode
        // method) as the third argument to createTreeWalker. However,
        // Internet Explorer expects a function to be passed as the third
        // argument, not an object, and will in fact throw a JavaScriptError
        // on the first call to nextNode() if an object is provided instead of
        // a function.
        //
        // Therefore, we pass null as the third parameter here and handle the
        // filtering manually using an if statement in the while loop below.
        TreeWalker treeWalker = DomGlobal.document.createTreeWalker(context, NodeFilter.SHOW_TEXT, null, false);

        while (treeWalker.nextNode() != null) {
            if (treeWalker.getCurrentNode().nodeValue != null && treeWalker.getCurrentNode().nodeValue.contains(
                    expression)) {
                treeWalker.getCurrentNode().nodeValue = treeWalker.getCurrentNode().nodeValue.replace(expression,
                        value);
            }
        }
    }

    private static void replaceNestedExpressionInAttributes(HTMLElement context, String expression, String value) {
        TreeWalker treeWalker = DomGlobal.document.createTreeWalker(context, NodeFilter.SHOW_ELEMENT, null, false);
        while (treeWalker.nextNode() != null) {
            if (treeWalker.getCurrentNode() instanceof HTMLElement) {
                replaceExpressionInAttributes((HTMLElement) treeWalker.getCurrentNode(), expression, value);
            }
        }
    }

    private static void replaceExpressionInAttributes(HTMLElement context, String expression, String value) {
        NamedNodeMap<Attr> attributes = context.attributes;
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String currentValue = attribute.nodeValue;
            if (currentValue != null && currentValue.contains(expression)) {
                attribute.nodeValue = currentValue.replace(expression, value);
            }
        }
    }

    private ExpressionUtil() {
    }
}
