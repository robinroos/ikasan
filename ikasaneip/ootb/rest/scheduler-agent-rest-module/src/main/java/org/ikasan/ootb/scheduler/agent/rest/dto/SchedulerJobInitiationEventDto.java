package org.ikasan.ootb.scheduler.agent.rest.dto;

import org.ikasan.spec.scheduled.event.model.SchedulerJobInitiationEvent;

import java.util.List;

public class SchedulerJobInitiationEventDto implements SchedulerJobInitiationEvent<ContextParameterInstanceDto, InternalEventDrivenJobInstanceDto, DryRunParametersDto> {

    private String agentName;
    private String agentUrl;
    private String jobName;
    private List<ContextParameterInstanceDto> contextParameters;
    private InternalEventDrivenJobInstanceDto internalEventDrivenJob;
    private String contextId;
    private List<String> childContextIds;
    private String contextInstanceId;
    private boolean dryRun;
    private DryRunParametersDto dryRunParametersDto;
    private boolean skipped;

    @Override
    public InternalEventDrivenJobInstanceDto getInternalEventDrivenJob() {
        return this.internalEventDrivenJob;
    }

    @Override
    public void setInternalEventDrivenJob(InternalEventDrivenJobInstanceDto internalEventDrivenJob) {
        this.internalEventDrivenJob = internalEventDrivenJob;
    }

    @Override
    public void setContextParameters(List<ContextParameterInstanceDto> contextParameters) {
        this.contextParameters = contextParameters;
    }

    @Override
    public List<ContextParameterInstanceDto> getContextParameters() {
        return this.contextParameters;
    }

    @Override
    public String getAgentName() {
        return this.agentName;
    }

    @Override
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Override
    public String getAgentUrl() {
        return agentUrl;
    }

    @Override
    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }

    @Override
    public String getJobName() {
        return this.jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String getContextName() {
        return this.contextId;
    }

    @Override
    public void setContextName(String contextName) {
        this.contextId = contextName;
    }

    @Override
    public String getContextInstanceId() {
        return contextInstanceId;
    }

    @Override
    public void setContextInstanceId(String contextInstanceId) {
        this.contextInstanceId = contextInstanceId;
    }

    @Override
    public List<String> getChildContextNames() {
        return childContextIds;
    }

    @Override
    public void setChildContextNames(List<String> childContextIds) {
        this.childContextIds = childContextIds;
    }

    @Override
    public boolean isDryRun() {
        return this.dryRun;
    }

    @Override
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public void setDryRunParameters(DryRunParametersDto dryRunParametersDto) {
        this.dryRunParametersDto = dryRunParametersDto;
    }

    @Override
    public DryRunParametersDto getDryRunParameters() {
        return this.dryRunParametersDto;
    }

    @Override
    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    @Override
    public boolean isSkipped() {
        return this.skipped;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SchedulerJobInitiationEventDto{");
        sb.append("agentName='").append(agentName).append('\'');
        sb.append(", agentUrl='").append(agentUrl).append('\'');
        sb.append(", jobName='").append(jobName).append('\'');
        sb.append(", internalEventDrivenJob=").append(internalEventDrivenJob);
        sb.append(", contextId='").append(contextId).append('\'');
        if(childContextIds != null) {
            sb.append(", childContextIds=[ ");
            childContextIds.forEach(id -> sb.append("[").append(id).append("] "));
        }
        else {
            sb.append(", childContextIds='").append(this.childContextIds).append('\'');
        }
        sb.append("], contextInstanceId='").append(contextInstanceId).append('\'');
        sb.append(", contextParameters=").append(contextParameters);
        sb.append(", dryRun=").append(dryRun);
        sb.append(", dryRunParameters=").append(dryRunParametersDto);
        sb.append(", skipped=").append(skipped);
        sb.append('}');
        return sb.toString();
    }
}
