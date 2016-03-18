package org.openmrs.module.mohbilling.businesslogic;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.service.BillingService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Class to contain all of the business logic helper classes for the creation of
 * facility services, and billable service items.
 * <p/>
 * The parent class is FacilityServicePrice, and BillableServices are child objects.
 *
 * @author Kamonyo
 */
public class FacilityServicePriceUtil {

    private static Log log = LogFactory.getLog(FacilityServicePriceUtil.class);

    /**
     * Offers the BillingService to be use to talk to the DB
     *
     * @return the BillingService
     */
    private static BillingService getService() {
        return Context.getService(BillingService.class);
    }

    private static Comparator<BillableService> BILLABLE_SERVICE_COMPARATOR = new Comparator<BillableService>() {
        // This is where the sorting happens.
        public int compare(BillableService bs1, BillableService bs2) {
            int categoryDifference = bs1
                    .getServiceCategory()
                    .getName()
                    .toLowerCase()
                    .compareTo(bs2.getServiceCategory().getName().toLowerCase());

            if (categoryDifference == 0) {
                return bs1
                        .getFacilityServicePrice()
                        .getName()
                        .toLowerCase()
                        .compareTo(bs2.getFacilityServicePrice().getName().toLowerCase());
            }
            else {
                return categoryDifference;
            }
        }
    };

    private static Comparator<FacilityServicePrice> FACILITY_SERVICE_PRICE_COMPARATOR = new Comparator<FacilityServicePrice>() {
        // This is where the sorting happens.
        public int compare(FacilityServicePrice fsp1, FacilityServicePrice fsp2) {
            return fsp1.getName().toLowerCase().compareTo(fsp2.getName().toLowerCase());
        }
    };

    /**
     * Gets the billable services associated with a given insurance
     * @param insurance the Insurance to look up
     */
    public static List<BillableService> getBillableServicesByInsurance(Insurance insurance) {

        List<BillableService> services = getService().getBillableServicesByInsurance(insurance);

        // Sorting by Service Category
        Collections.sort(services, BILLABLE_SERVICE_COMPARATOR);

        return services;
    }

    /**
     * Gets all Billable services for a given facility service
     *
     * @param fsp       the Facility service to be matched
     * @return the list of Billable service that matched the conditions
     */
    public static List<BillableService> getBillableServices(FacilityServicePrice fsp) {
        return getService().getBillableServicesByFacilityService(fsp);
    }

    /**
     * Retires a Billable service for a given Facility Service
     *
     * @param billableService      the billable service to be retired
     * @param retiredDate          the date we retire the service
     * @param retireReason         the reason for retirement
     */
    public static void retireBillableService(BillableService billableService, Date retiredDate, String retireReason) {
        FacilityServicePrice fsp = billableService.getFacilityServicePrice();
        billableService.setRetired(true);
        billableService.setRetiredBy(Context.getAuthenticatedUser());
        billableService.setRetiredDate((retiredDate != null) ? retiredDate : new Date());
        billableService.setRetireReason(StringUtils.isNotBlank(retireReason) ? retireReason : "No reason provided");
        getService().saveFacilityServicePrice(fsp);
    }

    /**
     * Retires the Facility Service at same time it has to retire its
     * BillableServices where they are not
     *
     * @param facilityServicePrice the facility service to be retired
     * @param retiredDate          the date of retirement
     * @param retireReason         the reason of retirement
     */
    public static void retireFacilityServicePrice(FacilityServicePrice facilityServicePrice, Date retiredDate, String retireReason) {

        for (BillableService bs : facilityServicePrice.getBillableServices()) {
            if (!bs.isRetired()) {
                bs.setRetired(true);
                bs.setRetiredBy(Context.getAuthenticatedUser());
                bs.setRetiredDate(retiredDate != null ? retiredDate : new Date());
                bs.setRetireReason("The parent Facility Service (No :"  + facilityServicePrice.getFacilityServicePriceId() + ") was retired as well because '" + retireReason + "'");
            }
        }

        facilityServicePrice.setRetired(true);
        facilityServicePrice.setRetiredBy(Context.getAuthenticatedUser());
        facilityServicePrice.setRetiredDate(retiredDate != null ? retiredDate : new Date());
        facilityServicePrice.setRetireReason(StringUtils.isNotBlank(retireReason) ? retireReason : "No reason provided");

        getService().saveFacilityServicePrice(facilityServicePrice);
    }

