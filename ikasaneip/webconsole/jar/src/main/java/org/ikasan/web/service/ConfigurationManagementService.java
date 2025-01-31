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
package org.ikasan.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.ikasan.spec.configuration.Configuration;
import org.ikasan.spec.configuration.ConfigurationManagement;
import org.ikasan.spec.configuration.ConfiguredResource;
import org.ikasan.spec.flow.Flow;
import org.ikasan.spec.flow.FlowElement;
import org.ikasan.spec.module.Module;
import org.ikasan.spec.module.ModuleService;
import org.ikasan.spec.systemevent.SystemEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.webflow.execution.RequestContext;

/**
 * Configuration Management Service for maintenance of runtime component configurations.
 * 
 * @author Ikasan Development Team
 *
 */
public class ConfigurationManagementService
{
    /**
     * Logger
     */
    private final static Logger logger = LoggerFactory.getLogger(ConfigurationManagementService.class);

    /** configuration service */
    private ConfigurationManagement<ConfiguredResource,Configuration> configurationManagement;
    
    /** system event service records all changes to configurations */
    private SystemEventService systemEventService;
    
    /** module service */
    private ModuleService moduleService;

    private ObjectMapper mapper;

    /**
     * Constructor
     * @param configurationManagement
     * @param systemEventService
     * @param moduleService
     */
    public ConfigurationManagementService(ConfigurationManagement<ConfiguredResource,Configuration> configurationManagement, SystemEventService systemEventService, 
            ModuleService moduleService)
    {
        this.configurationManagement = configurationManagement;
        if(configurationManagement == null)
        {
            throw new IllegalArgumentException("configurationManagement cannot be 'null'");
        }

        this.systemEventService = systemEventService;
        if(systemEventService == null)
        {
            throw new IllegalArgumentException("systemEventService cannot be 'null'");
        }

        this.moduleService = moduleService;
        if(moduleService == null)
        {
            throw new IllegalArgumentException("moduleService cannot be 'null'");
        }

        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule m = new SimpleModule();
        this.mapper.registerModule(m);

    }

    /**
     * Is this moduleName/flowName/flowElementName referencing a component that
     * is marked as a ConfiguredResource.
     * @param moduleName
     * @return boolean
     */
    public boolean isConfiguredResource(String moduleName)
    {
        Module<Flow> module = moduleService.getModule(moduleName);
        return ( module instanceof ConfiguredResource );
    }

    /**
     * Is this moduleName/flowName/flowElementName referencing a component that
     * is marked as a ConfiguredResource.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @return boolean
     */
    public boolean isConfiguredResource(String moduleName, String flowName, String flowElementName)
    {
        FlowElement flowElement = getFlowElement(moduleName, flowName, flowElementName);
        return ( flowElement.getFlowComponent() instanceof ConfiguredResource );
    }

    /**
     * Is this moduleName/flowName/flowElementInvoker referencing an invoker that
     * is marked as a ConfiguredResource.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @return boolean
     */
    public boolean isInvokerConfiguredResource(String moduleName, String flowName, String flowElementName)
    {
        FlowElement flowElement = getFlowElement(moduleName, flowName, flowElementName);
        return ( flowElement.getFlowElementInvoker() instanceof ConfiguredResource );
    }
    
    /**
     * Find the configuration instance for this moduleName/flowName/flowElementName.
     * Report any issues back via the RequestContext.
     * @param moduleName
     * @param context
     * @return Configuration
     */
    public Configuration findConfiguration(String moduleName, RequestContext context)
    {
        try
        {
            ConfiguredResource configuredResource = getModuleConfiguredResource(moduleName);
            return this.configurationManagement.getConfiguration(configuredResource);
        }
        catch(RuntimeException e)
        {
            context.getMessageContext().addMessage(new MessageBuilder().error().source("findConfiguration").defaultText(
                    e.getMessage()).build());
        }
        
        return null;
    }

