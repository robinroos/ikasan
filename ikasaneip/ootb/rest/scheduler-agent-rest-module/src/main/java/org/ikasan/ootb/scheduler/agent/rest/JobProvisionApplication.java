package org.ikasan.ootb.scheduler.agent.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.ikasan.component.endpoint.filesystem.messageprovider.FileConsumerConfiguration;
import org.ikasan.component.endpoint.quartz.consumer.ScheduledConsumerConfiguration;
import org.ikasan.job.orchestration.model.job.SchedulerJobWrapperImpl;
import org.ikasan.module.ConfiguredModuleConfiguration;
import org.ikasan.ootb.scheduler.agent.rest.dto.ErrorDto;
import org.ikasan.rest.module.util.UserUtil;
import org.ikasan.spec.configuration.ConfigurationService;
import org.ikasan.spec.configuration.ConfiguredResource;
import org.ikasan.spec.flow.Flow;
import org.ikasan.spec.module.Module;
import org.ikasan.spec.module.ModuleActivator;
import org.ikasan.spec.module.ModuleService;
import org.ikasan.spec.scheduled.job.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/rest/jobProvision")
@RestController
public class JobProvisionApplication {

    Logger logger = LoggerFactory.getLogger(JobProvisionApplication.class);

    @Value( "${module.name}" )
    private String moduleName;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ModuleActivator moduleActivator;

    @Autowired
    private ConfigurationService configurationService;

    private ObjectMapper mapper;

