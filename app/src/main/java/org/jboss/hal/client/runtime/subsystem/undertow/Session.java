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
package org.jboss.hal.client.runtime.subsystem.undertow;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import java.util.Date;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CREATION_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LAST_ACCESSED_TIME;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeDate;

class Session extends NamedNode {

    Session(String sessionId, ModelNode modelNode) {
        super(sessionId, modelNode);
    }

    String getSessionId() {
        return getName();
    }

    Date getCreationTime() {
        return failSafeDate(this, CREATION_TIME);
    }

    Date getLastAccessTime() {
        return failSafeDate(this, LAST_ACCESSED_TIME);
    }
}
