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

package com.couchbase.graph.conn;

import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.cfg.CouchbaseConfig;
import com.couchbase.graph.error.ConnectionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This factory is used to create Couchbase client connections.
 * Yet only one connection is created.
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class ConnectionFactory {
  
    /**
     * The connection factory's logger
     */
    private static final Logger LOG = Logger.getLogger(ConnectionFactory.class.getName());
  
    /**
     * The client singleton
     */
    private static Bucket client;
    
    /**
     * The cluster manager singleton
     */
    private static ClusterManager clusterManager;

    
    /**
     * To get the client and it's connection
     * @return 
     */
    public static Bucket getBucketCon() {
       
        if (client == null) try {
          
            createCon();
        
        } catch (ConnectionException ex) {
            
            LOG.severe(ex.toString());
        }
        
        return client;
    }
    
    /**
     * To get the cluster manager
     * @return 
     */
    public static ClusterManager getClusterCon() {
       
        if (clusterManager == null) try {
          
            createCon();
        
        } catch (ConnectionException ex) {
            
            LOG.severe(ex.toString());
        }
        
        return clusterManager;
    }
    
    
    
    /**
     * To create a new client
     *
     * @return 
     * @throws com.couchbase.graph.error.ConnectionException* @return 
     */
    public static Bucket createCon() throws ConnectionException
    {
       
        CouchbaseConfig cfg = ConfigManager.getCbConfig();
        String[] hosts = cfg.getHosts();
        
        int port = cfg.getPort();
        int timeout = cfg.getTimeout();
        String bucket = cfg.getBucket();
        String password = cfg.getPassword();
        String admin = cfg.getAdminUser();
        String admpwd = cfg.getAdminPassword();
        
        LOG.info("Using the following configuration ...");
        LOG.log(Level.INFO, "hosts = {0}", hosts);
        LOG.log(Level.INFO, "port = {0}", port);
        LOG.log(Level.INFO, "timeout = {0}", timeout);
        LOG.log(Level.INFO, "bucket = {0}", bucket);
        LOG.log(Level.INFO, "password = {0}", password);
        
     
        CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder().kvTimeout(timeout).build();
        CouchbaseCluster cluster = CouchbaseCluster.create(env, hosts);
        
        client = cluster.openBucket(bucket, password);
        clusterManager = cluster.clusterManager(admin, admpwd);
        
        return client;   
    }
    
    
    
    
    
}
