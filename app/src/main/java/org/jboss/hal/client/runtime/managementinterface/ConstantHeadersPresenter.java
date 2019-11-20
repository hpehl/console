package org.jboss.hal.client.runtime.managementinterface;

import org.jboss.hal.meta.Metadata;

/** Not a real presenter, but common methods for {@code HostPresenter} and {@code StandaloneServerPresenter} */
public interface ConstantHeadersPresenter {

    void addConstantHeaderPath(Metadata metadata);

    void saveConstantHeaderPath(String path, int index);

    void removeConstantHeaderPath(String path, int index);
}