    /**
     * Find the configuration instance for this moduleName/flowName/flowElementName.
     * Report any issues back via the RequestContext.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @param context
     * @return Configuration
     */
    public Configuration findConfiguration(String moduleName, String flowName, String flowElementName, RequestContext context)
    {
        try
        {
            ConfiguredResource configuredResource = getComponentConfiguredResource(moduleName, flowName, flowElementName);
            return this.configurationManagement.getConfiguration(configuredResource);
        }
        catch(RuntimeException e)
        {
            context.getMessageContext().addMessage(new MessageBuilder().error().source("findConfiguration").defaultText(
                e.getMessage()).build());
        }

        return null;
    }

    /**
     * Find the configuration instance for this moduleName/flowName/flowElementName.
     * Report any issues back via the RequestContext.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @param context
     * @return Configuration
     */
    public Configuration findInvokerConfiguration(String moduleName, String flowName, String flowElementName, RequestContext context)
    {
        try
        {
            ConfiguredResource configuredResource = getInvokerConfiguredResource(moduleName, flowName, flowElementName);
            return this.configurationManagement.getConfiguration(configuredResource);
        }
        catch(RuntimeException e)
        {
            context.getMessageContext().addMessage(new MessageBuilder().error().source("findConfiguration").defaultText(
                    e.getMessage()).build());
        }

        return null;
    }

    /**
     * Create a new configuration instance for this moduleName/flowName/flowElementName.
     * Report any issues back via the RequestContext.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @param context
     * @return Configuration
     */
    public Configuration createConfiguration(String moduleName, String flowName, String flowElementName, RequestContext context)
    {
        try
        {
            ConfiguredResource configuredResource = getComponentConfiguredResource(moduleName, flowName, flowElementName);
            return configurationManagement.createConfiguration(configuredResource);
        }
        catch(RuntimeException e)
        {
            context.getMessageContext().addMessage(new MessageBuilder().error().source("createConfiguration").defaultText(
                    e.getMessage()).build());
        }
        
        return null;
    }

    /**
     * Create a new configuration instance for this moduleName/flowName/flowElementName.
     * Report any issues back via the RequestContext.
     * @param moduleName
     * @param context
     * @return Configuration
     */
    public Configuration createConfiguration(String moduleName, RequestContext context)
    {
        try
        {
            ConfiguredResource configuredResource = getModuleConfiguredResource(moduleName);
            return configurationManagement.createConfiguration(configuredResource);
        }
        catch(RuntimeException e)
        {
            context.getMessageContext().addMessage(new MessageBuilder().error().source("createConfiguration").defaultText(
                e.getMessage()).build());
        }

        return null;
    }

    /**
     * Find the invoker configuration instance for this moduleName/flowName/flowElementName.
     * Report any issues back via the RequestContext.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @param context
     * @return Configuration
     */
    public Configuration createInvokerConfiguration(String moduleName, String flowName, String flowElementName, RequestContext context)
    {
        try
        {
            ConfiguredResource configuredResource = getInvokerConfiguredResource(moduleName, flowName, flowElementName);
            return this.configurationManagement.createConfiguration(configuredResource);
        }
        catch(RuntimeException e)
        {
            context.getMessageContext().addMessage(new MessageBuilder().error().source("createConfiguration").defaultText(
                    e.getMessage()).build());
        }

        return null;
    }

    /**
     * Insert a new Configuration instance.
     * @param configuration
     */
    public void insertConfiguration(Configuration configuration)
    {

        try
        {
            String newConfigJson = mapper.writeValueAsString(configuration);
            this.systemEventService.logSystemEvent(
                configuration.getConfigurationId(),
                String.format("Configuration created, NewConfig [%s]", newConfigJson),
                getAuthentication().getName());

        }
        catch (JsonProcessingException e)
        {
            logger.warn("Issue converting configuration to json.", e);
        }

        this.configurationManagement.saveConfiguration(configuration);
    }
    
