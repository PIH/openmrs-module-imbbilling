/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.impl;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.BillingTestData;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.List;

public class BillingServiceImplTest extends BaseModuleContextSensitiveTest {

    @Test
    public void getAllFacilityServicePricesTest() {
        BillingTestData.createFacilityServicePrices();
        List<FacilityServicePrice> allItems = getBillingService().getAllFacilityServicePrices();
        Assert.assertEquals(11, allItems.size());
    }

    public BillingService getBillingService() {
        return Context.getService(BillingService.class);
    }
}
