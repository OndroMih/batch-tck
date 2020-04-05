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

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.StepExecution;

import com.ibm.jbatch.tck.utils.JobOperatorBridge;

import org.junit.jupiter.api.*;

public class ContextAndListenerTests {

	private final static Logger logger = Logger.getLogger(ContextAndListenerTests.class.getName());
	private JobOperatorBridge jobOp = null;


	@BeforeEach
	public void setUp() throws Exception {
		jobOp = new JobOperatorBridge();
	}

	/*
	 * @testName: testExamineJobContextInArtifact
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
 
	public void testExamineJobContextInArtifact() throws Exception {

		String METHOD = "testExamineJobContextInArtifact()";

		try {

			logger.info("Locate job XML file: JobContextTestBatchlet.xml<p>");

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("app.timeinterval=10<p>");
			jobParams.put("app.timeinterval", "10");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("JobContextTestBatchlet", jobParams);
		
			
			String testString = "JobName=job1;JobInstanceId=" + jobOp.getJobInstance(execution1.getExecutionId()).getInstanceId() + ";JobExecutionId=" + execution1.getExecutionId();
			logger.info("EXPECTED JobExecution getBatchStatus()=COMPLETED<p>");
			logger.info("ACTUAL JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("EXPECTED JobExecution getExitStatus()="+testString+"<p>");
			logger.info("ACTUAL JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing batch status", BatchStatus.COMPLETED, execution1.getBatchStatus());
			assertWithMessage("Testing exit status", testString, execution1.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}
	
	/*
	 * @testName: testExamineStepContextInArtifact
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
 
	public void testExamineStepContextInArtifact() throws Exception {

		String METHOD = "testExamineStepContextInArtifact()";

		try {

			logger.info("Locate job XML file: StepContextTestBatchlet.xml<p>");

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("app.timeinterval=10<p>");
			jobParams.put("app.timeinterval", "10");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("StepContextTestBatchlet", jobParams);
		
			List<StepExecution> steps = jobOp.getStepExecutions(execution1.getExecutionId());
			
			assertWithMessage("list of step executions == 1", steps.size() == 1);
			
			String testString = "StepName=step1;StepExecutionId=" + steps.get(0).getStepExecutionId();
			logger.info("EXPECTED JobExecution getBatchStatus()=COMPLETED<p>");
			logger.info("ACTUAL JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("EXPECTED JobExecution getExitStatus()="+testString+"<p>");
			logger.info("ACTUAL JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing batch status", BatchStatus.COMPLETED, execution1.getBatchStatus());
			assertWithMessage("Testing exit status", testString, execution1.getExitStatus());
			
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}
	
	/*
	 * @testName: testOneArtifactIsJobAndStepListener
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
 
	public void testOneArtifactIsJobAndStepListener() throws Exception {

		String METHOD = "testOneArtifactIsJobAndStepListener";

		try {
			String expectedStr = "BeforeJob" + 
					"BeforeStep" + "UnusedExitStatusForPartitions" + "AfterStep" +
					"BeforeStep" + "UnusedExitStatusForPartitions" + "AfterStep" + 
					"AfterJob";

			logger.info("Locate job XML file: oneArtifactIsJobAndStepListener.xml<p>");

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("app.timeinterval=10<p>");
			jobParams.put("app.timeinterval", "10");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("oneArtifactIsJobAndStepListener", jobParams);

			logger.info("EXPECTED JobExecution getBatchStatus()=COMPLETED<p>");
			logger.info("ACTUAL JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("EXPECTED JobExecution getExitStatus()="+expectedStr+"<p>");
			logger.info("ACTUAL JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing batch status", BatchStatus.COMPLETED, execution1.getBatchStatus());
			assertWithMessage("Testing exit status", expectedStr, execution1.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testgetException
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
 
	public void testgetException() throws Exception {

		String METHOD = "testgetException";

		try {
			String expectedStr = "MyChunkListener: found instanceof MyParentException";

			logger.info("Locate job XML file: job_chunk_getException.xml<p>");

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("fail.immediate=true<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("job_chunk_getException", jobParams);

			logger.info("EXPECTED JobExecution getBatchStatus()=FAILED<p>");
			logger.info("ACTUAL JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("EXPECTED JobExecution getExitStatus()="+expectedStr+"<p>");
			logger.info("ACTUAL JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing batch status", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing exit status", expectedStr, execution1.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testgetExceptionListenerBased
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
 
	public void testgetExceptionListenerBased() throws Exception {

		String METHOD = "testgetExceptionListenerBased";

		try {
			String expectedStr = "MyChunkListener: found instanceof MyParentException";

			logger.info("Locate job XML file: job_chunk_getExceptionListeners.xml<p>");

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("fail.immediate=true<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("job_chunk_getExceptionListeners", jobParams);

			logger.info("EXPECTED JobExecution getBatchStatus()=FAILED<p>");
			logger.info("ACTUAL JobExecution getBatchStatus()="+execution1.getBatchStatus()+"<p>");
			logger.info("EXPECTED JobExecution getExitStatus()="+expectedStr+"<p>");
			logger.info("ACTUAL JobExecution getExitStatus()="+execution1.getExitStatus()+"<p>");
			assertWithMessage("Testing batch status", BatchStatus.FAILED, execution1.getBatchStatus());
			assertWithMessage("Testing exit status", expectedStr, execution1.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

    /*
     * @testName: testJobContextIsUniqueForMainThreadAndPartitions
     * 
     * @assertion: FIXME
     * 
     * @test_Strategy: FIXME
     */
    @Test
 
