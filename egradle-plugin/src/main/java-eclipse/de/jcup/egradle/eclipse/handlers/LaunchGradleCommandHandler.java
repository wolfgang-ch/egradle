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
package de.jcup.egradle.eclipse.handlers;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ParameterValuesException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;

import de.jcup.egradle.core.domain.GradleCommand;
import de.jcup.egradle.core.domain.GradleContext;
import de.jcup.egradle.core.domain.GradleSubproject;
import de.jcup.egradle.core.process.ProcessOutputHandler;
import de.jcup.egradle.core.process.SimpleProcessExecutor;
import de.jcup.egradle.eclipse.execution.EclipseGradleExecution;
import de.jcup.egradle.eclipse.launch.EGradleLaunchConfigurationMainTab;
import de.jcup.egradle.eclipse.launch.EGradleLaunchDelegate;

/**
 * This handler is only for launching. So complete mechanism is same as on
 * normal handlers
 * 
 * @author Albert Tregnaghi
 *
 */
public class LaunchGradleCommandHandler extends AbstractEGradleCommandHandler {

	public static final String COMMAND_ID = "egradle.commands.launch";
	public static final String PARAMETER_LAUNCHCONFIG = "egradle.command.launch.config";

	private GradleCommand[] commands;
	private ILaunch launch;

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IParameter configparameter = event.getCommand().getParameter(PARAMETER_LAUNCHCONFIG);
			IParameterValues configParamValues = configparameter.getValues();
			Map<?, ?> values = configParamValues.getParameterValues();
			String projectName = (String) values.get(EGradleLaunchConfigurationMainTab.PROPERTY_PROJECTNAME);
			String commandString = (String) values.get(EGradleLaunchConfigurationMainTab.PROPERTY_ARGUMENTS);
			launch = (ILaunch) values.get(EGradleLaunchDelegate.LAUNCH_ARGUMENT);

			String[] commandStrings = commandString.split(" ");
			if (StringUtils.isEmpty(projectName)) {
				this.commands = GradleCommand.build(commandStrings);
			} else {
				this.commands = GradleCommand.build(new GradleSubproject(projectName), commandStrings);
			}

		} catch (NotDefinedException | ParameterValuesException e) {
			throw new IllegalStateException("Cannot fetch command parameter!", e);
		}
		return super.execute(event);
	}

	protected EclipseGradleExecution createGradleExecution(ProcessOutputHandler processOutputHandler, GradleContext context) {
		return new EclipseGradleExecution(processOutputHandler, context, new SimpleProcessExecutor(processOutputHandler) {
			@Override
			protected void handleProcessStarted(Process process) {
				/*
				 * bind process to runtime process, so visible and correct
				 * handled in debug UI
				 */
				RuntimeProcess rp = new RuntimeProcess(launch, process, context.getCommandString(),
						context.getEnvironment());
			}
			
			@Override
			protected void handleOutputStreams(Process p) throws IOException {
				/* do nothing - is printed to console output on current launcher*/
			}
		});
	}

	@Override
	protected GradleCommand[] createCommands() {
		return commands;
	}

}
