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
package org.ikasan.spec.hospital.service;

import java.security.Principal;


/**
 * Hospital service interface
 * 
 * @author Ikasan Development Team
 * 
 */
public interface HospitalService<EVENT>
{
	/**
	 * Method to resubmit an event to the appropriate module flow.
	 * 
	 * @param moduleName The name of the module we are re-submitting to.
	 * @param flowName The name of the flow we are re-submitting to.
	 * @param errorUri The error uri of the event being resubmitted.
	 * @param event The event we are resubmitting.
	 * @param principal The principal object of the user we are resubmitting on behalf of.
	 */
	@Deprecated
	void resubmit(String moduleName, String flowName, String errorUri, EVENT event, Principal principal);

    /**
     * Method to resubmit an event to the appropriate module flow.
     *
     * @param moduleName The name of the module we are re-submitting to.
     * @param flowName The name of the flow we are re-submitting to.
     * @param errorUri The error uri of the event being resubmitted.
     * @param principal The the user name we are resubmitting on behalf of.
     */
    void resubmit(String moduleName, String flowName, String errorUri, String userName);


    /**
	 * Method to ignore an excluded event
	 * 
	 * @param moduleName The name of the module we are re-submitting to.
	 * @param flowName The name of the flow we are re-submitting to.
	 * @param errorUri The error uri of the event being resubmitted.
	 * @param event The event we are resubmitting.
	 * @param principal The principal object of the user we are resubmitting on behalf of.
	 */
    @Deprecated
	void ignore(String moduleName, String flowName, String errorUri, EVENT event, Principal principal);

    /**
     * Method to ignore an excluded event
     *
     * @param moduleName The name of the module we are re-submitting to.
     * @param flowName The name of the flow we are re-submitting to.
     * @param errorUri The error uri of the event being resubmitted.
     * @param principal The principal object of the user we are resubmitting on behalf of.
     */
    void ignore(String moduleName, String flowName, String errorUri, String userName);
}
