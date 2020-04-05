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

import static com.ibm.jbatch.tck.utils.AssertionUtils.assertWithMessage;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.Metric;
import javax.batch.runtime.StepExecution;

import com.ibm.jbatch.tck.artifacts.specialized.MetricsStepListener;
import com.ibm.jbatch.tck.utils.JobOperatorBridge;
import java.util.logging.Logger;
import org.junit.jupiter.api.*;

public class MetricsTests {
	private static final Logger logger = Logger.getLogger(MetricsTests.class.getName());
	private JobOperatorBridge jobOp = null;

	@BeforeEach
	public void setUp() throws Exception {
		jobOp = new JobOperatorBridge();
	}

	/* cleanup */
	public void cleanup() {

	}

	/*
	 * Obviously would be nicer to have more granular tests for some of this
	 * function, but here we're going a different route and saying, if it's
	 * going to require restart it will have some complexity, so let's test a
	 * few different functions in one longer restart scenario.
	 */

	/*
	 * @testName: testMetricsInApp
	 * 
	 * 
	 * @assertion: Section 7.1 Job Metrics - Ensure Metrics are available to Batch Artifacts during job execution
	 * @test_Strategy: Batch Artifact reads a known number of items - test that those reads are reflected 
	 *                 in the read count and accessible at job execution time to the Batch Artifact
	 * 
	 */
	@Test