    /**
     * Creates a Facility service and at the same time create the Billable
     * service for non-insured patients (Billable service StartDate and EndDate
     * are same as the Facility Service ones, and the MaximaToPay is the same
     * amount as the FullPrice of the Facility Service)
     *
     * @param fsp the Facility service to be added to the DB
     */
    public static FacilityServicePrice createFacilityService(FacilityServicePrice fsp) {
        if (fsp != null) {
            getService().saveFacilityServicePrice(fsp);
            return fsp;
        }
        return null;
    }

    /**
     * Edits a Facility service and at the same time create the Billable service
     * for non-insured patients (Billable service StartDate and EndDate are same
     * as the Facility Service ones, and the MaximaToPay is the same amount as
     * the FullPrice of the Facility Service)
     *
     *
     * @param fsp the Facility service to be added to the DB
     */
    public static FacilityServicePrice editFacilityService(FacilityServicePrice fsp) {

        if (fsp != null) {
            // Retiring the existing Billable service
            for (BillableService serv : fsp.getBillableServices()) {
                if (serv.isRetired()) {
                    retireBillableService(serv, fsp.getStartDate(), "The facility service has changed");
                }
            }
            getService().saveFacilityServicePrice(fsp);
            return fsp;
        }
        return null;
    }

    /**
     * Gets non-voided Facility Services
     */
    public static List<FacilityServicePrice> getFacilityServices() {

        List<FacilityServicePrice> fspList = getService().getAllFacilityServicePrices();

        // Sorting Facility Service Price by Name
        Collections.sort(fspList, FACILITY_SERVICE_PRICE_COMPARATOR);

        return fspList;
    }

    /**
     * Looks up an insurance by the given category, and for all of it's billable services,
     * ensure that any facility service price that is missing a category is assigned one that matches the billable service category
     *
     * @param category the insurance category to look up
     */
    public static Boolean addCategoryToAllFacilityServices(String category) {

        Insurance insurance = InsuranceUtil.getInsuranceByCategory(category);
        if (insurance != null) {

            for (BillableService billable : getBillableServicesByInsurance(insurance)) {

                if (billable.getServiceCategory() != null) {

                    FacilityServicePrice fsp = billable.getFacilityServicePrice();
                    if (fsp.getCategory() == null) {
                        fsp.setCategory(billable.getServiceCategory().getName());
                    }

                    getService().saveFacilityServicePrice(fsp);
                }
            }
            return true;

        }
        else {
            return false;
        }
    }

    /**
     * Gets FacilityServicePrice by facilityId
     *
     * @param facilityId the ID to match
     * @return facilityServicePrice that matches the ID
     */
    public static FacilityServicePrice getFacilityServicePrice(Integer facilityId) {
        return getService().getFacilityServicePrice(facilityId);
    }

    /**
     * Adds Category to Facility Service Price where it misses
     *
     * @param facilityServicePrice the one to be updated
     */
    public static void addCategoryToFacilityService(FacilityServicePrice facilityServicePrice) {

        for (BillableService bill : getBillableServices(facilityServicePrice)) {
            if (bill.getServiceCategory() != null) {
                facilityServicePrice.setCategory(bill.getServiceCategory().getName());
                getService().saveFacilityServicePrice(facilityServicePrice);
                break;
            }
        }
    }

    public static boolean isBillableCreated(FacilityServicePrice facilityService, Insurance insurance) {
        return getService().getBillableServiceByConcept(facilityService, insurance) != null;
    }

