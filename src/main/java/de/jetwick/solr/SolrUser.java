/**
 * Copyright (C) 2010 Peter Karich <jetwick_@_pannous_._info>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jetwick.solr;

import de.jetwick.data.YUser;
import de.jetwick.tw.TwitterSearch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import twitter4j.Tweet;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class SolrUser extends YUser {

    private Collection<SolrTweet> ownTweets = new LinkedHashSet<SolrTweet>();
    private Collection<SavedSearch> savedSearches = new ArrayList<SavedSearch>();

    /**
     * You'll need to call init after this
     */
    public SolrUser(String name) {
        super(name);
    }

    public SolrUser init(Tweet tw) {
        setProfileImageUrl(tw.getProfileImageUrl());
        setLocation(TwitterSearch.toStandardLocation(tw.getLocation()));
        return this;
    }

    public void addSavedSearch(SavedSearch ss) {
        savedSearches.add(ss);
    }

    public Collection<SavedSearch> getSavedSearches() {
        return savedSearches;
    }

    public void addOwnTweet(SolrTweet tw) {
        addOwnTweet(tw, true);
    }

    public void addOwnTweet(SolrTweet tw, boolean reverse) {
        ownTweets.add(tw);
//        dirtyOwnTweets = true;

        if (reverse)
            tw.setFromUser(this, false);
    }

    public void deleteOwnTweet(SolrTweet tw) {
        ownTweets.remove(tw);
    }

    public Collection<SolrTweet> getOwnTweets() {
//        if (dirtyOwnTweets) {
//            SolrTweet.deduplicate(ownTweets);
//            dirtyOwnTweets = false;
//        }
        return Collections.unmodifiableCollection(ownTweets);
    }
}
