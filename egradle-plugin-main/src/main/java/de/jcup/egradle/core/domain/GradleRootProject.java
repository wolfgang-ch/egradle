/*
 * Copyright 2016 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */
package de.jcup.egradle.core.domain;

import static org.apache.commons.lang3.Validate.*;

import java.io.File;
import java.io.IOException;

public class GradleRootProject extends AbstractGradleProject {

	private File file;

	/**
	 * Creates a gradle root project
	 * 
	 * @param file
	 * @throws IOException
	 *             when given fils is not a directory or does not exists
	 */
	public GradleRootProject(File file) throws IOException {
		notNull(file);
		if (!file.exists()) {
			throw new IOException("Given root project folder does not exist:" + file);
		}
		if (!file.isDirectory()) {
			throw new IOException("Given root project folder is not a directory:" + file);
		}
		this.file = file;
	}

	public File getFolder() {
		return file;
	}
}
