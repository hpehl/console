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
package org.jboss.hal.client.runtime.subsystem.logging;

import com.google.common.base.Strings;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.hal.ballroom.*;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.form.SwitchBridge;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import javax.inject.Inject;
import java.util.Date;

import static elemental2.dom.DomGlobal.*;
import static java.lang.Math.max;
import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.click;
import static org.jboss.elemento.InputType.checkbox;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

public class LogFileView extends HalViewImpl implements LogFilePresenter.MyView {

    private static final int MIN_HEIGHT = 70;
    private static final String SLASH = " / ";

    private final Environment environment;
    private final StatementContext statementContext;
    private final LogFiles logFiles;
    private final Resources resources;

    private final HTMLElement header;
    private final HTMLElement logFileControls;
    private final HTMLElement status;
    private final HTMLInputElement tailMode;
    private final HTMLElement copyToClipboard;
    private final HTMLElement download;
    private final HTMLElement editorContainer;
    private final HTMLElement loading;

    private final Search search;
    private AceEditor editor;
    private LogFilePresenter presenter;

    @Inject
    public LogFileView(Environment environment, StatementContext statementContext, LogFiles logFiles,
            Resources resources) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.logFiles = logFiles;
        this.resources = resources;

        search = new Search.Builder(Ids.LOG_FILE_SEARCH, query -> editor.getEditor().find(query))
                .onPrevious(query -> editor.getEditor().findPrevious())
                .onNext(query -> editor.getEditor().findNext())
                .build();

        Options editorOptions = new Options();
        editorOptions.readOnly = true;
        editorOptions.showGutter = true;
        editorOptions.showLineNumbers = true;
        editorOptions.showPrintMargin = false;
        editor = new AceEditor(Ids.LOG_FILE_EDITOR, editorOptions);

        HTMLDivElement root = div().css(row)
                .add(div().css(column(12, columnLg, columnMd, columnSm))
                        .add(header = h(1).element())
                        .add(logFileControls = div().css(editorControls, marginBottomSmall)
                                .add(search)
                                .add(status = div().css(editorStatus, marginLeftSmall).element())
                                .add(div().css(logFileFollow)
                                        .add(label().apply(l -> l.htmlFor = Ids.LOG_FILE_FOLLOW)
                                                .textContent(resources.constants().tailMode()))
                                        .add(tailMode = input(checkbox).css(bootstrapSwitch)
                                                .id(Ids.LOG_FILE_FOLLOW)
                                                .element()))
                                .add(div().css(editorButtons, btnGroup)
                                        .add(a().css(btn, btnDefault, clickable)
                                                .data(TOGGLE, TOOLTIP)
                                                .data(CONTAINER, BODY)
                                                .data(PLACEMENT, TOP)
                                                .on(click, event -> presenter.reloadFile())
                                                .title(resources.constants().references())
                                                .add(i().css(fontAwesome(refresh))))
                                        .add(copyToClipboard = a().css(btn, btnDefault, clickable)
                                                .data(TOGGLE, TOOLTIP)
                                                .data(CONTAINER, BODY)
                                                .data(PLACEMENT, TOP)
                                                .title(resources.constants().copyToClipboard())
                                                .add(i().css(fontAwesome("clipboard")))
                                                .element())
                                        .add(download = a("#").css(btn, btnDefault, clickable)
                                                .data(TOGGLE, TOOLTIP)
                                                .data(CONTAINER, BODY)
                                                .data(PLACEMENT, TOP)
                                                .title(resources.constants().download())
                                                .add(i().css(fontAwesome("download")))
                                                .element()))
                                .element())
                        .add(editorContainer = div().css(marginBottomLarge, logFileEditorContainer)
                                .add(editor)
                                .add(loading = div().css(spinner, spinnerLg).element())
                                .element()))
                .element();

