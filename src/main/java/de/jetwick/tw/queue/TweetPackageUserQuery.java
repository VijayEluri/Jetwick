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
package de.jetwick.tw.queue;

import de.jetwick.solr.SolrTweet;
import de.jetwick.solr.SolrUser;
import de.jetwick.tw.Credits;
import de.jetwick.tw.TwitterSearch;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.TwitterException;

/**
 *
 * @author Peter Karich, jetwick_@_pannous_._info
 */
public class TweetPackageUserQuery extends AbstractTweetPackage {

    private static final Logger logger = LoggerFactory.getLogger(TweetPackageUserQuery.class);
    private Credits credits;
    private String userName;

    public TweetPackageUserQuery() {
    }

    public TweetPackageUserQuery init(int id, String user, Credits credits, int maxTweets) {
        super.init(id, maxTweets);
        this.credits = credits;
        this.userName = user;
        return this;
    }

    @Override
    public TweetPackage retrieveTweets(BlockingQueue<SolrTweet> res) {
        try {            
            TwitterSearch tweetSearch = getTwitterSearch(credits);
            res.addAll(tweetSearch.getTweets(new SolrUser(userName), new ArrayList<SolrUser>(), getMaxTweets()));
            logger.info("add tweets from user search: " + userName);
        } catch (TwitterException ex) {
            doAbort(ex);
            logger.warn("Couldn't update user: " + userName + " " + ex.getLocalizedMessage());
        }
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + " user:" + userName;
    }
}