    public void testJobContextIsUniqueForMainThreadAndPartitions() throws Exception {

        String METHOD = "testJobContextIsUniqueForMainThreadAndPartitions";
        begin(METHOD);

        try {
            logger.info("Locate job XML file: job_partitioned_1step.xml<p>");

            logger.info("Invoke startJobAndWaitForResult<p>");
            JobExecution jobExecution = jobOp.startJobAndWaitForResult("job_partitioned_1step");

            logger.info("JobExecution getBatchStatus()="+jobExecution.getBatchStatus()+"<p>");
            assertObjEquals(BatchStatus.COMPLETED, jobExecution.getBatchStatus());
            assertObjEquals("COMPLETED", jobExecution.getExitStatus());
            
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
	
    /*
     * @testName: testJobContextIsUniqueForMainThreadAndFlowsInSplits
     * 
     * @assertion: FIXME
     * 
     * @test_Strategy: FIXME
     */
    @Test
 
    public void testJobContextIsUniqueForMainThreadAndFlowsInSplits() throws Exception {

        String METHOD = "testJobContextIsUniqueForMainThreadAndFlowsInSplits";
        begin(METHOD);

        try {
            logger.info("Locate job XML file: job_split_batchlet_4steps.xml<p>");

            logger.info("Invoke startJobAndWaitForResult<p>");
            JobExecution execution = jobOp.startJobAndWaitForResult("job_split_batchlet_4steps");

            logger.info("JobExecution getBatchStatus()="+execution.getBatchStatus()+"<p>");
            assertObjEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
            assertObjEquals("COMPLETED", execution.getExitStatus());
            
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }

    /*
     * @testName: testStepContextIsUniqueForMainThreadAndPartitions
     * 
     * @assertion: FIXME
     * 
     * @test_Strategy: FIXME
     */
    @Test

    public void testStepContextIsUniqueForMainThreadAndPartitions() throws Exception {
        String METHOD = "testStepContextIsUniqueForMainThreadAndPartitions";
        begin(METHOD);

        try {
            logger.info("Locate job XML file: job_partitioned_1step.xml<p>");

            logger.info("Invoke startJobAndWaitForResult<p>");
            JobExecution jobExecution = jobOp.startJobAndWaitForResult("job_partitioned_1step");

            logger.info("JobExecution getBatchStatus()=" + jobExecution.getBatchStatus() + "<p>");
            
            assertObjEquals(BatchStatus.COMPLETED, jobExecution.getBatchStatus());
            
            List<StepExecution> stepExecs = jobOp.getStepExecutions(jobExecution.getExecutionId());
            
            //only one step in job
            StepExecution stepExec = stepExecs.get(0);
            
            //verify step context is defaulted because it was never set on main thread.
            assertObjEquals("COMPLETED", stepExec.getExitStatus());
            
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }

    @AfterAll
    public static void cleanup() throws Exception {
    }

    private static void handleException(String methodName, Exception e) throws Exception {
		logger.info("Caught exception: " + e.getMessage()+"<p>");
		logger.info(methodName + " failed<p>");
		throw e;
	}

    private void begin(String str) {
        logger.info("Begin test method: " + str + "<p>");
    }
}
