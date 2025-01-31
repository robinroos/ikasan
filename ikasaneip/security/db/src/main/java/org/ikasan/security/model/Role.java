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
package org.ikasan.security.model;

import java.util.*;

/**
 * @author CMI2 Development Team
 *
 */
public class Role implements Comparable<Role>
{
    private Long id;
    private String name = "";
    private String description = "";
    private Set<Policy> policies = new HashSet<>();
    private Set<RoleModule> roleModules = new HashSet<>();
    private Set<RoleJobPlan> roleJobPlans = new HashSet<>();

    /** The data time stamp when an instance was first created */
    private Date createdDateTime;

    /** The data time stamp when an instance was last updated */
    private Date updatedDateTime;

    /**
     * Default constructor
     */
    public Role()
    {
        long now = System.currentTimeMillis();
        this.createdDateTime = new Date(now);
        this.updatedDateTime = new Date(now);
    }

	/**
	 *  extra constructor
	 */
	public Role(String name, String description) {
		this.description = description;
		this.name = name;
		long now = System.currentTimeMillis();
		this.createdDateTime = new Date(now);
		this.updatedDateTime = new Date(now);
	}

	public void addPolicy(Policy policy){

		if(policies!=null)
		{
			if(!policies.contains(policy))
			{
				policies.add(policy);
			}
		}
		else
		{
			policies = new HashSet<>();
			policies.add(policy);
		}
	}

    public void addRoleModule(RoleModule roleModule){

        if(this.roleModules!=null)
        {
            if(!this.roleModules.contains(roleModule))
            {
                this.roleModules.add(roleModule);
            }
        }
        else
        {
            this.roleModules = new HashSet<>();
            this.roleModules.add(roleModule);
        }
    }

    public void addRoleJobPlan(RoleJobPlan roleJobPlan){

        if(this.roleJobPlans!=null)
        {
            if(!this.roleJobPlans.contains(roleJobPlan))
            {
                this.roleJobPlans.add(roleJobPlan);
            }
        }
        else
        {
            this.roleJobPlans = new HashSet<>();
            this.roleJobPlans.add(roleJobPlan);
        }
    }

	/**
     * @return the id
     */
    public Long getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the createdDateTime
     */
    public Date getCreatedDateTime()
    {
        return createdDateTime;
    }

    /**
     * @param createdDateTime the createdDateTime to set
     */
    public void setCreatedDateTime(Date createdDateTime)
    {
        this.createdDateTime = createdDateTime;
    }

    /**
     * @return the updatedDateTime
     */
    public Date getUpdatedDateTime()
    {
        return updatedDateTime;
    }

    /**
     * @param updatedDateTime the updatedDateTime to set
     */
    public void setUpdatedDateTime(Date updatedDateTime)
    {
        this.updatedDateTime = updatedDateTime;
    }

    /**
     * @return the policies
     */
    public Set<Policy> getPolicies()
    {
        return policies;
    }

    /**
     * @param policies the policies to set
     */
    public void setPolicies(Set<Policy> policies)
    {
        this.policies = policies;
    }

    public Set<RoleModule> getRoleModules() {
        return roleModules;
    }

    public void setRoleModules(Set<RoleModule> roleModules) {
        this.roleModules = roleModules;
    }

    public Set<RoleJobPlan> getRoleJobPlans() {
        return roleJobPlans;
    }

    public void setRoleJobPlans(Set<RoleJobPlan> roleJobPlans) {
        this.roleJobPlans = roleJobPlans;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Role other = (Role) obj;
        if (name == null)
        {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }


    /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */

	@Override
	public String toString()
	{
		return "Role [id=" + id + ", name=" + name + ", description="
				+ description + ", createdDateTime="
				+ createdDateTime + ", updatedDateTime=" + updatedDateTime
				+ "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Role role)
	{		
		if(role.hashCode() == this.hashCode())
		{
			return 0;
		}
		else
		{
			return role.hashCode() - this.hashCode();
		}
	}
}
