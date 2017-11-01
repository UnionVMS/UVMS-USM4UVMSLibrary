/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.rest.security;

import org.junit.Test;

/**
 * Created by georgige on 10/29/2015.
 */

public class AuthorizationFilterTest {

    private AuthorizationFilter filter;

    @Test
    public void testDatasetsCategorization() {
        /*filter = new AuthorizationFilter();

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
        assertEquals(2, mapCategories.get(null).size());*/
    }

    @Test
    public void testDatasetsCategorizationNoContext() {
       /* filter = new AuthorizationFilter();

        Map<String, List<Dataset>> mapCategories = filter.getCategorizedDatasets(null);

        assertNotNull(mapCategories);
        assertEquals(0, mapCategories.size());*/
    }
}