package org.ikasan.ootb.scheduler.agent.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.leansoft.bigqueue.IBigQueue;
import org.ikasan.ootb.scheduler.agent.rest.cache.InboundJobQueueCache;
import org.ikasan.ootb.scheduler.agent.rest.dto.ContextParameterDto;
import org.ikasan.ootb.scheduler.agent.rest.dto.ContextualisedScheduledProcessEventDto;
import org.ikasan.ootb.scheduler.agent.rest.dto.ErrorDto;
import org.ikasan.ootb.scheduler.agent.rest.dto.SchedulerJobInitiationEventDto;
import org.ikasan.spec.scheduled.context.model.ContextParameter;
import org.ikasan.spec.scheduled.event.model.ScheduledProcessEvent;
import org.ikasan.spec.scheduled.event.model.SchedulerJobInitiationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Module application implementing the REST contract
 */
@RequestMapping("/rest/schedulerJobInitiation")
@RestController
public class SchedulerJobInitiationEventApplication
{
    private static Logger logger = LoggerFactory.getLogger(SchedulerJobInitiationEventApplication.class);

    private ObjectMapper mapper;

    public SchedulerJobInitiationEventApplication() {
        this.mapper = new ObjectMapper();
        final var simpleModule = new SimpleModule()
            .addAbstractTypeMapping(List.class, ArrayList.class)
            .addAbstractTypeMapping(Map.class, HashMap.class)
            .addAbstractTypeMapping(SchedulerJobInitiationEvent.class, SchedulerJobInitiationEventDto.class)
            .addAbstractTypeMapping(ScheduledProcessEvent.class, ContextualisedScheduledProcessEventDto.class)
            .addAbstractTypeMapping(ContextParameter.class, ContextParameterDto.class);

        this.mapper.registerModule(simpleModule);
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @PreAuthorize("hasAnyAuthority('ALL','WebServiceAdmin')")
    public ResponseEntity raiseSchedulerJobInitiationEvent(@RequestBody SchedulerJobInitiationEventDto schedulerJobInitiationEvent)
    {
        try {
            logger.info("Received - {}", schedulerJobInitiationEvent);
            String queueName = schedulerJobInitiationEvent.getAgentName()+"-"+schedulerJobInitiationEvent.getJobName()+"-inbound-queue";
            IBigQueue inboundQueue = InboundJobQueueCache.instance().get(queueName);
            inboundQueue.enqueue(mapper.writeValueAsBytes(schedulerJobInitiationEvent));
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(new ErrorDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

}
