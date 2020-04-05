/*
 * Copyright 2013 International Business Machines Corp.
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

import static com.ibm.jbatch.tck.utils.AssertionUtils.assertWithMessage;

import java.util.Properties;

import javax.batch.operations.JobExecutionNotMostRecentException;
import javax.batch.runtime.JobExecution;

import com.ibm.jbatch.tck.utils.JobOperatorBridge;
import java.util.logging.Logger;
import org.junit.jupiter.api.*;

public class RestartNotMostRecentTests {

	private static final Logger logger = Logger.getLogger(RestartNotMostRecentTests.class.getName());
	private JobOperatorBridge jobOp = null;
	
	/*
	 * @testName: testRestartNotMostRecentException
	 * 
	 * @assertion: FIXME
	 * 
	 * @test_Strategy: FIXME
	 */
	@Test

	public void testRestartNotMostRecentException() throws Exception {
		String METHOD = "testRestartNotMostRecentException";
		
		try {
			logger.info("starting job");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			jobParams.put("execution.number", "1");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_attributes_restart_true_test", jobParams);
			
			Properties restartParams = new Properties();
			logger.info("execution.number=2<p>");
			restartParams.put("execution.number", "2");
			jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartParams);
		
			try {
				logger.info("Trying to execute the first job execution again.");
				jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartParams);
				assertWithMessage("It should have thrown JobExecutionNotMostRecentException", false);
			} catch(JobExecutionNotMostRecentException e) {
				assertWithMessage("JobExecutionNotMostRecentException thrown", true);
			}

			logger.info("Job Status = " + jobExec.getBatchStatus());
			logger.info("job completed");
			
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}
	
	
	private static void handleException(String methodName, Exception e) throws Exception {
		logger.info("Caught exception: " + e.getMessage()+"<p>");
		logger.info(methodName + " failed<p>");
		throw e;
	}

	public void setup(String[] args, Properties props) throws Exception {

		String METHOD = "setup";

		try {
			jobOp = new JobOperatorBridge();
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/* cleanup */
	public void  cleanup()
	{		

	}

	@BeforeEach
	public void beforeTest() throws ClassNotFoundException {
		jobOp = new JobOperatorBridge(); 
	}

	@AfterEach
	public void afterTest() {
		jobOp = null;
	}
	
}
