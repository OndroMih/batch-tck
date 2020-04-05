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

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.batch.operations.JobExecutionAlreadyCompleteException;
import javax.batch.operations.JobExecutionIsRunningException;
import javax.batch.operations.JobRestartException;
import javax.batch.operations.NoSuchJobException;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.JobInstance;
import javax.batch.runtime.Metric;
import javax.batch.runtime.StepExecution;


import com.ibm.jbatch.tck.utils.JobOperatorBridge;
import com.ibm.jbatch.tck.utils.TCKJobExecutionWrapper;
import org.junit.jupiter.api.*;

public class JobOperatorTests {

	private final static Logger logger = Logger.getLogger(JobOperatorTests.class.getName());

	private JobOperatorBridge jobOp;

	/* cleanup */
	public void  cleanup()
	{		

	}

	@BeforeEach
	public void setUp() throws Exception {
		jobOp = new JobOperatorBridge();
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	private void begin(String str) {
		logger.info("Begin test method: " + str);
	}

	private static void handleException(String methodName, Exception e) throws Exception {
		logger.info("Caught exception: " + e.getMessage()+"<p>");
		logger.info(methodName + " failed<p>");
		throw e;
	}

	/*
	 * @testName: testJobOperatorStart
	 * 
	 * @assertion:  Job Operator - start
	 * @test_Strategy: start a job that completes successfully with no exceptions thrown.
	 * @throws JobStartException               
	 */
	@Test

	public void testJobOperatorStart() throws Exception {

		String METHOD = "testJobOperatorStart";
		begin(METHOD);

		try {
			logger.info("Locate job XML file: job_batchlet_1step.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_1step");

			logger.info("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		} 

	}

	/*
	 * @testName: testJobOperatorRestart
	 * 
	 * @assertion:  Job Operator - restart.  Tests also that a restart JobExecution is associated with the
	 *             same JobInstance as the original execution.
	 * @test_Strategy: start a job that is configured to fail. Change configuration of job to ensure success. Restart job.
	 *                 Test that job completes successfully and no exceptions are thrown.
	 * @throws JobExecutionAlreadyCompleteException
	 * @throws NoSuchJobExecutionException
	 * @throws JobExecutionNotMostRecentException 
	 * @throws JobRestartException
	 *                 
	 */
	@Test

	public void testJobOperatorRestart() throws Exception {

		String METHOD = "testJobOperatorRestart";
		begin(METHOD);

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=12<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.next.writepoints=10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.next.writepoints", "10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			long jobInstanceId = execution1.getInstanceId();
			long lastExecutionId = execution1.getExecutionId();
			TCKJobExecutionWrapper exec = null;
			logger.info("Got Job instance id: " + jobInstanceId + "<p>");
			logger.info("Got Job execution id: " + lastExecutionId + "<p>");
			{
				logger.info("Invoke restartJobAndWaitForResult for execution id: " + lastExecutionId + "<p>");
				exec = jobOp.restartJobAndWaitForResult(lastExecutionId, jobParams);
				logger.info("execution #2 JobExecution getBatchStatus()="+exec.getBatchStatus()+"<p>");
				logger.info("execution #2 JobExecution getExitStatus()="+exec.getExitStatus()+"<p>");
				logger.info("execution #2 Job instance id="+exec.getInstanceId()+"<p>");
				assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
				assertWithMessage("Testing execution #2", "COMPLETED", exec.getExitStatus());
				assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());  
			}
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOperatorRestartAlreadyCompleteException
	 * 
	 * @assertion: testJobOperatorRestartAlreadyCompleteException
	 * @test_Strategy: start a job that is configured to fail. Change configuration of job to ensure success. Restart job
	 *                 and let it run to completion.  Then restart another time and confirm a JobExecutionAlreadyCompleteException
	 *                 is caught.
	 *                 
	 *                 The first start isn't serving a huge purpose, just making the end-to-end test slightly more interesting.
	 *                 
	 *                 Though it might be interesting to restart from the first executionId, this would force the issue
	*                  as to whether the implementation throws JobExecutionAlreadyCompleteException or JobExecutionNotMostRecentException.
	*                  We don't want to specify a behavior not in the spec, and the spec purposely avoids overspecifying exception behavior.
	*                  The net is we only restart from the second id.
	*                   
	 * @throws JobExecutionAlreadyCompleteException
	 * @throws NoSuchJobExecutionException
	 * @throws JobExecutionNotMostRecentException 
	 * @throws JobRestartException
	 *                 
	 */
	@Test

	public void testJobOperatorRestartAlreadyCompleteException() throws Exception {

		String METHOD = "testJobOperatorRestartAlreadyCompleteException";
		begin(METHOD);

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=12<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.next.writepoints=10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.next.writepoints", "10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			long jobInstanceId = execution1.getInstanceId();
			long firstExecutionId = execution1.getExecutionId();
			TCKJobExecutionWrapper exec = null;
			logger.info("Got Job instance id: " + jobInstanceId + "<p>");
			logger.info("Got Job execution id: " + firstExecutionId + "<p>");
			{
				logger.info("Invoke restartJobAndWaitForResult for execution id: " + firstExecutionId + "<p>");
				exec = jobOp.restartJobAndWaitForResult(firstExecutionId, jobParams);
				logger.info("execution #2 JobExecution getBatchStatus()="+exec.getBatchStatus()+"<p>");
				logger.info("execution #2 JobExecution getExitStatus()="+exec.getExitStatus()+"<p>");
				logger.info("execution #2 Job instance id="+exec.getInstanceId()+"<p>");
				assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
				assertWithMessage("Testing execution #2", "COMPLETED", exec.getExitStatus());
				assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());  
			}

			long secondExecutionId = exec.getExecutionId();
			logger.info("execution #2 Job execution id="+ secondExecutionId+"<p>");


			// Restart from second execution id (not first, see strategy comment above).
			logger.info("Now invoke restart again, expecting JobExecutionAlreadyCompleteException, for execution id: " + secondExecutionId + "<p>");
			boolean seenException = false;
			try {
				jobOp.restartJobAndWaitForResult(secondExecutionId, jobParams);
			} catch (JobExecutionAlreadyCompleteException e) {
				logger.info("Caught JobExecutionAlreadyCompleteException as expected<p>");
				seenException = true;
			}
			assertWithMessage("Caught JobExecutionAlreadyCompleteException for bad restart #2" , seenException);

		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}
	
	/*
	 * @testName: testJobOperatorAbandonJobDuringARestart
	 * 
	 * @assertion: testJobOperatorAbandonJobDuringARestart
	 * @test_Strategy: start a job that is configured to fail. Change configuration of job to cause the job to sleep for 5 seconds. Restart job
	 *                 and let it run without waiting for completion.  Attempt to abandon the job and confirm a JobExecutionIsRunningException
	 *                 is caught.
	 *                 
	 *                  
	 * @throws JobExecutionAlreadyCompleteException
	 * @throws NoSuchJobExecutionException
	 * @throws JobExecutionNotMostRecentException 
	 * @throws JobRestartException
	 *                 
	 */
	@Test

	public void testJobOperatorAbandonJobDuringARestart() throws Exception {

		String METHOD = "testJobOperatorAbandonJobDuringARestart";
		begin(METHOD);
		
		String DEFAULT_SLEEP_TIME = "5000";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			jobParams.put("execution.number", "1");
			logger.info("execution.number=1<p>");
			String sleepTime = System.getProperty("JobOperatorTests.testJobOperatorTestAbandonActiveRestart.sleep",DEFAULT_SLEEP_TIME);
			jobParams.put("sleep.time", sleepTime);
			logger.info("sleep.time=" + sleepTime + "<p>");
			

			logger.info("Locate job XML file: abandonActiveRestart.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("abandonActiveRestart", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			long jobInstanceId = execution1.getInstanceId();
			long firstExecutionId = execution1.getExecutionId();
			TCKJobExecutionWrapper exec = null;
			logger.info("Got Job instance id: " + jobInstanceId + "<p>");
			logger.info("Got Job execution id: " + firstExecutionId + "<p>");
			
			jobParams = new Properties();
			jobParams.put("execution.number", "2");
			logger.info("execution.number=2<p>");
			sleepTime = System.getProperty("JobOperatorTests.testJobOperatorTestAbandonActiveRestart.sleep",DEFAULT_SLEEP_TIME);
			jobParams.put("sleep.time", sleepTime);
			logger.info("sleep.time=" + sleepTime + "<p>");
			
			logger.info("Invoke restartJobWithoutWaitingForResult for execution id: " + firstExecutionId + "<p>");
			exec = jobOp.restartJobWithoutWaitingForResult(firstExecutionId, jobParams);
			long secondExecutionId = exec.getExecutionId();			
			{
				logger.info("Invoke restartJobAndWaitForResult for execution id: " + firstExecutionId + "<p>");
				
				boolean seen = false;
				try {
					jobOp.abandonJobExecution(secondExecutionId);
				}
				catch (JobExecutionIsRunningException jobRunningEx){
					logger.info("Caught JobExecutionIsRunningException as expected<p>");
					seen = true;
				}
				assertWithMessage("Did not see expected JobExecutionIsRunningException for abandon attempt during restart" , seen);
				
			}

		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}
	
	/*
	 * @testName: testJobOperatorRestartJobAlreadyAbandoned
	 * 
	 * @assertion: testJobOperatorRestartJobAlreadyAbandoned
	 * @test_Strategy: start a job that is configured to fail. Once Job fails, abandon it.
	 *                 Attempt to restart the already abandon the job and confirm a JobRestartException
	 *                 is caught.
	 *                 
	 *                  
	 * @throws JobExecutionAlreadyCompleteException
	 * @throws NoSuchJobExecutionException
	 * @throws JobExecutionNotMostRecentException 
	 * @throws JobRestartException
	 *                 
	 */
	@Test

	public void testJobOperatorRestartJobAlreadyAbandoned() throws Exception {

		String METHOD = "testJobOperatorRestartAlreadyCompleteException";
		begin(METHOD);
		
		String DEFAULT_SLEEP_TIME = "1";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			jobParams.put("execution.number", "1");
			logger.info("execution.number=1<p>");
			String sleepTime = System.getProperty("JobOperatorTests.testJobOperatorTestRestartAlreadAbandonedJob.sleep",DEFAULT_SLEEP_TIME);
			jobParams.put("sleep.time", sleepTime);
			logger.info("sleep.time=" + sleepTime + "<p>");
			

			logger.info("Locate job XML file: abandonActiveRestart.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("abandonActiveRestart", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());
			long jobInstanceId = execution1.getInstanceId();
			long firstExecutionId = execution1.getExecutionId();
			
			jobOp.abandonJobExecution(firstExecutionId);
			
			logger.info("Got Job instance id: " + jobInstanceId + "<p>");
			logger.info("Got Job execution id: " + firstExecutionId + "<p>");
			
			jobParams = new Properties();
			jobParams.put("execution.number", "2");
			logger.info("execution.number=2<p>");
			
			logger.info("Invoke restartJobWithoutWaitingForResult for execution id: " + firstExecutionId + "<p>");
						
			{
				logger.info("Invoke restartJobAndWaitForResult for execution id: " + firstExecutionId + "<p>");
				
				boolean seen = false;
				try {
					jobOp.restartJobWithoutWaitingForResult(firstExecutionId, jobParams);
				}
				catch (JobRestartException jobRestartEx){
					logger.info("Caught JobRestartException as expected<p>");
					seen = true;
				}
				assertWithMessage("Caught JobRestartException for abandon attempt during restart" , seen);
				
			}

		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}
	
	/*
	 * @testName: testInvokeJobWithUserStop
	 * @assertion: The batch status of a job is set to stopped after it is stopped through the job operator
	 * @test_Strategy: Issue a job that runs in an infinite loop. Issue a job operator stop and verify the 
	 * batch status.
	 */
	@Test

	public void testInvokeJobWithUserStop() throws Exception {
		String METHOD = "testInvokeJobWithUserStop";
		begin(METHOD);
		
		final String DEFAULT_SLEEP_TIME = "1000";

		try {
			logger.info("Locate job XML file: job_batchlet_longrunning.xml<p>");

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParameters = new Properties();
			logger.info("run.indefinitely=true<p>");
			jobParameters.setProperty("run.indefinitely" , "true");

			logger.info("Invoking startJobWithoutWaitingForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobWithoutWaitingForResult("job_batchlet_longrunning", jobParameters);

			int sleepTime = Integer.parseInt(System.getProperty("JobOperatorTests.testInvokeJobWithUserStop.sleep",DEFAULT_SLEEP_TIME));
			logger.info("Thread.sleep(" +  sleepTime + ")<p>");
			Thread.sleep(sleepTime);

			logger.info("Invoking stopJobAndWaitForResult for Execution #1<p>");
			jobOp.stopJobAndWaitForResult(jobExec);

			JobExecution jobExec2 = jobOp.getJobExecution(jobExec.getExecutionId());
			logger.info("execution #1 JobExecution getBatchStatus()="+jobExec2.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.STOPPED, jobExec2.getBatchStatus());

		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOperatorGetStepExecutions
	 * 
	 * @assertion:  Job Operator - getStepExecutions
	 * @test_Strategy: start a job that completes successfully. Get the list of all StepException objects associated with job execution id.
	 *                 Test that all objects retrieved are of type StepExecution.
	 * @throws Exception
	 */
	@Test

	public void testJobOperatorGetStepExecutions() throws Exception {

		String METHOD = "testJobOperatorGetStepExecutions";
		begin(METHOD);

		try {
			logger.info("Locate job XML file: job_batchlet_1step.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_1step");

			logger.info("Obtaining StepExecutions for execution id: " + jobExec.getExecutionId() + "<p>");
			List<StepExecution> stepExecutions = jobOp.getStepExecutions(jobExec.getExecutionId());

			assertObjEquals(1, stepExecutions.size());

			for (StepExecution step : stepExecutions) {
				// make sure all steps finish successfully
				showStepState(step);
				logger.info("Step status="+step.getBatchStatus()+"<p>");
				assertObjEquals(BatchStatus.COMPLETED, step.getBatchStatus());
			}

			logger.info("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		} 

	}

	/*
	 * @testName: testJobOpGetJobNames
	 * 
	 * @assertion:  Job Operator - getJobNames
	 * @test_Strategy: This test is a bit weak in that, while the first time it runs,
	 *                 it does perform a real validation that the newly-submitted job
	 *                 is added to the getJobNames() result set, on subsequent runs
	 *                 it may have already been there from before and so isn't newly 
	 *                 verifying anything.  This is a simple function to implement so not
	 *                 a big deal.
	 */
	@Test

	public void testJobOpGetJobNames() throws Exception {

		String METHOD = "testJobOpGetJobNames";
		begin(METHOD);

		String jobName ="job_unique_get_job_names";
		
		try {
			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			List<String> jobNames = jobOp.getJobNames();
			jobOp.startJobWithoutWaitingForResult("job_unique_get_job_names");
			if (jobNames.contains(jobName)) {
				logger.info("JobOperator.getJobNames() already includes " + jobName + ", test is not so useful<p>");
			} else {
				logger.info("JobOperator.getJobNames() does not include " + jobName + " yet.<p>");
			}

			jobNames = jobOp.getJobNames();
			assertWithMessage("Now JobOperator.getJobNames() definitely includes " + jobName, jobNames.contains(jobName));
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testAbandoned
	 * 
	 * @assertion:  Job Operator - abandon()
	 * @test_Strategy: run a job that completes successfully. Abandon the job. Test to ensure the Batch Status for the said job is marked as 'ABANDONED'
	 * @throws Exception
	 * 
	 */
	@Test

	public void testAbandoned() throws Exception {

		String METHOD = "testAbandoned";
		begin(METHOD);

		try {
			logger.info("Locate job XML file: job_batchlet_4steps.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_4steps");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());

			jobOp.abandonJobExecution(jobExec.getExecutionId());

			JobExecution jobExec2 = jobOp.getJobExecution(jobExec.getExecutionId());
			assertObjEquals(BatchStatus.ABANDONED, jobExec2.getBatchStatus());

		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testJobOpgetJobInstanceCount
	 * 
	 * @assertion:  Job Operator - getJobInstanceCount
	 * @test_Strategy: Retrieve the job instance count for a known job name. Run that job. 
	 *                 Retrieve the job instance count for that job again. Test that the count has increased by 1.
	 * @throws Exception
	 * 
	 */
	@Test  

	public void testJobOpgetJobInstanceCount() throws Exception {
		String METHOD = "testJobOpgetJobInstanceCount";
		begin(METHOD);

		try {

			int countTrackerBEFORE = 0;

			try {
				countTrackerBEFORE = jobOp.getJobInstanceCount("chunksize5commitinterval5");
			} catch (NoSuchJobException e) {
				// Can continue.
			}

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.faile=12<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			long jobInstanceId = execution1.getInstanceId();
			long lastExecutionId = execution1.getExecutionId();
			logger.info("Got Job instance id: " + jobInstanceId + "<p>");
			logger.info("Got Job execution id: " + lastExecutionId + "<p>");

			int countTrackerAFTER = jobOp.getJobInstanceCount("chunksize5commitinterval5");

			assertWithMessage("job count for job1 increased by 1", 1, countTrackerAFTER - countTrackerBEFORE);

			List<String> jobNames = jobOp.getJobNames();

			for (String jobname : jobNames) {
				logger.info(jobname + " instance count : " + jobOp.getJobInstanceCount(jobname) + " - ");
			}
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testJobOpgetJobInstanceCountException
	 * 
	 * @assertion:  Job Operator - getJobInstanceCountException
	 * @test_Strategy: Retrieve the job instance count for a known job name. Run that job. 
	 *                 Retrieve the job instance count for a job name that does not exist. Test that the NoSuchJobException is returned.
	 * @throws Exception
	 * 
	 */
	@Test  

	public void testJobOpgetJobInstanceCountException() throws Exception {
		String METHOD = "testJobOpgetJobInstanceCountException";
		begin(METHOD);

		try {
			int countTrackerBEFORE = 0;

			try {
				countTrackerBEFORE = jobOp.getJobInstanceCount("ChunkStopOnEndOn");
			} catch (NoSuchJobException e) {
				// Can continue.
			}

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.faile=12<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			long jobInstanceId = execution1.getInstanceId();
			long lastExecutionId = execution1.getExecutionId();
			logger.info("Got Job instance id: " + jobInstanceId + "<p>");
			logger.info("Got Job execution id: " + lastExecutionId + "<p>");

			boolean seenException = false;
			try {
				int countTrackerAFTER = jobOp.getJobInstanceCount("NoSuchJob");
			} catch (NoSuchJobException noJobEx) {
				logger.info("Confirmed we caught NoSuchJobException<p>");
				seenException = true;
			}
			assertWithMessage("Saw NoSuchJobException for job 'NoSuchJob'", seenException);
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOpgetJobInstances
	 * 
	 * @assertion:  Job Operator - getJobInstances
	 * @test_Strategy: start a job 10 times which will ensure at least one job instance known to the runtime. 
	 *                 Retrieve a list of job instance ids for the job name just started. Ask for the first 200 found.
	 *                 Test that size grows by 10. 
	 * @throws Exception
	 * 
	 */
	@Test  

	public void testJobOpgetJobInstances() throws Exception {
		String METHOD = " testJobOpgetJobInstances";
		begin(METHOD);

		int submitTimes = 10;
		
		try {
			int countTrackerBEFORE = 0;
			int countTrackerAFTER = 0;

			try {
				countTrackerBEFORE = jobOp.getJobInstanceCount("chunksize5commitinterval5");
				logger.info("Before test ran the JobInstance count for chunksize5commitinterval5 was " + countTrackerBEFORE +"<p>");
			} catch (NoSuchJobException e) {
				logger.info("Not an error, but just the first time executing this job <p>");
			}

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.faile=12<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			for (int i=0; i < submitTimes; i++) {
				jobOp.startJobWithoutWaitingForResult("chunksize5commitinterval5", jobParams);
			}

			List<JobInstance> jobInstances = null;
			try {
				jobInstances = jobOp.getJobInstances("chunksize5commitinterval5", 0, 10);
				countTrackerAFTER = jobOp.getJobInstanceCount("chunksize5commitinterval5");
				assertWithMessage("Check that we see: " + submitTimes + " new submissions", 
						submitTimes, countTrackerAFTER - countTrackerBEFORE);
			} catch (NoSuchJobException noJobEx) {
				logger.info("Failing test, caught NoSuchJobException<p>");
				throw noJobEx;
			}

			logger.info("Size of Job Instances list = " + jobInstances.size() + "<p>");
			assertWithMessage("Testing that a list of Job Instances were obtained", 10, jobInstances.size());

		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOpgetJobInstancesException
	 * 
	 * @assertion:  Job Operator - getJobInstancesException
	 * @test_Strategy: Retrieve a list of job instances for a job name that does not exist. 
	 *                 Test that the NoSuchJobException is thrown.
	 * @throws  Exception 
	 * 
	 */
	@Test  

	public void testJobOpgetJobInstancesException() throws Exception {
		String METHOD = "testJobOpgetJobInstancesException";
		begin(METHOD);

		try {

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.faile=12<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");


			logger.info("Locate job XML file: /chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			List<JobInstance> jobIds = null;
			boolean seenException = false;
			try {
				jobIds = jobOp.getJobInstances("NoSuchJob", 0, 12);
			} catch (NoSuchJobException noJobEx) {
				seenException = true;
				logger.info("Confirmed we caught NoSuchJobException<p>");
			}
			assertWithMessage("Saw NoSuchJobException for job 'NoSuchJob'", seenException);
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOperatorGetParameters
	 * 
	 * @assertion:  Job Operator - getParameters
	 * @test_Strategy: Start a job with a set of parameters. Restart the job with a set of override parameters. Once completed, retrieve the 
	 *                 parameters object associated with the job instance. Test that the object retrieved is a Properties object. 
	 *                 Test that the NoSuchJobException is thrown.
	 * @throws Exception
	 * 
	 */
	@Test  

	public void testJobOperatorGetParameters() throws Exception {
		String METHOD = "testJobOperatorGetParameters";
		begin(METHOD);

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties originalJobParams = new Properties();
			Properties restartJobParams = null;
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=12<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.next.writepoints=10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			originalJobParams.put("execution.number", "1");
			originalJobParams.put("readrecord.fail", "12");
			originalJobParams.put("app.arraysize", "30");
			originalJobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			originalJobParams.put("app.next.writepoints", "10,15,20,25,30");
			originalJobParams.put("app.commitinterval", "5");
			
			// Expected parameters on restart only.
			String N1="extra.parm.name1";  String V1="extra.parm.value1";
			String N2="extra.parm.name2";  String V2="extra.parm.value2";

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", originalJobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			long jobInstanceId = execution1.getInstanceId();
			long lastExecutionId = execution1.getExecutionId();
			TCKJobExecutionWrapper execution2 = null;
			logger.info("Got Job instance id: " + jobInstanceId + "<p>");
			logger.info("Got Job execution id: " + lastExecutionId + "<p>");
			{
				logger.info("Invoke clone original job execution parameters<p>");
				// Shallow copy is OK for simple <String,String> of java.util.Properties
				restartJobParams = (Properties)originalJobParams.clone(); 
				logger.info("Put some extra parms in the restart execution<p>");
				restartJobParams.put(N1, V1);
				restartJobParams.put(N2, V2);
				logger.info("Invoke restartJobAndWaitForResult for execution id: " + lastExecutionId + "<p>");
				execution2 = jobOp.restartJobAndWaitForResult(lastExecutionId, restartJobParams);
				logger.info("execution #2 JobExecution getBatchStatus()="+execution2.getBatchStatus()+"<p>");
				logger.info("execution #2 JobExecution getExitStatus()="+execution2.getExitStatus()+"<p>");
				logger.info("execution #2 Job instance id="+execution2.getInstanceId()+"<p>");
				assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, execution2.getBatchStatus());
				assertWithMessage("Testing execution #2", "COMPLETED", execution2.getExitStatus());
				assertWithMessage("Testing execution #2", jobInstanceId, execution2.getInstanceId());  
			}
			
			// Test original execution
			Properties jobParamsFromJobOperator = jobOp.getParameters(execution1.getExecutionId());
			Properties jobParamsFromJobExecution = execution1.getJobParameters();
  			assertWithMessage("Comparing original job params with jobOperator.getParameters", originalJobParams, jobParamsFromJobOperator);
			logger.info("JobOperator.getParameters() matches for original execution <p>");
			assertWithMessage("Comparing original job params with jobExecution.getParameters", originalJobParams, jobParamsFromJobExecution);
			logger.info("JobExecution.getParameters() matches for original execution <p>");
			
			// Test restart execution
			Properties restartJobParamsFromJobOperator = jobOp.getParameters(execution2.getExecutionId());
			Properties restartJobParamsFromJobExecution = execution2.getJobParameters();
			assertWithMessage("Comparing restart job params with jobOperator.getParameters", restartJobParams, restartJobParamsFromJobOperator);
			logger.info("JobOperator.getParameters() matches for restart execution <p>");
			assertWithMessage("Comparing restart job params with jobExecution.getParameters", restartJobParams, restartJobParamsFromJobExecution);
			logger.info("JobExecution.getParameters() matches for restart execution <p>");
			
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOperatorGetJobInstances
	 * 
	 * @assertion:  Job Operator - getJobInstances
	 * @test_Strategy: Start a specific job four times, all of which will finish successfully. Retrieve two separate lists of JobExecutions for the job.
	 *                 List 1 will contain JobExecution Objects for job start 1 - 3. List 2 will contain JobExecution Objects for job start 2 - 4.
	 *                 Test that the second and third JobExecution objects of List 1 is equivalent to the first and second JobExecution objects in List 2.
	 *                                   
	 * @throws Exception
	 * 
	 */
	@Test  

	public void testJobOperatorGetJobInstances() throws Exception {
		String METHOD = "testJobOperatorGetJobInstances";
		begin(METHOD);

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=31<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "31");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "COMPLETED", execution1.getExitStatus());

			logger.info("Create job parameters for execution #2:<p>");
			jobParams = new Properties();
			logger.info("execution.number=2<p>");
			logger.info("readrecord.fail=31<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "31");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Invoke startJobAndWaitForResult for execution #2<p>");
			JobExecution execution2 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);
			logger.info("execution #2 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #2 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, execution2.getBatchStatus());
			assertWithMessage("Testing execution #2", "COMPLETED", execution2.getExitStatus());

			logger.info("Invoke startJobAndWaitForResult for execution #3<p>");
			JobExecution execution3 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);
			logger.info("execution #3 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #3 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #3", BatchStatus.COMPLETED, execution3.getBatchStatus());
			assertWithMessage("Testing execution #3", "COMPLETED", execution3.getExitStatus());

			logger.info("Invoke startJobAndWaitForResult for execution #4<p>");
			JobExecution execution4 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);
			logger.info("execution #4 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #4 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #4", BatchStatus.COMPLETED, execution4.getBatchStatus());
			assertWithMessage("Testing execution #4", "COMPLETED", execution4.getExitStatus());

			List <JobInstance> jobInstances012 = jobOp.getJobInstances("chunksize5commitinterval5",0,3);
			List <JobInstance> jobInstances123 = jobOp.getJobInstances("chunksize5commitinterval5",1,3);

			for (int i=0; i<3; i++){
				logger.fine("AJM: instance id012["+i+"] = " + jobInstances012.get(i).getInstanceId());
				logger.fine("AJM: instance id123["+i+"] = " + jobInstances123.get(i).getInstanceId());
			}

			assertWithMessage("job instances should not be equal", jobInstances012.get(0).getInstanceId()!=jobInstances123.get(0).getInstanceId()); 
			assertWithMessage("job instances should be equal", jobInstances012.get(1).getInstanceId()==jobInstances123.get(0).getInstanceId()); 
			assertWithMessage("job instances should be equal", jobInstances012.get(2).getInstanceId()==jobInstances123.get(1).getInstanceId()); 
			assertWithMessage("job instances should not be equal", jobInstances012.get(2).getInstanceId()!=jobInstances123.get(2).getInstanceId()); 

			logger.info("Size of jobInstancesList = " + jobInstances012.size() + "<p>");
			logger.info("Testing retrieval of the JobInstances list, size = " + jobInstances012.size() + "<p>");
			assertWithMessage("Testing retrieval of the JobInstances list", jobInstances012.size() > 0);
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOperatorGetRunningJobExecutions
	 * 
	 * @assertion:  Job Operator - getRunningExecutions
	 * @test_Strategy: start a job which will ensure at least one job execution is known to the runtime. Job will be long running. Testcase does not wait for job to complete.
	 *                 Retrieve a list of JobExecution(s) for the job name just started that are in running state. Ensure that at least one JobExecution is returned
	 *                 Test that 
	 * @throws Exception
	 * 
	 */
	@Test

	public void testJobOperatorGetRunningJobExecutions() throws Exception {
		String METHOD = "testJobOperatorGetRunningJobExecutions";
		begin(METHOD);
		
		final String DEFAULT_SLEEP_TIME = "1000";
		final String DEFAULT_APP_TIME_INTERVAL = "10000";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			String timeinterval = System.getProperty("JobOperatorTests.testJobOperatorGetRunningJobExecutions.app.timeinterval",DEFAULT_APP_TIME_INTERVAL);
			
			jobParams.put("app.timeinterval", timeinterval);

			logger.info("Invoke startJobWithoutWaitingForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobWithoutWaitingForResult("job_batchlet_step_listener", jobParams);

			Properties newJobParameters = new Properties();
			newJobParameters.put("app.timeinterval", timeinterval);
			logger.info("Invoke startJobWithoutWaitingForResult<p>");

			JobExecution exec = jobOp.startJobWithoutWaitingForResult("job_batchlet_step_listener", newJobParameters);

			// Sleep to give the runtime the chance to start the job.  The job has a delay built into the stepListener afterStep() 
			// so we aren't worried about the job finishing early leaving zero running executions.
			int sleepTime = Integer.parseInt(System.getProperty("ExecutionTests.testJobOperatorGetRunningJobExecutions.sleep",DEFAULT_SLEEP_TIME));
			logger.info("Thread.sleep(" + sleepTime + ")<p>");
			Thread.sleep(sleepTime);

			List<Long> jobExecutions = jobOp.getRunningExecutions("job_batchlet_step_listener");
			assertWithMessage("Found job instances in the RUNNING state", jobExecutions.size() > 0);

		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOperatorGetRunningJobInstancesException
	 * 
	 * @assertion:  Job Operator - getJobInstances
	 * @test_Strategy: start a job which will ensure at least one job instance known to the runtime. Job will be long running. Testcase does not wait for job to complete.
	 *                 Retrieve a list of job instance ids for a job name that does not exist in running state. Ensure that NoSuchJobException exception is thrown
	 *                 Test that 
	 * @throws Exception
	 * 
	 */
	@Test

	public void testJobOperatorGetRunningJobInstancesException() throws Exception {
		String METHOD = "testJobOperatorGetRunningJobInstancesException";
		begin(METHOD);
		
		final String DEFAULT_APP_TIME_INTERVAL = "10000";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
		
			String timeinterval = System.getProperty("JobOperatorTests.testJobOperatorGetRunningJobInstancesException.app.timeinterval",DEFAULT_APP_TIME_INTERVAL);
			
			jobParams.put("app.timeinterval", timeinterval);

			logger.info("Invoke startJobWithoutWaitingForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobWithoutWaitingForResult("job_batchlet_step_listener", jobParams);

			Properties restartJobParameters = new Properties();
			restartJobParameters.put("app.timeinterval", timeinterval);

			logger.info("Invoke startJobWithoutWaitingForResult");
			JobExecution exec = jobOp.startJobWithoutWaitingForResult("job_batchlet_step_listener", restartJobParameters);

			boolean seenException = false;
			try {
				logger.info("Check for an instance of a non-existent job<p>");
				jobOp.getRunningExecutions("JOBNAMEDOESNOTEXIST");
			}
			catch (NoSuchJobException e) {
				logger.info("Confirmed that exception caught is an instanceof NoSuchJobException<p>");
				seenException = true;
			}
			assertWithMessage("Saw NoSuchJobException for job 'JOBNAMEDOESNOTEXIST'", seenException);
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}
	/*
	 * @testName: testJobOperatorGetJobExecution
	 * 
	 * @assertion:  Job Operator - getJobExecution
	 * @test_Strategy: start a job which will run to successful completion.
	 *                 Retrieve a JobExecution object using the execution ID returned by the start command.
	 *                 Ensure the object returned is an instance of JobExecution
	 *                  
	 * @throws Exception 
	 * 
	 */
	@Test 

	public void testJobOperatorGetJobExecution() throws Exception {
		String METHOD = "testJobOperatorGetJobExecution";
		begin(METHOD);

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=12<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
			logger.info("app.next.writepoints=10,15,20,25,30<p>");
			logger.info("app.commitinterval=5<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.next.writepoints", "10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval5", jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			long jobInstanceId = execution1.getInstanceId();
			long lastExecutionId = execution1.getExecutionId();
			TCKJobExecutionWrapper exec = null;

			{

				logger.info("Invoke restartJobAndWaitForResult for execution id: " + lastExecutionId + "<p>");
				exec = jobOp.restartJobAndWaitForResult(lastExecutionId, jobParams);
				lastExecutionId = exec.getExecutionId();
				logger.info("execution #2 JobExecution getBatchStatus()="+exec.getBatchStatus()+"<p>");
				logger.info("execution #2 JobExecution getExitStatus()="+exec.getExitStatus()+"<p>");
				logger.info("execution #2 Job instance id="+exec.getInstanceId()+"<p>");
				assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
				assertWithMessage("Testing execution #2", "COMPLETED", exec.getExitStatus());
				assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());  
			}

			logger.info("Testing retrieval of a JobExecution obj");
			JobExecution jobEx = jobOp.getJobExecution(lastExecutionId);
			logger.info("Status retreived from JobExecution obj: " + jobEx.getBatchStatus() + "<p>");
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testJobOperatorGetJobExecutions
	 * 
	 * @assertion:  Job Operator - getJobExecutions and JobExecution APIs
	 * @test_Strategy: start a job which will fail, then restart and run to successful completion.
	 *                 Validate the two JobExecution instances, e.g. ensure both map to the same
	 *                 JobInstance when getJobInstance() is passed the respective executionIds.
	 *                 Also ensure that getJobExecutions() on the instance returns these same two executionIds. 
	 * @throws Exception 
	 * 
	 */
	@Test 

	public void testJobOperatorGetJobExecutions() throws Exception {
		String METHOD = "testJobOperatorGetJobExecutions";
		begin(METHOD);

		String jobName = "chunksize5commitinterval5";
		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
			jobParams.put("app.next.writepoints", "10,15,20,25,30");
			jobParams.put("app.commitinterval", "5");

			logger.info("Locate job XML file: chunksize5commitinterval5.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult(jobName, jobParams);
		    
			logger.info("execution #1 JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("execution #1 JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

			long execution1ID= execution1.getExecutionId();
			TCKJobExecutionWrapper execution2 = null;

			{
				logger.info("Invoke restartJobAndWaitForResult for execution id: " + execution1ID + "<p>");
				execution2= jobOp.restartJobAndWaitForResult(execution1ID, jobParams);
				logger.info("execution #2 JobExecution getBatchStatus()="+execution2.getBatchStatus()+"<p>");
				logger.info("execution #2 JobExecution getExitStatus()="+execution2.getExitStatus()+"<p>");
				logger.info("execution #2 Job instance id="+execution2.getInstanceId()+"<p>");
				assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, execution2.getBatchStatus());
				assertWithMessage("Testing execution #2", "COMPLETED", execution2.getExitStatus());
			}
			
			// Execution 1 and 2 have the same instanceId
			assertWithMessage("Testing execution #1 and execution #2 use the same instanceId", 
					execution1.getInstanceId(), execution2.getInstanceId());  

			long execution2ID= execution2.getExecutionId();
			JobInstance jobInstance = jobOp.getJobInstance(execution2ID);
			
			// Verify getJobExecutions() based on instance gives us the same two JobExecution(s);
			List<JobExecution> jobExecutions = jobOp.getJobExecutions(jobInstance);
			assertWithMessage("Testing list size of JobExecutions", 2, jobExecutions.size());
			boolean seen1 = false; boolean seen2=false;
			for (JobExecution je : jobExecutions){
				if (je.getExecutionId() == execution1ID) {
					assertWithMessage("Dup of execution 1", !seen1);
					logger.info("Seen execution #1 <p>");
					seen1= true;
				} else if (je.getExecutionId() == execution2ID) {
					assertWithMessage("Dup of execution 2", !seen2);
					logger.info("Seen execution #2 <p>");
					seen2= true;
				} 
			}
			assertWithMessage("Seen both of the two JobExecutions", seen1 && seen2);
			
			assertWithMessage("Job name from JobInstance matches", jobName, jobInstance.getJobName());
			assertWithMessage("Job name from JobExecution 1 matches", jobName, execution1.getJobName());
			assertWithMessage("Job name from JobExecution 2 matches", jobName, execution2.getJobName());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}


	private void showStepState(StepExecution step) {


		logger.info("---------------------------");
		logger.info("getStepName(): " + step.getStepName() + " - ");
		logger.info("getJobExecutionId(): " + step.getStepExecutionId() + " - ");
		//System.out.print("getStepExecutionId(): " + step.getStepExecutionId() + " - ");
		Metric[] metrics = step.getMetrics();

		for (int i = 0; i < metrics.length; i++) {
			logger.info(metrics[i].getType() + ": " + metrics[i].getValue() + " - ");
		}

		logger.info("getStartTime(): " + step.getStartTime() + " - ");
		logger.info("getEndTime(): " + step.getEndTime() + " - ");
		//System.out.print("getLastUpdateTime(): " + step.getLastUpdateTime() + " - ");
		logger.info("getBatchStatus(): " + step.getBatchStatus() + " - ");
		logger.info("getExitStatus(): " + step.getExitStatus());
		logger.info("---------------------------");
	}
}
