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

import java.util.function.Consumer;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.openapi.models.V1Secret;

import org.springframework.core.log.LogAccessor;

final class SecretResourceEventHandler implements ResourceEventHandler<V1Secret> {

	private final LogAccessor log;

	private final Consumer<V1Secret> onEvent;

	SecretResourceEventHandler(LogAccessor log, Consumer<V1Secret> onEvent) {
		this.log = log;
		this.onEvent = onEvent;
	}

	@Override
	public void onAdd(V1Secret secret) {
		log.debug(() -> "Secret " + secret.getMetadata().getName() + " was added in namespace "
				+ secret.getMetadata().getNamespace());
		onEvent.accept(secret);
	}

	@Override
	public void onUpdate(V1Secret oldSecret, V1Secret newSecret) {
		log.debug(() -> "Secret " + newSecret.getMetadata().getName() + " was updated in namespace "
				+ newSecret.getMetadata().getNamespace());

		if (KubernetesClientEventBasedSecretsChangeDetector.equals(oldSecret.getData(), newSecret.getData())) {
			log.debug(() -> "data in secret has not changed, will not reload");
		}
		else {
			onEvent.accept(newSecret);
		}
	}

	@Override
	public void onDelete(V1Secret secret, boolean deletedFinalStateUnknown) {
		log.debug(() -> "Secret " + secret.getMetadata().getName() + " was deleted in namespace "
				+ secret.getMetadata().getNamespace());
		onEvent.accept(secret);
	}

}
