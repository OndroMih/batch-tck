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
import java.util.logging.Logger;

import javax.batch.operations.JobStartException;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

import com.ibm.jbatch.tck.artifacts.common.StatusConstants;
import com.ibm.jbatch.tck.artifacts.specialized.DeciderTestsBatchlet;
import com.ibm.jbatch.tck.utils.JobOperatorBridge;
import org.junit.jupiter.api.*;

public class DeciderTests implements StatusConstants {
	private final static Logger logger = Logger.getLogger(DeciderTests.class.getName());
	private JobOperatorBridge jobOp = null;

	private final static String FORCE_STOP_EXITSTATUS = "STEP_COMPLETE_BUT_FORCE_JOB_STOPPED_STATUS";
	private final static String FORCE_FAIL_EXITSTATUS = "STEP_COMPLETE_BUT_FORCE_JOB_FAILED_STATUS";

	@BeforeEach
	public void setUp() throws Exception {
		jobOp = new JobOperatorBridge();                              
	}

	/* cleanup */
	public void  cleanup()
	{		

	}

	/*
	 * @testName: testDeciderEndNormal
	 * @assertion: Tests that decision child elements: next, step, end, fail, all work as expected (by verifying batch status)
	 *             Tests exit status globbing ('*' and '?') 
	 *             Tests StepExecution from previous step passed to Decider
	 *             Tests that decider can be configured via property
	 *             Tests that JobContext can be utilized in decider (the same instance as that of the earlier steps).
	 *             Tests setting of <stop>, <end>, <fail> @exit-status attribute value.
	 *             
	 * @test_Strategy: The test methods in this class have a tighter coupling than usual.  That's because they all use the
	 *                 same artifacts, the same basic JSL, and the same basic <decision> element (with the same set of glob 
	 *                 patterns). We set up different "branches" in the job execution sequence depending on exit status, 
	 *                 (e.g. stop vs. fail, end with one exit status vs. another), and we take the various branches in the
	 *                 various test methods.
	 *                    
	 *                 1. Batchlet is coded to end with one of two different exit status values depending on @BatchProperty populated by job param set either to:
	 *                       jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);
	 *                              OR
	 *                       jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);
	 *                    Batch status is tested as well
	 *                 2. Decider returns value based on configured property, step execution exit status, and job context transient data. 
	 *                 3. JobContext utilized to adjust "core exit status" by prefixing the number of times the step has run.
	 *                 4. Special globbing chars '*' and '?' are used in the @on values to test exit status globbing.
	 *                 5. Test asserts specific exit status as well as batch status.  In addition to testing the overall test flow this 
	 *                    tests the @exit-status attributes of fail, stop, end.
	 *                    
	 */
	@Test

