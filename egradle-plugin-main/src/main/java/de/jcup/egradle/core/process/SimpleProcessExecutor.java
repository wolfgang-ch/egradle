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
package de.jcup.egradle.core.process;

import static org.apache.commons.lang3.Validate.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import de.jcup.egradle.core.domain.CancelStateProvider;

public class SimpleProcessExecutor implements ProcessExecutor {

	
	public static final String MESSAGE__EXECUTION_CANCELED_BY_USER = "[Execution CANCELED by user]";
	
	protected OutputHandler handler;
	private boolean handleProcessOutputStream;
	private long timeOutInSeconds=ENDLESS_RUNNING;
	/**
	 * Simple process executor implementation
	 * @param outputHandler handle process information output
	 * @param handleProcessOutputStream when true process output stream will be fetched and handled by given {@link OutputHandler} too
	 * @param timeOutInSeconds - time out in seconds,  0 = endless running
	 */
	public SimpleProcessExecutor(OutputHandler outputHandler, boolean handleProcessOutputStream, int timeOutInSeconds) {
		if (outputHandler==null){
			outputHandler= OutputHandler.NO_OUTPUT;
		}
		this.handler = outputHandler;
		this.handleProcessOutputStream=handleProcessOutputStream;
		this.timeOutInSeconds = timeOutInSeconds;
	}

	@Override
	public int execute(ProcessConfiguration wdProvider, EnvironmentProvider envProvider, ProcessContext processContext, String... commands) throws IOException {
		notNull(wdProvider, "'wdProvider' may not be null");
		notNull(envProvider, "'envProvider' may not be null");
		String wd = wdProvider.getWorkingDirectory();
		/* Working directory*/
		File workingDirectory = null;
		if (StringUtils.isNotBlank(wd)){
			workingDirectory=new File(wd);
		}
		if (workingDirectory != null) {
			if (!workingDirectory.exists()) {
				throw new FileNotFoundException("Working directory does not exist:" + workingDirectory);
			}
		}
		/* Create process with dedicated environment*/
		ProcessBuilder pb = new ProcessBuilder(commands);
		Map<String, String> env = envProvider.getEnvironment();
		/* init environment */
		if (env != null) {
			Map<String, String> pbEnv = pb.environment();
			for (String key : env.keySet()) {
				pbEnv.put(key, env.get(key));
			}
		}
		/* init working directory */
		pb.directory(workingDirectory);
		pb.redirectErrorStream(true);

		Date started = new Date();
		Process p = startProcess(pb);
		ProcessTimeoutTerminator timeoutTerminator = null;
		if (timeOutInSeconds!=ENDLESS_RUNNING){
			timeoutTerminator = new ProcessTimeoutTerminator(p,handler,timeOutInSeconds);
			timeoutTerminator.start();
		}
		ProcessCancelTerminator cancelTerminator = new ProcessCancelTerminator(p, processContext.getCancelStateProvider());
		Thread cancelCheckThread = new Thread(cancelTerminator,"process-cancel-terminator");
		cancelCheckThread.start();
		
		handleProcessStarted(envProvider, p, started, workingDirectory, commands);
		
		handleOutputStreams(p, timeoutTerminator, processContext.getCancelStateProvider());
		
		/* wait for execution */
		try {
			while (isAlive(p)){
				waitFor(p);
			}
		} catch (InterruptedException e) {
			/* ignore */
		}
		/* done */
		int exitValue = p.exitValue();
		handleProcessEnd(p);
		return exitValue;
	}

	void waitFor(Process p) throws InterruptedException {
		p.waitFor(3,TimeUnit.SECONDS);
	}

	boolean isAlive(Process p) {
		return p.isAlive();
	}

	Process startProcess(ProcessBuilder pb) throws IOException {
		return pb.start();
	}

	class ProcessCancelTerminator implements Runnable{
		static final int TIME_TO_WAIT_FOR_NEXT_CANCEL_CHECK = 200;
		private Process process;
		private CancelStateProvider cancelStateProvider;

		public ProcessCancelTerminator(Process p, CancelStateProvider provider){
			this.process=p;
			this.cancelStateProvider= provider;
		}

		@Override
		public void run() {
			while (isAlive(process)) {
				if (cancelStateProvider.isCanceled()){
					handler.output(MESSAGE__EXECUTION_CANCELED_BY_USER);
					process.destroy();
					break;
				}
				try {
					Thread.sleep(TIME_TO_WAIT_FOR_NEXT_CANCEL_CHECK);
				} catch (InterruptedException e) {
					/* ignore */
				}
			}
		}
	}
	
	/**
	 * @return <code>true</code> when ongoing output restarts timeout
	 */
	protected boolean isOutputRestartingTimeout(){
		return true;
	}
	
	/**
	 * If process output handling is enabled this method handles output as long as the process is running and returning output.
	 * If process output handling is NOT enabled it just returns
	 * @param p
	 * @param timeoutTerminator
	 * @param cancelStateProvider
	 * @throws IOException
	 */
	protected void handleOutputStreams(Process p, ProcessTimeoutTerminator timeoutTerminator, CancelStateProvider cancelStateProvider) throws IOException {
		if (!handleProcessOutputStream){
			return;
		}
		if (cancelStateProvider.isCanceled()){
			return;
		}
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))){
			String line = null;
			while ((!cancelStateProvider.isCanceled()) && (line = reader.readLine()) != null) {
				handler.output(line);
				if (timeoutTerminator!=null){
					if (isOutputRestartingTimeout()){
						timeoutTerminator.reset();
					}
				}
			}
		};
		
	}

	/**
	 * Handle process end - process can have failed (result != 0...)
	 * @param p process
	 */
	protected void handleProcessEnd(Process p) {
		/* per default nothing special to do*/
	}


	protected void handleProcessStarted(EnvironmentProvider context, Process p, Date started, File workingDirectory,
			String[] commands) {

	}

}
