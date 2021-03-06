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
package de.jetwick.tw.cmd;

import de.jetwick.data.UrlEntry;
import de.jetwick.data.JTweet;
import de.jetwick.data.JUser;
import de.jetwick.tw.FakeUrlExtractor;
import de.jetwick.tw.TweetDetector;
import de.jetwick.util.MyDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class TermCreateCommandTest {

    public TermCreateCommandTest() {
    }

    static void execute(Collection<JTweet> tweets) {
        execute(tweets, true);
    }

    static void execute(Collection<JTweet> tweets, boolean termRemoving) {
        // remove executor since we only have one remaining command?
        for (JTweet tw : tweets) {
            for (UrlEntry entry : new FakeUrlExtractor().setText(tw.getText()).run().getUrlEntries()) {
                tw.addUrlEntry(entry);
            }
        }
        new SerialCommandExecutor(tweets).add(new TermCreateCommand(termRemoving)).execute();
    }

    static void execute(JTweet tw) {
        execute(Arrays.asList(tw), false);
    }

    @Test
    public void testQuality() {
        JTweet tw1 = createSolrTweet(1L, "@lwr32 #JAVA! "
                + "#COFFEE! #JAVA! #COFFEE! #JAVA! #COFFEE! #JAVA! #COFFEE! #JAVA! #COFFEE! #JAVA! #COFFEE! #JAVA! #COFFEE! #JAVA!", "usera");
        JTweet tw2 = createSolrTweet(2L, "@meggytron JAH-VA! java java java java "
                + "java java java. /Dante's Peak #requirescaffeine mashup", "userb");
        JTweet tw3 = createSolrTweet(3L, "@ierinleker ...JAVA JAVA JAVA JAVA JAVA "
                + "JAVA JAVA http://twitpic.com/2kk65u", "userc");
        JTweet tw4 = createSolrTweet(4L, "java", "userd");

        execute(Arrays.asList(tw1, tw2, tw3, tw4));

        assertTrue(tw4.getQuality() > tw3.getQuality());
        // both tweets have 7 java terms
        assertEquals(tw3.getQuality(), tw2.getQuality());
        assertTrue(tw2.getQuality() > tw1.getQuality());
    }

    @Test
    public void testQuality2() {
        String[] tweetsAsStr = new String[]{
            "Fernsehen entut Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html",
            "Fernsehen taek Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html",
            "Fernsehen stream Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html",
            "Fernsehen live Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html",
            "Televisie kijken Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html",
            "Fernsehen Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html",
            "Fernsehen kijken Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html",
            "Fernsehen Televisie Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html",
            "Fernsehen Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html#1live",
            "Fernsehen Werder Bremen vs FC Twente Enschede http://watchlivefree.blogspot.com/2010/11/fernsehen-werder-bremen-vs-fc-twente.html#1"
        };
        List<JTweet> list = new ArrayList();
        int counter = 0;
        JUser user = new JUser("sakilamahipallb");
        for (String tw : tweetsAsStr) {
            counter++;
            list.add(new JTweet(counter, tw, user).setCreatedAt(new Date(counter)));
        }
        execute(list);

        counter = 0;
        int spamCounter = 0;
        for (JTweet tw : list) {
            if (counter++ > 0) {
                assertTrue("tweet:" + tw, tw.getQuality() < JTweet.QUAL_LOW);

                if (tw.getQuality() < JTweet.QUAL_SPAM)
                    spamCounter++;
            }
        }
        // a lot of those tweets are spam - not only bad!
        assertTrue(spamCounter > 5);

        user = new JUser("user2");
        JTweet tw1 = new JTweet(1L, "E Grant Rd / N Swan Rd Accident no injury (Tue 3:24 PM)  http://tinyurl.com/5hwubc", user).setCreatedAt(new Date(1));
        JTweet tw2 = new JTweet(2L, "N Columbus Bl / E Grant Rd Accident no injury (Tue 3:26 PM)  http://tinyurl.com/658t96", user).setCreatedAt(new Date(2));
        execute(Arrays.asList(tw1, tw2));
//        assertTrue("tweet:" + tw1, tw1.getQuality() < SolrTweet.QUAL_MAX);
        assertTrue("tweet:" + tw1, tw1.getQuality() > JTweet.QUAL_SPAM);
        assertTrue("tweet:" + tw2, tw2.getQuality() < JTweet.QUAL_MAX);
        assertTrue("tweet:" + tw2, tw2.getQuality() > JTweet.QUAL_SPAM);

        user = new JUser("user2");
        tw1 = new JTweet(1L, "Werder Bremen verliert sein Heimspiel gegen Twente http://goo.gl/fb/fKFEi #werder #svw", user).setCreatedAt(new Date(1));
        tw2 = new JTweet(2L, "Werder Bremen verliert gegen Twente Enschede http://goo.gl/fb/O8maL #werder #svw", user).setCreatedAt(new Date(2));
        execute(Arrays.asList(tw1, tw2));
        assertTrue("tweet:" + tw1, tw1.getQuality() == JTweet.QUAL_MAX);
        assertTrue("tweet:" + tw2, tw2.getQuality() < JTweet.QUAL_MAX);
        assertTrue("tweet:" + tw2, tw2.getQuality() > JTweet.QUAL_SPAM);
    }

    @Test
    public void testDecreaseQualityOnlyOnce() {
        String url1, url2, url3;
        url1 = url2 = url3 = "http://watchlivefree.blogspot.com";
        String[] tweetsAsStr = new String[]{
            "blap notspamword " + url1,
            "blup secondnotspamword " + url2,
            "bli secondsomething" + url3};

        JUser user = new JUser("user1");
        JTweet tw1 = new JTweet(1L, tweetsAsStr[0], user).setCreatedAt(new Date(1L));
        tw1.getUrlEntries().add(new UrlEntry(5, 123, url1).setResolvedTitle("title1"));
        JTweet tw2 = new JTweet(2L, tweetsAsStr[1], user).setCreatedAt(new Date(2L));
        tw2.getUrlEntries().add(new UrlEntry(5, 123, url2).setResolvedTitle("title2"));
        JTweet tw3 = new JTweet(3L, tweetsAsStr[2], user).setCreatedAt(new Date(3L));
        tw3.getUrlEntries().add(new UrlEntry(5, 123, url3).setResolvedTitle("title3"));

        execute(Arrays.asList(tw1, tw2, tw3));

        assertEquals(JTweet.QUAL_MAX, tw1.getQuality());
        assertTrue(tw2.getQuality() > JTweet.QUAL_SPAM);
        assertTrue(tw3.getQuality() > JTweet.QUAL_SPAM);
    }

//    @Test
//    public void testUrlTitleQuality() {
//        String url1 = "http://watchlivefree.blogspot.different.domain.com",
//                url2 = "http://watchlivefree.blogspot.com";
//        String[] tweetsAsStr = new String[]{
//            "blap notspamword " + url1,
//            "blup secondnotspamword " + url2};
//
//        JUser user = new JUser("user1");
//        JTweet tw1 = new JTweet(1L, tweetsAsStr[0], user).setCreatedAt(new Date(1L));
//        tw1.getUrlEntries().add(new UrlEntry(5, 123, url1).setResolvedTitle("identical title"));
//        JTweet tw2 = new JTweet(2L, tweetsAsStr[1], user).setCreatedAt(new Date(2L));
//        tw2.getUrlEntries().add(new UrlEntry(5, 123, url2).setResolvedTitle("identical title"));
//
//        execute(Arrays.asList(tw1, tw2));
//
//        assertTrue("tweet:" + tw1, tw1.getQuality() > 90);
//        assertTrue("tweet:" + tw2, tw2.getQuality() < 90);
//    }

    @Test
    public void testExecute() {
        JTweet tw = new JTweet(1L, "java lava", new JUser("tmp")).setCreatedAt(new Date(1L));
        execute(tw);
        assertEquals(2, tw.getTextTerms().size());

        JUser u = new JUser("peter");
        tw = new JTweet(1L, "java lava", u);
        JTweet tw2 = new JTweet(2L, "peter java", u).setCreatedAt(new Date(2L));
        execute(tw);
        assertEquals(2, tw.getTextTerms().size());
        assertEquals(2, tw2.getTextTerms().size());
    }

    JTweet createSolrTweet(long id, String twText, String user) {
        return new JTweet(id, twText, new JUser(user)).setCreatedAt(new MyDate(id).toDate());
    }

    @Test
    public void testTermDetection() {
        JUser user = new JUser("Peter");
        user.addOwnTweet(new JTweet(1, "term1 term2 term1", user));
        JTweet tw = new JTweet(2, "term3 not term2 important term3", user);
        user.addOwnTweet(tw);

        execute(tw);
        Collection<Entry<String, Integer>> coll = tw.getTextTerms().entrySet();
        assertEquals(3, (int) coll.size());
        int counter = 0;
        int counter2 = 0;
        for (Entry<String, Integer> e : coll) {
            if (e.getKey().equals("term1"))
                counter++;

            if (e.getKey().equals("not"))
                counter2++;
        }
        assertEquals(0, counter);
        assertEquals(0, counter2);
    }

    @Test
    public void testTermDetection2() {
        JUser user = new JUser("Peter");
        JTweet tw1 = new JTweet(1, "#term1 #term1", user);
        user.addOwnTweet(tw1);
        JTweet tw2 = new JTweet(2, "term1", user);
        user.addOwnTweet(tw2);

        execute(tw2);

        // two tweets with 'term1'
//        assertEquals(2, (int) extractor.run().getSortedTerms().get(0).getValue());
        assertEquals(1, (int) tw2.getTextTerms().size());
    }

    @Test
    public void testTermDetection3() {
        JTweet tw = new JTweet(1L, "A Year Without Rain "
                + "A Year Without Rain A Year Without Rain A Year Without Rain "
                + "A Year Without Rain A Year Without Rain A Year Without Rain", new JUser("peter"));
        execute(tw);
        assertEquals(2, tw.getTextTerms().size());
    }

    @Test
    public void testOtherTweets() {
        JUser u = new JUser("peter");
        JTweet tw1 = new JTweet(1L, "A Year Without Rain Will Give Us desert xyz", u).setCreatedAt(new Date(2L));
        // tw2 is older than tw1
        JTweet tw2 = new JTweet(2L, "A Year Without Rain Will Give Us really fat desert", u).setCreatedAt(new Date(1L));
        JTweet tw3 = new JTweet(3L, "great hui desert", u).setCreatedAt(new Date(0L));
        tw1.setQuality(100);
        tw2.setQuality(89);
        execute(tw1);
        // unchanged
        assertEquals(89, tw2.getQuality());
        assertTrue(tw1.getQuality() < 100);

        tw1.setQuality(100);
        StringFreqMap tFreq = new StringFreqMap();
        StringFreqMap lFreq = new StringFreqMap();
        new TermCreateCommand().checkSpamInExistingTweets(tw1, tFreq, lFreq);
        // without tw1
        assertEquals(9, (int) lFreq.get(TweetDetector.EN));
        assertEquals(1, (int) lFreq.get(TweetDetector.DE));

        assertEquals(6, (int) tw1.getLanguages().get(TweetDetector.EN));

        // without tw1
        assertEquals(2, (int) tFreq.get("desert"));
        assertEquals(1, (int) tFreq.get("hui"));
        assertNull(tFreq.get("xyz"));

        assertEquals(1, (int) tw2.getTextTerms().get("fat"));
    }

    @Test
    public void testLanguageDetection2() {
        JUser user = new JUser("peter");
        JTweet tw1 = new JTweet(0, "this is lastwordIsNotRecognizedBecauseItCouldBeStrippedOut", user);
        execute(tw1);
        assertEquals(2, tw1.getLanguages().get(TweetDetector.EN).intValue());
        assertEquals(TweetDetector.UNKNOWN_LANG, tw1.getLanguage());

        // now the language is detected because a lot noise NOISE_WORDS were found
        JTweet tw = new JTweet(2, "viele ist dort deutscher Tweet!", user);
        execute(tw);
        assertEquals(TweetDetector.DE, tw.getLanguage());

        user = new JUser("peter");
        tw = new JTweet(3L, "Togos with @munckytown on lunch break. "
                + "Hall and Oates \"kiss on my list\" is playing... groovy", user);
        execute(tw);
        assertEquals(TweetDetector.EN, tw.getLanguage());

        user = new JUser("peter");
        tw = new JTweet(4L, "@ibood Bedankt voor de code! :-)", user);
        execute(tw);
        // only de and en are known so detect as unknown!
        assertEquals(TweetDetector.UNKNOWN_LANG, tw.getLanguage());

        // now detect the nl language 
        tw = new JTweet(5L, "@MrDeek Klinkt goed toch, een bestek set is altijd leuk om te krijgen of te geven!", user);
        execute(tw);
        assertEquals(TweetDetector.NL, tw.getLanguage());
    }

    @Test
    public void testLanguageDetection3() {
        JTweet tw = new JTweet(1L, "tmptext", new JUser("tmp"));
        tw.getLanguages().inc("de", 1);
        StringFreqMap otherLanguages = new StringFreqMap();
        assertEquals(TweetDetector.UNKNOWN_LANG, new TermCreateCommand().detectLanguage(tw, otherLanguages));

        tw = new JTweet(1L, "tmptext", new JUser("tmp"));
        tw.getLanguages().inc("de", 2);
        otherLanguages = new StringFreqMap().set("de", 1);
        assertEquals("de", new TermCreateCommand().detectLanguage(tw, otherLanguages));

        tw = new JTweet(1L, "tmptext", new JUser("tmp"));
        tw.getLanguages().inc(TweetDetector.UNKNOWN_LANG, 2);
        tw.getLanguages().inc("de", 2);
        otherLanguages = new StringFreqMap().set("de", 1);
        assertEquals("de", new TermCreateCommand().detectLanguage(tw, otherLanguages));

        tw = new JTweet(1L, "tmptext", new JUser("tmp"));
        tw.getLanguages().inc(TweetDetector.UNKNOWN_LANG, 2);
        tw.getLanguages().inc("de", 2);
        tw.getLanguages().inc("en", 2);
        otherLanguages = new StringFreqMap().set("de", 1).set("en", 1);
        assertEquals(TweetDetector.UNKNOWN_LANG, new TermCreateCommand().detectLanguage(tw, otherLanguages));
    }

//    @Test
//    public void testSignature() {
//        SolrTweet tw = new SolrTweet(1L, "wtf wtf text", new SolrUser("tmp"));
//        new TermCreateCommand().calcTermsWithoutNoise(tw);
//        assertTrue(tw.getTextSignature().size() > 0);
//        SolrTweet tw2 = new SolrTweet(2L, "wtf wtf text", new SolrUser("tmp"));
//        new TermCreateCommand().calcTermsWithoutNoise(tw2);
//        assertEquals(tw.getTextSignature(), tw2.getTextSignature());
//
//        SolrTweet tw3 = new SolrTweet(3L, "wtf wtf text wikileaks info", new SolrUser("tmp"));
//        new TermCreateCommand().calcTermsWithoutNoise(tw3);
//        int counter = 0;
//        for (Long val : tw3.getTextSignature()) {
//            if (tw2.getTextSignature().contains(val))
//                counter++;
//        }
//        assertTrue("At least on signature should be identical for tweet2 and tweet3", counter > 0);
//    }    
}
