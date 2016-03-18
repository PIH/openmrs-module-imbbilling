package org.openmrs.module.mohbilling.businesslogic;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BillingGlobalProperties {

	public static List<String> getListOfServiceCategory() {
		return parseGlobalPropertyValue(BillingConstants.GLOBAL_PROPERTY_SERVICE_CATEGORIES);
	}

	public static List<String> getReportColumns() {
        return parseGlobalPropertyValue(BillingConstants.GLOBAL_PROPERTY_SERVICE_CATEGORY_REPORT_COLUMNS);
    }

    public static PatientIdentifierType getPrimaryPatientIdentiferType() {
        String gpVal = getGlobalPropertyValue(BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE);

        PatientIdentifierType ret = null;
        try {
            Integer typeId = Integer.valueOf(gpVal);
            ret = Context.getPatientService().getPatientIdentifierType(typeId);
        }
        catch (Exception ex) {}

        if (ret == null) {
            ret = Context.getPatientService().getPatientIdentifierTypeByUuid(gpVal);
        }

        if (ret == null) {
            ret = Context.getPatientService().getPatientIdentifierTypeByName(gpVal);
        }

        if (ret == null) {
            throw new RuntimeException("Cannot find patient identifier type specified by global property " + BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE);
        }

        return ret;
    }

    public static Location getDefaultLocation() {
        String gpVal = getGlobalPropertyValue(BillingConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION);

        Location ret = null;
        try {
            Integer typeId = Integer.valueOf(gpVal);
            ret = Context.getLocationService().getLocation(typeId);
        }
        catch (Exception ex) {}

        if (ret == null) {
            ret = Context.getLocationService().getLocationByUuid(gpVal);
        }

        if (ret == null) {
            ret = Context.getLocationService().getLocation(gpVal);
        }

        if (ret == null) {
            throw new RuntimeException("Cannot find patient identifier type specified by global property " + BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE);
        }

        return ret;
    }

    public static List<String> parseGlobalPropertyValue(String globalPropertyName) {
        List<String> list = new ArrayList<String>();
        String gpVal = getGlobalPropertyValue(globalPropertyName);
        if (StringUtils.isNotBlank(gpVal)) {
            StringTokenizer tokenizer = new StringTokenizer(gpVal,",");
            while (tokenizer.hasMoreTokens()) {
                String catgoryStr = tokenizer.nextToken();
                list.add(catgoryStr.trim());
            }
        }
        return list;
    }

    public static String getGlobalPropertyValue(String globalPropertyName) {
        return Context.getAdministrationService().getGlobalProperty(globalPropertyName);
    }
}