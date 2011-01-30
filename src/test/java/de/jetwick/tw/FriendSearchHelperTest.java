/*
 * Copyright 2011 Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net.
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
package de.jetwick.tw;

import de.jetwick.solr.SolrUser;
import java.util.Collection;
import org.junit.Before;
import de.jetwick.JetwickTestClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class FriendSearchHelperTest extends JetwickTestClass {

    public FriendSearchHelperTest() {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetFriendsOf() {
        FriendSearchHelper helper = new FriendSearchHelper(null, null) {

            @Override
            public void updateUser(SolrUser user) {                
            }
            
            @Override
            public void updateFromTwitter(Collection<String> friends, String screenName) {
                friends.add("test");
            }
        };
        assertEquals(1, helper.getFriendsOf(new SolrUser("test")).size());
    }
}