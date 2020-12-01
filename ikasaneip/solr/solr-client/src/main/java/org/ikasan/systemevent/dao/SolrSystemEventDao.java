package org.ikasan.systemevent.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.common.SolrInputDocument;
import org.ikasan.spec.solr.SolrDaoBase;
import org.ikasan.spec.systemevent.SystemEvent;
import org.ikasan.systemevent.model.SolrSystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrSystemEventDao extends SolrDaoBase<SystemEvent>
{
    /**
     * Logger for this class
     */
    private static Logger logger = LoggerFactory.getLogger(SolrSystemEventDao.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * We need to give this dao it's context.
     */
    public static final String SYSTEM_EVENT = "systemEvent";

    protected SolrInputDocument convertEntityToSolrInputDocument(Long expiry, SystemEvent systemEvent)
    {
        SolrInputDocument document = new SolrInputDocument();
        document.addField(TYPE, SYSTEM_EVENT);
        try {
            document.addField(PAYLOAD_CONTENT, getSystemEventContent(systemEvent));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Cannot convert system event to string! [%s]", systemEvent));
        }

        if(systemEvent.getModuleName() != null){
            document.addField(ID, systemEvent.getModuleName()
                + "-" + SYSTEM_EVENT + "-" + systemEvent.getId());
            document.addField(MODULE_NAME, systemEvent.getModuleName());
        }
        else {
            document.addField(ID, SYSTEM_EVENT + "-" + systemEvent.getSubject() + "-" + systemEvent.getId());
        }
        document.addField(CREATED_DATE_TIME, systemEvent.getTimestamp().getTime());
        document.setField(EXPIRY, expiry);
        return document;
    }

    private String getSystemEventContent(SystemEvent systemEvent) throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(systemEvent);
    }
}
