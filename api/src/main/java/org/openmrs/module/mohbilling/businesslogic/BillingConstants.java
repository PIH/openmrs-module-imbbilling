package org.openmrs.module.mohbilling.businesslogic;

public class BillingConstants {

    // The following 2 global properties serve only to find the patient's primary identifier of the given type at the given location
    // If found, this serves as the Policy card number in cases where the Insurance Category for the Policy = None
	public static final String GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE = "billing.primaryIdentifierType";
	public static final String GLOBAL_PROPERTY_DEFAULT_LOCATION = "billing.defaultLocation";

    // The following 5 global properties are used exclusively by the FileExporter class for output on the various CSV and PDF reports
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO = "billing.healthFacilityLogo";
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_NAME = "billing.healthFacilityName";
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS = "billing.healthFacilityPhysicalAddress";
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE = "billing.healthFacilityShortCode";
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL = "billing.healthFacilityEmail";

    // The following 2 properties seem to be used only by the revenue reports / cashier reports
    // The first defines the service categories for the report, the second presumably maps to the service categories defined above
    // to provide an alternative, more concise heading for the cell in the report
    public static final String GLOBAL_PROPERTY_SERVICE_CATEGORIES = "billing.serviceCategories";
    public static final String GLOBAL_PROPERTY_SERVICE_CATEGORY_REPORT_COLUMNS = "billing.reportColumns";
}
