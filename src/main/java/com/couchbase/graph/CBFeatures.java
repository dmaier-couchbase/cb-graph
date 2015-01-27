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

import com.tinkerpop.blueprints.Features;

/**
 * The list of this those are supported by this implementation
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class CBFeatures extends Features {
    
    public CBFeatures()
    {  
        //General
        this.ignoresSuppliedIds = false; //It is possible to supply id-s
        this.isPersistent = true;
        this.isWrapper = false;
        
        //Edges
        this.supportsEdgeIteration = true; //To get edges is possible
        this.supportsDuplicateEdges = false; //v1 -> name -> v2 twice
        this.supportsEdgeProperties = true;
        this.supportsEdgeRetrieval = true;
        this.supportsSelfLoops = true;
        
        //Vertices
        this.supportsVertexIteration = true;
        this.supportsVertexProperties = true;
        
        //Indexing -- Disabled for now, but basically possible
        this.supportsEdgeIndex = false; //To index on edge properties on demand
        this.supportsEdgeKeyIndex = false;
        this.supportsIndices = false;
        this.supportsKeyIndices = false;
        this.supportsVertexIndex = false;
        this.supportsVertexKeyIndex = false;
        
        //Transactions -- The Graph will be distributed, so Transactions are not the focus
        this.supportsThreadedTransactions = false;
        this.supportsTransactions = false;
        
        //Data types
        this.supportsBooleanProperty = true;
        this.supportsDoubleProperty = true;
        this.supportsFloatProperty = true;
        this.supportsIntegerProperty = true;
        this.supportsLongProperty = true;
        this.supportsMapProperty = true;
        this.supportsMixedListProperty = true;
        this.supportsPrimitiveArrayProperty = true;
        this.supportsSerializableObjectProperty = false;
        this.supportsStringProperty = true;
        this.supportsUniformListProperty = true;
    }
}