        Clipboard clipboard = new Clipboard(copyToClipboard);
        clipboard.onCopy(event -> copyToClipboard(event.client));
        registerAttachable(editor);
        initElement(root);
    }

    private void copyToClipboard(Clipboard clipboard) {
        String value = editor.getEditor().getSession().getValue();
        if (!Strings.isNullOrEmpty(value)) {
            clipboard.setText(value);
            Tooltip tooltip = Tooltip.element(copyToClipboard);
            tooltip.hide()
                    .setTitle(resources.constants().copied())
                    .show()
                    .onHide(() -> tooltip.setTitle(resources.constants().copyToClipboard()));
            setTimeout((o) -> tooltip.hide(), 1000);
        }
    }

    @Override
    public void attach() {
        super.attach();

        SwitchBridge.Api.element(tailMode).onChange((event, state) -> presenter.toggleTailMode(state));

        editor.getEditor().$blockScrolling = 1;
        editor.getEditor().setTheme("ace/theme/logfile"); //NON-NLS
        editor.getEditor().getSession().setMode("ace/mode/logfile"); //NON-NLS

        adjustEditorHeight();
        window.onresize = event -> {
            adjustEditorHeight();
            return null;
        };
    }

    @Override
    public void detach() {
        super.detach();
        window.onresize = null;

        SwitchBridge.Api.element(tailMode).destroy();
    }

    private void adjustEditorHeight() {
        int height = Skeleton.applicationHeight();
        height -= 2 * MARGIN_BIG;
        height -= (header.offsetHeight + logFileControls.offsetHeight + 20);
        height = max(height, MIN_HEIGHT);
        editor.element().style.height = CSS.height(px(height));
        editor.getEditor().resize();
    }

    @Override
    public void setPresenter(LogFilePresenter presenter) {
        this.presenter = presenter;
    }


    // ------------------------------------------------------ API

    @Override
    public void loading() {
        status.textContent = resources.constants().loadingPleaseWait();
        status.title = resources.constants().loadingPleaseWait();
        int top = (int) (loading.offsetHeight + editor.element().offsetHeight / 2);
        loading.style.top = -1 * top + "px"; //NON-NLS
        editorContainer.classList.add(logFileLoading);
    }

    @Override
    public void show(LogFile logFile, int lines, String content) {
        statusUpdate(lines);
        StringBuilder builder = new StringBuilder();
        if (presenter.isExternal()) {
            if (!environment.isStandalone()) {
                builder.append(statementContext.selectedHost())
                        .append(SLASH)
                        .append(statementContext.selectedServer())
                        .append(SLASH);
            } else {
                builder.append(Server.STANDALONE.getName())
                        .append(SLASH);
            }
        }
        if (logFile.getLoggingProfile() != null) {
            builder.append(logFile.getLoggingProfile())
                    .append(SLASH);
        }
        builder.append(logFile.getFilename());
        header.textContent = builder.toString();
        download.setAttribute(UIConstants.DOWNLOAD, logFile.getFilename());
        download.setAttribute(UIConstants.HREF,
                logFiles.downloadUrl(logFile.getFilename(), logFile.getLoggingProfile()));

        editor.getEditor().getSession().setValue(content);
        editor.getEditor().gotoLine(lines, 0, false);
    }

    @Override
    public void refresh(int lines, String content) {
        statusUpdate(lines);
        editor.getEditor().getSession().setValue(content);
        editor.getEditor().gotoLine(lines, 0, false);
    }

    @Override
    public int visibleLines() {
        int lineHeight = 15;
        HTMLElement lineElement = (HTMLElement) document.querySelector(
                HASH + Ids.LOG_FILE_EDITOR + " .ace_text-layer .ace_line"); //NON-NLS
        if (lineElement != null) {
            lineHeight = (int) lineElement.offsetHeight;
        }
        return (int) (editor.element().offsetHeight / lineHeight);
    }

    private void statusUpdate(int lines) {
        String statusText = lines < LogFiles.LINES
                ? resources.messages().logFileFullStatus(lines, Format.time(new Date()))
                : resources.messages().logFilePartStatus(lines, Format.time(new Date()));
        status.textContent = statusText;
        status.title = statusText;
        editorContainer.classList.remove(logFileLoading);
        search.clear();
    }
}
