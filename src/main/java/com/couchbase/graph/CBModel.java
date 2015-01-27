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

/**
 *  The structure of the propery graph model as constants
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public interface CBModel {
    
    //Keys
    public static final String VERTEX_PREFIX = "v_";
    public static final String EDGE_PREFIX = "e_";
    public static final String VERTEX_KEY = VERTEX_PREFIX + "{1}";
    public static final String EDGE_DELIM = "->";
    public static final String EDGE_LABEL_DELIM = "|";
    public static final String EDGE_ID = "{1}" + EDGE_DELIM + EDGE_LABEL_DELIM + "{2}" + EDGE_LABEL_DELIM + EDGE_DELIM + "{3}";
    public static final String EDGE_KEY = EDGE_PREFIX + EDGE_ID;
    
    //Properties
    public static final String PROP_TYPE = "type";
    public static final String PROP_PROPS = "props";
    public static final String PROP_LABEL = "label";
    public static final String PROP_VALUE = "value";
    public static final String PROP_EDGES = "edges";
    public static final String PROP_EDGES_OUT = "out";
    public static final String PROP_EDGES_IN = "in";
    public static final String PROP_FROM = "from";
    public static final String PROP_TO = "to";
    
    //Counters
    public static final String VERTEX_COUNTER_KEY = "vertex_counter";
    
    //Values
    public static final String VAL_TYPE_EDGE = "edge";
    public static final String VAL_TYPE_VERTEX = "vertex";
    
}
