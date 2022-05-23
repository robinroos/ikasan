package org.ikasan.ootb.scheduler.agent.module.component.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ikasan.ootb.scheduler.agent.rest.dto.ContextParameterDto;
import org.ikasan.ootb.scheduler.agent.rest.dto.DryRunParametersDto;
import org.ikasan.ootb.scheduler.agent.rest.dto.InternalEventDrivenJobDto;
import org.ikasan.ootb.scheduler.agent.rest.dto.SchedulerJobInitiationEventDto;
import org.ikasan.spec.component.transformation.TransformationException;
import org.ikasan.spec.scheduled.event.model.ContextualisedScheduledProcessEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JobInitiationToContextualisedScheduledProcessEventConverterTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_exception_null_module_name_constructor() {
        new JobInitiationToContextualisedScheduledProcessEventConverter(null, "logFolder", "logParen");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_exception_null_log_folder_constructor() {
        new JobInitiationToContextualisedScheduledProcessEventConverter("moduleName", null, "logParen");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_exception_null_log_paren_constructor() {
        new JobInitiationToContextualisedScheduledProcessEventConverter("moduleName", "logFolder", null);
    }

    @Test
    public void test_convert_success() throws JsonProcessingException {
        SchedulerJobInitiationEventDto schedulerJobInitiationEventDto = new SchedulerJobInitiationEventDto();
        schedulerJobInitiationEventDto.setSkipped(true);
        schedulerJobInitiationEventDto.setDryRunParameters(new DryRunParametersDto());
        schedulerJobInitiationEventDto.setDryRun(true);
        schedulerJobInitiationEventDto.setAgentName("agentName");
        schedulerJobInitiationEventDto.setAgentUrl("agentUrl");
        schedulerJobInitiationEventDto.setChildContextIds(List.of("childContextId1", "childContextId2"));
        schedulerJobInitiationEventDto.setContextId("contextId");
        schedulerJobInitiationEventDto.setContextInstanceId("contextInstanceId");
        schedulerJobInitiationEventDto.setJobName("jobName");

        InternalEventDrivenJobDto internalEventDrivenJob = new InternalEventDrivenJobDto();
        internalEventDrivenJob.setAgentName("agentName");
        internalEventDrivenJob.setContextParameters(List.of(new ContextParameterDto(), new ContextParameterDto()));

        schedulerJobInitiationEventDto.setInternalEventDrivenJob(internalEventDrivenJob);

        ObjectMapper objectMapper = new ObjectMapper();

        JobInitiationToContextualisedScheduledProcessEventConverter converter
            = new JobInitiationToContextualisedScheduledProcessEventConverter("agentName", "logFolder", "/");

        ContextualisedScheduledProcessEvent result = converter.convert(objectMapper.writeValueAsString(schedulerJobInitiationEventDto));

        long currentTestTimeMillis = System.currentTimeMillis();

        //make sure the millis is in the last 500ms (If the test is taking more than this something is wrong)
        Assert.assertTrue(isWithinTheLast500Millis(result.getFireTime(), currentTestTimeMillis));

        Assert.assertEquals("agentName", result.getAgentName());
        Assert.assertEquals("jobName", result.getJobName());
        Assert.assertEquals("contextId", result.getContextId());
        Assert.assertEquals("contextInstanceId", result.getContextInstanceId());
        Assert.assertEquals(2, result.getChildContextIds().size());
        Assert.assertTrue(result.isJobStarting());
        Assert.assertFalse(result.isSuccessful());
        Assert.assertTrue(result.isSkipped());
        Assert.assertTrue(result.isDryRun());

        // file name is "logFolder/contextId-contextInstanceId-agentName-jobName-System.currentTimeMillis()-err.log"
        Assert.assertTrue(result.getResultError().startsWith("logFolder/contextId-contextInstanceId-agentName-jobName-"));
        Assert.assertTrue(result.getResultError().endsWith("-err.log"));
        String millis = result.getResultError().substring("logFolder/contextId-contextInstanceId-agentName-jobName-".length(), result.getResultError().length() - "-err.log".length());
        Long millisFromFileName = Long.valueOf(millis);
        //make sure the millis from the filename is in the last 500ms (If the test is taking more than this something is wrong)
        Assert.assertTrue(isWithinTheLast500Millis(millisFromFileName, currentTestTimeMillis));

        // file name is "logFolder/contextId-contextInstanceId-agentName-jobName-System.currentTimeMillis()-out.log"
        Assert.assertTrue(result.getResultOutput().startsWith("logFolder/contextId-contextInstanceId-agentName-jobName-"));
        Assert.assertTrue(result.getResultOutput().endsWith("-out.log"));
        millis = result.getResultOutput().substring("logFolder/contextId-contextInstanceId-agentName-jobName-".length(), result.getResultError().length() - "-err.log".length());
        millisFromFileName = Long.valueOf(millis);
        //make sure the millis from the filename is in the last 500ms (If the test is taking more than this something is wrong)
        Assert.assertTrue(isWithinTheLast500Millis(millisFromFileName, currentTestTimeMillis));

        Assert.assertEquals(2, result.getInternalEventDrivenJob().getContextParameters().size());
    }

    @Test(expected = TransformationException.class)
    public void test_convert_exception() {
        JobInitiationToContextualisedScheduledProcessEventConverter converter
            = new JobInitiationToContextualisedScheduledProcessEventConverter("agentName", "logFolder", "/");

        converter.convert("BAD PAYLOAD");
    }

    private boolean isWithinTheLast500Millis(long resultMillis, long testMillis) {
        return resultMillis > testMillis - 500 && resultMillis < testMillis + 500;
    }
}