	public void testDeciderEndNormal() throws Exception {

		String METHOD = "testDeciderEndNormal";

		try {
			// 1. Here "EndSpecial" is the exit status the decider will return if the step exit status
			// is the "special" exit status value.  It is set as a property on the decider.
			logger.info("Build job parameters for EndSpecial exit status<p>");

			Properties jobParameters = new Properties();

			// 2. Here "EndNormal" is the exit status the decider will return if the step exit status
			// is the "normal" exit status value.  It is set as a property on the batchlet and passed
			// along to the decider via stepContext.setTransientUserData().
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value EndNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "EndNormal");
			// 3. This "ACTUAL_VALUE" is a property set on the batchlet.  It will either indicate to end
			// the step with a "normal" or "special" exit status.            
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.NORMAL_VALUE + "<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);

			logger.info("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value EndSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "EndSpecial");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			logger.info("Expected JobExecution getExitStatus()=EndNormal<p>");
			logger.info("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			logger.info("Expected JobExecution getStatus()=COMPLETED<p>");
			logger.info("Actual JobExecution getStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("EndNormal", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testDeciderEndSpecial
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test

	public void testDeciderEndSpecial() throws Exception {
		String METHOD = "testDeciderEndSpecial";

		try {
			logger.info("Build job parameters for EndSpecial exit status<p>");	    

			Properties jobParameters = new Properties();        
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "EndNormal");
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value EndNormal<p>");
			// 1. This is the only test parameter that differs from testDeciderEndNormal().
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.SPECIAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);
			logger.info("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value EndSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "EndSpecial");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			// 2. And the job exit status differs accordingly.
			logger.info("Expected JobExecution getExitStatus()=EndSpecial<p>");
			logger.info("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			logger.info("Expected JobExecution getBatchStatus()=COMPLETED<p>");
			logger.info("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("EndSpecial", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	// See the first two test methods for an explanation of parameter values.
	/*
	 * @testName: testDeciderStopNormal
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test

	public void testDeciderStopNormal() throws Exception {
		String METHOD = " testDeciderStopNormal";

		try {
			logger.info("Build job parameters for StopSpecial exit status<p>");

			Properties jobParameters = new Properties();    
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value StopNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "StopNormal");

			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.NORMAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);

			logger.info("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value StopSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "StopSpecial");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			logger.info("Expected JobExecution getExitStatus()=StopNormal<p>");
			logger.info("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			logger.info("Expected JobExecution getBatchStatus()=STOPPED<p>");
			logger.info("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("StopNormal", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}

	}

	// See the first two test methods for an explanation of parameter values.
	/*
	 * @testName: testDeciderStopSpecial
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test

	public void testDeciderStopSpecial() throws Exception {
		String METHOD = "testDeciderStopSpecial";

		try {
			logger.info("Build job parameters for StopSpecial exit status<p>");

			Properties jobParameters = new Properties();     
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value StopNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "StopNormal");

			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.SPECIAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);

			logger.info("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value StopSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "StopSpecial");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			logger.info("Expected JobExecution getExitStatus()=StopSpecial<p>");
			logger.info("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			logger.info("Expected JobExecution getBatchStatus()=STOPPED<p>");
			logger.info("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("StopSpecial", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	// See the first two test methods for an explanation of parameter values.
	/*
	 * @testName: testDeciderFailNormal
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test

	public void testDeciderFailNormal() throws Exception {

		String METHOD = "testDeciderFailNormal";

		try {
			logger.info("Build job parameters for FailSpecial exit status<p>");	    	

			Properties jobParameters = new Properties();        
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value FailNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "FailNormal");

			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.NORMAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);

			logger.info("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value FailSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "FailSpecial");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			logger.info("Expected JobExecution getExitStatus()=FailNormal<p>");
			logger.info("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			logger.info("Expected JobExecution getBatchStatus()=FAILED<p>");
			logger.info("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("FailNormal", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.FAILED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	// See the first two test methods for an explanation of parameter values.
	/*
	 * @testName: testDeciderFailSpecial
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test

	public void testDeciderFailSpecial() throws Exception {
		String METHOD = "testDeciderFailSpecial";

		try {
			logger.info("Build job parameters for FailSpecial exit status<p>");

			Properties jobParameters = new Properties();        
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value FailNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "FailNormal");

			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.SPECIAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);

			logger.info("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value FailSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "FailSpecial");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			logger.info("Expected JobExecution getExitStatus()=FailSpecial<p>");
			logger.info("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			logger.info("Expected JobExecution getBatchStatus()=FAILED<p>");
			logger.info("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("FailSpecial", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.FAILED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderNextNormal
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test

	public void testDeciderNextNormal() throws Exception {
		String METHOD = "testDeciderNextNormal";

		try {
			logger.info("Build job parameters for NextSpecial exit status<p>");

			Properties jobParameters = new Properties();        
			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value NextNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "NextNormal");

			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.NORMAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);

			logger.info("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value NextSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "NextSpecial");

			logger.info("Create single job listener deciderTestJobListener and get JSL<p>");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_completerun", jobParameters); 

			logger.info("Expected JobExecution getExitStatus()="+GOOD_JOB_EXIT_STATUS+"<p>");
			logger.info("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			logger.info("Expected JobExecution getBatchStatus()=COMPLETED<p>");
			logger.info("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(GOOD_JOB_EXIT_STATUS, jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderNextSpecial
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test

	public void testDeciderNextSpecial() throws Exception {
		String METHOD = "testDeciderNextSpecial";

		try {
			logger.info("Build job parameters for NextSpecial exit status<p>");	

			Properties jobParameters = new Properties();        

			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value NextNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "NextNormal");

			logger.info("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.SPECIAL_VALUE + "<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);

			logger.info("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value NextSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "NextSpecial");

			logger.info("Create single job listener deciderTestJobListener and get JSL<p>");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_completerun", jobParameters); 

			// This actually exits with the exact same status as the "...NextNormal" test.
			logger.info("Expected JobExecution getExitStatus()="+GOOD_JOB_EXIT_STATUS+"<p>");
			logger.info("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			logger.info("Expected JobExecution getBatchStatus()=COMPLETED<p>");
			logger.info("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(GOOD_JOB_EXIT_STATUS, jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderExitStatusIsSetOnJobContext
	 * @assertion:  Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 * @test_Strategy:  The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *   be equal to the return value of the last decision (i.e. Decider#decide). Note the test doesn't necessarily 
	 *   confirm that the exit status is set on the JobContext directly, but this is the intent behind the test method name.
	 */
	@Test

	public void testDeciderExitStatusIsSetOnJobContext() throws Exception {

		String METHOD = "testDeciderExitStatusIsSetOnJobContext";

		try {
			logger.info("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "None");
			jobParameters.setProperty("fail.job.after.this.step", "step1");


			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.FAILED, jobExec.getBatchStatus());
			assertObjEquals("1:"+FORCE_FAIL_EXITSTATUS, jobExec.getExitStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderCannotbeFirstElementOnStart
	 * @assertion: Tests that the first execution element of a job cannot be a decision 
	 *  (it's a misnomer to say 'decider' but we'll leave the test name alone).
	 * @test_Strategy: Since an implementation may either throw a JobStartException or 
	 *  begin an execution which fails, we will pass the test if we see either JobStartException
	 *  or BatchStatus of FAILED.
	 */
	@Test

	public void testDeciderCannotbeFirstElementOnStart() throws Exception {

		String METHOD = "testDeciderCannotbeFirstElementOnStart";

		try {
			boolean seenException = false;
			JobExecution jobExec = null;
			try {
				jobExec = jobOp.startJobAndWaitForResult("decider_as_first_job_element_fails"); 
			} catch (JobStartException e) {
				logger.info("Caught JobStartException:  " + e.getLocalizedMessage());
				seenException = true;
			}

			// If we caught an exception we'd expect that a JobExecution would not have been created,
			// though we won't validate that it wasn't created.  

			// If we didn't catch an exception that we require that the implementation fail the job execution.
			if (!seenException) {
				logger.info("Didn't catch JobStartException, Job Batch Status = " + jobExec.getBatchStatus());
				assertWithMessage("Job should have failed because of decision as first execution element.", BatchStatus.FAILED, jobExec.getBatchStatus());
			}
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromStepAndAllowRestart
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the new, restarted execution (not the original execution). 
	 *                 (See Sec. 10.8 Restart Processing)
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Steps are configured with allow-start-if-complete = true.  Job parameter is used to vary 
	 *                     exit status on original vs. restart execution and, on restart, the
	 *                     StepExecution exit status is asserted to be the one on the restarted execution. 
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 */
	@Test

	public void testDeciderTransitionFromStepAndAllowRestart() throws Exception {
		String METHOD = "testDeciderTransitionFromStepAndAllowRestart";

		try {
			logger.info("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "step1");
			jobParameters.setProperty("fail.job.after.this.step", "None");


			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("1:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");

			logger.info("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}


	}

	/*
	 * @testName: testDeciderTransitionFromStepWithinFlowAndAllowRestart
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the new, restarted execution (not the original execution). 
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that a decision within a flow can terminate the top-level job through appropriate transition elements
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Steps are configured with allow-start-if-complete = true.  Job parameter is used to vary 
	 *                     exit status on original vs. restart execution and, on restart, the
	 *                     StepExecution exit status is asserted to be the one on the restarted execution. 
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 *                  3. A decision within a flow is configured to stop (based on exit status matching against a <stop> element).
	 */
	@Test

	public void testDeciderTransitionFromStepWithinFlowAndAllowRestart() throws Exception {

		String METHOD = "testDeciderTransitionFromStepWithinFlowAndAllowRestart";

		try {
			logger.info("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "flow1step1");
			jobParameters.setProperty("fail.job.after.this.step", "None");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("2:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");

			logger.info("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromFlowAndAllowRestart
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the new, restarted execution (not the original execution). 
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that when a flow is followed by a decision, that the decision element's Decider#decide 
	 *                 will be passed the StepExecution of the last step in the preceding flow.
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Steps are configured with allow-start-if-complete = true.  Job parameter is used to vary 
	 *                     exit status on original vs. restart execution and, on restart, the
	 *                     StepExecution exit status is asserted to be the one on the restarted execution. 
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 */
	@Test

	public void testDeciderTransitionFromFlowAndAllowRestart() throws Exception {

		String METHOD = "testDeciderTransitionFromFlowAndAllowRestart";


		try {
			logger.info("Build job parameters.<p>");    
			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "flow1step2");
			jobParameters.setProperty("fail.job.after.this.step", "None");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("3:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");

			logger.info("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}



	/*
	 * @testName: testDeciderTransitionFromSplitAndAllowRestart
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed StepExecution(s) 
	 *                 corresponding to the new, restarted execution (not the original execution). 
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that when a split is followed by a decision, that the decision element's Decider#decide 
	 *                 will be passed the StepExecution(s) of each of the last steps of the member flows in the preceding split.
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Steps are configured with allow-start-if-complete = true.  Job parameter is used to vary 
	 *                     exit status on original vs. restart execution and, on restart, the
	 *                     StepExecution exit status is asserted to be the one on the restarted execution. 
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 *              	3. NOTE: TODO for future - Perhaps the strategy in asserting that EACH StepExecution is passed should be
	 *                     tightened.  We could go further to assert that the full list of StepExecution(s) is what we'd expect.
	 */
	@Test

	public void testDeciderTransitionFromSplitAndAllowRestart() throws Exception {

		String METHOD = "testDeciderTransitionFromSplitAndAllowRestart";

		try {
			logger.info("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "split1flow1step2");
			jobParameters.setProperty("stop.job.after.this.step2", "split1flow2step2");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_from_split_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("4:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			jobParameters.setProperty("stop.job.after.this.step2", "None");

			logger.info("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("4:split1flow2step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromStepAndAllowRestartFalse
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the original execution (since the step doesn't re-execute on the
	 *                 new, restart execution),  with the original StepExecution's exit status.
	 *                 (See Sec. 10.8 Restart Processing)
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Allow-start-if-complete for the steps is set to "false".   (This is where the "False" in the
	 *                  test method name comes from).   Decision is configured to expect the StepExecution exitStatus
	 *                  from the original execution, and the test will fail if not.  I.e. the exit status from the earlier 
	 *                  execution is confirmed to have been persisted. Job parameter is used to allow Decider#decide
	 *                  to return a different result on restart.
	 *                  JobContext transient user data is used to assert the correct number of decider invocations have
	 *                  been performed.
	 */
	@Test

	public void testDeciderTransitionFromStepAndAllowRestartFalse() throws Exception {

		String METHOD = "testDeciderTransitionFromStepAndAllowRestartFalse";

		try {
			logger.info("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "false");

			jobParameters.setProperty("stop.job.after.this.step", "step1");
			jobParameters.setProperty("fail.job.after.this.step", "None");


			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("1:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			restartJobParameters.setProperty("is.restart", "true");

			logger.info("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromStepWithinFlowAndAllowRestartFalse
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the original execution (since the step doesn't re-execute on the
	 *                 new, restart execution),  with the original StepExecution's exit status.
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that a decision within a flow can terminate the top-level job through appropriate transition elements
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Allow-start-if-complete for the steps is set to "false".   (This is where the "False" in the
	 *                     test method name comes from).   Decision is configured to expect the StepExecution exitStatus
	 *                     from the original execution, and the test will fail if not.  I.e. the exit status from the earlier 
	 *                     execution is confirmed to have been persisted. Job parameter is used to allow Decider#decide
	 *                     to return a different result on restart.      
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 *                  3. A decision within a flow is configured to stop (based on exit status matching against a <stop> element).
	 */
	@Test

	public void testDeciderTransitionFromStepWithinFlowAndAllowRestartFalse() throws Exception {

		String METHOD = "testDeciderTransitionFromStepWithinFlowAndAllowRestartFalse";

		try {
			logger.info("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "false");

			jobParameters.setProperty("stop.job.after.this.step", "flow1step1");
			jobParameters.setProperty("fail.job.after.this.step", "None");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("2:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			restartJobParameters.setProperty("is.restart", "true");

			logger.info("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromFlowAndAllowRestartFalse
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the original execution (since the step doesn't re-execute on the
	 *                 new, restart execution),  with the original StepExecution's exit status.
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that when a flow is followed by a decision, that the decision element's Decider#decide 
	 *                 will be passed the StepExecution of the last step in the preceding flow.
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Allow-start-if-complete for the steps is set to "false".   (This is where the "False" in the
	 *                     test method name comes from).   Decision is configured to expect the StepExecution exitStatus
	 *                     from the original execution, and the test will fail if not.  I.e. the exit status from the earlier 
	 *                     execution is confirmed to have been persisted. Job parameter is used to allow Decider#decide
	 *                     to return a different result on restart.
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 */
	@Test

	public void testDeciderTransitionFromFlowAndAllowRestartFalse() throws Exception {

		String METHOD = "testDeciderTransitionFromFlowAndAllowRestartFalse";

		try {
			logger.info("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "false");

			jobParameters.setProperty("stop.job.after.this.step", "flow1step2");
			jobParameters.setProperty("fail.job.after.this.step", "None");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("3:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			restartJobParameters.setProperty("is.restart", "true");

			logger.info("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}


	/*
	 * @testName: testDeciderTransitionFromSplitAndAllowRestartFalse
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the original execution (since the step doesn't re-execute on the
	 *                 new, restart execution),  with the original StepExecution's exit status.
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that when a split is followed by a decision, that the decision element's Decider#decide 
	 *                 will be passed the StepExecution(s) of each of the last steps of the member flows in the preceding split.
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Allow-start-if-complete for the steps is set to "false".   (This is where the "False" in the
	 *                     test method name comes from).   Decision is configured to expect the StepExecution exitStatus
	 *                     from the original execution, and the test will fail if not.  I.e. the exit status from the earlier 
	 *                     execution is confirmed to have been persisted. Job parameter is used to allow Decider#decide
	 *                     to return a different result on restart.
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 *              	3. NOTE: TODO for future - Perhaps the strategy in asserting that EACH StepExecution is passed should be
	 *                     tightened.  We could go further to assert that the full list of StepExecution(s) is what we'd expect.
	 */
	@Test

	public void testDeciderTransitionFromSplitAndAllowRestartFalse() throws Exception {

		String METHOD = "testDeciderTransitionFromSplitAndAllowRestartFalse";

		try {
			logger.info("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "false");

			jobParameters.setProperty("stop.job.after.this.step", "split1flow1step2");
			jobParameters.setProperty("stop.job.after.this.step2", "split1flow2step2");

			logger.info("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_from_split_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("4:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			restartJobParameters.setProperty("is.restart", "true");

			logger.info("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("4:split1flow2step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}



	private static void handleException(String methodName, Exception e) throws Exception {
		logger.info("Caught exception: " + e.getMessage()+"<p>");
		logger.info(methodName + " failed<p>");
		throw e;
	}

}
