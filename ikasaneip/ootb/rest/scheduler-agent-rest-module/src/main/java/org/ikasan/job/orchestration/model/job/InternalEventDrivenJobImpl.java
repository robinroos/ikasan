package org.ikasan.job.orchestration.model.job;

import org.ikasan.spec.scheduled.context.model.ContextParameter;
import org.ikasan.spec.scheduled.job.model.InternalEventDrivenJob;

import java.util.ArrayList;
import java.util.List;

public class InternalEventDrivenJobImpl extends SchedulerJobImpl implements InternalEventDrivenJob {

    private List<String> successfulReturnCodes;
    private String workingDirectory;
    private String commandLine;
    private long minExecutionTime;
    private long maxExecutionTime;
    private List<ContextParameter> contextParameters = new ArrayList<>();
    private List<Integer> daysOfWeekToRun;
    private boolean targetResidingContextOnly;
    private boolean participatesInLock;
    private String executionEnvironmentProperties;

    @Override
    public List<String> getSuccessfulReturnCodes() {
        return this.successfulReturnCodes;
    }

    @Override
    public void setSuccessfulReturnCodes(List<String> successfulReturnCodes) {
        this.successfulReturnCodes = successfulReturnCodes;
    }

    @Override
    public String getWorkingDirectory() {
        return this.workingDirectory;
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public String getCommandLine() {
        return this.commandLine;
    }

    @Override
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public long getMinExecutionTime() {
        return minExecutionTime;
    }

    @Override
    public void setMinExecutionTime(long minExecutionTime) {
        this.minExecutionTime = minExecutionTime;
    }

    @Override
    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    @Override
    public void setMaxExecutionTime(long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    @Override
    public List<ContextParameter> getContextParameters() {
        return contextParameters;
    }

    @Override
    public void setContextParameters(List<ContextParameter> contextParameters) {
        this.contextParameters = contextParameters;
    }

    @Override
    public List<Integer> getDaysOfWeekToRun() {
        return this.daysOfWeekToRun;
    }

    @Override
    public void setDaysOfWeekToRun(List<Integer> daysOfWeekToRun) {
        this.daysOfWeekToRun = daysOfWeekToRun;
    }

    @Override
    public boolean isTargetResidingContextOnly() {
        return targetResidingContextOnly;
    }

    @Override
    public void setTargetResidingContextOnly(boolean targetResidingContextOnly) {
        this.targetResidingContextOnly = targetResidingContextOnly;
    }

    @Override
    public boolean isParticipatesInLock() {
        return participatesInLock;
    }

    @Override
    public void setParticipatesInLock(boolean participatesInLock) {
        this.participatesInLock = participatesInLock;
    }

    @Override
    public String getExecutionEnvironmentProperties() {
        return executionEnvironmentProperties;
    }

    @Override
    public void setExecutionEnvironmentProperties(String executionEnvironmentProperties) {
        this.executionEnvironmentProperties = executionEnvironmentProperties;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("InternalEventDrivenJobImpl{");
        sb.append("successfulReturnCodes=").append(successfulReturnCodes);
        sb.append(", workingDirectory='").append(workingDirectory).append('\'');
        sb.append(", commandLine='").append(commandLine).append('\'');
        sb.append(", minExecutionTime=").append(minExecutionTime);
        sb.append(", maxExecutionTime=").append(maxExecutionTime);
        sb.append(", contextParameters=").append(contextParameters);
        sb.append(", jobIdentifier='").append(jobIdentifier).append('\'');
        sb.append(", agentName='").append(agentName).append('\'');
        sb.append(", jobName='").append(jobName).append('\'');
        sb.append(", contextName='").append(contextName).append('\'');
        if(childContextNames != null) {
            sb.append(", childContextNames=[ ");
            childContextNames.forEach(id -> sb.append("[").append(id).append("] "));
        }
        else {
            sb.append(", childContextNames='").append(this.childContextNames).append('\'');
        }
        sb.append("], description='").append(description).append('\'');
        sb.append(", startupControlType='").append(startupControlType).append('\'');
        sb.append(", targetResidingContextOnly='").append(targetResidingContextOnly).append('\'');
        sb.append(", participatesInLock='").append(participatesInLock).append('\'');
        sb.append(", executionEnvironmentProperties='").append(executionEnvironmentProperties).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
