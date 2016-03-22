/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 *  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.mohbilling;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Category;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The goal of this class is to produce sufficient test data to ensure the system works as advertised
 */
public class BillingTestData {

    // CHIRURGIES

    public static final String APPENDECTOMY = "APPENDECTOMY";
    public static final String GASTRIC_BYPASS = "GASTRIC BYPASS";

    // CONSOMMABLES

    public static final String CATHETER = "CATHETER SHORT IV 22G";
    public static final String MASK = "CLEAN ROOM FACE MASK";
    public static final String GLOVES = "EXAMINATION GLOVES NS UU T 7.5";
    public static final String SCALPEL = "SURGICAL BLADE NO 22";

    // LABORATOIRE

    public static final String MALARIA_TEST = "MALARIA TEST";
    public static final String CBC = "COMPLETE BLOOD COUNT";

    // MEDICAMENTS

    public static final String ALBENDAZOLE = "ALBENDAZOLE 400MG TABLET B/100";
    public static final String AMOXICILLIN = "AMOXICILLIN 250MG CAPSULE B/1000";
    public static final String ANTIVENOM = "ANTIVENOUS SERUM 10ML INJECTION B/1";

    /**
     * Creates test data
     */
    public static void createFacilityServicePrices() {

        createFacilityServicePrice(APPENDECTOMY, APPENDECTOMY, 50000, Category.CHIRURGIE, "2015-01-01", null, null);
        createFacilityServicePrice(GASTRIC_BYPASS, GASTRIC_BYPASS, 40000, Category.CHIRURGIE, "2015-01-01", null, null);

        createFacilityServicePrice(CATHETER, CATHETER, 263.52, Category.CONSOMMABLES, "2015-01-01", null, null);
        createFacilityServicePrice(MASK, MASK, 109.44, Category.CONSOMMABLES, "2015-01-01", null, null);
        createFacilityServicePrice(GLOVES, GLOVES, 32.7, Category.CONSOMMABLES, "2015-01-01", null, null);
        createFacilityServicePrice(SCALPEL, SCALPEL, 62.1, Category.CONSOMMABLES, "2015-01-01", null, null);

        createFacilityServicePrice(MALARIA_TEST, MALARIA_TEST, 500, Category.LABORATOIRE, "2015-01-01", null, null);
        createFacilityServicePrice(CBC, CBC, 1000, Category.LABORATOIRE, "2015-01-01", null, null);

        createFacilityServicePrice(ALBENDAZOLE, ALBENDAZOLE, 34.848, Category.MEDICAMENTS, "2015-01-01", null, null);
        createFacilityServicePrice(AMOXICILLIN, AMOXICILLIN, 15.22656, Category.MEDICAMENTS, "2015-01-01", null, null);
        createFacilityServicePrice(ANTIVENOM, ANTIVENOM, 64800, Category.MEDICAMENTS, "2015-01-01", null, null);
    }

    /**
     * Creates a new Concept, if it does not already exist with the given name, followed by a FacilityServicePrice associated with it
     */
    protected static FacilityServicePrice createFacilityServicePrice(String conceptName, String serviceName, double price, Category category, String sd, String ed, Location location) {

        ConceptService conceptService = Context.getConceptService();
        BillingService billingService = Context.getService(BillingService.class);

        // First create the generic billable Concept if it does not exist
        Concept concept = conceptService.getConceptByName(conceptName);
        if (concept == null) {
            concept = new Concept();
            concept.setFullySpecifiedName(new ConceptName(conceptName, Locale.ENGLISH));
            concept.setConceptClass(conceptService.getConceptClassByName(getConceptClassNameForCategory(category)));
            concept.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            conceptService.saveConcept(concept);
        }

        // Next, create a FacilityServicePrice for this item
        FacilityServicePrice fsp = new FacilityServicePrice();
        fsp.setName(serviceName);
        fsp.setShortName(serviceName.replaceAll(" ", "_"));
        fsp.setCategory(category.getDescription());
        fsp.setConcept(concept);
        fsp.setFullPrice(new BigDecimal(price));
        fsp.setStartDate(parseDate(sd));
        fsp.setEndDate(parseDate(ed));
        fsp.setLocation(location == null ? Context.getLocationService().getDefaultLocation() : location);
        billingService.saveFacilityServicePrice(fsp);

        return fsp;
    }

    /**
     * Return an appropriate concept class given the Category
     */
    protected static String getConceptClassNameForCategory(Category category) {
        if (category == Category.CHIRURGIE) {
            return "Procedure";
        }
        if (category == Category.LABORATOIRE) {
            return "Test";
        }
        if (category == Category.MEDICAMENTS) {
            return "Drug";
        }
        return "Misc";
    }

    /**
     * Parses a date in yyyy-MM-dd format into a date, returning null if the input is null
     */
    protected static Date parseDate(String dateStr) {
        if (dateStr != null) {
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                return df.parse(dateStr);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Expected date in format yyyy-MM-dd but got " + dateStr, e);
            }
        }
        return null;
    }
}