    /**
     * Update an existing Configuration instance.
     * @param configuration
     */
    public void updateConfiguration(Configuration configuration)
    {

        Configuration oldConfig = this.configurationManagement.getConfiguration(configuration.getConfigurationId());

        try
        {
            String oldConfigJson = mapper.writeValueAsString(oldConfig);
            String newConfigJson = mapper.writeValueAsString(configuration);

            this.systemEventService.logSystemEvent(
                configuration.getConfigurationId(),
                String.format("Configuration Updated OldConfig [%s] NewConfig [%s]", oldConfigJson, newConfigJson),
                getAuthentication().getName());

        }
        catch (JsonProcessingException e)
        {
            logger.warn("Issue converting configuration to json.", e);
        }

        this.configurationManagement.saveConfiguration(configuration);
    }
    
    /**
     * Delete an existing Configuration instance.
     * @param configuration
     */
    public void deleteConfiguration(Configuration configuration)
    {

        try
        {
            String newConfigJson = mapper.writeValueAsString(configuration);
            this.systemEventService.logSystemEvent(
                configuration.getConfigurationId(),
                String.format("Configuration deleted, OldConfig [%s]", newConfigJson),
                getAuthentication().getName());

        }
        catch (JsonProcessingException e)
        {
            logger.warn("Issue converting configuration to json.", e);
        }


        this.configurationManagement.deleteConfiguration(configuration);
    }

    /**
     * Utility method for getting the authentication principal of the invoking user.
     * @return Authentication
     */
    protected Authentication getAuthentication()
    {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Utility method for locating and returning the ConfiguredResource instance based on the given
     * moduleName/flowName/flowElementName.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @return ConfiguredResource
     */
    private ConfiguredResource getComponentConfiguredResource(String moduleName, String flowName, String flowElementName)
    {
        FlowElement flowElement = getFlowElement(moduleName, flowName, flowElementName);
        if(flowElement.getFlowComponent() instanceof ConfiguredResource)
        {
            return (ConfiguredResource)flowElement.getFlowComponent();
        }
        else
        {
            throw new UnsupportedOperationException("Component must be of type 'ConfiguredResource' to support component configuration");
        }
    }

    /**
     * Utility method for locating and returning the ConfiguredResource instance based on the given
     * moduleName/flowName/flowElementName.
     * @param moduleName
     * @return ConfiguredResource
     */
    private ConfiguredResource getModuleConfiguredResource(String moduleName)
    {
        Module<Flow> module = moduleService.getModule(moduleName);
        if(module instanceof ConfiguredResource)
        {
            return (ConfiguredResource)module;
        }
        else
        {
            throw new UnsupportedOperationException("Module must be of type 'ConfiguredResource' to support module configuration");
        }
    }

    /**
     * Utility method for locating and returning the ConfiguredResource instance based on the given
     * moduleName/flowName/flowElementInvoker.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @return ConfiguredResource
     */
    private ConfiguredResource getInvokerConfiguredResource(String moduleName, String flowName, String flowElementName)
    {
        FlowElement flowElement = getFlowElement(moduleName, flowName, flowElementName);
        if(flowElement.getFlowElementInvoker() instanceof ConfiguredResource)
        {
            return (ConfiguredResource)flowElement.getFlowElementInvoker();
        }
        else
        {
            throw new UnsupportedOperationException("Invoker must be of type 'ConfiguredResource' to support configuration");
        }
    }

    /**
     * Utility method for locating and returning the FlowComponent for the given
     * moduleName/flowName/flowElementName.
     * @param moduleName
     * @param flowName
     * @param flowElementName
     * @return FlowElement
     */
    private FlowElement getFlowElement(String moduleName, String flowName, String flowElementName)
    {
        Module<Flow> module = moduleService.getModule(moduleName);
        Flow flow = module.getFlow(flowName);
        FlowElement flowElement = flow.getFlowElement(flowElementName);
        if(flowElement == null)
        {
            throw new RuntimeException("FlowComponent not found for module ["
                    + moduleName + "] flow [" + flowName + "] flowElementName [" + flowElementName + "]");
        }

        return flowElement;
    }
}
