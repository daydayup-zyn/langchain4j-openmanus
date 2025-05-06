/*
 * Copyright 2024-2025 the original author or authors.
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
package cn.daydayup.dev.workflow.checkpoint;

import cn.daydayup.dev.workflow.RunnableConfig;

import java.util.*;

import static java.util.Optional.ofNullable;

public interface BaseCheckpointSaver {

	Collection<Checkpoint> list(RunnableConfig config);

	Optional<Checkpoint> get(RunnableConfig config);

	RunnableConfig put(RunnableConfig config, Checkpoint checkpoint) throws Exception;

	boolean clear(RunnableConfig config);

	default Optional<Checkpoint> getLast(LinkedList<Checkpoint> checkpoints, RunnableConfig config) {
		return (checkpoints == null || checkpoints.isEmpty()) ? Optional.empty() : ofNullable(checkpoints.peek());
	}

	default LinkedList<Checkpoint> getLinkedList(List<Checkpoint> checkpoints) {
		return Objects.nonNull(checkpoints) ? new LinkedList<>(checkpoints) : new LinkedList<>();
	}

}
