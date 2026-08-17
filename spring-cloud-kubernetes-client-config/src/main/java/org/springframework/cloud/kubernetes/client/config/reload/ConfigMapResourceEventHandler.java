/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.kubernetes.client.config.reload;

import java.util.Objects;
import java.util.function.Consumer;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.log.LogAccessor;

/**
 * @author wind57
 */
final class ConfigMapResourceEventHandler implements ResourceEventHandler<V1ConfigMap> {

	private static final LogAccessor LOG = new LogAccessor(LogFactory.getLog(ConfigMapResourceEventHandler.class));

	private final Consumer<V1ConfigMap> onEvent;

	ConfigMapResourceEventHandler(Consumer<V1ConfigMap> onEvent) {
		this.onEvent = onEvent;
	}

	@Override
	public void onAdd(V1ConfigMap configMap) {
		LOG.debug(() -> "ConfigMap " + configMap.getMetadata().getName() + " was added in namespace "
				+ configMap.getMetadata().getNamespace());
		onEvent.accept(configMap);
	}

	@Override
	public void onUpdate(V1ConfigMap oldConfigMap, V1ConfigMap newConfigMap) {
		LOG.debug(() -> "ConfigMap " + newConfigMap.getMetadata().getName() + " was updated in namespace "
				+ newConfigMap.getMetadata().getNamespace());
		if (Objects.equals(oldConfigMap.getData(), newConfigMap.getData())) {
			LOG.debug(() -> "data in configmap has not changed, will not reload");
		}
		else {
			onEvent.accept(newConfigMap);
		}
	}

	@Override
	public void onDelete(V1ConfigMap configMap, boolean deletedFinalStateUnknown) {
		LOG.debug(() -> "ConfigMap " + configMap.getMetadata().getName() + " was deleted in namespace "
				+ configMap.getMetadata().getNamespace());
		onEvent.accept(configMap);
	}

}
