package org.ikasan.job.orchestration.model.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.ikasan.spec.scheduled.context.model.Context;
import org.ikasan.spec.scheduled.context.model.ContextDependency;
import org.ikasan.spec.scheduled.context.model.JobDependency;
import org.ikasan.spec.scheduled.job.model.SchedulerJob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContextImpl<CONTEXT extends Context, CONTEXT_PARAM, JOB extends SchedulerJob> implements Context<CONTEXT, CONTEXT_PARAM, JOB> {
    protected String name;
    protected String description;
    protected String timezone;
    protected List<JobDependency> jobDependencies;
    protected List<CONTEXT> contexts;
    protected List<ContextDependency> contextDependencies;
    protected List<CONTEXT_PARAM> contextParameters;
    protected List<JOB> scheduledJobs;
    protected String timeWindowStart;
    protected String timeWindowEnd;
    protected Map<String, List<JOB>> jobLocks;

    @JsonIgnore
    protected Map<String, JOB> scheduledJobsMap = new HashMap<>();
    @JsonIgnore
    protected Map<String, CONTEXT> contextsMap = new HashMap<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getTimezone() {
        return timezone;
    }

    @Override
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public List<CONTEXT_PARAM> getContextParameters() {
        return contextParameters;
    }

    @Override
    public void setContextParameters(List<CONTEXT_PARAM> contextParameters) {
        this.contextParameters = contextParameters;
    }

    @Override
    public List<JOB> getScheduledJobs() {
        return scheduledJobs;
    }

    @Override
    public void setScheduledJobs(List<JOB> scheduledJobs) {
        this.scheduledJobs = scheduledJobs;
        if(scheduledJobs != null) {
            this.scheduledJobsMap = this.scheduledJobs.stream()
                .collect(Collectors.toMap(item -> item.getIdentifier()   , item -> item));
        }
    }

    @Override
    public List<JobDependency> getJobDependencies() {
        return jobDependencies;
    }

    @Override
    public void setJobDependencies(List<JobDependency> jobDependencies) {
        this.jobDependencies = jobDependencies;
    }

    @Override
    public List<CONTEXT> getContexts() {
        return contexts;
    }

    @Override
    public void setContexts(List<CONTEXT> contexts) {
        this.contexts = contexts;
        if(this.contexts != null) {
            this.contextsMap = this.contexts.stream()
                .collect(Collectors.toMap(item -> item.getName(), item -> item));
        }
    }

    @Override
    public List<ContextDependency> getContextDependencies() {
        return contextDependencies;
    }

    @Override
    public void setContextDependencies(List<ContextDependency> contextDependencies) {
        this.contextDependencies = contextDependencies;
    }

    @Override
    public Map<String, JOB> getScheduledJobsMap() {
        return scheduledJobsMap;
    }

    @Override
    public Map<String, CONTEXT> getContextsMap() {
        return contextsMap;
    }

    @Override
    public String getTimeWindowStart() {
        return timeWindowStart;
    }

    @Override
    public void setTimeWindowStart(String timeWindowStart) {
        this.timeWindowStart = timeWindowStart;
    }

    @Override
    public String getTimeWindowEnd() {
        return timeWindowEnd;
    }

    @Override
    public void setTimeWindowEnd(String timeWindowEnd) {
        this.timeWindowEnd = timeWindowEnd;
    }

    @Override
    public Map<String, List<JOB>> getJobLocks() {
        return jobLocks;
    }

    @Override
    public void setJobLocks(Map<String, List<JOB>> jobLocks) {
        this.jobLocks = jobLocks;
    }
}