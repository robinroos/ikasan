package org.ikasan.spec.scheduled.job.model;

public interface QuartzScheduleDrivenJobRecord {

    public String getId();

    public String getAgentName();

    public void setAgentName(String agentName);

    public String getJobName();

    public void setJobName(String jobName);

    String getDisplayName();

    void setDisplayName(String displayName);

    String getContextName();

    void setContextName(String contextName);

    public QuartzScheduleDrivenJob getQuartzScheduleDrivenJob();

    public void setQuartzScheduleDrivenJob(QuartzScheduleDrivenJob quartzScheduleDrivenJob);

    public long getTimestamp();

    public void setTimestamp(long timestamp);

    long getModifiedTimestamp();

    void setModifiedTimestamp(long timestamp);

    String getModifiedBy();

    void setModifiedBy(String modifiedBy);
}
