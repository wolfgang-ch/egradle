package de.jcup.egradle.eclipse.gradleeditor.preferences;
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
 

import static de.jcup.egradle.eclipse.gradleeditor.preferences.GradleEditorPreferenceConstants.*;
import static de.jcup.egradle.eclipse.gradleeditor.preferences.GradleEditorPreferences.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

import de.jcup.egradle.eclipse.gradleeditor.GradleEditorColorConstants;

/**
 * Class used to initialize default preference values.
 */
public class GradleEditorPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = EDITOR_PREFERENCES.getPreferenceStore();
		store.setDefault(P_LINK_OUTLINE_WITH_EDITOR.getId(), true);
		
		/* bracket rendering configuration */
		store.setDefault(P_EDITOR_MATCHING_BRACKETS_ENABLED.getId(), true); // per default matching is enabled, but without the two other special parts
		store.setDefault(P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION.getId(), false);
		store.setDefault(P_EDITOR_ENCLOSING_BRACKETS.getId(), false);
		/* bracket color */
		PreferenceConverter.setDefault(store, P_EDITOR_MATCHING_BRACKETS_COLOR.getId(), GradleEditorColorConstants.DEFAULT_CARET_HIGHLIGHTING);
		
	}

}