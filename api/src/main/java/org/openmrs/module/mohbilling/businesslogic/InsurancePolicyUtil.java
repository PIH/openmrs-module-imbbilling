package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Helper class to support the InsurancePolicy domain. (in other words, the
 * insurance card)
 * 
 * Parent class is InsurancePolicy, child class is Beneficiary
 * 
 * @author Kamonyo
 * 
 */
public class InsurancePolicyUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {
		return Context.getService(BillingService.class);
	}

	/**
	 * Creates the insurance policy and at the same time sets the Owner as the
	 * very first beneficiary of the card
	 * 
	 * @param card
	 *            the Insurance Policy to be added into the DB
	 * @return card if the card is not null, and null otherwise
	 */
	public static InsurancePolicy createInsurancePolicy(InsurancePolicy card) {

		if (card != null) {

			if (card.getInsurancePolicyId() != null)
				getService().saveInsurancePolicy(card);

			else {

				card.setCreatedDate(new Date());
				card.setCreator(Context.getAuthenticatedUser());
				card.setRetired(false);

				if (card.getInsurance().getCategory().equalsIgnoreCase(InsuranceCategory.NONE.toString())) {
					PatientIdentifier pi = getPrimaryPatientIdentifierForDefaultLocation(card.getOwner());
					card.setInsuranceCardNo(pi.getIdentifier().toString());
					card.setCoverageStartDate(new Date());
				}

				getService().saveInsurancePolicy(card);

				/**
				 * Creating the owner who is at the same time the very first
				 * beneficiary of this insurance policy (In this case
				 * <code>insuranceCardNo == policyIdNumber</code>)
				 */
				Beneficiary beneficiary = new Beneficiary();

				beneficiary.setInsurancePolicy(card);

				/* Check how it should be working for NONE insurance */
				beneficiary.setPolicyIdNumber(card.getInsuranceCardNo());
				/* End of checking */

				beneficiary.setCreatedDate(card.getCreatedDate());
				beneficiary.setCreator(card.getCreator());
				beneficiary.setPatient(card.getOwner());
				beneficiary.setRetired(card.isRetired());
				beneficiary.setRetiredBy(card.getRetiredBy());
				beneficiary.setRetiredDate(card.getRetiredDate());
				beneficiary.setRetireReason(card.getRetireReason());

				card.addBeneficiary(beneficiary);

				getService().saveInsurancePolicy(card);
			}

			return card;
		}

		return null;
	}


	/**
	 * Gets the Insurance policy that has the provided beneficiary
	 * 
	 * @param beneficiary
	 *            the beneficiary to be matched
	 * @return card if matched, null otherwise
	 */
	public static InsurancePolicy getInsurancePolicyByBeneficiary(
			Beneficiary beneficiary) {

		return getService().getInsurancePolicyByBeneficiary(beneficiary);
	}

	/**
	 * Gets the Beneficiary where it matches its policyIdNumber, you can pass
	 * the InsuranceCardNo as well, given the fact that this will be matching
	 * also the Beneficiary PolicyIdNo for the very first beneficiary of the
	 * InsurancePolicy
	 * 
	 * @param policyIdNumber
	 * @return beneficiary whether matched the policyIdNumber, null otherwise
	 */
	public static Beneficiary getBeneficiaryByPolicyIdNo(String policyIdNumber) {
		return getService().getBeneficiaryByPolicyNumber(policyIdNumber);
	}

	public static PatientIdentifier getPrimaryPatientIdentifierForDefaultLocation(Patient patient) {
        Integer typeId = BillingGlobalProperties.getPrimaryPatientIdentiferType().getPatientIdentifierTypeId();
        Integer locationId = BillingGlobalProperties.getDefaultLocation().getLocationId();

		List<PatientIdentifier> piList = patient.getActiveIdentifiers();
		for (PatientIdentifier piTmp : piList) {
			if (piTmp.getIdentifierType().getPatientIdentifierTypeId().equals(typeId) && piTmp.getLocation().getLocationId().equals(locationId)) {
				return piTmp;
			}
		}
		return null;
	}

	/**
	 * Gets all Third Parties
	 * 
	 * @return
	 */
	public static List<ThirdParty> getAllThirdParties() {

		List<ThirdParty> parts = getService().getAllThirdParties();

		if (parts != null)
			return parts;
		else
			return new ArrayList<ThirdParty>();
	}

	/**
	 * Gets all Third Parties
	 * 
	 * @return
	 */
	public static ThirdParty getThirdParty(Integer thirdPartyId) {

		return getService().getThirdParty(thirdPartyId);
	}

	/**
	 * Gets all Third Parties
	 * 
	 * @return
	 */
	public static void saveThirdParty(ThirdParty thirdParty) {

		thirdParty.setVoided(false);
		thirdParty.setCreator(Context.getAuthenticatedUser());
		thirdParty.setCreatedDate(new Date());

		getService().saveThirdParty(thirdParty);
	}

	/**
	 * Voids Third Party that is provided
	 * 
	 * @param thirdParty
	 *            the ThirdParty to void
	 */
	public static void voidThirdParty(ThirdParty thirdParty) {

		thirdParty.setVoided(true);
		thirdParty.setVoidedBy(Context.getAuthenticatedUser());
		thirdParty.setVoidedDate(new Date());
		thirdParty.setVoidReason("This Third Party : "
				+ thirdParty.getName().toUpperCase()
				+ " is no longer in use...");

		getService().saveThirdParty(thirdParty);
	}

	/**
	 * Gets all PolicyIds that are associated to the given patient
	 * 
	 * @param patientId
	 *            the patient ID to match
	 * @return list of String[] : {INSURANCE NAME, POLICY ID}
	 */
	public static List<String[]> getPolicyIdByPatient(Integer patientId) {

		return getService().getPolicyIdByPatient(patientId);
	}
	/**
	 * Checks whether there is an insurance policy associated insurance card number
	 * @param insuranceCardNo
	 * @return true if the insurance exists,
	 * else return false if the insurance policy does not exist
	 */
	public  static boolean isInsurancePolicyExists(String insuranceCardNo){
		
		InsurancePolicy insurancePolicy = getService().getInsurancePolicyByCardNo(insuranceCardNo);
		if (insurancePolicy !=null) {
			return true;			
		}
		else return false;		
	}
}
