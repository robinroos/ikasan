/* 
 * $Id$
 * $URL$
 *
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 * Copyright (c) 2003-2008 Mizuho International plc. and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the 
 * Free Software Foundation Europe e.V. Talstrasse 110, 40217 Dusseldorf, Germany 
 * or see the FSF site: http://www.fsfeurope.org/.
 * ====================================================================
 */
package org.ikasan.console.pointtopointflow.service;

import java.util.Set;

import org.ikasan.console.pointtopointflow.PointToPointFlowProfile;

/**
 * Service layer for PointToPointFlowProfiles
 * 
 * @author Ikasan Development Team
 */
public interface PointToPointFlowProfileService
{

    /**
     * Get a list of PointToPointFlowProfiles
     * 
     * @return List of all of the PointToPointFlowProfiles
     */
    public Set<PointToPointFlowProfile> getAllPointToPointFlowProfiles();

    /**
     * Get a set of all of the names for all of the PointToPointFlowProfiles
     * 
     * @return Set of all of the PointToPointFlowProfile names
     */
    public Set<String> getAllPointToPointFlowProfileNames();
    
    /**
     * Get a set of all of the module names for all of the PointToPointFlowProfile Names
     * 
     * @return Set of all of the module names
     */
    public Set<String> getAllModuleNames();
    
    /**
     * Get a set of all of the module names for the given PointToPointFlowProfile Names
     * 
     * @param pointToPointFlowProfileNames - The point to point profile names to search on 
     * @return Set of all of the module names
     */
    public Set<String> getModuleNames(Set<String> pointToPointFlowProfileNames);
    
}
