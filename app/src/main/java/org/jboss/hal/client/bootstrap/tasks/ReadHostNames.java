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
package org.jboss.hal.client.bootstrap.tasks;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import rx.Completable;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Reads the domain controller. Only executed in domain mode. Depends on {@link ReadEnvironment}. */
public class ReadHostNames implements BootstrapTask {

    static final String HOST_NAMES = "bootstrap.hostNames";

    private final Dispatcher dispatcher;
    private final Environment environment;

    @Inject
    public ReadHostNames(Dispatcher dispatcher, Environment environment) {
        this.dispatcher = dispatcher;
        this.environment = environment;
    }

    @Override
    public Completable call(FlowContext context) {
        if (environment.isStandalone()) {
            return Completable.complete();
        } else {
            Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, HOST)
                    .build();
            return dispatcher.execute(operation)
                    .doOnSuccess(result -> {
                        List<String> hosts = result.asList().stream()
                                .map(ModelNode::asString)
                                .collect(toList());
                        context.set(HOST_NAMES, hosts);
                    })
                    .toCompletable();
        }
    }
}
