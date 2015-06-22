/*
 * Copyright 2015 Couchbase, Inc.
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

package com.couchbase.graph.tinkerpop;


import com.tinkerpop.blueprints.Graph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class GraphTest extends BaseTest {

    public abstract Graph generateGraph();

    public abstract Graph generateGraph(final String graphDirectoryName);

    public abstract void doTestSuite(final TestSuite testSuite) throws Exception;

    public void dropGraph(final String graphDirectoryName) {

    }

    public Object convertId(final Object id) {
        return id;
    }

    public String convertLabel(final String label) {
        return label;
    }
}