    /**
     * Constructor
     */
    public JobProvisionApplication() {
        this.mapper = new ObjectMapper();
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("org.ikasan.spec.scheduled.job.model")
            .allowIfSubType("org.ikasan.job.orchestration.model.job")
            .allowIfSubType("org.ikasan.job.orchestration.model.context")
            .allowIfSubType("java.util.ArrayList")
            .allowIfSubType("java.util.HashMap")
            .build();
        final var simpleModule = new SimpleModule()
            .addAbstractTypeMapping(List.class, ArrayList.class)
            .addAbstractTypeMapping(Map.class, HashMap.class);

        this.mapper.registerModule(simpleModule);
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @PreAuthorize("hasAnyAuthority('ALL','WebServiceAdmin')")
    public ResponseEntity provisionJobs(@RequestBody String schedulerJobs) {
        try
        {
            SchedulerJobWrapper schedulerJobWrapper = this.mapper.readValue(schedulerJobs
                , SchedulerJobWrapperImpl.class);

            long now = System.currentTimeMillis();
            logger.info(String.format("Provisioning %s jobs for agent %s", schedulerJobWrapper.getJobs().size(), this,moduleName));

            Module<Flow> module = this.moduleService.getModule(moduleName);
            moduleActivator.deactivate(module);

            ConfiguredResource<ConfiguredModuleConfiguration> configuredModule = getConfiguredResource(module);
            ConfiguredModuleConfiguration configuredModuleConfiguration = configuredModule.getConfiguration();

            this.updateInitialModuleConfiguration(schedulerJobWrapper.getJobs(), configuredModuleConfiguration);
            this.configurationService.update(configuredModule);

            moduleActivator.activate(module);

            this.configureComponents(schedulerJobWrapper.getJobs(), module);

            this.updateModuleConfigurationStartupType(schedulerJobWrapper.getJobs(), configuredModuleConfiguration);
            this.configurationService.update(configuredModule);

            this.startJobs(schedulerJobWrapper.getJobs());
            logger.info(String.format("Finished provisioning %s jobs. Time taken %s milliseconds."
                , schedulerJobWrapper.getJobs().size(), System.currentTimeMillis()-now));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new ResponseEntity(
                new ErrorDto("An error has occurred attempting to provision scheduler jobs! Error message ["
                    + e.getMessage() + "]"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Update the initial module configuration in order to define the job types. All jobs are initially set
     * to start manually allowing for the relevant components to be configured.
     *
     * @param jobs
     * @param configuredModuleConfiguration
     */
    private void updateInitialModuleConfiguration(List<SchedulerJob> jobs, ConfiguredModuleConfiguration configuredModuleConfiguration) {
        configuredModuleConfiguration.getFlowDefinitionProfiles().clear();
        configuredModuleConfiguration.getFlowDefinitions().clear();
        jobs.forEach(job -> {
            if(job instanceof FileEventDrivenJob) {
                configuredModuleConfiguration.getFlowDefinitions().put(job.getJobName(), "MANUAL");
                configuredModuleConfiguration.getFlowDefinitionProfiles().put(job.getJobName(), "FILE");
            }
            else if(job instanceof QuartzScheduleDrivenJob) {
                configuredModuleConfiguration.getFlowDefinitions().put(job.getJobName(), "MANUAL");
                configuredModuleConfiguration.getFlowDefinitionProfiles().put(job.getJobName(), "QUARTZ");
            }
            else if(job instanceof InternalEventDrivenJob) {
                configuredModuleConfiguration.getFlowDefinitions().put(job.getJobName(), "MANUAL");
                configuredModuleConfiguration.getFlowDefinitionProfiles().put(job.getJobName(), "SCHEDULER_JOB");
            }
        });
    }

    /**
     * Method to set all job startup types to the type configured on the job.
     *
     * @param jobs
     * @param configuredModuleConfiguration
     */
    private void updateModuleConfigurationStartupType(List<SchedulerJob> jobs, ConfiguredModuleConfiguration configuredModuleConfiguration) {
        jobs.forEach(job -> {
            configuredModuleConfiguration.getFlowDefinitions().put(job.getJobName(), job.getStartupControlType());
        });
    }

    /**
     * Configure all relevant components.
     *
     * @param jobs
     * @param module
     */
    private void configureComponents(List<SchedulerJob> jobs, Module<Flow> module) {
        jobs.forEach(job -> {
            if(job instanceof FileEventDrivenJob) {
                Flow flow = module.getFlow(job.getJobName());
                ConfiguredResource<ScheduledConsumerConfiguration> consumer = (ConfiguredResource<ScheduledConsumerConfiguration>)flow
                    .getFlowElement("File Consumer").getFlowComponent();

                FileConsumerConfiguration configuration = (FileConsumerConfiguration)consumer.getConfiguration();
                this.updateFileConsumerConfiguration((FileEventDrivenJob)job, configuration);

                this.configurationService.update(consumer);
            }
            else if(job instanceof QuartzScheduleDrivenJob) {
                Flow flow = module.getFlow(job.getJobName());
                ConfiguredResource<ScheduledConsumerConfiguration> consumer = (ConfiguredResource<ScheduledConsumerConfiguration>)flow
                    .getFlowElement("Scheduled Consumer").getFlowComponent();

                ScheduledConsumerConfiguration configuration = consumer.getConfiguration();
                this.updateScheduleConsumerConfiguration((QuartzScheduleDrivenJob)job, configuration);

                this.configurationService.update(consumer);
            }
        });
    }

    /**
     * Method to star all jobs if they are configured to be started.
     *
     * @param jobs
     */
    private void startJobs(List<SchedulerJob> jobs) {
        String user = UserUtil.getUser();
        jobs.forEach(job -> {
            if(job.getStartupControlType().equals("AUTOMATIC")) {
                this.moduleService.startFlow(job.getAgentName(), job.getJobName(), user);
            }
        });
    }

    /**
     * Update the scheduled consumer configuration.
     *
     * @param job
     * @param scheduledConsumerConfiguration
     */
    private void updateScheduleConsumerConfiguration(QuartzScheduleDrivenJob job, ScheduledConsumerConfiguration scheduledConsumerConfiguration) {
        scheduledConsumerConfiguration.setJobName(job.getJobName());
        scheduledConsumerConfiguration.setJobGroupName(job.getJobGroup());
        scheduledConsumerConfiguration.setDescription(job.getJobDescription());
        scheduledConsumerConfiguration.setCronExpression(job.getCronExpression());
        scheduledConsumerConfiguration.setTimezone(job.getTimeZone());
        scheduledConsumerConfiguration.setEager(job.isEager());
        scheduledConsumerConfiguration.setIgnoreMisfire(job.isIgnoreMisfire());
        scheduledConsumerConfiguration.setMaxEagerCallbacks(job.getMaxEagerCallbacks());
        scheduledConsumerConfiguration.setPassthroughProperties(job.getPassthroughProperties());
        scheduledConsumerConfiguration.setPersistentRecovery(job.isPersistentRecovery());
        scheduledConsumerConfiguration.setRecoveryTolerance(job.getRecoveryTolerance());
    }

    /**
     * Update the file consumer configuration.
     *
     * @param job
     * @param fileConsumerConfiguration
     */
    private void updateFileConsumerConfiguration(FileEventDrivenJob job, FileConsumerConfiguration fileConsumerConfiguration) {
        fileConsumerConfiguration.setFilenames(job.getFilenames());
        fileConsumerConfiguration.setJobName(job.getJobName());
        fileConsumerConfiguration.setJobGroupName(job.getJobGroup());
        fileConsumerConfiguration.setDescription(job.getJobDescription());
        fileConsumerConfiguration.setCronExpression(job.getCronExpression());
        fileConsumerConfiguration.setTimezone(job.getTimeZone());
        fileConsumerConfiguration.setEager(job.isEager());
        fileConsumerConfiguration.setIgnoreMisfire(job.isIgnoreMisfire());
        fileConsumerConfiguration.setMaxEagerCallbacks(job.getMaxEagerCallbacks());
        fileConsumerConfiguration.setPassthroughProperties(job.getPassthroughProperties());
        fileConsumerConfiguration.setPersistentRecovery(job.isPersistentRecovery());
        fileConsumerConfiguration.setRecoveryTolerance(job.getRecoveryTolerance());
        fileConsumerConfiguration.setDirectoryDepth(job.getDirectoryDepth());
        fileConsumerConfiguration.setEncoding(job.getEncoding());
        fileConsumerConfiguration.setIgnoreFileRenameWhilstScanning(job.isIgnoreFileRenameWhilstScanning());
        fileConsumerConfiguration.setIncludeHeader(job.isIncludeHeader());
        fileConsumerConfiguration.setLogMatchedFilenames(job.isLogMatchedFilenames());
        fileConsumerConfiguration.setIncludeTrailer(job.isIncludeTrailer());
        fileConsumerConfiguration.setSortAscending(job.isSortAscending());
        fileConsumerConfiguration.setSortByModifiedDateTime(job.isSortByModifiedDateTime());
    }

    /**
     * Cast module to configured resource.
     *
     * @param module
     * @return
     */
    protected ConfiguredResource<ConfiguredModuleConfiguration> getConfiguredResource(Module<Flow> module)
    {
        return (ConfiguredResource<ConfiguredModuleConfiguration>)module;
    }
}
