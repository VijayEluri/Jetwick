/*
 *  Copyright 2010 Peter Karich jetwick_@_pannous_._info
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.jetwick.ui;

import de.jetwick.solr.SolrTweet;
import de.jetwick.solr.SolrUser;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Karich, jetwick_@_pannous_._info
 */
public class ResultsPanelTest extends WicketPagesTestClass {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void textFillTranslateMap() {
        ResultsPanel panel = (ResultsPanel) tester.startPanel(ResultsPanel.class);

        panel.fillTranslateMap(Arrays.asList(
                new SolrTweet(1L, "cars are great but are too expensive", new SolrUser("empty")),
                new SolrTweet(2L, "autos sind toll aber überflüssig", new SolrUser("empty"))),
                "de");

        assertTrue(panel.getTranslateMap().get(1L).contains("aber"));
        assertTrue(panel.getTranslateMap().get(2L).contains("aber"));
    }
}
