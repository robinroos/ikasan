package org.ikasan.ootb.scheduler.agent.module.rest.dto;

import org.ikasan.spec.scheduled.SchedulerJobInitiationEvent;

import java.util.List;

public class SchedulerJobInitiationEventDto implements SchedulerJobInitiationEvent<ContextParameterDto, InternalEventDrivenJobDto> {

    @Override
    public String getAgentName() {
        return null;
    }

    @Override
    public String getJobName() {
        return null;
    }

    @Override
    public InternalEventDrivenJobDto getInternalEventDrivenJob() {
        return null;
    }

    @Override
    public void setAgentName(String agentName) {

    }

    @Override
    public void setJobName(String jobName) {

    }

    @Override
    public void setInternalEventDrivenJob(InternalEventDrivenJobDto internalEventDrivenJob) {

    }

    @Override
    public void setContextParameters(List<ContextParameterDto> contextParameters) {

    }

    @Override
    public List<ContextParameterDto> getContextParameters() {
        return null;
    }

}
