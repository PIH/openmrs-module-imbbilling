package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Category;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * This is a helper class for the Insurance domain, to contain all business
 * logic.
 * 
 * Parent Class is Insurance, child classes are ServiceCategory, and
 * InsuranceRate
 * 
 * @author dthomas
 * 
 */
public class InsuranceUtil {

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

			if (categoryDifference == 0)
				return bs1
						.getFacilityServicePrice()
						.getName()
						.toLowerCase()
						.compareTo(
								bs2.getFacilityServicePrice().getName()
										.toLowerCase());
			else
				return categoryDifference;
		}
	};

	/**
	 * Creates an new or edit an existing one. It just offers the option of
	 * adding only and only one rate to the given insurance. If InsuranceRate is
	 * not provided, it will just save the insurance.
	 * 
	 * @param insurance
	 *            the insurance to be added/edited
	 * @param rate
	 *            the rate to be added
	 * @return the saved insurance
	 */
	public static Insurance createInsurance(Insurance insurance,
			InsuranceRate rate) {

		if (insurance != null) {
			if (rate != null) {
				if (insurance.getRates() != null && rate.isRetired() != null)
					for (InsuranceRate ir : insurance.getRates()) {
						if (!ir.isRetired() && ir.getRetiredDate() == null) {
							ir.setRetiredDate(rate.getStartDate());
							retireInsuranceRate(
									insurance,
									ir,
									"A New Insurance Rate of : '"
											+ rate.getRate()
											+ "%' and Flat fee of : '"
											+ rate.getFlatFee()
											+ "' is created.");
							break;
						}
					}

				insurance.addInsuranceRate(rate);
			}

			getService().saveInsurance(insurance);
			return insurance;
		} else
			return null;
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param insurance
	 * @param rate
	 * @param reason
	 */
	public static void retireInsuranceRate(Insurance insurance,
			InsuranceRate rate, String reason) {

		// Retire the previous rate.
		for (InsuranceRate ir : insurance.getRates()) {
			if (ir.equals(rate)) {
				ir.setRetiredBy(Context.getAuthenticatedUser());
				ir.setRetiredDate(new Date());
				ir.setEndDate(new Date());
				ir.setRetireReason(reason);
				ir.setRetired(true);
				break;
			}
		}

		getService().saveInsurance(insurance);
	}

	/**
	 * This involves retiring current (not retired) InsuranceRate
	 * 
	 * @param insurance
	 * @param voidReason
	 */
	public static void voidInsurance(Insurance insurance, String voidReason) {

		if (!insurance.isVoided()) {
			insurance.setVoided(true);
			insurance.setVoidedBy(Context.getAuthenticatedUser());
			insurance.setVoidedDate(new Date());
			insurance.setVoidReason(voidReason);

			for (InsuranceRate ir : insurance.getRates()) {
				if (!ir.isRetired()) {
					ir.setRetired(true);
					ir.setRetiredBy(Context.getAuthenticatedUser());
					ir.setRetiredDate(new Date());
					ir.setRetireReason("The Insurance " + insurance.getName()
							+ " has been voided");
				}
			}
			getService().saveInsurance(insurance);
		}
	}

	/**
	 * Gets insurances considering the provided validity: i.e. if isValid ==
	 * true it will return the non voided Insurance, and voided ones otherwise.
	 * 
	 * @param isValid
	 *            the validity condition
	 * @return voided Insurances if isValid == false, unvoided otherwise.
	 */
	public static List<Insurance> getInsurances(Boolean isValid) {

		List<Insurance> insurances = new ArrayList<Insurance>();

		for (Insurance insurance : getService().getAllInsurances())
			if (insurance.isVoided() != isValid)
				insurances.add(insurance);

		return insurances;
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param sc
	 * @param date
	 * @param isRetired
	 * @return
	 */
	public static List<BillableService> getBillableServicesByServiceCategory(
			ServiceCategory sc, Date date, Boolean isRetired) {

		List<BillableService> bsByServiceCategory = getService().getBillableServiceByCategory(sc);
		
		// Sorting by Service Category
		Collections.sort(bsByServiceCategory, BILLABLE_SERVICE_COMPARATOR);
		return bsByServiceCategory;
	}

	/**
	 * Saves the Billable service into the DB
	 * 
	 * @param service
	 *            the Billable service to be saved
	 * @return the saved Billable service
	 */

	// We will have to remove the "Maxima to pay" label from the Billable
	// Service form on the GUI. Or just find a way of handling it.
	public static BillableService saveBillableService(BillableService service) {

		FacilityServicePrice fsp = service.getFacilityServicePrice();
		BigDecimal quarter = new BigDecimal(25).divide(new BigDecimal(100));
		BigDecimal fifth = new BigDecimal(20).divide(new BigDecimal(100));

		if (service != null && fsp != null) {

			BigDecimal amount = fsp.getFullPrice();

			// This means the Maxima to Pay is not set in the service
			if (service.getMaximaToPay() == null) {
				if (service.getInsurance().getCategory()
						.equalsIgnoreCase(InsuranceCategory.BASE.toString()))
					service.setMaximaToPay(amount);

				if (service
						.getInsurance()
						.getCategory()
						.equalsIgnoreCase(InsuranceCategory.MUTUELLE.toString()))

					if (!service.getServiceCategory().getName()
							.equalsIgnoreCase("MEDICAMENTS")
							&& !service.getServiceCategory().getName()
									.equalsIgnoreCase("CONSOMABLE")) {
						service.setMaximaToPay(amount.divide(new BigDecimal(2)));

					} else {
						service.setMaximaToPay(amount);
					}

				if (service.getInsurance().getCategory()
						.equalsIgnoreCase(InsuranceCategory.PRIVATE.toString()))
					service.setMaximaToPay(amount.add(amount.multiply(quarter)));

				if (service.getInsurance().getCategory()
						.equalsIgnoreCase(InsuranceCategory.NONE.toString())) {
					BigDecimal initial = amount.add(amount.multiply(quarter));
					service.setMaximaToPay(initial.add(initial.multiply(fifth)));
				}
			} else
				//This happens when Maxima to Pay is set in the service
				service.setMaximaToPay(service.getMaximaToPay());

			fsp.addBillableService(service);
			getService().saveFacilityServicePrice(fsp);

			return service;
		} else
			return null;
	}


	/**
	 * Gets a Billable service by the provided ID
	 * 
	 * @param id the ID to be found
	 * @return service Billable if ID matches, and null otherwise
	 */
	public static BillableService getValidBillableService(Integer id) {
		return getService().getBillableService(id);
	}

	/**
	 * Gets a Service category by the provided ID
	 * 
	 * @param id the ID to be found
	 * @return category Service if ID matches, and null otherwise
	 */
	public static ServiceCategory getValidServiceCategory(Integer id) {
		return getService().getServiceCategory(id);
	}

	public static List<String> getAllServiceCategories() {

		List<String> categories = new ArrayList<String>();

		categories.add(Category.CHIRURGIE.getDescription());
		categories.add(Category.CONSOMMABLES.getDescription());
		categories.add(Category.CONSULTATION.getDescription());
		categories.add(Category.DERMATOLOGIE.getDescription());
		categories.add(Category.ECHOGRAPHIE.getDescription());
		categories.add(Category.FORMALITES_ADMINISTRATIVES.getDescription());
		categories.add(Category.HOSPITALISATION.getDescription());
		categories.add(Category.KINESITHERAPIE.getDescription());
		categories.add(Category.LABORATOIRE.getDescription());
		categories.add(Category.MATERNITE.getDescription());
		categories.add(Category.MEDECINE_INTERNE.getDescription());
		categories.add(Category.MEDICAMENTS.getDescription());
		categories.add(Category.OPHTALMOLOGIE.getDescription());
		categories.add(Category.ORL.getDescription());
		categories.add(Category.OXYGENOTHERAPIE.getDescription());
		categories.add(Category.PEDIATRIE.getDescription());
		categories.add(Category.RADIOLOGIE.getDescription());
		categories.add(Category.SOINS_INFIRMIERS.getDescription());
		categories.add(Category.SOINS_INTENSIFS.getDescription());
		categories.add(Category.STOMATOLOGIE.getDescription());
		categories.add(Category.NEUROLOGIE.getDescription());
		categories.add(Category.AUTRES.getDescription());

		return categories;
	}

	public static Insurance getInsurance(Integer insuranceId) {
		return getService().getInsurance(insuranceId);
	}

	public static List<Insurance> getAllInsurances() {
		return getService().getAllInsurances();
	}

    public static Insurance getInsuranceByConcept(Concept concept) {
        for (Insurance insurance : getInsurances(true)) {
            if (insurance.getConcept().equals(concept))
                return insurance;
        }
        return null;
    }

    /**
     * @param category the category of Insurance to look up, case insensitive
     * @return the first insurance found that has the given category
     */
    public static Insurance getInsuranceByCategory(String category) {
        for (Insurance ins : InsuranceUtil.getAllInsurances()) {
            if (ins.getCategory().equalsIgnoreCase(category)) {
                return ins;
            }
        }
        return null;
    }

}
