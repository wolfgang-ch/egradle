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
package de.jcup.egradle.eclipse.gradleeditor;

import static de.jcup.egradle.eclipse.gradleeditor.preferences.GradleEditorPreferenceConstants.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.jcup.egradle.core.model.Item;
import de.jcup.egradle.eclipse.api.ColorManager;
import de.jcup.egradle.eclipse.api.EGradleUtil;
import de.jcup.egradle.eclipse.gradleeditor.document.GradleDocumentProvider;
import de.jcup.egradle.eclipse.gradleeditor.outline.GradleEditorContentOutlinePage;
import de.jcup.egradle.eclipse.gradleeditor.outline.GradleEditorOutlineContentProvider;
import de.jcup.egradle.eclipse.gradleeditor.outline.QuickOutlineDialog;
import de.jcup.egradle.eclipse.gradleeditor.preferences.GradleEditorPreferences;
public class GradleEditor extends TextEditor implements StatusMessageSupport {

	/** The COMMAND_ID of this editor as defined in plugin.xml */
	public static final String EDITOR_ID = "org.egradle.editors.GradleEditor";
	/** The COMMAND_ID of the editor context menu */
	public static final String EDITOR_CONTEXT_MENU_ID = EDITOR_ID + ".context";
	/** The COMMAND_ID of the editor ruler context menu */
	public static final String EDITOR_RULER_CONTEXT_MENU_ID = EDITOR_CONTEXT_MENU_ID + ".ruler";

	


	private GradleEditorContentOutlinePage outlinePage;

	private DelayedDocumentListener documentListener;

	private GradleEditorOutlineContentProvider contentProvider;

	private Object monitor = new Object();

	private boolean quickOutlineOpened;

	private int lastCaretPosition;

	private boolean dirty;

	private GradleBracketsSupport bracketMatcher = new GradleBracketsSupport();

	public GradleEditor() {
		setPreferenceStore(GradleEditorPreferences.EDITOR_PREFERENCES.getPreferenceStore());
		setSourceViewerConfiguration(new GradleSourceViewerConfiguration(this));
		setDocumentProvider(new GradleDocumentProvider());

		/*
		 * TODO ATR, 25.11.2016 - even when same content provider, the model
		 * itself will always be new created by outlines. Is this really
		 * necessary are should we have reuse of model and a dedicated trigger
		 * to rebuild model ?!
		 */
		contentProvider = new GradleEditorOutlineContentProvider(this);
		outlinePage = new GradleEditorContentOutlinePage(this);
		documentListener = new DelayedDocumentListener();

	}

	public void setErrorMessage(String message){
		super.setStatusLineErrorMessage(message);
	}
	
	public GradleBracketsSupport getBracketMatcher() {
		return bracketMatcher;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		Control adapter = getAdapter(Control.class);
		if (adapter instanceof StyledText) {
			StyledText text = (StyledText) adapter;
			text.addCaretListener(new GradleEditorCaretListener());
		}

		activateGradleEditorContext();

	}

	@Override
	public void dispose() {
		super.dispose();
		if (bracketMatcher != null) {
			bracketMatcher.dispose();
			bracketMatcher = null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (GradleEditor.class.equals(adapter)) {
			return (T) this;
		}
		if (ColorManager.class.equals(adapter)){
			return (T) getColorManager();
		}
		if (IFile.class.equals(adapter)){
			IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput){
				IFileEditorInput feditorInput = (IFileEditorInput) input;
				return (T) feditorInput.getFile();
			}
			return null; 
		}
		if (IContentOutlinePage.class.equals(adapter)) {
			return (T) outlinePage;
		}
		if (ITreeContentProvider.class.equals(adapter) || GradleEditorOutlineContentProvider.class.equals(adapter)) {
			return (T) contentProvider;
		}
		if (ISourceViewer.class.equals(adapter)){
			return (T) getSourceViewer();
		}
		if (StatusMessageSupport.class.equals(adapter)){
			return (T) this;
		}
		return super.getAdapter(adapter);
	}

