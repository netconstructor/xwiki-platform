/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rest.resources.wikis;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;

import org.xwiki.component.annotation.Component;
import org.xwiki.query.QueryException;
import org.xwiki.rest.model.jaxb.SearchResults;
import org.xwiki.rest.resources.BaseSearchResult;

import com.xpn.xwiki.XWikiException;

@Component("org.xwiki.rest.resources.wikis.WikiSearchResource")
@Path("/wikis/{wikiName}/search")
public class WikiSearchResource extends BaseSearchResult
{
    @GET
    public SearchResults search(@PathParam("wikiName") String wikiName, @QueryParam("q") String keywords,
        @QueryParam("scope") List<String> searchScopeStrings, @QueryParam("number") @DefaultValue("-1") Integer number)
        throws QueryException, XWikiException
    {
        String database = xwikiContext.getDatabase();

        SearchResults searchResults = objectFactory.createSearchResults();
        searchResults.setTemplate(String.format("%s?q={keywords}(&scope={content|name|title|objects})*", UriBuilder
            .fromUri(uriInfo.getBaseUri()).path(WikiSearchResource.class).build(wikiName).toString()));

        /* This try is just needed for executing the finally clause. */
        try {
            xwikiContext.setDatabase(wikiName);

            List<SearchScope> searchScopes = parseSearchScopeStrings(searchScopeStrings);

            searchResults.getSearchResults().addAll(
                search(searchScopes, keywords, wikiName, null, xwiki.getRightService().hasProgrammingRights(
                    xwikiContext), number));
        } finally {
            xwikiContext.setDatabase(database);
        }

        return searchResults;
    }
}
