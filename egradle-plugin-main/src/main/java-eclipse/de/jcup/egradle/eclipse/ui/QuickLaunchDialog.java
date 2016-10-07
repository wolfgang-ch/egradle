package de.jcup.egradle.eclipse.ui;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.jcup.egradle.eclipse.api.EGradleUtil;

public class QuickLaunchDialog extends PopupDialog {

	private static final boolean GRAB_FOCUS = true;
	private static final boolean PERSIST_SIZE = false;
	private static final boolean PERSIST_BOUNDS = false;
	private static final boolean SHOW_DIALOG_MENU = false;
	private static final boolean SHOW_PERSIST_ACTIONS = false;
	private static final String TITLE = "EGradle quick launch";
	private static final String INFOTEXT = "Enter your tasks (press enter to execute)";

	public QuickLaunchDialog(Shell parent) {
		super(parent, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, GRAB_FOCUS, PERSIST_SIZE, PERSIST_BOUNDS,
				SHOW_DIALOG_MENU, SHOW_PERSIST_ACTIONS, TITLE, INFOTEXT);
	}

	@Override
	public int open() {
		int value = super.open();
		runEventLoop(getShell());
		return value;
	}
	
	public String getValue(){
		return inputText;
	}
	
	private void runEventLoop(Shell loopShell) {
		Display display;
		if (getShell() == null) {
			display = Display.getCurrent();
		} else {
			display = loopShell.getDisplay();
		}

		while (loopShell != null && !loopShell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
				EGradleUtil.log(e);
			}
		}
		if (!display.isDisposed()) {
			display.update();
		}
	}
	private String inputText;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		boolean isWin32 = Util.isWindows();
		GridLayoutFactory.fillDefaults().extendedMargins(isWin32 ? 0 : 3, 3, 2, 2).applyTo(composite);

		Text text = new Text(composite, SWT.NONE);
		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent event) {
				if (event.character == '\r' ){
					inputText = text.getText();
					close();
				}
			}
		});
		return composite;
	}

	@Override
	protected boolean canHandleShellCloseEvent() {
		return true;
	}

	/**
	 * Just for direct simple UI testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Image backgroundImage = new Image(display, "./icons/gradle-build-failed.png");
		Image titleImage = new Image(display, "./icons/gradle-og.gif");
		Shell shell = new Shell(display);
		shell.setText("Shell");
		shell.setSize(200, 200);
		shell.open();

		QuickLaunchDialog dialog = new QuickLaunchDialog(shell);
		dialog.open();
		String input = dialog.getValue();
		System.out.println("input was:"+input);
		display.dispose();

	}
}