    public static String saveBillableServiceByInsurance(HttpServletRequest request) {
        List<Insurance> insurances = getService().getAllInsurances();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        FacilityServicePrice fsp = null;
        String msg = null;
        Date startDate = null;

        String startDateStr = request.getParameter("startDate");
        String facilityServiceIdStr = request.getParameter("facilityServiceId");

        if (StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(facilityServiceIdStr)) {
            try {
                startDate = sdf.parse(startDateStr.split("/")[2] + "-" + startDateStr.split("/")[1] + "-" + startDateStr.split("/")[0]);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("Expected start date in format dd/MM/yyyy, but received " + startDateStr, e);
            }

            fsp = getService().getFacilityServicePrice(Integer.valueOf(facilityServiceIdStr));
        }

        for (Insurance insurance : insurances) {
            if (!insurance.isVoided()) {
                try {
                    BillableService bs = new BillableService();
                    if (FacilityServicePriceUtil.isBillableCreated(fsp, insurance)) {
                        bs = getService().getBillableServiceByConcept(fsp, insurance);
                    }
                    updateBillableService(bs, fsp, insurance, startDate);

                    msg = "Updated Successfully";
                }
                catch (Exception e) {
                    log.error(">>>MOH>>BILLING>>BULK UPDATE>> " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return msg;
    }

    public static void updateBillableService(BillableService bs, FacilityServicePrice fsp, Insurance insurance, Date startDate) {
        bs.setStartDate(startDate);
        bs.setInsurance(insurance);
        bs.setServiceCategory(getService().getServiceCategoryByName(fsp.getCategory(), insurance));
        bs.setCreatedDate(new Date());
        bs.setRetired(false);
        bs.setCreator(Context.getAuthenticatedUser());
        bs.setFacilityServicePrice(fsp);
        bs.setMaximaToPay(calculateMaximaToPay(bs));  // TODO: SHOULDN'T WE JUST MAKE MAXIMA TO PAY CALCULATED IN THE OBJECT, AND REMOVE THIS AS A PROPERTY?
        fsp.addBillableService(bs);
        getService().saveFacilityServicePrice(fsp);
    }

    public static BigDecimal calculateMaximaToPay(BillableService bs) {

        String fspCategory = bs.getFacilityServicePrice().getCategory();
        String insuranceCategory = bs.getInsurance().getCategory();
        BigDecimal fspFullPrice = bs.getFacilityServicePrice().getFullPrice();

        BigDecimal quarter = new BigDecimal(25).divide(new BigDecimal(100));
        BigDecimal fifth = new BigDecimal(20).divide(new BigDecimal(100));

        if (!fspCategory.equalsIgnoreCase("medicaments") && !fspCategory.equalsIgnoreCase("consommables") && !fspCategory.equalsIgnoreCase("AUTRES")) {
            if (insuranceCategory.equalsIgnoreCase("base")) {
                return fspFullPrice;
            }
            else if (insuranceCategory.equalsIgnoreCase("mutuelle")) {
                return fspFullPrice.divide(new BigDecimal(2));
            }
            else if (insuranceCategory.equalsIgnoreCase("private")) {
                return fspFullPrice.add(fspFullPrice.multiply(quarter));
            }
            else if (insuranceCategory.equalsIgnoreCase("none")) {
                BigDecimal initial = fspFullPrice.add(fspFullPrice.multiply(quarter));
                return initial.add(initial.multiply(fifth));
            }
        }
        return fspFullPrice;
    }

    public static void cascadeUpdateFacilityService(FacilityServicePrice fsp) {

        List<Insurance> insurances = getService().getAllInsurances();
        Set<BillableService> billableServices = fsp.getBillableServices();

        for (Insurance insurance : insurances) {
            if (!insurance.isVoided()) {
                for (BillableService bs : billableServices) {
                    bs = getService().getBillableServiceByConcept(fsp, insurance);
                    updateBillableService(bs, fsp, insurance, new Date());
                }
            }
            getService().saveFacilityServicePrice(fsp);
            log.info("Updated Successfully");
        }
    }
}
