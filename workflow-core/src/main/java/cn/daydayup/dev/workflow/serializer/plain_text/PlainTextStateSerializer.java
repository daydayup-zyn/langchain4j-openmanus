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
package cn.daydayup.dev.workflow.serializer.plain_text;

import cn.daydayup.dev.workflow.OverAllState;
import cn.daydayup.dev.workflow.serializer.StateSerializer;
import cn.daydayup.dev.workflow.state.AgentStateFactory;

import java.io.*;

public abstract class PlainTextStateSerializer extends StateSerializer<OverAllState> {

	protected PlainTextStateSerializer(AgentStateFactory<OverAllState> stateFactory) {
		super(stateFactory);
	}

	@Override
	public String mimeType() {
		return "plain/text";
	}

	@SuppressWarnings("unchecked")
	public Class<OverAllState> getStateType() {
		return OverAllState.class;
	}

	public OverAllState read(String data) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();

		try (ObjectOutputStream out = new ObjectOutputStream(bytesStream)) {
			out.writeUTF(data);
			out.flush();
		}

		try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bytesStream.toByteArray()))) {
			return read(in);
		}

	}

	public OverAllState read(Reader reader) throws IOException, ClassNotFoundException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(reader)) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line).append(System.lineSeparator());
			}
		}
		return read(sb.toString());
	}

}
