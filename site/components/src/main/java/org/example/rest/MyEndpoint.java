package org.example.rest;

import org.example.beans.BaseDocument;
import org.hippoecm.hst.cache.CacheElement;
import org.hippoecm.hst.cache.HstCache;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Author hedu on 21/02/20.
 */
@Path("/api/")
public class MyEndpoint extends org.hippoecm.hst.jaxrs.services.AbstractResource {

    private HstCache customCache;

    public MyEndpoint(HstCache cache) {
        this.customCache = cache;
    }

    private final static Logger logger = LoggerFactory.getLogger(MyEndpoint.class);

    @GET
    @Path("/search/")
    @Produces("application/json")
    public List<String> searchProductResources(
            @Context HttpServletRequest servletRequest,
            @Context HttpServletResponse servletResponse,
            @Context UriInfo uriInfo) {

        String key = "MY_KEY";
        CacheElement cacheElement = customCache.get(key);

        if (cacheElement == null) {
            logger.error("cache is empty...");
            List<String> identifiers = new ArrayList<>();
            try {

                HstRequestContext requestContext = RequestContextProvider.get();

                HstQueryManager hstQueryManager =
                        getHstQueryManager(requestContext);

                HippoBean scope = getMountContentBaseBean(requestContext);

                HstQuery hstQuery = hstQueryManager.createQuery(scope, BaseDocument.class, true);
                HstQueryResult result = hstQuery.execute();
                HippoBeanIterator iterator = result.getHippoBeans();

                while (iterator.hasNext()) {
                    HippoBean hippoBean = iterator.nextHippoBean();

                    if (hippoBean != null) {
                        identifiers.add(hippoBean.getIdentifier());
                    }
                }

            } catch (Exception e) {
                throw new WebApplicationException(e);
            }

            customCache.put(customCache.createElement(key, identifiers));
            return identifiers;
        }
        else {
            logger.error("cache had the value...");
            return (List<String>) cacheElement.getContent();
        }
    }
}
