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

import java.util.List;
import java.util.Properties;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.JobInstance;
import javax.batch.runtime.Metric;
import javax.batch.runtime.StepExecution;

import com.ibm.jbatch.tck.artifacts.reusable.MyPersistentRestartUserData;
import com.ibm.jbatch.tck.artifacts.specialized.MyItemProcessListenerImpl;
import com.ibm.jbatch.tck.artifacts.specialized.MyItemReadListenerImpl;
import com.ibm.jbatch.tck.artifacts.specialized.MyItemWriteListenerImpl;
import com.ibm.jbatch.tck.artifacts.specialized.MyMultipleExceptionsRetryReadListener;
import com.ibm.jbatch.tck.artifacts.specialized.MySkipProcessListener;
import com.ibm.jbatch.tck.artifacts.specialized.MySkipReadListener;
import com.ibm.jbatch.tck.artifacts.specialized.MySkipWriteListener;
import com.ibm.jbatch.tck.utils.JobOperatorBridge;
import com.ibm.jbatch.tck.utils.TCKJobExecutionWrapper;
import java.util.logging.Logger;

import org.junit.jupiter.api.*;

public class ChunkTests {

    private static final Logger logger = Logger.getLogger(ChunkTests.class.getName());
    private JobOperatorBridge jobOp = null;

    @BeforeEach
    public void setUp() throws Exception {
        jobOp = new JobOperatorBridge();
    }

    /* cleanup */
    public void cleanup() {

    }
    
    /*
     * @testName: testChunkNoProcessorDefined
     * @assertion: job will finish successfully with COMPLETED and buffer size = default value of 10 is recognized
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count default value
     *             5.2.1 - Chunk item checkpointing/restart
     * 
     * @test_Strategy: start a job with no item-count specified. 
     *                 Batch artifact checks that the checkpointing occurs at the default item-count (10). Test that the 
     *                 job completes successfully. 
     */
    @Test

