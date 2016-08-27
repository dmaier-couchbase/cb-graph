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

package com.couchbase.graph;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.conn.ConnectionFactory;
import com.couchbase.graph.error.DocNotFoundException;
import com.couchbase.graph.helper.JSONHelper;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Both vertices and edges are elemnets
 *  
 * @author David Maier <david.maier at couchbase.com>
 */
public class CBElement implements Element {

    /**
     * The Logger
     */
    private static final Logger LOG = Logger.getLogger(CBElement.class.getName());
    
    /**
     * The graph to which the element belongs
     */
    protected Graph graph;
    
    /**
     * We want to have interactive access to the Vertex, so we need to read the
     * data from the database without caching it in this class
     */
    protected static final Bucket client = ConnectionFactory.getBucketCon();

    /**
     * The id, so the name of the element
     */
    protected final Object id;
    
    /**
     * The key is the key of the object in the database, it is a combination of the type prefix and the id
     */
    protected String cbKey;
    
    /**
     * Flag to decide if the object should be fetched again for every operation
     */
    protected boolean refreshEnabled;
   
    /**
     * The JSON object which is associated to this element
     */
    protected JsonObject innerObj;
    
    
    /**
     * The properties of the inner JSONObject
     */
    protected JsonObject innerProps;
    
    
    /**
     * The constructor which takes the id as argument
     * 
     * @param id 
     * @param graph 
     */
    public CBElement(Object id, Graph graph) {
   
        this.graph = graph;
        this.refreshEnabled = ConfigManager.getGraphConfig().getRefreshMode();
        this.id = id;

        //Needs to be set by the implementation
        this.cbKey = null;
    }
    
    /**
     * The full constructor
     * 
     * @param id
     * @param props
     * @param graph 
     */
    public CBElement(Object id, JsonObject props, Graph graph) {
        
        this(id, graph);
        this.innerObj = JsonObject.empty();
        
        if (props != null) {
            
            innerProps = props;
            innerObj.put(CBModel.PROP_PROPS, innerProps);
        }
    }
    
    /**
     * To refresh the inner object instance, what happens during
     * a refresh depends on the refresh mode.
     * 
     * (1)
     * If the refresh mode is set to true, this means that an automatic
     * object refresh happens before any changes are applied. So before E.G an edge
     * is added to a vertex, the vertex is gathered again from the database to minimize
     * the risk that the already perfomed changes of another users are overridden.
     * 
     * (2)
     * If it is set to false, we will basically work with the initially gathered data.
     * So you would not even realize that another user did change the vertex already
     * 
     * (3)
     * Another way would be to combine them by supporting a kind of transaction, by using Couchbase's CAS mechanism. 
     * 
     *  1. Get a an element (vertex or edge) at t_0 and also store the CAS value of it
     *  2. Change the element multiple times on the client side (in memory)
     *  3. Commit the changes by submitting them as a kind of atomic operation to
     *     the database
     *  4a. If another user did change the same vertex or edge at t_1 then the CAS value changed. Then perform a refresh and do the change again
     *  4b. ... Then simply interupt the update by throwing an execption.
     *  
     * @return 
     * @throws com.couchbase.graph.error.DocNotFoundException 
     */
    public boolean refresh() throws DocNotFoundException
    {   
        try
        {
            if (innerObj == null || refreshEnabled)
            {
                innerObj = client.get(cbKey).content();
                innerProps = innerObj.getObject(CBModel.PROP_PROPS);
            
                return true;
            }
        }
        catch (Exception e)
        {
            throw new DocNotFoundException(cbKey, e);
        }
        
        return false;
    }
    
    /**
     * Get a property
     * 
     * @param <T>
     * @param key
     * @return 
     */
    @Override
    public <T> T getProperty(String key) {
 
        try 
        {
            refresh();
            Object prop = innerProps.get(key);

            if (prop != null)
            {
                if (prop instanceof JsonArray) {
                    final Object object = ((JsonArray) prop).toList();
                    return (T) object;
                }
                return (T) prop;
            }

        }
        catch (DocNotFoundException e)
        {
            LOG.severe(e.toString());
        }

        return null;
    }

    /**
     * Get the property keys
     * 
     * @return 
     */
    @Override
    public Set<String> getPropertyKeys() {
        
        Set<String> result = new HashSet<>();
               
        try {

            refresh();
            result = innerProps.getNames();
        
        } catch (DocNotFoundException e) {
            
            LOG.severe(e.toString());
        }
        
        return result;

    }

    /**
     * Set a property
     * 
     * @param key
     * @param value 
     */
    @Override
    public void setProperty(String key, Object value) {
       
        try {
            refresh();

            if (value instanceof List) {
                innerProps.put(key, JsonArray.from((List)value));
            } else {
                innerProps.put(key, value);
            }

            client.replace(JsonDocument.create(cbKey, innerObj));
           
        } catch (DocNotFoundException e) {
            
            LOG.severe(e.toString());
        }
    }
        
    /**
     * Remove a property
     * 
     * @param <T>
     * @param key
     * @return 
     */
    @Override
    public <T> T removeProperty(String key) {

        try {
            
            refresh();
            Object toRemove = innerProps.get(key);
            JsonObject removed = innerProps.removeKey(key);
            
            if (removed != null)
            {
                client.replace(JsonDocument.create(cbKey, innerObj));
                return (T) toRemove;
            }
            
            
        } catch (DocNotFoundException e) {
            
            LOG.severe(e.toString());
        }
     
       return null;
    }

    /**
     * Remove the element
     * 
     */
    @Override
    public void remove() {
        
        client.remove(cbKey);
    }

    /**
     * Get the id of the element
     * 
     * @return 
     */
    @Override
    public Object getId() {
       
        return this.id;
    }

    /**
     * Get the Couchbase Key of this element
     * 
     * @return 
     */
    public String getCbKey() {
        return cbKey;
    }

    /**
     * Returns the JSON String of the element.
     * 
     * This is mainly used for testing (easier assertEquals) purposes,
     * and so the order of the properties is important here.
     * 
     * @return 
     */
    @Override
    public String toString() {
                
        
        return JSONHelper.sort(innerObj).toString();

    }
    
}
