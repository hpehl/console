package org.jboss.hal.client.runtime.managementinterface;

import java.util.Map;

import org.jboss.hal.meta.Metadata;

/** Not a real presenter, but common methods for {@code HostPresenter} and {@code StandaloneServerPresenter} */
public interface ConstantHeadersPresenter {

    void addConstantHeaderPath(Metadata metadata);

    void saveConstantHeaderPath(int index, String path);

    void removeConstantHeaderPath(int index, String path);

    void addHeader(int pathIndex, Metadata metadata);

    void saveHeader(int pathIndex, int index, String header, Metadata metadata, Map<String, Object> changedValues);

    void removeHeader(int pathIndex, int index, String header);
}