	public Item getItemAtCarretPosition() {
		if (contentProvider == null) {
			return null;
		}
		Item item = contentProvider.tryToFindByOffset(lastCaretPosition);
		return item;
	}

	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {

		bracketMatcher.gotoMatchingBracket(this);
	}

	/**
	 * Opens quick outline
	 */
	public void openQuickOutline() {
		synchronized (monitor) {
			if (quickOutlineOpened) {
				/*
				 * already opened - this is in future the anker point for
				 * ctrl+o+o...
				 */
				return;
			}
			quickOutlineOpened = true;
		}
		Shell shell = getEditorSite().getShell();
		QuickOutlineDialog dialog = new QuickOutlineDialog(this, shell);
		IDocument document = getDocumentProvider().getDocument(getEditorInput());
		dialog.setInput(document);
		dialog.open();
		synchronized (monitor) {
			quickOutlineOpened = false;
		}
	}

	public void openSelectedTreeItemInEditor(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object firstElement = ss.getFirstElement();
			if (firstElement instanceof Item) {
				Item item = (Item) firstElement;
				int offset = item.getOffset();
				int length = item.getLength();
				selectAndReveal(offset, length);
			}
		}
	}

	@Override
	public void selectAndReveal(int start, int length) {
		super.selectAndReveal(start, length);
		/* TODO ATR: remove the status line information ?!?!? */
		setStatusLineMessage("selected range: start=" + start + ", length=" + length);
	}

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		// @formatter:off
		support.setCharacterPairMatcher(bracketMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(
				P_EDITOR_MATCHING_BRACKETS_ENABLED.getId(), 
				P_EDITOR_MATCHING_BRACKETS_COLOR.getId(),
				P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION.getId(), 
				P_EDITOR_ENCLOSING_BRACKETS.getId());
		
		super.configureSourceViewerDecorationSupport(support);
		// @formatter:on
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		IDocument document = getDocument();
		document.addDocumentListener(documentListener);
		outlinePage.inputChanged(document);
	}

	protected IDocument getDocument() {
		return getDocumentProvider().getDocument(getEditorInput());
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setEditorContextMenuId(EDITOR_CONTEXT_MENU_ID);
		setRulerContextMenuId(EDITOR_RULER_CONTEXT_MENU_ID);
	}

	private void activateGradleEditorContext() {
		IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		if (contextService != null) {
			contextService.activateContext("org.egradle.editors.GradleEditor.context");
		}
	}

	private ColorManager getColorManager() {
		return Activator.getDefault().getColorManager();
	}

	

	private class DelayedDocumentListener implements IDocumentListener {

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			synchronized (monitor) {
				if (dirty) {
					/* already marked as dirty, nothing to do */
					return;
				}
				dirty = true;
			}
			/*
			 * we use a job to refresh the outline delayed and only when still
			 * dirty. This is to avoid too many updates inside the outline -
			 * otherwise every char entered at keyboard will reload complete AST
			 * ...
			 */
			/*
			 * TODO ATR, 12.11.2016: while caret changes the update may not
			 * proceed, only when caret position no longer moves the update of
			 * the document has to be done
			 */
			Job job = new Job("update gradle editor outline") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					synchronized (monitor) {
						if (!dirty) {
							/* already cleaned up by another job */
							return Status.CANCEL_STATUS;
						}
						dirty = false;
					}
					EGradleUtil.safeAsyncExec(new Runnable() {
						public void run() {
							IDocument document = getDocument();
							outlinePage.inputChanged(document);
						}
					});
					return Status.OK_STATUS;
				}
			};
			job.schedule(1500);
		}

	}

	private class GradleEditorCaretListener implements CaretListener {

		@Override
		public void caretMoved(CaretEvent event) {
			if (event == null) {
				return;
			}
			lastCaretPosition = event.caretOffset;
			/* TODO ATR: remove the status line information ?!?!? */
			setStatusLineMessage("caret moved:" + event.caretOffset);
			if (outlinePage == null) {
				return;
			}
			outlinePage.onEditorCaretMoved(event.caretOffset);
		}

	}

}
