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
package org.ikasan.wiretap.model;

import org.ikasan.harvest.HarvestEvent;
import org.ikasan.spec.wiretap.WiretapEvent;

import java.io.Serializable;
import java.util.Objects;

/**
 * Implementation of a flowEvent based on payload being of any generic type.
 * 
 * @author Ikasan Development Team
 *
 */
public class WiretapFlowEvent extends GenericWiretapEvent implements WiretapEvent<String>, Serializable, HarvestEvent
{
    /** related event id */
    private String relatedEventId;

    /** has the record been harvested */
    private boolean harvested;

    /** the time the record was harvested */
    private long harvestedDateTime;


    public WiretapFlowEvent()
    {
    }

    public WiretapFlowEvent(final String moduleName, final String flowName, final String componentName,
            final String eventId, final String relatedEventId, final long eventTimestamp, final String event, final Long expiry)
    {
        super(moduleName, flowName, componentName, event, expiry);
        super.eventId = eventId;
        this.relatedEventId = relatedEventId;
        super.timestamp = eventTimestamp;
    }

    public String getEventId()
    {
        return eventId;
    }

    protected void setEventId(String eventId)
    {
        this.eventId = eventId;
    }

    public String getRelatedEventId()
    {
        return relatedEventId;
    }

    protected void setRelatedEventId(String relatedEventId)
    {
        this.relatedEventId = relatedEventId;
    }

    public boolean isHarvested()
    {
        return harvested;
    }

    public void setHarvested(boolean harvested)
    {
        this.harvested = harvested;
    }

    public long getHarvestedDateTime()
    {
        return harvestedDateTime;
    }

    public void setHarvestedDateTime(long harvestedDateTime)
    {
        this.harvestedDateTime = harvestedDateTime;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer("WiretapFlowEvent{");
        sb.append(super.toString());
        sb.append("relatedEventId='").append(relatedEventId).append('\'');
        sb.append(", eventTimestamp=").append(super.timestamp);
        sb.append(", harvested=").append(harvested);
        sb.append(", harvestedDateTime=").append(harvestedDateTime);
        sb.append('}');
        return sb.toString();
    }
}