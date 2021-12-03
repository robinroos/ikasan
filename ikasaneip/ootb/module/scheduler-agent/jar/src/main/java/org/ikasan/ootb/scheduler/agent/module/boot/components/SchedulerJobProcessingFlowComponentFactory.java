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
import org.ikasan.component.endpoint.bigqueue.consumer.BigQueueConsumer;
import org.ikasan.component.endpoint.bigqueue.producer.BigQueueProducer;
import org.ikasan.component.endpoint.bigqueue.serialiser.SimpleStringSerialiser;
import org.ikasan.ootb.scheduler.agent.module.component.broker.ProcessExecutionBroker;
import org.ikasan.ootb.scheduler.agent.module.component.broker.configuration.ProcessExecutionBrokerConfiguration;
import org.ikasan.ootb.scheduler.agent.module.component.converter.JobExecutionConverter;
import org.ikasan.ootb.scheduler.agent.module.component.converter.JobInitiationToScheduledProcessEventConverter;
import org.ikasan.ootb.scheduler.agent.module.component.endpoint.ScheduledProcessEventProducer;
import org.ikasan.ootb.scheduler.agent.module.component.endpoint.SchedulerProcessorEventSerialiser;
import org.ikasan.ootb.scheduler.agent.module.component.filter.ScheduledProcessEventFilter;
import org.ikasan.ootb.scheduler.agent.module.component.router.BlackoutRouter;
import org.ikasan.ootb.scheduler.agent.rest.cache.*;
import org.ikasan.spec.component.endpoint.Broker;
import org.ikasan.spec.component.endpoint.Consumer;
import org.ikasan.spec.component.endpoint.Producer;
import org.ikasan.spec.component.filter.Filter;
import org.ikasan.spec.component.routing.SingleRecipientRouter;
import org.ikasan.spec.component.transformation.Converter;
import org.ikasan.spec.scheduled.ScheduledProcessEvent;
import org.ikasan.spec.scheduled.ScheduledProcessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Scheduler Agent component factory.
 *
 * @author Ikasan Development Team
 */
@Configuration
public class SchedulerJobProcessingFlowComponentFactory
{
    @Value( "${module.name}" )
    String moduleName;

    @Value( "${big.queue.consumer.configuration.queueDir}" )
    private String queueDir;

    @Resource
    private IBigQueue outboundQueue;

    @Resource
    ScheduledProcessService scheduledProcessService;

    public Consumer bigQueueConsumer(String jobName) throws IOException {
        IBigQueue inboundQueue = new BigQueueImpl(queueDir, jobName+"-inbound-queue");

        // Add the inbound queue to the cache.
        InboundJobQueueCache.instance().put(jobName, inboundQueue);


        BigQueueConsumer consumer = new BigQueueConsumer(inboundQueue, new SimpleStringSerialiser());
        return consumer;
    }

    /**
     * Get the converter that converts messages from a JobExecution to a ScheduledProcessEvent.
     *
     * @return the converter
     */
    public Converter getJobInitiationEventConverter() { return new JobInitiationToScheduledProcessEventConverter(moduleName); }

    /**
     * Get the router responsible for determining if a job has been run in a blackout window.
     *
     * @return
     */
    public SingleRecipientRouter getBlackoutRouter()
    {
        return new BlackoutRouter();
    }

    /**
     * Get the broker that actually executes the job.
     *
     * @return
     */
    public Broker getProcessExecutionBroker()
    {
        ProcessExecutionBrokerConfiguration configuration = new ProcessExecutionBrokerConfiguration();
        configuration.setCommandLine("pwd");    // default safe command across all platforms

        ProcessExecutionBroker processExecutionBroker = new ProcessExecutionBroker();
        processExecutionBroker.setConfiguration(configuration);
        return processExecutionBroker;
    }

    /**
     * Get the filter that drops ScheduledProcessEvents that should not be published back to the dashboard.
     *
     * @return
     */
    public Filter getScheduledStatusFilter()
    {
        return new ScheduledProcessEventFilter();
    }

    /**
     * Get the producer that publishes ScheduledProcessEvents.
     *
     * @return
     */
    public Producer getScheduledStatusProducer() {
        return new BigQueueProducer<ScheduledProcessEvent>(this.outboundQueue, new SchedulerProcessorEventSerialiser());
    }

}

