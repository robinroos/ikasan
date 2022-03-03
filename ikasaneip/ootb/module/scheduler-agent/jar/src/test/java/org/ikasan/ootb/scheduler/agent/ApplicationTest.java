/*
 * $Id$
 * $URL$
 *
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 *
 * Distributed under the Modified BSD License.
 * Copyright notice: The copyright for this software and a full listing
 * of individual contributors are as shown in the packaged copyright.txt
 * file.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  - Neither the name of the ORGANIZATION nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.ikasan.ootb.scheduler.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Resource;

import org.ikasan.component.endpoint.filesystem.messageprovider.FileConsumerConfiguration;
import org.ikasan.component.endpoint.filesystem.messageprovider.FileMessageProvider;
import org.ikasan.ootb.scheduled.model.ContextualisedScheduledProcessEventImpl;
import org.ikasan.ootb.scheduled.model.InternalEventDrivenJobImpl;
import org.ikasan.ootb.scheduler.agent.module.Application;
import org.ikasan.ootb.scheduler.agent.module.pointcut.FileMessageProviderAspect;
import org.ikasan.ootb.scheduler.agent.rest.cache.InboundJobQueueCache;
import org.ikasan.ootb.scheduler.agent.rest.dto.InternalEventDrivenJobDto;
import org.ikasan.ootb.scheduler.agent.rest.dto.SchedulerJobInitiationEventDto;
import org.ikasan.spec.flow.Flow;
import org.ikasan.spec.module.Module;
import org.ikasan.spec.scheduled.event.model.ScheduledProcessEvent;
import org.ikasan.spec.scheduled.job.model.InternalEventDrivenJob;
import org.ikasan.testharness.flow.rule.IkasanFlowTestRule;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.leansoft.bigqueue.IBigQueue;

/**
 * This test class supports the <code>vanilla integration module</code> application.
 *
 * @author Ikasan Development Team
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfiguration.class})
public class ApplicationTest {
    @Resource
    private Module<Flow> moduleUnderTest;

    @Resource
    private IBigQueue outboundQueue;

    @Resource
    private FileMessageProviderAspect fileMessageProviderAspect;

    private static ObjectMapper objectMapper = new ObjectMapper();

    public IkasanFlowTestRule flowTestRule = new IkasanFlowTestRule();

    @BeforeClass
    public static void setupObjectMapper() {
        final var simpleModule = new SimpleModule()
            .addAbstractTypeMapping(InternalEventDrivenJob.class, InternalEventDrivenJobImpl.class);

        objectMapper.registerModule(simpleModule);
    }

    @Before
    public void setup() throws IOException {
        outboundQueue.removeAll();
    }

    /**
     * Test simple invocation.
     */
    @Test
    public void test_create_module_start_and_stop_flow() throws Exception {
        Flow flow = moduleUnderTest.getFlow("Scheduler Flow 3");
        flow.start();
        assertEquals(Flow.RUNNING, flow.getState());

        flow.stop();
        assertEquals(Flow.STOPPED, flow.getState());

        flow = moduleUnderTest.getFlow("Scheduler Flow 1");
        flow.start();
        assertEquals(Flow.RUNNING, flow.getState());

        flow.stop();
        assertEquals(Flow.STOPPED, flow.getState());

        flow = moduleUnderTest.getFlow("Scheduler Flow 4");
        flow.start();
        assertEquals(Flow.RUNNING, flow.getState());

        flow.stop();
        assertEquals(Flow.STOPPED, flow.getState());

        flow = moduleUnderTest.getFlow("Scheduler Flow 2");
        flow.start();
        assertEquals(Flow.RUNNING, flow.getState());

        flow.stop();
        assertEquals(Flow.STOPPED, flow.getState());

        flow = moduleUnderTest.getFlow("Scheduled Process Event Outbound Flow");
        flow.start();
        assertEquals(Flow.RUNNING, flow.getState());

        flow.stop();
        assertEquals(Flow.STOPPED, flow.getState());
    }

    @Test
    public void test_job_processing_flow_success() throws IOException {
        flowTestRule.withFlow(moduleUnderTest.getFlow("Scheduler Flow 1"));
        flowTestRule.consumer("Job Consumer")
            .converter("JobInitiationEvent to ScheduledStatusEvent")
            .broker("Job Started Broker")
            .broker("Process Execution Broker")
            .producer("Scheduled Status Producer");

        flowTestRule.startFlow();
        assertEquals(Flow.RUNNING, flowTestRule.getFlowState());

        IBigQueue bigQueue = InboundJobQueueCache.instance().get("scheduler-agent-Scheduler Flow 1-inbound-queue");
        SchedulerJobInitiationEventDto schedulerJobInitiationEvent = new SchedulerJobInitiationEventDto();
        InternalEventDrivenJobDto internalEventDrivenJobDto = new InternalEventDrivenJobDto();
        internalEventDrivenJobDto.setAgentName("agent name");
        internalEventDrivenJobDto.setCommandLine("pwd");
        internalEventDrivenJobDto.setContextId("contextId");
        internalEventDrivenJobDto.setIdentifier("identifier");
        internalEventDrivenJobDto.setMinExecutionTime(1000L);
        internalEventDrivenJobDto.setMaxExecutionTime(10000L);
        internalEventDrivenJobDto.setWorkingDirectory(".");
        schedulerJobInitiationEvent.setInternalEventDrivenJob(internalEventDrivenJobDto);

        bigQueue.enqueue(objectMapper.writeValueAsBytes(schedulerJobInitiationEvent));

        flowTestRule.sleep(1000);

        flowTestRule.assertIsSatisfied();

        assertEquals(Flow.RUNNING, flowTestRule.getFlowState());

        assertEquals(2, outboundQueue.size());

        ScheduledProcessEvent event = objectMapper.readValue(outboundQueue.dequeue(), ContextualisedScheduledProcessEventImpl.class);
        assertEquals(true, event.isJobStarting());
        assertEquals(false, event.isSuccessful());

        event = objectMapper.readValue(outboundQueue.dequeue(), ContextualisedScheduledProcessEventImpl.class);
        assertEquals(false, event.isJobStarting());
        assertEquals(true, event.isSuccessful());

        flowTestRule.stopFlow();
    }

    @Test
    public void test_quartz_flow_success() throws IOException {
        flowTestRule.withFlow(moduleUnderTest.getFlow("Scheduler Flow 4"));
        flowTestRule.consumer("Scheduled Consumer")
            .converter("JobExecution to ScheduledStatusEvent")
            .producer("Scheduled Status Producer");

        flowTestRule.startFlow();
        assertEquals(Flow.RUNNING, flowTestRule.getFlowState());
        flowTestRule.fireScheduledConsumer();

        flowTestRule.sleep(2000);

        flowTestRule.assertIsSatisfied();

        assertEquals(Flow.RUNNING, flowTestRule.getFlowState());

        assertEquals(1, outboundQueue.size());

        flowTestRule.stopFlow();
    }

    @Test
    public void test_file_flow_success() throws IOException {
        flowTestRule.withFlow(moduleUnderTest.getFlow("Scheduler Flow 2"));
        FileConsumerConfiguration fileConsumerConfiguration = flowTestRule.getComponentConfig("File Consumer"
            , FileConsumerConfiguration.class);
        fileConsumerConfiguration.setFilenames(List.of("src/test/resources/data/test.txt"));

        flowTestRule.consumer("File Consumer")
            .converter("JobExecution to ScheduledStatusEvent")
            .producer("Scheduled Status Producer");

        flowTestRule.startFlow();
        assertEquals(Flow.RUNNING, flowTestRule.getFlowState());
        flowTestRule.fireScheduledConsumer();

        flowTestRule.sleep(2000);

        flowTestRule.assertIsSatisfied();

        assertEquals(Flow.RUNNING, flowTestRule.getFlowState());

        assertEquals(1, outboundQueue.size());

        flowTestRule.stopFlow();
    }

    @Test
    public void test_file_aspect() {
        // will get an error as the file message provider has nothing wired in etc
        FileMessageProvider fileMessageProvider = new FileMessageProvider();

        try {
            fileMessageProvider.invoke(null);
            fail("should not get here");
        } catch (Exception e) {}

        ReflectionTestUtils.setField(fileMessageProviderAspect, "dryRunMode", true);

        List<File> files = fileMessageProvider.invoke(null);
        assertEquals(1, files.size());

        ReflectionTestUtils.setField(fileMessageProviderAspect, "dryRunMode", false);
    }

    @Test
    @Ignore
    public void test() throws JsonProcessingException {
        HashMap<String, String> map = new HashMap<>();

        IntStream.range(0, 2000)
            .forEach(a -> map.put("Scheduler Flow " + a, "AUTOMATIC"));

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
    }

    @After
    public void teardown() {
        // post-test teardown
    }

}