    public void testChunkNoProcessorDefined() throws Exception {
        String METHOD = "testChunkDefaultItemCount";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=40<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("app.checkpoint.position" , "0");
            jobParams.put("readrecord.fail", "40");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkNoProcessorDefined.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkNoProcessorDefined", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "buffer size = 10", execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }
    
    /*
     * @testName: testChunkNullCheckpointInfo
     * @assertion: job will finish successfully with COMPLETED and buffer size = default value of 10 is recognized
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count default value
     *             5.2.1 - Chunk item checkpointing/restart
     * 
     * @test_Strategy: start a job with no item-count specified. 
     *                 Batch artifact checks that the checkpointing occurs at the default item-count (10). Test that the 
     *                 job completes successfully. 
     */
    @Test

    public void testChunkNullCheckpointInfo() throws Exception {
        String METHOD = "testChunkDefaultItemCount";

        try {

            logger.info("Locate job XML file: nullChkPtInfo.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("nullChkPtInfo", null);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "checkpointInfo is null in reader.open...checkpointInfo is null in writer.open", execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }
    
    /*
    * @testName: testChunkArtifactInstanceUniqueness
    * @assertion: job will finish successfully with COMPLETED and 2 unique listener lifecycles will be reported in the exit status 
    *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
    *             5.2.1.2 - Processor
    *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
    *             5.2.1 - Chunk, item-count default value
    *             5.2.1 - Chunk item checkpointing/restart
    * 
    * @test_Strategy: start a job with no item-count specified. 
    *                 Two unique Batch artifact Chunk, Step and Job listeners are defined.
    *                 Each listener contains validation logic to determine that separate instances of 
    *                 the listeners are being produced by the runtime. Test finishes in COMPLETED state and
    *                 the before/after lifecycle of each listener is contained in the  exit status.
    */
   @Test

   public void testChunkArtifactInstanceUniqueness() throws Exception {
       String METHOD = "testChunkDefaultItemCount";

       try {

           logger.info("Locate job XML file: uniqueInstanceTest.xml<p>");

           logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
           JobExecution execution1 = jobOp.startJobAndWaitForResult("uniqueInstanceTest", null);
           logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
           logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
           assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
           assertWithMessage("Testing execution #1", "nullChunkListenerChunkListenerStepListenerStepListenerJobListenerJobListener", execution1.getExitStatus());
       } catch (Exception e) {
           handleException(METHOD, e);
       }

   }

    /*
     * Obviously would be nicer to have more granular tests for some of this
     * function, but here we're going a different route and saying, if it's
     * going to require restart it will have some complexity, so let's test a
     * few different functions in one longer restart scenario.
     */
    
    /*
     * @testName: testChunkOnErrorListener
     * 
     * @assertion: Test will finish in FAILED status, with the onError chunk listener invoked
     * 
     * @test_Strategy: Test that the ChunkListener.onError method is driven for an exception occurring
     * 		during the read-write-process batch loop
     */
    @Test

    public void testChunkOnErrorListener() throws Exception {
    	
    	String METHOD = "testChunkOnErrorListener";
    	
    	try {
    		logger.info("Create job parameters for execution #1:<p>");
    		Properties jobParams = new Properties();
    		
    		logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=5<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "5");
            jobParams.put("app.writepoints", "0,10");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkListenerTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkListenerTest", jobParams);
            
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "Chunk onError invoked", execution1.getExitStatus());
          
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    

    /*
     * @testName: testChunkRestartItemCount7
     * @assertion: first job started will finish as FAILED. Restart of job will finish successfully with COMPLETED.
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count
     *             5.2.1 - Chunk item checkpointing
     * 
     * @test_Strategy: start a job configured to a item-count of 7 configured to fail on the 12 item read. Restart job and 
     *                 test that the processing begins at last recorded check point (item 7) prior to previous failure
     */
    @Test

    public void testChunkRestartItemCount7() throws Exception {
        String METHOD = "testChunkRestartItemCount7";

        try {

            logger.info("Create job parameters for execution #1:<p>");
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=12<p>");
            logger.info("app.arraysize=30<p>");
            logger.info("app.chunksize=7<p>");
            logger.info("app.commitinterval=10<p>");
            Properties jobParams = new Properties();
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "12");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.checkpoint.position" , "0");
            jobParams.put("app.writepoints", "0,7,14,21,28,30");
            jobParams.put("app.next.writepoints", "7,14,21,28,30");

            logger.info("Locate job XML file: chunkStopOnEndOn.xml<p>");

            logger.info("Invoke startJobAndWaitForResult<p>");
            TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunkStopOnEndOn", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

            long jobInstanceId = execution1.getInstanceId();
            long lastExecutionId = execution1.getExecutionId();
            logger.info("Got Job instance id: " + jobInstanceId + "<p>");
            logger.info("Got Job execution id: " + lastExecutionId + "<p>");
            {
                logger.info("Create job parameters for execution #2:<p>");
                logger.info("execution.number=2<p>");
                logger.info("app.arraysize=30<p>");
                logger.info("app.chunksize=7<p>");
                logger.info("app.commitinterval=10<p>");
                Properties restartJobParameters = new Properties();
                restartJobParameters.put("execution.number", "2");
                restartJobParameters.put("app.arraysize", "30");
                jobParams.put("app.checkpoint.position" , "7");
                logger.info("Invoke restartJobAndWaitForResult with executionId: " + lastExecutionId + "<p>");
                TCKJobExecutionWrapper exec = jobOp.restartJobAndWaitForResult(lastExecutionId, jobParams);
                lastExecutionId = exec.getExecutionId();
                logger.info("execution #2 JobExecution getBatchStatus()=" + exec.getBatchStatus() + "<p>");
                logger.info("execution #2 JobExecution getExitStatus()=" + exec.getExitStatus() + "<p>");
                logger.info("execution #2 Job instance id=" + exec.getInstanceId() + "<p>");
                assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
                assertWithMessage("Testing execution #2", "COMPLETED", exec.getExitStatus());
                assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());
            }
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkRestartItemCount10
     * @assertion: first job started will finish as FAILED. Restart of job will finish successfully with COMPLETED.
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count
     *             5.2.1 - Chunk item checkpointing
     * 
     * @test_Strategy: start a job configured to a item-count of 10 configured to fail on the 12 item read. Restart job and 
     *                 test that the processing begins at last recorded check point (item 10) prior to previous failure
     */
    @Test

    public void testChunkRestartItemCount10() throws Exception {

        String METHOD = "testChunkRestartItemCount10";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=12<p>");
            logger.info("app.arraysize=30<p>");
            logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
            logger.info("app.next.writepoints=0,5,10,15,20,25,30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "12");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.checkpoint.position" , "0");
            jobParams.put("app.writepoints", "0,10,20,30");
            jobParams.put("app.next.writepoints", "10,20,30");

            logger.info("Locate job XML file: chunkrestartCheckpt10.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunkrestartCheckpt10", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

            long jobInstanceId = execution1.getInstanceId();
            long lastExecutionId = execution1.getExecutionId();
            logger.info("Got Job instance id: " + jobInstanceId + "<p>");
            logger.info("Got Job execution id: " + lastExecutionId + "<p>");

            {
                logger.info("Create job parameters for execution #2:<p>");
                Properties restartJobParameters = new Properties();
                logger.info("execution.number=2<p>");
                logger.info("app.arraysize=30<p>");
                logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
                restartJobParameters.put("execution.number", "2");
                jobParams.put("app.arraysize", "30");
                jobParams.put("app.checkpoint.position" , "10");
                jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
                logger.info("Invoke restartJobAndWaitForResult with execution id: " + lastExecutionId + "<p>");
                TCKJobExecutionWrapper exec = jobOp.restartJobAndWaitForResult(lastExecutionId, jobParams);
                lastExecutionId = exec.getExecutionId();
                logger.info("execution #2 JobExecution getBatchStatus()=" + exec.getBatchStatus() + "<p>");
                logger.info("execution #2 JobExecution getExitStatus()=" + exec.getExitStatus() + "<p>");
                logger.info("execution #2 Job instance id=" + exec.getInstanceId() + "<p>");
                assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
                assertWithMessage("Testing execution #2", "COMPLETED", exec.getExitStatus());
                assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());
            }
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkRestartChunk5
     * @assertion: first job started will finish as FAILED. Restart of job will finish successfully with COMPLETED. 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count
     *             5.2.1 - Chunk item checkpointing/restart
     * 
     * @test_Strategy: start a job configured to a item-count of 5 configured to fail on the 12 item read. Restart job and 
     *                 test that the processing begins at last recorded check point (item 10) prior to previous failure
     */
    @Test

    public void testChunkRestartChunk5() throws Exception {

        String METHOD = "testChunkRestartChunk5";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=12<p>");
            logger.info("app.arraysize=30<p>");
            logger.info("app.writepoints=0,3,6,9,12,15,18,21,24,27,30<p>");
            logger.info("app.next.writepoints=9,12,15,18,21,24,27,30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "12");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.checkpoint.position" , "0");
            jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
            jobParams.put("app.next.writepoints", "10,15,20,25,30");

            logger.info("Locate job XML file: chunksize5commitinterval3.xml<p>");

            logger.info("Invoke startJobAndWaitForResul for execution #1<p>");
            TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunksize5commitinterval3", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

            long jobInstanceId = execution1.getInstanceId();
            long lastExecutionId = execution1.getExecutionId();
            logger.info("Got Job instance id: " + jobInstanceId + "<p>");
            logger.info("Got Job execution id: " + lastExecutionId + "<p>");
            {
                logger.info("Create job parameters for execution #2:<p>");
                Properties restartJobParameters = new Properties(jobParams);
                logger.info("execution.number=2<p>");
                logger.info("app.arraysize=30<p>");
                
                restartJobParameters.put("execution.number", "2");
                restartJobParameters.put("app.checkpoint.position" , "10");
                restartJobParameters.put("app.arraysize", "30");
                logger.info("Invoke restartJobAndWaitForResult with execution id: " + lastExecutionId + "<p>");
                TCKJobExecutionWrapper exec = jobOp.restartJobAndWaitForResult(lastExecutionId, restartJobParameters);
                lastExecutionId = exec.getExecutionId();
                logger.info("execution #2 JobExecution getBatchStatus()=" + exec.getBatchStatus() + "<p>");
                logger.info("execution #2 JobExecution getExitStatus()=" + exec.getExitStatus() + "<p>");
                logger.info("execution #2 Job instance id=" + exec.getInstanceId() + "<p>");
                assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
                assertWithMessage("Testing execution #2", "COMPLETED", exec.getExitStatus());
                assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());
            }
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkDefaultItemCount
     * @assertion: job will finish successfully with COMPLETED and buffer size = default value of 10 is recognized
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count default value
     *             5.2.1 - Chunk item checkpointing/restart
     * 
     * @test_Strategy: start a job with no item-count specified. 
     *                 Batch artifact checks that the checkpointing occurs at the default item-count (10). Test that the 
     *                 job completes successfully. 
     */
    @Test

    public void testChunkDefaultItemCount() throws Exception {
        String METHOD = "testChunkDefaultItemCount";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=40<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("app.checkpoint.position" , "0");
            jobParams.put("readrecord.fail", "40");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunksizeDEFAULTcommitIntervalDEFAULT.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunksizeDEFAULTcommitIntervalDEFAULT", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "buffer size = 10", execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkRestartCustomCheckpoint
     * @assertion: first job start will finish with FAILED. restart of job will finish successfully with COMPLETED.
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, checkpoint-policy
     *             5.2.1 - Chunk item checkpointing/restart
     *             5.2.1.7 - Custom Checkpoint Algorithm
     *             5.2.1.7.1 - Custom Checkpoint Algorithm Properties
     * 
     * @test_Strategy: start a job with item-count specified, checkpoint-policy set to 'custom' and configured to fail on the 12 item read. Restart job.
     *                  Batch artifact enforces that the checkpointing occurs at the custom defined checkpointing points and that 
     *                  reading/writing resumes at last good custom defined checkpoint.
     *                  test that the job completes successfully.
     */
    @Test

    public void testChunkRestartCustomCheckpoint() throws Exception {
        String METHOD = "testChunkRestartCustomCheckpoint";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=2<p>");
            logger.info("readrecord.fail=12<p>");
            logger.info("app.arraysize=30<p>");
            logger.info("app.writepoints=0,4,9,13,15,20,22,27,30<p>");
            logger.info("app.next.writepoints=9,13,15,20,22,27,30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "12");
            jobParams.put("app.checkpoint.position" , "0");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.writepoints", "0,4,9,13,15,20,22,27,30");
            jobParams.put("app.next.writepoints", "9,13,15,20,22,27,30");

            logger.info("Locate job XML file: chunkCustomCheckpoint.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunkCustomCheckpoint", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());

            long jobInstanceId = execution1.getInstanceId();
            long lastExecutionId = execution1.getExecutionId();
            logger.info("Got Job instance id: " + jobInstanceId + "<p>");
            logger.info("Got Job execution id: " + lastExecutionId + "<p>");
            {
                logger.info("Create job parameters for execution #2:<p>");
                Properties restartJobParameters = new Properties(jobParams);
                logger.info("execution.number=2<p>");
                logger.info("app.arraysize=30<p>");
                logger.info("app.writepoints=9,13,15,20,22,27,30<p>");
                restartJobParameters.put("execution.number", "2");
                restartJobParameters.put("app.checkpoint.position" , "9");
                restartJobParameters.put("app.arraysize", "30");
                restartJobParameters.put("app.writepoints", "9,13,15,20,22,27,30");
                logger.info("Invoke restartJobAndWaitForResult with execution id: " + lastExecutionId + "<p>");
                TCKJobExecutionWrapper exec = jobOp.restartJobAndWaitForResult(lastExecutionId, restartJobParameters);
                lastExecutionId = exec.getExecutionId();
                logger.info("execution #2 JobExecution getBatchStatus()=" + exec.getBatchStatus() + "<p>");
                logger.info("execution #2 JobExecution getExitStatus()=" + exec.getExitStatus() + "<p>");
                logger.info("execution #2 Job instance id=" + exec.getInstanceId() + "<p>");
                assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
                assertWithMessage("Testing execution #2", "COMPLETED", exec.getExitStatus());
                assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());
            }
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkTimeBasedDefaultCheckpoint
     * @assertion: job will finish successfully with COMPLETED and the default time-limit of 10 seconds recognized
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, time-limit default
     *             5.2.1 - Chunk item checkpointing
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set. time-limit not specified so as to default to 10. 
     *                  Batch artifact enforces that the checkpointing occurs at the default time-limit boundary (10 seconds) .
     *                  test that the job completes successfully.     
     */
    @Test

    public void testChunkTimeBasedDefaultCheckpoint() throws Exception {
        String METHOD = "testChunkTimeBasedDefaultCheckpoint";
        
        String DEFAULT_SLEEP_TIME = "500";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=31<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "31");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.sleeptime", System.getProperty("ChunkTests.testChunkTimeBasedDefaultCheckpoint.sleep",DEFAULT_SLEEP_TIME));

            logger.info("Locate job XML file: chunkTimeBasedDefaultCheckpoint.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkTimeBasedDefaultCheckpoint", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            String exitStatus = execution1.getExitStatus();
	        assertWithMessage("Testing execution #1", (exitStatus.equals("TRUE: 0") || exitStatus.equals("TRUE: 1")));
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    /*
     * @testName: testChunkTimeBasedTimeLimit0
     * @assertion: job will finish successfully with COMPLETED and the default time-limit of 10 seconds recognized
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, time-limit default
     *             5.2.1 - Chunk item checkpointing
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set. time-limit not specified so as to default to 10. 
     *                  Batch artifact enforces that the checkpointing occurs at the default time-limit boundary (10 seconds) .
     *                  test that the job completes successfully.     
     */
    @Test

    public void testChunkTimeBasedTimeLimit0() throws Exception {
        String METHOD = "testChunkTimeBasedDefaultCheckpoint";
        
        String DEFAULT_SLEEP_TIME = "500";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=31<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "31");
            jobParams.put("app.arraysize", "30");
            
            jobParams.put("app.sleeptime", System.getProperty("ChunkTests.testChunkTimeBasedTimeLimit0.sleep",DEFAULT_SLEEP_TIME));

            logger.info("Locate job XML file: chunkTimeLimit0.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkTimeLimit0", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            String exitStatus = execution1.getExitStatus();
	        assertWithMessage("Testing execution #1", (exitStatus.equals("TRUE: 0") || exitStatus.equals("TRUE: 1")));
	
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }    

    /*
     * @testName: testChunkTimeBased10Seconds
     * @assertion: job will finish successfully with COMPLETED and the time-limit of 10 seconds recognized
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, time-limit
     *             5.2.1 - Chunk item checkpointing/restart
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set. time-limit specified as 10. 
     *                  and configured to fail on the 12 item read. Restart job.
     *                  Batch artifact enforces that the checkpointing occurs at the specified time-limit boundary (10 seconds) and that 
     *                  reading/writing resumes at last good checkpoint.
     *                  test that the job completes successfully.     
     */
    @Test

    public void testChunkTimeBased10Seconds() throws Exception {
    	
    	 String METHOD = "testChunkTimeBased10Seconds";
    	 
    	 String DEFAULT_SLEEP_TIME = "500";

    	try {
	        Properties jobParams = new Properties();
	        jobParams.put("execution.number", "1");
	        jobParams.put("readrecord.fail", "31");
	        jobParams.put("app.arraysize", "30");
	        jobParams.put("app.sleeptime", System.getProperty("ChunkTests.testChunkTimeBased10Seconds.sleep",DEFAULT_SLEEP_TIME));
	
	
	        JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkTimeBasedCheckpoint", jobParams);
	        assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
	        String exitStatus = execution1.getExitStatus();
	        assertWithMessage("Testing execution #1", (exitStatus.equals("TRUE: 9") || exitStatus.equals("TRUE: 10") || exitStatus.equals("TRUE: 11")));
	
	        logger.info("exit status = " + execution1.getExitStatus() + "<p>");
    	 } catch (Exception e) {
             handleException(METHOD, e);
         }
    }

    /*
     * @testName: testChunkRestartTimeBasedCheckpoint
     * @assertion: first job start will finishas FAILED. Restart of job will finish successfully as COMPLETED
     *             and the time-limit of 10 seconds recognized
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, time-limit
     *             5.2.1 - Chunk item checkpointing/restart
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set. time-limit specified as 10. 
     *                  and configured to fail on the 12 item read. Restart job.
     *                  Batch artifact enforces that the checkpointing occurs at the specified time-limit boundary (10 seconds) and that 
     *                  reading/writing resumes at last good checkpoint.
     *                  test that the job completes successfully.     
     */
    @Test

    public void testChunkRestartTimeBasedCheckpoint() throws Exception {
        String METHOD = "testChunkRestartTimeBasedCheckpoint";
        
        String DEFAULT_SLEEP_TIME = "500";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=12<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "12");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.sleeptime", System.getProperty("ChunkTests.testChunkRestartTimeBasedCheckpoint.sleep",DEFAULT_SLEEP_TIME));



            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunkTimeBasedCheckpoint", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "FALSE: 0", execution1.getExitStatus());

            long jobInstanceId = execution1.getInstanceId();
            long lastExecutionId = execution1.getExecutionId();
            logger.info("Got Job instance id: " + jobInstanceId + "<p>");
            logger.info("Got Job execution id: " + lastExecutionId + "<p>");

            {
                logger.info("Invoke restartJobAndWaitForResult with execution id: " + lastExecutionId + "<p>");
                TCKJobExecutionWrapper exec = jobOp.restartJobAndWaitForResult(lastExecutionId, jobParams);
                lastExecutionId = exec.getExecutionId();
                logger.info("execution #2 JobExecution getBatchStatus()=" + exec.getBatchStatus() + "<p>");
                logger.info("execution #2 JobExecution getExitStatus()=" + exec.getExitStatus() + "<p>");
                logger.info("execution #2 Job instance id=" + exec.getInstanceId() + "<p>");
                assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
                String exitStatus = exec.getExitStatus();
    	        assertWithMessage("Testing execution #2", (exitStatus.equals("TRUE: 9") || exitStatus.equals("TRUE: 10") || exitStatus.equals("TRUE: 11")));
    	
                assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());
            }
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkRestartTimeBasedDefaultCheckpoint
     * @assertion: first job start will finishas FAILED. Restart of job will finish successfully as COMPLETED
     *             and the default time-limit of 10 seconds recognized
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, time-limit
     *             5.2.1 - Chunk item checkpointing/restart
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set. time-limit not specified so as to default to 10. 
     *                  and configured to fail on the 12 item read. Restart job.
     *                  Batch artifact enforces that the checkpointing occurs at the default time-limit boundary (10 seconds) and that 
     *                  reading/writing resumes at last good checkpoint.
     *                  test that the job completes successfully.     
     */
    @Test

    public void testChunkRestartTimeBasedDefaultCheckpoint() throws Exception {

        String METHOD = "testChunkRestartTimeBasedDefaultCheckpoint";
        
        String DEFAULT_SLEEP_TIME = "500";
        
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=2<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "2");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.sleeptime", System.getProperty("ChunkTests.testChunkRestartTimeBasedDefaultCheckpoint.sleep",DEFAULT_SLEEP_TIME));

            logger.info("Locate job XML file: chunkTimeBasedDefaultCheckpoint.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunkTimeBasedDefaultCheckpoint", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "TRUE: 0", execution1.getExitStatus());

            long jobInstanceId; 
            JobInstance jobInstance = jobOp.getJobInstance(execution1.getExecutionId());
            jobInstanceId = jobInstance.getInstanceId();
            long lastExecutionId = execution1.getExecutionId();
            logger.info("Got Job instance id: " + jobInstanceId + "<p>");
            logger.info("Got Job execution id: " + lastExecutionId + "<p>");
            {

                logger.info("Invoke restartJobAndWaitForResult with execution id: " + lastExecutionId + "<p>");
                TCKJobExecutionWrapper exec = jobOp.restartJobAndWaitForResult(lastExecutionId, jobParams);
                lastExecutionId = exec.getExecutionId();
                logger.info("execution #2 JobExecution getBatchStatus()=" + exec.getBatchStatus() + "<p>");
                logger.info("execution #2 JobExecution getExitStatus()=" + exec.getExitStatus() + "<p>");
                logger.info("execution #2 Job instance id=" + exec.getInstanceId() + "<p>");
                assertWithMessage("Testing execution #2", BatchStatus.COMPLETED, exec.getBatchStatus());
                assertWithMessage("Testing execution #2", "TRUE: 0", exec.getExitStatus());
                assertWithMessage("Testing execution #2", jobInstanceId, exec.getInstanceId());
            }
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }


    /*
     * @testName: testChunkSkipMultipleExceptions
     * @assertion: job will finish successfully as COMPLETED and skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at 3.  
     *                  Application is configured to encounter a read error on two separate reads, at which point
     *                  different instances of exceptions are thrown by the application at the fail points. 
     *                  The application is configured with two skippable exceptions and one un-skippable exception.
     *                  the 2nd skippable exception extends the unskippable exception. Batch Application enforces that 
     *                  if the skippable exception which extends the unskippable is encountered, it is treated as unskippable itself.
     *                  test that the job completes fails and that the application recognized the skippable exception 
     *                  that extends the unskippable is not treated as skippable.     
     */

	@Test // Disabling per Bug 5403
	@Disabled("Bug 5403.  Decided to exclude this test. Hopefully will introduce a modified version in 1.1")
    public void testChunkSkipMultipleExceptions() throws Exception {

        String METHOD = "testChunkSkipRead";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=1,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "1,3");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipMultipleExceptions.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipMultipleExceptions", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MySkipReadListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }
    
    /*
     * @testName: testChunkSkipRead
     * @assertion: job will finish successfully as COMPLETED and skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at 3.  
     *                  Application is configured to encounter an error on two separate reads, at which point
     *                  a skippable exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as skippable.
     *                  test that the job completes successfully and that the application recognized the exceptions as skippable     
     */
    @Test

    public void testChunkSkipRead() throws Exception {

        String METHOD = "testChunkSkipRead";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=1,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "1,3");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipInitialTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipInitialTest", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MySkipReadListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkSkipProcess
     * @assertion: job will finish successfully as COMPLETED and skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified as 3. skip-limit is set to 1000.
     *                  Application is configured to encounter an error on two separate processing actions, at which point
     *                  a skippable exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as skippable. Batch Application also ensures that the item being processed is passed to the skip listener.
     *                  test that the job completes successfully and that the application recognized the exception as skippable 
     *                  and that the item was passed to the skip listener.     
     */
    @Test

    public void testChunkSkipProcess() throws Exception {
        String METHOD = "testChunkSkipProcess";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("processrecord.fail=7,13<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("processrecord.fail", "7,13");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipInitialTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipInitialTest", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MySkipProcessListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }

    /*
     * @testName: testChunkSkipWrite
     * @assertion: job will finish successfully as COMPLETED and skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified as 3. skip-limit set to 1000  
     *                  Application is configured to encounter an error on two separate writes, at which point
     *                  a skippable exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as skippable. Batch Application also ensures that the item being processed is passed to the skip listener.
     *                  test that the job completes successfully and that the application recognized the exceptions as skippable
     *                  and that the item was passed to the skip listener.
     */
    @Test

    public void testChunkSkipWrite() throws Exception {
        String METHOD = "testChunkSkipWrite";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("writerecord.fail=1,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("writerecord.fail", "1,3");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipInitialTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipInitialTest", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MySkipWriteListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkSkipOnError
     * @assertion: job will finish successfully as COMPLETED and skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at 3.  
     *                  Application is configured to encounter an error on two separate reads, at which point
     *                  a skippable exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as skippable.
     *                  test that the job completes successfully and that the application recognized the exceptions as skippable     
     */
    @Test

    public void testChunkSkipOnError() throws Exception {

        String METHOD = "testChunkSkipOnError";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=1,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "1,3");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipOnErrorTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipOnErrorTest", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "MyItemReadListenerImpl.onReadError", execution1.getExitStatus());
            
            //process skip
            jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("processrecord.fail=7,13<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("processrecord.fail", "7,13");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipOnErrorTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution2 = jobOp.startJobAndWaitForResult("chunkSkipOnErrorTest", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution2.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution2.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution2.getBatchStatus());
            assertWithMessage("Testing execution #1", "MyItemProcessListenerImpl.onProcessError", execution2.getExitStatus());
            
            //write skip
            jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("writerecord.fail=1,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("writerecord.fail", "1,3");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipOnErrorTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution3 = jobOp.startJobAndWaitForResult("chunkSkipOnErrorTest", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution3.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution3.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution3.getBatchStatus());
            assertWithMessage("Testing execution #1", "MyItemWriteListenerImpl.onWriteError", execution3.getExitStatus());
            
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }
    
    /*
     * @testName: testChunkRetryOnError
     * @assertion: job will finish successfully as COMPLETED and skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at 3.  
     *                  Application is configured to encounter an error on two separate reads, at which point
     *                  a retry exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as retryable and that the listener's onError method is coalled correctly.
     *                  test that the job completes successfully and that the listener's onError method is called by the runtime.    
     */
    @Test

    public void testChunkRetryOnError() throws Exception {

        String METHOD = "testChunkRetryOnError";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=1,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "1,3");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkRetryOnError.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkRetryOnError", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "MyItemReadListenerImpl.onReadError", execution1.getExitStatus());
            
            //process skip
            jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("processrecord.fail=7,13<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("processrecord.fail", "7,13");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkRetryOnError.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution2 = jobOp.startJobAndWaitForResult("chunkRetryOnError", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution2.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution2.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution2.getBatchStatus());
            assertWithMessage("Testing execution #1", "MyItemProcessListenerImpl.onProcessError", execution2.getExitStatus());
            
            //write skip
            jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("writerecord.fail=1,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("writerecord.fail", "1,3");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkRetryOnError.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution3 = jobOp.startJobAndWaitForResult("chunkRetryOnError", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution3.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution3.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution3.getBatchStatus());
            assertWithMessage("Testing execution #1", "MyItemWriteListenerImpl.onWriteError", execution3.getExitStatus());
            
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }
    
    /*
     * @testName: testChunkSkipReadExceedSkip
     * @assertion: job will finish as FAILED and exceeded skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set and skip-limit set to 1.  
     *                  Application is configured to encounter an error on two separate reads, at which point
     *                  a skippable exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as skippable and that the second exception exceeded the skip-limit
     *                  test that the job fails but the skip-limit was recognized.     
     */
    @Test

    public void testChunkSkipReadExceedSkip() throws Exception {
        String METHOD = "testChunkSkipReadExceedSkip";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("processrecord.fail=1,2<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "1,2");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipExceededTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipExceededTest", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MySkipReadListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkSkipProcessExceedSkip
     * @assertion: job will finish as FAILED and exceeded skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set and skip-limit set to 1.  
     *                  Application is configured to encounter an error on two processing actions, at which point
     *                  a skippable exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as skippable and that the second exception exceeded the skip-limit
     *                  test that the job fails but the skip-limit was recognized.     
     */
    @Test

    public void testChunkSkipProcessExceedSkip() throws Exception {

        String METHOD = "testChunkSkipProcessExceedSkip";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("processrecord.fail=5,7<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("processrecord.fail", "5,7");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipExceededTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipExceededTest", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MySkipProcessListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkSkipWriteExceedSkip
     * @assertion: job will finish as FAILED and exceeded skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set and skip-limit set to 1.  
     *                  Application is configured to encounter an error on two separate writes, at which point
     *                  a skippable exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as skippable and that the second exception exceeded the skip-limit
     *                  test that the job fails but the skip-limit was recognized.     
     */
    @Test

    public void testChunkSkipWriteExceedSkip() throws Exception {
        String METHOD = "testChunkSkipWriteExceedSkip";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("writerecord.fail=2,8<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("writerecord.fail", "2,8");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipExceededTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipExceededTest", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MySkipWriteListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkSkipReadNoSkipChildEx
     * @assertion: job will finish as FAILED and excluded skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - skippable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set and skip-limit set to 1000.  
     *                  Application is configured to encounter an error on three separate reads.On the first two fails, the application
     *                  throws a skippable exception. On the third fail, the application throws a non-skippable exception.
     *                  The Batch Application enforces that the final exception is non-skippable.
     *                  were recognized as skippable and that the second exception exceeded the skip-limit
     *                  test that the job fails but the final exception was non skippable was recognized.     
     */
    @Test

    public void testChunkSkipReadNoSkipChildEx() throws Exception {
        String METHOD = "testChunkSkipReadNoSkipChildEx";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("writerecord.fail=1,2,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "1,2,3");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkSkipNoSkipChildExTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkSkipNoSkipChildExTest", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MySkipReadListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkRetryRead
     * @assertion: job will finish successfully as COMPLETED and retryable skippable exceptions will be recognized 
     * 			   5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, retry-limit
     *             5.2.1.5 - Exception Handling - retry-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at a value greater than the read data set and retry-limit set to 4.  
     *                  Application is configured to encounter an error on three separate reads, at which point
     *                  a retryable exception is thrown by the application. Batch Application enforces that the exceptions
     *                  were recognized as retryable and that the processing retrys the execution.
     *                  test that the job succeeds.    
     */
    @Test

    public void testChunkRetryRead() throws Exception {
        String METHOD = "testChunkRetryRead";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("writerecord.fail=8,13,22<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "8,13,22");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkRetryInitialTest.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkRetryInitialTest", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "COMPLETED", execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }
    
    /*
     * @testName: testChunkRetryMultipleExceptions
     * @assertion: job will finish successfully as COMPLETED and skippable exceptions will be recognized 
     *             5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, skip-limit
     *             5.2.1.4 - Exception Handling - retryable-exception-classes
     * 
     * @test_Strategy: start a job with item-count specified at 3.  
     *                  Application is configured to encounter a read error on two separate reads, at which point
     *                  different instances of exceptions are thrown by the application. The application is configured with two retryable exceptions and one un-retryable exception.
     *                  the 2nd retryable exception extends the unretryable exception. Batch Application enforces that if the retryable exception which extends the unretryable is encountered, it is treated
     *                  as unretryable itself.
     *                  test that the job completes fails and that the application recognized the retryable exception that extends the unretryable is not treated as retryable.  
     */

	@Test // Disabling per Bug 5403
	@Disabled("Bug 5403.  Decided to exclude this test. Hopefully will introduce a modified version in 1.1")
    public void testChunkRetryMultipleExceptions() throws Exception {

        String METHOD = "testChunkRetryMultipleExceptions";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=1,3<p>");
            logger.info("app.arraysize=30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "1,3,5");
            jobParams.put("app.arraysize", "30");

            logger.info("Locate job XML file: chunkRetryMultipleExceptions.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("chunkRetryMultipleExceptions", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", MyMultipleExceptionsRetryReadListener.GOOD_EXIT_STATUS, execution1.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }

    /*
     * @testName: testChunkItemListeners
     * @assertion: each job will finish successfully as COMPLETED and the invocation of each type of item listener
     *             will be recognized 
     * 			   5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor, 5.2.2.1 - Processor Properties
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, retry-limit
     *             5.2.1.5 - Exception Handling - retry-exception-classes
     *             6.2.4 - ItemReadListener
     *             6.2.5 - ItemProcessListener
     *             6.2.6 - ItemWriteListener
     * 
     * @test_Strategy: start 3 separate jobs with item-count specified at a value greater than the read data set.
     *                  Each job is configured to enable an itemreadlistener, and itemprocesslistener and an itemwritelistener 
     *                  batch artifact.
     *                  The Batch Artifact enforces that each listener has been called correctly by the runtime.
     *                  test that each job succeeds and that the appropriate listener was called.    
     */
    @Test

    public void testChunkItemListeners() throws Exception {
        String METHOD = "testChunkItemListeners";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=31<p>");
            logger.info("app.arraysize=30<p>");
            logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
            logger.info("app.listenertest=READ<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "31");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
            jobParams.put("app.listenertest", "READ");

            logger.info("Locate job XML file: testListeners.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("testListeners", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1 for the READ LISTENER", BatchStatus.COMPLETED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1 for the READ LISTENER", MyItemReadListenerImpl.GOOD_EXIT_STATUS,
                    execution1.getExitStatus());

            logger.info("Create job parameters for execution #2:<p>");
            jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=31<p>");
            logger.info("app.arraysize=30<p>");
            logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
            logger.info("app.listenertest=PROCESS<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "31");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
            jobParams.put("app.listenertest", "PROCESS");

            logger.info("Invoke startJobAndWaitForResult for execution #2<p>");
            JobExecution execution2 = jobOp.startJobAndWaitForResult("testListeners", jobParams);
            logger.info("execution #2 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #2 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #2 for the PROCESS LISTENER", BatchStatus.COMPLETED, execution2.getBatchStatus());
            assertWithMessage("Testing execution #2 for the PROCESS LISTENER", MyItemProcessListenerImpl.GOOD_EXIT_STATUS,
                    execution2.getExitStatus());

            logger.info("Create job parameters for execution #3:<p>");
            jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=31<p>");
            logger.info("app.arraysize=30<p>");
            logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
            logger.info("app.listenertest=WRITE<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "31");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.writepoints", "0,5,10,15,20,25,30");
            jobParams.put("app.listenertest", "WRITE");

            logger.info("Invoke startJobAndWaitForResult for execution #3<p>");
            JobExecution execution3 = jobOp.startJobAndWaitForResult("testListeners", jobParams);
            logger.info("execution #3 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #3 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #3 for the WRITE LISTENER", BatchStatus.COMPLETED, execution3.getBatchStatus());
            assertWithMessage("Testing execution #3 for the WRITE LISTENER", MyItemWriteListenerImpl.GOOD_EXIT_STATUS,
                    execution3.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }
    
    /*
     * @testName: testChunkItemListenersOnError
     * @assertion: each job will finish successfully as COMPLETED and the invocation of each type of item listener
     *             will be recognized 
     * 			   5.2.1.1 - Reader, 5.2.1.1.1 - Reader Properties,
     *             5.2.1.2 - Processor, 5.2.2.1 - Processor Properties
     *             5.2.1.3 - Writer, 5.2.1.3.1 - Writer Properties
     *             5.2.1 - Chunk, item-count, retry-limit
     *             5.2.1.5 - Exception Handling - retry-exception-classes
     *             6.2.4 - ItemReadListener
     *             6.2.5 - ItemProcessListener
     *             6.2.6 - ItemWriteListener
     * 
     * @test_Strategy: start 3 separate jobs with item-count specified at a value greater than the read data set.
     *                  Each job is configured to enable an itemreadlistener, and itemprocesslistener and an itemwritelistener 
     *                  batch artifact. The Batch artifact is configured to raise an exception on the read, process and write in that order.
     *                  The Batch Artifact enforces that each listener (read, process and write) onError() methods are been called correctly by the runtime.    
     */
    @Test

    public void testChunkItemListenersOnError() throws Exception {
        String METHOD = "testChunkItemListeners";

        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            
            logger.info("read.fail.immediate=true<p>");
        
            jobParams.put("read.fail.immediate", "true");

            logger.info("Locate job XML file: testListeners.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            JobExecution execution1 = jobOp.startJobAndWaitForResult("testListenersOnError", jobParams);

            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1 for the READ LISTENER", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1 for the READ LISTENER", "MyItemReadListenerImpl.onReadError",
                    execution1.getExitStatus());

            logger.info("Create job parameters for execution #2:<p>");
            jobParams = new Properties();
            logger.info("process.fail.immediate=true<p>");
            
            jobParams.put("process.fail.immediate", "true");


            logger.info("Invoke startJobAndWaitForResult for execution #2<p>");
            JobExecution execution2 = jobOp.startJobAndWaitForResult("testListenersOnError", jobParams);
            logger.info("execution #2 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #2 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #2 for the PROCESS LISTENER", BatchStatus.FAILED, execution2.getBatchStatus());
            assertWithMessage("Testing execution #2 for the PROCESS LISTENER", "MyItemProcessListenerImpl.onProcessError",
                    execution2.getExitStatus());

            logger.info("Create job parameters for execution #3:<p>");
            jobParams = new Properties();
            logger.info("write.fail.immediate=true<p>");
            
            jobParams.put("write.fail.immediate", "true");


            logger.info("Invoke startJobAndWaitForResult for execution #3<p>");
            JobExecution execution3 = jobOp.startJobAndWaitForResult("testListenersOnError", jobParams);
            logger.info("execution #3 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #3 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #3 for the WRITE LISTENER", BatchStatus.FAILED, execution3.getBatchStatus());
            assertWithMessage("Testing execution #3 for the WRITE LISTENER", "MyItemWriteListenerImpl.onWriteError",
                    execution3.getExitStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }


    /*
     * @testName: testUserDataIsPersistedAfterCheckpoint
     * @assertion: 
     * 
     * @test_Strategy: start a job configured to a item-count of 10 configured to fail on the 12 item read.  
     *                 Verify that persisted step data is available even if step did not complete.
     */
    @Test

    public void testUserDataIsPersistedAfterCheckpoint() throws Exception {

        String METHOD = "testChunkRestartItemCount10";
        try {
            logger.info("Create job parameters for execution #1:<p>");
            Properties jobParams = new Properties();
            logger.info("execution.number=1<p>");
            logger.info("readrecord.fail=12<p>");
            logger.info("app.arraysize=30<p>");
            logger.info("app.writepoints=0,5,10,15,20,25,30<p>");
            logger.info("app.next.writepoints=0,5,10,15,20,25,30<p>");
            jobParams.put("execution.number", "1");
            jobParams.put("readrecord.fail", "12");
            jobParams.put("app.arraysize", "30");
            jobParams.put("app.checkpoint.position" , "0");
            jobParams.put("app.writepoints", "0,10,20,30");
            jobParams.put("app.next.writepoints", "10,20,30");

            logger.info("Locate job XML file: chunkrestartCheckpt10.xml<p>");

            logger.info("Invoke startJobAndWaitForResult for execution #1<p>");
            TCKJobExecutionWrapper execution1 = jobOp.startJobAndWaitForResult("chunkrestartCheckpt10", jobParams);
            logger.info("execution #1 JobExecution getBatchStatus()=" + execution1.getBatchStatus() + "<p>");
            logger.info("execution #1 JobExecution getExitStatus()=" + execution1.getExitStatus() + "<p>");
            assertWithMessage("Testing execution #1", BatchStatus.FAILED, execution1.getBatchStatus());
            assertWithMessage("Testing execution #1", "FAILED", execution1.getExitStatus());
            
            List<StepExecution> stepExecs = jobOp.getStepExecutions(execution1.getExecutionId());
            
            MyPersistentRestartUserData persistedStepData = null;
            for (StepExecution stepExec : stepExecs) {
                if (stepExec.getStepName().equals("step1")) {
                    persistedStepData = (MyPersistentRestartUserData) stepExec.getPersistentUserData();
                    break;
                }
            }
            
            assertWithMessage("Testing execution #1", 1, persistedStepData.getExecutionNumber());
            

        } catch (Exception e) {
            handleException(METHOD, e);
        }

    }
    
    private void showStepState(StepExecution step) {

        logger.info("---------------------------<p>");
        logger.info("getJobExecutionId(): " + step.getStepExecutionId() + " - ");

        Metric[] metrics = step.getMetrics();

        for (int i = 0; i < metrics.length; i++) {
            logger.info(metrics[i].getType() + ": " + metrics[i].getValue() + " - ");
        }

        logger.info("getStartTime(): " + step.getStartTime() + " - ");
        logger.info("getEndTime(): " + step.getEndTime() + " - ");
        logger.info("getBatchStatus(): " + step.getBatchStatus() + " - ");
        logger.info("getExitStatus(): " + step.getExitStatus());
        logger.info("---------------------------<p>");
    }

    private static void handleException(String methodName, Exception e) throws Exception {
        logger.info("Caught exception: " + e.getMessage() + "<p>");
        logger.info(methodName + " failed<p>");
        throw e;
    }

}
