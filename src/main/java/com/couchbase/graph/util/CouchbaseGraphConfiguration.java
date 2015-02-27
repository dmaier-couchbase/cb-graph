package com.couchbase.graph.util;
import com.couchbase.graph.CBGraph;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.Tokens;
import com.tinkerpop.rexster.config.GraphConfiguration;
import com.tinkerpop.rexster.config.GraphConfigurationException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
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
     *    <properties>
     *      <host>localhost</host>
     *      <port>8091</port>
     *      <!-- Username and password elements are optional -->
     *      <username>username</username>
     *      <password>password</password>
     *    </properties>
     *  </graph>
     * </code>
     *
     * Note username and password elements are optional
     * To deploy copy the cb-graph jar (with dependencies) to the Rexster ext directory.   Ensure that the Couchbase
     * is running.
     *
     * @author Simon Leigh (simon.leigh@couchbase.com)
     */
    @Override
    public Graph configureGraphInstance(Configuration configuration) throws GraphConfigurationException {
        final HierarchicalConfiguration graphSectionConfig = (HierarchicalConfiguration) configuration;
        SubnodeConfiguration orientDbSpecificConfiguration;

        try {
            orientDbSpecificConfiguration = graphSectionConfig.configurationAt(Tokens.REXSTER_GRAPH_PROPERTIES);
        } catch (IllegalArgumentException iae) {
            throw new GraphConfigurationException("Check graph configuration. Missing or empty configuration element: " + Tokens.REXSTER_GRAPH_PROPERTIES);
        }

        try {
            return new CBGraph();

        } catch (Exception ex) {
            throw new GraphConfigurationException(ex);
        }
    }

}


