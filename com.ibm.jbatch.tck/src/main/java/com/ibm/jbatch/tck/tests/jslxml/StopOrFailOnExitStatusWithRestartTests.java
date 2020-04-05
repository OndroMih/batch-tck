/*
 * Copyright 2012 International Business Machines Corp.
 * 
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.jbatch.tck.tests.jslxml;

import static com.ibm.jbatch.tck.utils.AssertionUtils.assertObjEquals;
import static com.ibm.jbatch.tck.utils.AssertionUtils.assertWithMessage;

import java.util.Properties;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

import com.ibm.jbatch.tck.utils.JobOperatorBridge;
import com.ibm.jbatch.tck.utils.TCKJobExecutionWrapper;
import java.util.logging.Logger;
import org.junit.jupiter.api.*;

public class StopOrFailOnExitStatusWithRestartTests {

	private static final Logger logger = Logger.getLogger(StopOrFailOnExitStatusWithRestartTests.class.getName());
	private JobOperatorBridge jobOp;

	private void begin(String str) {
		logger.info("Begin test method: " + str+"<p>");
	}

        @BeforeEach
	public void setUp() throws Exception {
		jobOp = new JobOperatorBridge();
	}

	public static void cleanup() throws Exception {
	}

	/*
	 * @testName: testInvokeJobWithUserStopAndRestart
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test

	public void testInvokeJobWithUserStopAndRestart() throws Exception {

		String METHOD = "testInvokeJobWithUserStopAndRestart";
		begin(METHOD);

		final String DEFAULT_SLEEP_TIME = "5000";

		try {
			logger.info("Locate job XML file: job_batchlet_longrunning.xml<p>");

			logger.info("Create job parameters for execution #1:<p>");
			Properties overrideJobParams = new Properties();
			logger.info("run.indefinitely=true<p>");
			overrideJobParams.setProperty("run.indefinitely" , "true");

			logger.info("Invoke startJobWithoutWaitingForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobWithoutWaitingForResult("job_batchlet_longrunning", overrideJobParams);

			long execID = execution1.getExecutionId(); 
			logger.info("StopRestart: Started job with execId=" + execID + "<p>");

			int sleepTime = Integer.parseInt(System.getProperty("StopOrFailOnExitStatusWithRestartTests.testInvokeJobWithUserStop.sleep",DEFAULT_SLEEP_TIME));
			logger.info("Sleep " +  sleepTime  + "<p>");
			Thread.sleep(sleepTime); 

			BatchStatus exec1BatchStatus = execution1.getBatchStatus();
			logger.info("execution #1 JobExecution getBatchStatus()="+ exec1BatchStatus + "<p>");
			
			// Bug 5614 - Tolerate STARTING state in addition to STARTED 
			boolean startedOrStarting = exec1BatchStatus == BatchStatus.STARTED || exec1BatchStatus == BatchStatus.STARTING;
			assertWithMessage("Found BatchStatus of " + exec1BatchStatus.toString() + "; Hopefully job isn't finished already, if it is fail the test and use a longer sleep time within the batch step-related artifact.", startedOrStarting);

			logger.info("Invoke stopJobAndWaitForResult");
			jobOp.stopJobAndWaitForResult(execution1);

			JobExecution postStopJobExecution = jobOp.getJobExecution(execution1.getExecutionId());
			logger.info("execution #1 JobExecution getBatchStatus()="+postStopJobExecution.getBatchStatus()+"<p>");
			assertWithMessage("The stop should have taken effect by now, even though the batchlet artifact had control at the time of the stop, it should have returned control by now.", 
					BatchStatus.STOPPED, postStopJobExecution.getBatchStatus());  

			logger.info("execution #1 JobExecution getBatchStatus()="+postStopJobExecution.getExitStatus()+"<p>");
			assertWithMessage("If this assert fails with an exit status of STOPPED, try increasing the sleep time. It's possible" +
					"the JobOperator stop is being issued before the Batchlet has a chance to run.", "BATCHLET CANCELED BEFORE COMPLETION", postStopJobExecution.getExitStatus());

			logger.info("Create job parameters for execution #2:<p>");
			logger.info("run.indefinitely=false<p>");
			overrideJobParams.setProperty("run.indefinitely" , "false");


			logger.info("Invoke restartJobAndWaitForResult with executionId: " + execution1.getInstanceId() + "<p>");
			JobExecution execution2 = jobOp.restartJobAndWaitForResult(execution1.getExecutionId(),overrideJobParams);

			logger.info("execution #2 JobExecution getBatchStatus()="+execution2.getBatchStatus()+"<p>");
			assertWithMessage("If the restarted job hasn't completed yet then try increasing the sleep time.", 
					BatchStatus.COMPLETED, execution2.getBatchStatus());

			logger.info("execution #2 JobExecution getExitStatus()="+execution2.getExitStatus()+"<p>");
			assertWithMessage("If this fails, the reason could be that step 1 didn't run the second time," + 
					"though it should since it won't have completed successfully the first time.", 
					"GOOD.STEP.GOOD.STEP", execution2.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobWithUncaughtExceptionFailAndRestart
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test

	public void testInvokeJobWithUncaughtExceptionFailAndRestart() throws Exception {
		String METHOD = "testInvokeJobWithUncaughtExceptionFailAndRestart";
		begin(METHOD);

		try {
			logger.info("Locate job XML file: job_batchlet_longrunning.xml<p>");

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParameters = new Properties();
			logger.info("throw.exc.on.number.3=true<p>");
			jobParameters.setProperty("throw.exc.on.number.3" , "true");  // JSL default is 'false'

			logger.info("Invoke startJobAndWaitForResult");
			TCKJobExecutionWrapper firstJobExecution = jobOp.startJobAndWaitForResult("job_batchlet_longrunning", jobParameters);

			logger.info("Started job with execId=" + firstJobExecution.getExecutionId()+"<p>");       

			logger.info("execution #1 JobExecution getBatchStatus()="+firstJobExecution.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+firstJobExecution.getExitStatus()+"<p>");
			assertWithMessage("If the job hasn't failed yet then try increasing the sleep time.", BatchStatus.FAILED, firstJobExecution.getBatchStatus());    
			assertObjEquals("FAILED", firstJobExecution.getExitStatus());

			logger.info("Create job parameters for execution #2:<p>");
			Properties overrideJobParams = new Properties();
			logger.info("throw.exc.on.number.3=false<p>");
			logger.info("run.indefinitely=false<p>");
			overrideJobParams.setProperty("throw.exc.on.number.3" , "false");
			overrideJobParams.setProperty("run.indefinitely" , "false");

			logger.info("Invoke restartJobAndWaitForResult with executionId: " + firstJobExecution.getInstanceId() + "<p>");
			JobExecution secondJobExecution = jobOp.restartJobAndWaitForResult(firstJobExecution.getExecutionId(),overrideJobParams);

			logger.info("execution #2 JobExecution getBatchStatus()="+secondJobExecution.getBatchStatus()+"<p>");
			assertWithMessage("If the restarted job hasn't completed yet then try increasing the sleep time.", 
					BatchStatus.COMPLETED, secondJobExecution.getBatchStatus());

			logger.info("execution #2 JobExecution getExitStatus()="+secondJobExecution.getExitStatus()+"<p>");
			assertWithMessage("If this fails with only \"GOOD.STEP\", the reason could be that step 1 didn't run the second time," + 
					"though it should since it won't have completed successfully the first time.", 
					"GOOD.STEP.GOOD.STEP", secondJobExecution.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}



	private static void handleException(String methodName, Exception e) throws Exception {
		logger.info("Caught exception: " + e.getMessage()+"<p>");
		logger.info(methodName + " failed<p>");
		throw e;
	}
}
