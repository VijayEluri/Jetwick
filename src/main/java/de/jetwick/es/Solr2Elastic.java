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
package de.jetwick.es;

import java.util.Date;
import de.jetwick.util.Helper;
import de.jetwick.util.MyDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.xcontent.FilterBuilders;
import org.elasticsearch.index.query.xcontent.QueryBuilders;
import org.elasticsearch.index.query.xcontent.RangeFilterBuilder;
import org.elasticsearch.index.query.xcontent.XContentFilterBuilder;
import org.elasticsearch.index.query.xcontent.XContentQueryBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.range.RangeFacetBuilder;
import org.elasticsearch.search.sort.SortOrder;

import static org.elasticsearch.index.query.xcontent.QueryBuilders.*;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Solr2Elastic {

    public static void createElasticQuery(SolrQuery query, SearchRequestBuilder srb) {
        // TODO use dis_max with fields tw, user and title
        Integer rows = query.getRows();

        if (rows == null)
            rows = 10;

        Integer start = query.getStart();
        if (start == null)
            start = 0;

        srb.setSearchType(SearchType.QUERY_THEN_FETCH).//QUERY_AND_FETCH would return too many results
                setFrom(start).setSize(rows);//.setExplain(true);

        if (query.getSortFields() != null) {
            for (String str : query.getSortFields()) {
                String sortSplitted[] = str.split(" ");
                if ("asc".equals(sortSplitted[1]))
                    srb.addSort(sortSplitted[0], SortOrder.ASC);
                else if ("desc".equals(sortSplitted[1]))
                    srb.addSort(sortSplitted[0], SortOrder.DESC);
            }
        }

        XContentQueryBuilder qb;
        if (query.getQuery() == null || query.getQuery().isEmpty())
            qb = QueryBuilders.matchAllQuery();
        else {
            // fields can also contain patterns like so name.* to match more fields
            qb = QueryBuilders.queryString(query.getQuery()).
                    field(ElasticTweetSearch.TWEET_TEXT).field("dest_title_t").field("user", 0).
                    allowLeadingWildcard(false).analyzer("myanalyzer").useDisMax(true);
        }

        // takes too long :/
//        qb = customScoreQuery(qb).script("queryboost").lang("mvel").param("mynow", new Date().getTime());
        
//        long time = new MyDate().castToHour().getTime();
//        qb = customScoreQuery(qb).script("_score * 100 /(10.0e-8 * (" + time + " - doc['dt'].value) + 1);").lang("python");
//        qb = customScoreQuery(qb).script("_score * 100 /(10.0e-8 * (" + time + " - doc['dt'].value) + 1);").lang("mvel");

        if (query.getFilterQueries() != null) {
            XContentFilterBuilder fb = null;
            for (String fq : query.getFilterQueries()) {
                XContentFilterBuilder tmp = filterQuery2Builder(fq);
                if (fb != null)
                    fb = FilterBuilders.andFilter(fb, tmp);
                else
                    fb = tmp;
            }

            if (fb != null)
                qb = QueryBuilders.filteredQuery(qb, fb);
        }

        if (query.getFacetFields() != null) {
            for (String ff : query.getFacetFields()) {
                ff = removeLocalParams(ff);
                srb.addFacet(FacetBuilders.termsFacet(ff).field(ff));
            }
        }

        if (query.getFacetQuery() != null) {
            for (String ff : query.getFacetQuery()) {
                srb.addFacet(FacetBuilders.filterFacet(ff).filter(filterQuery2Builder(ff)));
            }
        }

        if (query.getParams("facet.date") != null) {
            // too much work to convert the generic case with all the date math
            // so cheat for our case:
            String name = ElasticTweetSearch.DATE_FACET;
            RangeFacetBuilder rfb = FacetBuilders.rangeFacet(name).field(ElasticTweetSearch.DATE);

            MyDate date = new MyDate();

            // latest
            rfb.addUnboundedTo(Helper.toLocalDateTime(date.minusHours(8).castToHour().toDate()));

            for (int i = 0; i < 6; i++) {
                // from must be smaller than to!
                MyDate tmp = date.clone();
                rfb.addRange(Helper.toLocalDateTime(date.minusDays(1).castToDay().toDate()),
                        Helper.toLocalDateTime(tmp.toDate()));
            }

            // oldest
            rfb.addUnboundedFrom(Helper.toLocalDateTime(date.toDate()));

            srb.addFacet(rfb);
        }

        srb.setQuery(qb);
    }

    public static String getDateFacetName(String field) {
        return "df_" + field;
    }

    public static XContentFilterBuilder filterQuery2Builder(String fq) {
        // skip local parameter!
        fq = removeLocalParams(fq);

        String strs[] = fq.split(":", 2);
        String key = strs[0];
        String val = strs[1];

        if (val.contains(" OR ")) {
            String fqs[] = fq.split(" OR ");

            String field = null;
            int i = 0;
            Object[] terms = new Object[fqs.length];
            for (; i < fqs.length; i++) {
                String res[] = fqs[i].split(":", 2);
                terms[i] = getTermValue(res[1]);
                if (i > 0 && !res[0].equals(field))
                    break;

                field = res[0];
            }

            if (i == fqs.length) {
                // all fields are equal so use termsFilter
                return FilterBuilders.termsFilter(field, terms);
            } else {
                List<XContentFilterBuilder> filters = new ArrayList<XContentFilterBuilder>();
                for (String tmpFq : fqs) {
                    filters.add(filterQuery2Builder(tmpFq));
                }
                return FilterBuilders.orFilter(filters.toArray(new XContentFilterBuilder[filters.size()]));
            }
        }

        if (val.startsWith("[NOW") || val.startsWith("[DAY")) {
            throw new UnsupportedOperationException("not yet implemented");
        } else if (val.startsWith("[")) {
            val = val.substring(1, val.length() - 1);
            int index1 = val.indexOf(" ");
            if (index1 < 0)
                throw new IllegalStateException("couldn't handle filter:" + fq);

            RangeFilterBuilder rfb = FilterBuilders.rangeFilter(key);
            Object from = null;
            Object to = null;
            //+-Infinity comes from ES
            if (!val.startsWith("*") && !val.startsWith("-Infinity")) {
                try {
                    from = Integer.parseInt(val.substring(0, index1));
                } catch (NumberFormatException ex) {
                    from = Helper.toDate(val.substring(0, index1));
                }
                rfb.from(from).includeLower(true);
            }

            if (!val.endsWith("*") && !val.endsWith("Infinity")) {
                String tmp = val.substring(index1 + " TO ".length());
                try {
                    to = Integer.parseInt(tmp);
                } catch (NumberFormatException ex) {
                    to = Helper.toDate(tmp);
                }

                if (from != null)
                    rfb.to(to).includeUpper(true);
                else
                    rfb.lt(to).includeUpper(true); // lte(Object) is missing
            }

            if (from == null && to == null)
                return FilterBuilders.existsFilter(val);

            return rfb;
        } else
            return FilterBuilders.termFilter(key, getTermValue(val));
    }

    public static Object getTermValue(String val) {
        Object newVal = val;
        if (val.startsWith("\"") && val.endsWith("\""))
            newVal = val.substring(1, val.length() - 1);

        // guess the type            
        try {
            newVal = Long.parseLong(val);
        } catch (Exception ex) {
            try {
                newVal = Double.parseDouble(val);
            } catch (Exception ex2) {
                // necessary?
//                    try {
//                        retVal = Boolean.parseBoolean(val);
//                    } catch (Exception ex3) {
//                    }
            }
        }
        return newVal;
    }

    private static String removeLocalParams(String val) {
        if (val.startsWith("{")) {
            int index = val.indexOf("}");
            if (index > 0)
                val = val.substring(index + 1);
        }

        return val;
    }
}