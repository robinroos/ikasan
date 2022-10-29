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
package org.ikasan.ootb.scheduler.agent.module.boot.components;

import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;
import org.ikasan.builder.BuilderFactory;
import org.ikasan.component.endpoint.bigqueue.producer.BigQueueProducer;
import org.ikasan.component.endpoint.bigqueue.serialiser.BigQueueMessagePayloadToStringSerialiser;
import org.ikasan.component.router.multirecipient.RecipientListRouter;
import org.ikasan.flow.visitorPattern.invoker.MultiRecipientRouterInvokerConfiguration;
import org.ikasan.ootb.scheduler.agent.module.component.broker.JobMonitoringBroker;
import org.ikasan.ootb.scheduler.agent.module.component.broker.JobStartingBroker;
import org.ikasan.ootb.scheduler.agent.module.component.broker.configuration.JobMonitoringBrokerConfiguration;
import org.ikasan.ootb.scheduler.agent.module.component.converter.JobInitiationToContextualisedScheduledProcessEventConverter;
import org.ikasan.ootb.scheduler.agent.module.component.endpoint.ScheduledProcessEventToBigQueueMessageSerialiser;
import org.ikasan.ootb.scheduler.agent.rest.cache.InboundJobQueueCache;
import org.ikasan.spec.component.endpoint.Broker;
import org.ikasan.spec.component.endpoint.Consumer;
import org.ikasan.spec.component.endpoint.Producer;
import org.ikasan.spec.component.routing.MultiRecipientRouter;
import org.ikasan.spec.component.transformation.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;

/**
 * Scheduler Agent component factory.
 *
 * @author Ikasan Development Team
 */
@Configuration
public class JobProcessingFlowComponentFactory
{
    @Value( "${module.name}" )
    String moduleName;

    @Value( "${big.queue.consumer.queueDir}" )
    private String queueDir;

    @Value( "${scheduler.agent.log.folder}" )
    String logParentFolder;

    @Value( "${scheduler.agent.log.folder.parenthesis}" )
    String logParentFolderParenthesis;

    @Value( "${job.monitoring.broker.timeout.minutes:240}" )
    long timeout;

    @Resource
    private IBigQueue outboundQueue;

    @Resource
    BuilderFactory builderFactory;

    /**
     * Get the big queue consumer
     *
     * @param jobName
     * @return
     * @throws IOException
     */
    public Consumer bigQueueConsumer(String jobName) throws IOException {
        String queueName = moduleName+"-"+jobName+"-inbound-queue";
        IBigQueue inboundQueue = new BigQueueImpl(queueDir, queueName);

        // Add the inbound queue to the cache.
        InboundJobQueueCache.instance().put(queueName, inboundQueue);

        return builderFactory.getComponentBuilder().bigQueueConsumer()
            .setInboundQueue(inboundQueue)
            .setPutErrorsToBackOfQueue(false)
            .setSerialiser(new BigQueueMessagePayloadToStringSerialiser())
            .build();
    }

    /**
     * Get the converter that converts messages from a JobExecution to a ScheduledProcessEvent.
     *
     * @return the converter
     */
    public Converter getJobInitiationEventConverter() {
        return new JobInitiationToContextualisedScheduledProcessEventConverter
        (moduleName, logParentFolder, logParentFolderParenthesis);
    }


    /**
     * Get the broker that starts the job.
     *
     * @return
     */
    public Broker getJobStartingBroker() {
        return new JobStartingBroker();
    }

    /**
     * Get the broker that actually executes the job.
     *
     * @return
     */
    public Broker getJobMonitoringBroker()
    {
        JobMonitoringBroker jobMonitoringBroker = new JobMonitoringBroker();

        JobMonitoringBrokerConfiguration configuration = new JobMonitoringBrokerConfiguration();
        configuration.setTimeout(timeout);

        jobMonitoringBroker.setConfiguration(configuration);
        jobMonitoringBroker.setConfiguredResourceId(moduleName+"-jobMonitoringBroker");

        return jobMonitoringBroker;
    }

    /**
     * Get the producer that publishes ScheduledProcessEvents.
     *
     * @return
     */
    public Producer getStatusProducer() {
        ScheduledProcessEventToBigQueueMessageSerialiser serialiser = new ScheduledProcessEventToBigQueueMessageSerialiser();
        BigQueueProducer bigQueueProducer = new BigQueueProducer<>(this.outboundQueue);
        bigQueueProducer.setSerialiser(serialiser);
        return bigQueueProducer;
    }

    /**
     * Get the multi recipient router.
     *
     *
     * @return
     */
    public MultiRecipientRouter getJobMRRouter() {
        return new RecipientListRouter(Arrays.asList("dashboard","monitor"));
    }

    /**
     * Get the configuration for the multi recipient router.
     *
     * @return
     */
    public MultiRecipientRouterInvokerConfiguration getMultiRecipientRouterInvokerConfiguration() {
        MultiRecipientRouterInvokerConfiguration configuration = new MultiRecipientRouterInvokerConfiguration();
        configuration.setCloneEventPerRoute(false);
        return configuration;
    }

}

