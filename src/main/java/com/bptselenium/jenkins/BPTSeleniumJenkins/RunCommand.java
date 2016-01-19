/*
 * The MIT License
 *
 * Copyright 2016 Brainspire.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.bptselenium.jenkins.BPTSeleniumJenkins;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 *
 * @author Brainspire
 * Runs the command specified in the command variable set within jenkins -- expects an exit code of 10, with a non-10 exit code, set the build result to unstable if the build hasn't already failed or been marked as unstable
 */
public class RunCommand {
    public static void runCommand(String command, TaskListener listener, Run<?,?> build){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CommandLine cmdLine = CommandLine.parse(command);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        Executor executor = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        executor.setExitValue(10);
        executor.setWatchdog(watchdog);
        try{
            executor.execute(cmdLine);
        }catch(ExecuteException ee){
            //getting a non-standard execution value, set build result to unstable
            Result result = Result.UNSTABLE;
            if (build.getResult() == null) {
                build.setResult(result);
            } else if (build.getResult().isBetterThan(result)) {
                build.setResult(result.combine(build.getResult()));
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            listener.getLogger().println(outputStream.toString());
        }
    }
}
