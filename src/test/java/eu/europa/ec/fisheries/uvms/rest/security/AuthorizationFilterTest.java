package eu.europa.ec.fisheries.uvms.rest.security;

import eu.europa.ec.fisheries.wsdl.user.types.Context;
import eu.europa.ec.fisheries.wsdl.user.types.Dataset;
import eu.europa.ec.fisheries.wsdl.user.types.Scope;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by georgige on 10/29/2015.
 */

public class AuthorizationFilterTest {

    private AuthorizationFilter filter;

    @Test
    public void testDatasetsCategorization() {
        filter = new AuthorizationFilter();

        Dataset dt1 = new Dataset();
        dt1.setName("ReportingDS");
        dt1.setCategory("reporting");
        Dataset dt2 = new Dataset();
        dt2.setName("dfavgad");
        dt2.setCategory("reporting");
        Dataset dt3 = new Dataset();
        dt3.setName("sfsvfdsafas");
        //dt3.setCategory();
        Dataset dt4 = new Dataset();
        dt4.setName("asdfsdfds");
        dt4.setCategory("vessels");
        Dataset dt5 = new Dataset();
        dt5.setName("dsfgdfgdf");
        dt5.setCategory("reporting");
        Dataset dt6 = new Dataset();
        dt6.setName("srtfhgdfg");
        //dt6.setCategory();

        Context ctx = new Context();
        Scope scp = new Scope();
        scp.getDataset().add(dt1);
        scp.getDataset().add(dt2);
        scp.getDataset().add(dt3);
        scp.getDataset().add(dt4);
        scp.getDataset().add(dt5);
        scp.getDataset().add(dt6);
        ctx.setScope(scp);

        Map<String, List<Dataset>> mapCategories = filter.getCategorizedDatasets(ctx);

        assertTrue(mapCategories.containsKey("reporting"));
        assertTrue(mapCategories.containsKey("vessels"));
        assertTrue(mapCategories.containsKey(null));

        assertEquals(3, mapCategories.get("reporting").size());
        assertEquals(1, mapCategories.get("vessels").size());
        assertEquals(2, mapCategories.get(null).size());
    }

    @Test
    public void testDatasetsCategorizationNoContext() {
        filter = new AuthorizationFilter();

        Map<String, List<Dataset>> mapCategories = filter.getCategorizedDatasets(null);

        assertNotNull(mapCategories);
        assertEquals(0, mapCategories.size());
    }
}
