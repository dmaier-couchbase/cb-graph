/*
 * Copyright 2014 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.graph.cfg;

import com.couchbase.graph.error.ResourceReadException;

/**
 * To access the application's configuration
 *
 * @author David Maier <david.maier at couchbase.com>
 */
public class CouchbaseConfig extends BaseConfig {
    
    
    public CouchbaseConfig() throws ResourceReadException {
        super("couchbase.properties");
    }

    public String[] getHosts() {
        
        String propsStr = this.props.getProperty("cb.con.hosts");
        return propsStr.split(",");
    }

    public int getPort() {
        String portStr = this.props.getProperty("cb.con.port");
        return Integer.parseInt(portStr);
    }

    public String getBucket() {
        return this.props.getProperty("cb.con.bucket.name");
    }

    public String getPassword() {
        return this.props.getProperty("cb.con.bucket.pwd");
    }
    
    public int getTimeout() {
        String timeoutStr = this.props.getProperty("cb.timeout.op");
        return Integer.parseInt(timeoutStr);
    }
    
    public String getAdminUser()
    {
        return this.props.getProperty("cb.admin.user");
    }
    
    public String getAdminPassword()
    {
        return this.props.getProperty("cb.admin.pwd");
    }
    
    public String getDesignDoc()
    {
        return this.props.getProperty("cb.view.designdoc");
    }
     
    public String getAllEdgesView()
    {
        return this.props.getProperty("cb.view.alledges");
    }
    
    public String getAllVerticesView()
    {
        return this.props.getProperty("cb.view.allvertices");
    }
    
    public String getAllEdgeLablesView()
    {
        return this.props.getProperty("cb.view.alledgelabels");
    }
    
    public String getAllVertexPropsView()
    {
        return this.props.getProperty("cb.view.allvertexprops");
    }
}
