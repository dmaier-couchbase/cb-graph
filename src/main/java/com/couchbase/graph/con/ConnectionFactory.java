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

package com.couchbase.graph.con;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.cfg.CouchbaseConfig;
import com.couchbase.graph.error.ConnectionException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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
    private static CouchbaseClient client;

    
    /**
     * To get the client and it's connection
     * @return 
     */
    public static CouchbaseClient getClient() {
       
        if (client == null) try {
          
            client = createClient();
        
        } catch (ConnectionException ex) {
            
            LOG.severe(ex.toString());
        }
        
        return client;
    }
    
    
    /**
     * To create a new client
     *
     * @return 
     * @throws com.couchbase.graph.error.ConnectionException* @return 
     */
    public static CouchbaseClient createClient() throws ConnectionException
    {
       
        try
        {
            //Read the configuration
            CouchbaseConfig cfg = ConfigManager.getCbConfig();
           
            String[] hosts = cfg.getHosts();
            int port = cfg.getPort();
            int timeout = cfg.getTimeout();
            String bucket = cfg.getBucket();
            String password = cfg.getPassword();
            
            LOG.info("Using the following configuration ...");
            LOG.log(Level.FINE, "hosts = {0}", hosts);
            LOG.log(Level.FINE, "port = {0}", port);
            LOG.log(Level.FINE, "timeout = {0}", timeout);
            LOG.log(Level.FINE, "bucket = {0}", bucket);
            LOG.log(Level.FINE, "password = {0}", password);
            
            List<URI> uris = new ArrayList<>();
        
            for (String host : hosts) {
                URI uri = new URI("http://" + host + ":" + port + "/pools");
                uris.add(uri);
            }

            CouchbaseConnectionFactoryBuilder builder = new CouchbaseConnectionFactoryBuilder();
  
            client = new CouchbaseClient(builder.buildCouchbaseConnection(uris, bucket, password));

            
        }
        catch (URISyntaxException | IOException e)
        {
            throw new ConnectionException(e);
        }
        
        return client;   
    }
    
}
