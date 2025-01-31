/* 
 * $Id: 
 * $URL: 
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

package org.ikasan.spec.error.reporting;

import org.ikasan.spec.search.PagedSearchResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This contract represents a platform level service for the heavyweight logging of
 * Errors
 * 
 * @author Ikasan Development Team
 * 
 */
public interface ErrorReportingService<FAILED_EVENT,ERROR_REPORTING_EVENT>
{
    /** one year default time to live */
    public static final long DEFAULT_TIME_TO_LIVE = new Long(1000l * 60l * 60l * 24l * 365l);

    /**
     * Finds the EVENT logged for error reporting based on the provided uri.
     *
     * @param uri
     * @return EVENT for this uri
     */
    public ERROR_REPORTING_EVENT find(String uri);

    /**
     * Find a map of error reporting event instances from the incoming uris.
     *
     * @param uris
     * @return EVENT
     */
    public Map<String, ERROR_REPORTING_EVENT> find(List<String> uris);
    
    /**
     * Find an error reporting events based on a list of moduleName, flowName and flowElementName
     * as well as a date range.
     * 
     * @param moduleName
     * @param flowName
     * @param flowElementname
     * @param startDate
     * @param endDate
     * @return
     */
    public List<ERROR_REPORTING_EVENT> find(List<String> moduleName, List<String> flowName, List<String> flowElementname,
    		Date startDate, Date endDate, int size);

    /**
     * Find an error reporting events based on a list of moduleName, flowName and flowElementName
     * as well as a date range.
     *  
     * @param moduleName
     * @param flowName
     * @param flowElementname
     * @param startDate
     * @param endDate
     * @param action
     * @param exceptionClass
     * @param size
     * @return
     */
    public List<ERROR_REPORTING_EVENT> find(List<String> moduleName, List<String> flowName, List<String> flowElementname,
    		Date startDate, Date endDate, String action, String exceptionClass, int size);

    /**
     * Perform a paged search for <code>ErrorOccurrence</code>s
     *
     * @param pageNo - The page number to retrieve
     * @param pageSize - The size of the page
     * @param orderBy - order by field
     * @param orderAscending - ascending flag
     * @param moduleName - The module name
     * @param flowName - The name of Flow internal to the Module
     * @param componentName - The component name
     * @param fromDate - The from date
     * @param untilDate - The to date
     *
     * @return PagedSearchResult
     */
    public PagedSearchResult<ERROR_REPORTING_EVENT> find(final int pageNo, final int pageSize, final String orderBy, final boolean orderAscending,
        final String moduleName, final String flowName, final String componentName,  final Date fromDate, final Date untilDate);


    /**
     * Logs an Error where there is an inflight Event involved in a Flow
     * 
     * @param flowElementName
     * @param event
     * @param throwable
     * @return uri for this reported error instance
     */
    public String notify(String flowElementName, FAILED_EVENT event, Throwable throwable);

    /**
     * Logs an Error where there is an inflight Event involved in a Flow and a
     * resolved action has been identified
     *
     * @param flowElementName
     * @param event
     * @param throwable
     * @param action
     * @return uri for this reported error instance
     */
    public String notify(String flowElementName, FAILED_EVENT event, Throwable throwable, String action);

    /**
     * Logs an Error where no inflight Event was present.
     *
     * @param flowElementName
     * @param throwable
     * @return uri for this reported error instance
     */
    public String notify(String flowElementName, Throwable throwable);

    /**
     * Logs an Error where no inflight Event was present.
     *
     * @param flowElementName
     * @param throwable
     * @param action
     * @return uri for this reported error instance
     */
    public String notify(String flowElementName, Throwable throwable, String action);

    /**
     * Allow entities blacklisted to be marked with a timeToLive.
     * On expiry of the timeToLive the entity will no longer be blacklisted.
     *
     * @param timeToLive
     */
    public void setTimeToLive(Long timeToLive);

    /**
     * Housekeep expired exclusionEvents.
     */
    public void housekeep();
    
    /**
     * Helper method to return the row count based on the criteria.
     * 
     * @param moduleName
     * @param flowName
     * @param flowElementname
     * @param startDate
     * @param endDate
     * @return
     */
    public Long rowCount(List<String> moduleName, List<String> flowName, List<String> flowElementname,
			Date startDate, Date endDate);

}
