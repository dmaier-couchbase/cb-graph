package com.couchbase.graph.util;
import com.couchbase.graph.CBGraph;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.config.GraphConfiguration;
import com.tinkerpop.rexster.config.GraphConfigurationContext;
import com.tinkerpop.rexster.config.GraphConfigurationException;
/**
 * Created by simon on 27/02/15.
 */
public class CouchbaseGraphConfiguration implements GraphConfiguration {

    /**
     * Rexster configuration for CouchBaseGraph.  Accepts configuration in rexster.xml as follows:
     *
     * <code>
     *  <graph>
     *    <graph-name>couchbaseexample</graph-name>
     *    <graph-type>com.couchbase.graph.util.CouchbaseGraphConfiguration</graph-type>
     *  </graph>
     * </code>
     *
     * TODO: Optionally pass an external config as part the GraphConfigContext
     *
     * @author Simon Leigh (simon.leigh@couchbase.com)
     */

    @Override
    public Graph configureGraphInstance(GraphConfigurationContext gcc) throws GraphConfigurationException {
       
        return new CBGraph();
    }

}