	public void testMetricsInApp() throws Exception {
		String METHOD = "testMetricsInApp";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=40<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.chunksize=7<p>");
			logger.info("app.commitinterval=10<p>");
			logger.info("numberOfSkips=0<p>");
			logger.info("ReadProcessWrite=READ<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "40");
			jobParams.put("app.arraysize", "30");
			jobParams.put("app.chunksize", "7");
			jobParams.put("app.commitinterval", "10");
			jobParams.put("numberOfSkips", "0");
			jobParams.put("ReadProcessWrite", "READ");
			jobParams.put("app.writepoints", "0,7,14,21,28,30");
			jobParams.put("app.next.writepoints", "7,14,21,28,30");

			logger.info("Locate job XML file: testChunkMetrics.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testChunkMetrics",
					jobParams);
			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			logger.info("execution #1 JobExecution getExitStatus()="
					+ execution1.getExitStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());
			assertWithMessage("Testing metrics",
					MetricsStepListener.GOOD_EXIT_STATUS_READ,
					execution1.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testMetricsSkipRead
	 * 
	 * @assertion: Section 7.1 Job Metrics - Skip Read Count
	 * @test_Strategy: Force Batch Artifact to skip a known number of reads - test that those skips are reflected in the skip read count
	 * 
	 */
	@Test

	public void testMetricsSkipRead() throws Exception {

		String METHOD = "testMetricsSkipRead";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=1,3<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("numberOfSkips=2<p>");
			logger.info("ReadProcessWrite=READ_SKIP<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "1,3,4,12");
			jobParams.put("app.arraysize", "30");
			jobParams.put("numberOfSkips", "4");
			jobParams.put("ReadProcessWrite", "READ_SKIP");

			logger.info("Locate job XML file: testMetricsSkipCount.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricsSkipCount",
					jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			logger.info("execution #1 JobExecution getExitStatus()="
					+ execution1.getExitStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());
			assertWithMessage("Testing execution #1",
					MetricsStepListener.GOOD_EXIT_STATUS_READ,
					execution1.getExitStatus());

			List<StepExecution> stepExecutions = jobOp
					.getStepExecutions(execution1.getExecutionId());

			StepExecution step = null;
			String stepNameTest = "step1";

			for (StepExecution stepEx : stepExecutions) {
				if (stepNameTest.equals(stepEx.getStepName())) {
					step = stepEx;
				}
			}

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());

			Metric[] metrics = step.getMetrics();

			logger.info("Testing the read count for execution #1<p>");
			for (int i = 0; i < metrics.length; i++) {
				if (metrics[i].getType().equals(Metric.MetricType.READ_SKIP_COUNT)) {
					logger.info("AJM: in test, found metric: " + metrics[i].getType() + "<p>");
					assertWithMessage(
							"Testing the read skip count for execution #1", 4L,
							metrics[i].getValue());
				}
			}
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testMetricsSkipWrite
	 * 
	 * @assertion: Section 7.1 Job Metrics - Skip Write Count
	 * @test_Strategy: Force Batch Artifact to skip a known number of writes - test that those skips are reflected in the skip write count
	 * 
	 */
	@Test

	public void testMetricsSkipWrite() throws Exception {

		String METHOD = "testMetricsSkipWrite";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=1,3<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("numberOfSkips=2<p>");
			logger.info("ReadProcessWrite=WRITE_SKIP<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("writerecord.fail", "1,3,4");
			jobParams.put("app.arraysize", "30");
			jobParams.put("numberOfSkips", "3");
			jobParams.put("ReadProcessWrite", "WRITE_SKIP");

			logger.info("Locate job XML file: testMetricsSkipWriteCount.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricsSkipWriteCount",
					jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			logger.info("execution #1 JobExecution getExitStatus()="
					+ execution1.getExitStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());

			List<StepExecution> stepExecutions = jobOp
					.getStepExecutions(execution1.getExecutionId());

			StepExecution step = null;
			String stepNameTest = "step1";

			for (StepExecution stepEx : stepExecutions) {
				if (stepNameTest.equals(stepEx.getStepName())) {
					step = stepEx;
				}
			}

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());

			Metric[] metrics = step.getMetrics();

			logger.info("Testing the write skip count for execution #1<p>");
			for (int i = 0; i < metrics.length; i++) {
				if (metrics[i].getType().equals(Metric.MetricType.WRITE_SKIP_COUNT)) {
					logger.info("AJM: in test, found metric: " + metrics[i].getType() + "<p>");
					assertWithMessage(
							"Testing the write skip count for execution #1", 3L,
							metrics[i].getValue());
				}
			}
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testMetricsSkipProcess
	 * 
	 * @assertion: Section 7.1 Job Metrics - Skip Process Count
	 * @test_Strategy: Force Batch Artifact to skip a known number of processing - test that those skips are reflected in the skip process count
	 * 
	 */
	@Test

	public void testMetricsSkipProcess() throws Exception {
		String METHOD = "testMetricsSkipProcess";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=7,13<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("numberOfSkips=2<p>");
			logger.info("ReadProcessWrite=PROCESS<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("processrecord.fail", "7,13");
			jobParams.put("app.arraysize", "30");
			jobParams.put("numberOfSkips", "2");
			jobParams.put("ReadProcessWrite", "PROCESS");

			logger.info("Locate job XML file: testMetricsSkipCount.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricsSkipCount",
					jobParams);

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			logger.info("execution #1 JobExecution getExitStatus()="
					+ execution1.getExitStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());
			assertWithMessage("Testing execution #1",
					MetricsStepListener.GOOD_EXIT_STATUS_PROCESS,
					execution1.getExitStatus());

			List<StepExecution> stepExecutions = jobOp
					.getStepExecutions(execution1.getExecutionId());

			StepExecution step = null;
			String stepNameTest = "step1";

			for (StepExecution stepEx : stepExecutions) {
				if (stepNameTest.equals(stepEx.getStepName())) {
					step = stepEx;
				}
			}

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());

			Metric[] metrics = step.getMetrics();

			logger.info("Testing the read count for execution #1<p>");
			for (int i = 0; i < metrics.length; i++) {
				if (metrics[i].getType().equals(Metric.MetricType.PROCESS_SKIP_COUNT)) {
					logger.info("AJM: in test, found metric: " + metrics[i].getType() + "<p>");
					assertWithMessage(
							"Testing the read count for execution #1", 2L,
							metrics[i].getValue());
				}
			}
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testReadMetric
	 * 
	 * @assertion: Section 7.1 Job Metrics - Read Count
	 * @test_Strategy: Batch Artifact reads a known number of items - test that those reads are reflected in the read count
	 * 
	 */
	@Test

	public void testReadMetric() throws Exception {
		String METHOD = "testReadMetric";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			logger.info("execution.number=1<p>");
			logger.info("readrecord.fail=40<p>");
			logger.info("app.arraysize=30<p>");
			logger.info("app.chunksize=7<p>");
			logger.info("app.commitinterval=10<p>");
			logger.info("numberOfSkips=0<p>");
			logger.info("ReadProcessWrite=READ<p>");
			jobParams.put("execution.number", "1");
			jobParams.put("readrecord.fail", "-1");
			jobParams.put("app.arraysize", "30");

			logger.info("Locate job XML file: testChunkMetrics.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricCount",
					jobParams);
			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			logger.info("execution #1 JobExecution getExitStatus()="
					+ execution1.getExitStatus() + "<p>");

			List<StepExecution> stepExecutions = jobOp
					.getStepExecutions(execution1.getExecutionId());

			StepExecution step = null;
			String stepNameTest = "step1Metric";

			for (StepExecution stepEx : stepExecutions) {
				if (stepNameTest.equals(stepEx.getStepName())) {
					step = stepEx;
				}
			}

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());

			Metric[] metrics = step.getMetrics();

			logger.info("Testing the read count for execution #1<p>");
			for (int i = 0; i < metrics.length; i++) {
				if (metrics[i].getType().equals(Metric.MetricType.READ_COUNT)) {
					logger.info("AJM: in test, found metric: " + metrics[i].getType() + "<p>");
					assertWithMessage(
							"Testing the read count for execution #1", 9L,
							metrics[i].getValue());
				}
			}

		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testWriteMetric
	 * 
	 * @assertion: Section 7.1 Job Metrics - Write Count
	 * @test_Strategy: Batch Artifact writes a known number of items - test that those writes are reflected in the write count
	 * 
	 */
	@Test

	public void testWriteMetric() throws Exception {
		String METHOD = "testWriteMetric";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();

			logger.info("Locate job XML file: testChunkMetrics.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricCount",
					jobParams);
			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			logger.info("execution #1 JobExecution getExitStatus()="
					+ execution1.getExitStatus() + "<p>");

			List<StepExecution> stepExecutions = jobOp
					.getStepExecutions(execution1.getExecutionId());

			StepExecution step = null;
			String stepNameTest = "step1Metric";

			for (StepExecution stepEx : stepExecutions) {
				if (stepNameTest.equals(stepEx.getStepName())) {
					step = stepEx;
				}
			}

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());

			Metric[] metrics = step.getMetrics();

			logger.info("Testing the read count for execution #1<p>");
			for (int i = 0; i < metrics.length; i++) {
				if (metrics[i].getType().equals(Metric.MetricType.WRITE_COUNT)) {
					logger.info("AJM: in test, found metric: " + metrics[i].getType() + "<p>");
					assertWithMessage(
							"Testing the write count for execution #1", 9L,
							metrics[i].getValue());
				}
			}

		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testMetricsFilterCount
	 * 
	 * @assertion: Section 7.1 Job Metrics - Filter Count
	 * @test_Strategy: Batch Artifact filters a known number of items while processing - test that those filter actions are reflected in the filter count
	 * 
	 */
	@Test

	public void testMetricsFilterCount() throws Exception {

		String METHOD = "testMetricsFilterCount";

		try {

			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			jobParams.put("app.processFilterItem", "3");
			logger.info("app.processFilterItem=3<p>");

			logger.info("Locate job XML file: testMetricsFilterCount.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricsFilterCount",
					jobParams);

			logger.info("Obtaining StepExecutions for execution id: "
					+ execution1.getExecutionId() + "<p>");
			List<StepExecution> stepExecutions = jobOp
					.getStepExecutions(execution1.getExecutionId());

			StepExecution tempstep = null;
			StepExecution step = null;
			String stepNameTest = "step1FM";

			for (StepExecution stepEx : stepExecutions) {
				if (stepNameTest.equals(stepEx.getStepName())) {
					step = stepEx;
				}
			}

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());
			Metric[] metrics = step.getMetrics();

			logger.info("Testing the filter count for execution #1<p>");
			for (int i = 0; i < metrics.length; i++) {
				if (metrics[i].getType().equals(Metric.MetricType.FILTER_COUNT)) {
					assertWithMessage(
							"Testing the filter count for execution #1", 1L,
							metrics[i].getValue());
				}
			}
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testMetricsCommitCount
	 * 
	 * @assertion: Section 7.1 Job Metrics - Commit Count
	 * @test_Strategy: Batch Artifact read/process/writes a known number of items and all are committed - test that those commits are reflected in the commit count
	 * 
	 */
	@Test

	public void testMetricsCommitCount() throws Exception {

		String METHOD = "testMetricsCommitCount";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			jobParams.put("app.processFilterItem", "3");
			logger.info("app.processFilterItem=3<p>");

			logger.info("Locate job XML file: testMetricsCommitCount.xml<p>");

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricsCommitCount",
					jobParams);

			logger.info("Obtaining StepExecutions for execution id: "
					+ execution1.getExecutionId() + "<p>");
			List<StepExecution> stepExecutions = jobOp
					.getStepExecutions(execution1.getExecutionId());

			StepExecution tempstep = null;
			StepExecution step = null;
			String stepNameTest = "step1CCM";

			for (StepExecution stepEx : stepExecutions) {
				if (stepNameTest.equals(stepEx.getStepName())) {
					step = stepEx;
				}
			}

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());
			Metric[] metrics = step.getMetrics();

			logger.info("Testing the commit count for execution #1<p>");
			for (int i = 0; i < metrics.length; i++) {
				if (metrics[i].getType().equals(Metric.MetricType.COMMIT_COUNT)) {
					assertWithMessage(
							"Testing the commit count for execution #1", 4L,
							metrics[i].getValue());
				}
			}
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}


	/*
	 * @testName: testMetricsStepTimestamps
	 * 
	 * @assertion: Section 7.1 Job Metrics - Commit Count
	 * @test_Strategy: test case records a point in time and starts a job which contains a step. test that the step start time
	 *                 occurs after the test case point in time. test that the step end time occurs after the step start time. 
	 *                 test that the step end time occurs after the test case point in time.
	 * 
	 */
	@Test

	public void testMetricsStepTimestamps() throws Exception {

		String METHOD = "testMetricsStepTimestamps";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			jobParams.put("app.processFilterItem", "3");
			logger.info("app.processFilterItem=3<p>");

			logger.info("Locate job XML file: testMetricsCommitCount.xml<p>");
			long time = System.currentTimeMillis();
			Date ts = new Date(time);

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricsCommitCount",
					jobParams);

			logger.info("Obtaining StepExecutions for execution id: "
					+ execution1.getExecutionId() + "<p>");
			List<StepExecution> stepExecutions = jobOp
					.getStepExecutions(execution1.getExecutionId());

			StepExecution tempstep = null;
			StepExecution step = null;
			String stepNameTest = "step1CCM";

			for (StepExecution stepEx : stepExecutions) {
				if (stepNameTest.equals(stepEx.getStepName())) {
					step = stepEx;
				}
			}

			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());

			logger.info("AJM: testcase start time: " + ts + "<p>");
			logger.info("AJM: step start time: " + step.getStartTime() + "<p>");
			logger.info("AJM: step end time: " + step.getEndTime() + "<p>");

			assertWithMessage("Start time of test occurs approximately before start time of step", roughlyOrdered(ts, step.getStartTime()));
			assertWithMessage("Start time of step occurs approximately before end time of step", roughlyOrdered(step.getStartTime(), step.getEndTime()));
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testMetricsJobExecutionTimestamps
	 * 
	 * @assertion: Section 7.1 Job Metrics - Commit Count
	 * @test_Strategy: test starts a job. Test that the start time of the testcase occurs before the start time  of the job.
	 *                 test that the job create time occurs before the job start time. test that the job end time occurs
	 *                 after the job start time. test that the last status update time occurs after the job start time.
	 *                 test that the job end time occurs after the testcase start time.
	 * 
	 */
	@Test

	public void testMetricsJobExecutionTimestamps() throws Exception {

		String METHOD = "testMetricsJobExecutionTimestamps";

		try {
			logger.info("Create job parameters for execution #1:<p>");
			Properties jobParams = new Properties();
			jobParams.put("app.processFilterItem", "3");
			logger.info("app.processFilterItem=3<p>");

			logger.info("Locate job XML file: testMetricsCommitCount.xml<p>");

			long time = System.currentTimeMillis();
			Date ts = new Date(time);

			logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
			JobExecution execution1 = jobOp.startJobAndWaitForResult("testMetricsCommitCount",
					jobParams);



			logger.info("execution #1 JobExecution getBatchStatus()="
					+ execution1.getBatchStatus() + "<p>");
			assertWithMessage("Testing execution #1", BatchStatus.COMPLETED,
					execution1.getBatchStatus());

			logger.info("AJM: testcase start time: " + ts + "<p>");
			logger.info("AJM: job create time: " + execution1.getCreateTime() + "<p>");
			logger.info("AJM: job start time: " + execution1.getStartTime() + "<p>");
			logger.info("AJM: job last updated time: " + execution1.getLastUpdatedTime() + "<p>");
			logger.info("AJM: job end time: " + execution1.getEndTime() + "<p>");

			assertWithMessage("Start time of test occurs approximately before create time of job", roughlyOrdered(ts, execution1.getCreateTime()));
			assertWithMessage("Create time of job occurs approximately before start time of job", roughlyOrdered(execution1.getCreateTime(), execution1.getStartTime()));
			assertWithMessage("Start time of job occurs approximately before end time of job", roughlyOrdered(execution1.getStartTime(), execution1.getEndTime()));
			assertWithMessage("Start time of job occurs approximately before Last Updated time of job", roughlyOrdered(execution1.getStartTime(), execution1.getLastUpdatedTime()));
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	private static void handleException(String methodName, Exception e)
			throws Exception {
		logger.info("Caught exception: " + e.getMessage() + "<p>");
		logger.info(methodName + " failed<p>");
		throw e;
	}
	
	/*
	 * We want to confirm that 'd1' is roughly before 'd2', and also to
	 * allow for the fact that dates may be stored with a loss of precision.
	 * 
	 * Let's assume then that we only have whole seconds precision (without
	 * necessarily accounting for any fractional seconds).
	 * 
	 * So we can't simply perform d1 < d2, or even d1 <= d2 (the inclusion of 'equals' 
	 * corrects for a different problem, the problem of running so fast that
	 * the times for d1 and d2 are the same even though d1 may still have
	 * been executed first).
	 * 
	 * The "worst" case (in terms of highest rounding error), then, is that 'd1' gets
	 * rounded up while 'd2' gets rounded down, leaving the rounded 'd1' value a full 
	 * second higher than the rounded 'd2' value.
	 * 
	 * Therefore we check that d2 minus d1, which before rounding should be >= 0, is
	 * instead no less than -1000 (1 second).
	 */
	private static boolean roughlyOrdered(Date d1, Date d2) {
		long time1 = d1.getTime();
		long time2 = d2.getTime();
		
		long diff = time2 - time1;
		
		return diff >= -1000 ? true : false;
	}

}
