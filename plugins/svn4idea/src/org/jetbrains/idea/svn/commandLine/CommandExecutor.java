/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.svn.commandLine;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EventDispatcher;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 1/25/12
 * Time: 12:58 PM
 */
public class CommandExecutor {
  static final Logger LOG = Logger.getInstance(CommandExecutor.class.getName());
  private final AtomicReference<Integer> myExitCodeReference;

  private boolean myIsDestroyed;
  private boolean myNeedsDestroy;
  protected final GeneralCommandLine myCommandLine;
  private Process myProcess;
  private OSProcessHandler myHandler;
  // TODO: Try to implement commands in a way that they manually indicate if they need full output - to prevent situations
  // TODO: when large amount of data needs to be stored instead of just sequential processing.
  private CapturingProcessAdapter outputAdapter;
  private final Object myLock;

  private final EventDispatcher<LineCommandListener> myListeners = EventDispatcher.create(LineCommandListener.class);

  private final AtomicBoolean myWasError = new AtomicBoolean(false);
  @NotNull private final AtomicReference<Throwable> myExceptionRef;
  @Nullable private final LineCommandListener myResultBuilder;
  @NotNull private final Command myCommand;

  public CommandExecutor(@NotNull @NonNls String exePath, @NotNull Command command) {
    myCommand = command;
    myResultBuilder = command.getResultBuilder();
    if (myResultBuilder != null)
    {
      myListeners.addListener(myResultBuilder);
      // cancel tracker should be executed after result builder
      myListeners.addListener(new CommandCancelTracker());
    }
    myLock = new Object();
    myCommandLine = new GeneralCommandLine();
    myCommandLine.setExePath(exePath);
    myCommandLine.setWorkDirectory(command.getWorkingDirectory());
    if (command.getConfigDir() != null) {
      myCommandLine.addParameters("--config-dir", command.getConfigDir().getPath());
    }
    myCommandLine.addParameter(command.getName().getName());
    myCommandLine.addParameters(command.getParameters());
    myExitCodeReference = new AtomicReference<Integer>();
    myExceptionRef = new AtomicReference<Throwable>();
  }

  /**
   * Indicates if process was destroyed "manually" by command execution logic.
   *
   * @return
   */
  public boolean isManuallyDestroyed() {
    return myIsDestroyed;
  }

  public void start() {
    synchronized (myLock) {
      checkNotStarted();

      try {
        myProcess = myCommandLine.createProcess();
        if (LOG.isDebugEnabled()) {
          LOG.debug(myCommandLine.toString());
        }
        myHandler = new OSProcessHandler(myProcess, myCommandLine.getCommandLineString());
        startHandlingStreams();
      } catch (Throwable t) {
        listeners().startFailed(t);
        myExceptionRef.set(t);
      }
    }
  }

  private void startHandlingStreams() {
    outputAdapter = new CapturingProcessAdapter();
    myHandler.addProcessListener(outputAdapter);
    myHandler.addProcessListener(new ProcessTracker());
    myHandler.addProcessListener(new ResultBuilderNotifier(listeners()));
    myHandler.addProcessListener(new CommandOutputLogger());
    myHandler.startNotify();
  }

  public String getOutput() {
    return outputAdapter.getOutput().getStdout();
  }

  public String getErrorOutput() {
    return outputAdapter.getOutput().getStderr();
  }

  /**
   * Wait for process termination
   * @param timeout
   */
  public boolean waitFor(int timeout) {
    checkStarted();
    final OSProcessHandler handler;
    synchronized (myLock) {
      // TODO: This line seems to cause situation when exitCode is not set before SvnLineCommand.runCommand() is finished.
      // TODO: Carefully analyze behavior (on all operating systems) and fix.
      if (myIsDestroyed) return true;
      handler = myHandler;
    }
    if (timeout == -1) {
      return handler.waitFor();
    }
    else {
      return handler.waitFor(timeout);
    }
  }

  public void cancel() {
    synchronized (myLock) {
      checkStarted();
      destroyProcess();
    }
  }

  public void run() throws SvnBindException {
    start();
    boolean finished;
    do {
      finished = waitFor(500);
      if (!finished && (wasError() || needsDestroy())) {
        waitFor(1000);
        doDestroyProcess();
        break;
      }
    }
    while (!finished);

    throwIfError();
  }

  public void addListener(final LineCommandListener listener) {
    synchronized (myLock) {
      myListeners.addListener(listener);
    }
  }

  protected LineCommandListener listeners() {
    synchronized (myLock) {
      return myListeners.getMulticaster();
    }
  }

  public void destroyProcess() {
    synchronized (myLock) {
      myNeedsDestroy = true;
    }
  }

  /**
   * ProcessHandler.destroyProcess() implementations could acquire read lock in its implementation - like OSProcessManager.getInstance().
   * Some commands are called under write lock - which is generally bad idea, but such logic is not refactored yet.
   * To prevent deadlocks this method should only be called from thread that started the process.
   */
  public void doDestroyProcess() {
    synchronized (myLock) {
      if (!myIsDestroyed) {
        LOG.info("Destroying process by command: " + getCommandText());
        myIsDestroyed = true;
        myHandler.destroyProcess();
      }
    }
  }

  public boolean needsDestroy() {
    synchronized (myLock) {
      return myNeedsDestroy;
    }
  }

  public String getCommandText() {
    synchronized (myLock) {
      return StringUtil.join(myCommandLine.getExePath(), " ", myCommand.getText());
    }
  }

  /**
   * check that process is not started yet
   *
   * @throws IllegalStateException if process has been already started
   */
  private void checkNotStarted() {
    if (isStarted()) {
      throw new IllegalStateException("The process has been already started");
    }
  }

  /**
   * check that process is started
   *
   * @throws IllegalStateException if process has not been started
   */
  protected void checkStarted() {
    if (! isStarted()) {
      throw new IllegalStateException("The process is not started yet");
    }
  }

  /**
   * @return true if process is started
   */
  public boolean isStarted() {
    synchronized (myLock) {
      return myProcess != null;
    }
  }

  public SvnCommandName getCommandName() {
    return myCommand.getName();
  }

  public Integer getExitCodeReference() {
    return myExitCodeReference.get();
  }

  public void setExitCodeReference(int value) {
    myExitCodeReference.set(value);
  }

  public Boolean wasError() {
    return myWasError.get();
  }

  public void throwIfError() throws SvnBindException {
    Throwable error = myExceptionRef.get();

    if (error != null) {
      throw new SvnBindException(error);
    }
  }

  private class CommandCancelTracker extends LineCommandAdapter {
    @Override
    public void onLineAvailable(String line, Key outputType) {
      if (myResultBuilder != null && myResultBuilder.isCanceled()) {
        LOG.info("Cancelling command: " + getCommandText());
        destroyProcess();
      }
    }
  }

  private class ProcessTracker extends ProcessAdapter {

    @Override
    public void processTerminated(ProcessEvent event) {
      setExitCodeReference(event.getExitCode());
    }

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
      if (ProcessOutputTypes.STDERR == outputType) {
        myWasError.set(true);
      }
    }
  }
}
