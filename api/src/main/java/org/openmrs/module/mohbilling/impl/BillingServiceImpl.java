
/**
 * 
 */
package org.openmrs.module.mohbilling.impl;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kamonyo
 * 
 */
@Transactional
public class BillingServiceImpl implements BillingService {

	private BillingDAO billingDAO;

	/**
	 * @param billingDAO the billingDAO to set
	 */
	public void setBillingDAO(BillingDAO billingDAO) {
		this.billingDAO = billingDAO;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#getInsurance(Integer)
	 */
	@Override
	public Insurance getInsurance(Integer insuranceId) throws DAOException {
		return billingDAO.getInsurance(insuranceId);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#getInsurancePolicy(Integer)
	 */
	@Override
	public InsurancePolicy getInsurancePolicy(Integer insurancePolicyId) throws DAOException {
		return billingDAO.getInsurancePolicy(insurancePolicyId);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#getPatientBill(Integer)
	 */
	@Override
	public PatientBill getPatientBill(Integer billId) throws DAOException {
		return billingDAO.getPatientBill(billId);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#saveInsurance(Insurance)
	 */
	@Override
	public void saveInsurance(Insurance insurance) {
		if (insurance.getName() == null) {
			throw new APIException("Insurance name is required");
		}
		billingDAO.saveInsurance(insurance);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#saveInsurancePolicy(InsurancePolicy)
	 */
	@Override
	public void saveInsurancePolicy(InsurancePolicy card) {
		if (card.getInsuranceCardNo() == null && !card.getInsurance().getCategory().equals(InsuranceCategory.NONE.toString())) {
			throw new APIException("Insurance Card Number is required");
		}
        else {
            billingDAO.saveInsurancePolicy(card);
        }
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#savePatientBill(PatientBill)
	 */
	@Override
	public void savePatientBill(PatientBill bill) {
		billingDAO.savePatientBill(bill);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#getFacilityServicePrice(Integer)
	 */
	@Override
	public FacilityServicePrice getFacilityServicePrice(Integer id) {
		return billingDAO.getFacilityServicePrice(id);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see BillingService#saveFacilityServicePrice(FacilityServicePrice)
	 */
	@Override
	public void saveFacilityServicePrice(FacilityServicePrice fsp) {
		if (fsp.getName() == null) {
			throw new APIException("Facility Service name is required");
		}
		billingDAO.saveFacilityServicePrice(fsp);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#getAllInsurancePolicies()
	 */
	@Override
	public List<InsurancePolicy> getAllInsurancePolicies() {
		return billingDAO.getAllInsurancePolicies();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see BillingService#getAllInsurances()
	 */
	@Override
	public List<Insurance> getAllInsurances() throws DAOException {
		return billingDAO.getAllInsurances();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see BillingService#getAllPatientBills()
	 */
	@Override
	public List<PatientBill> getAllPatientBills() throws DAOException {
		return billingDAO.getAllPatientBills();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see BillingService#getAllFacilityServicePrices()
	 */
	@Override
	public List<FacilityServicePrice> getAllFacilityServicePrices() throws DAOException {
		return billingDAO.getAllFacilityServicePrices();
	}

	@Override
	public List<ServiceCategory> getAllServiceCategories() throws DAOException {
		return billingDAO.getAllServiceCategories();
	}

	@Override
	public List<BillableService> getAllBillableServices() {
		return billingDAO.getAllBillableServices();
	}

	@Override
	public void saveRecovery(Recovery recovery) {
		billingDAO.saveRecovery(recovery);
	}

	public Float getPaidAmountPerInsuranceAndPeriod(Insurance insurance, Date startDate, Date endDate) {
		return billingDAO.getPaidAmountPerInsuranceAndPeriod(insurance, startDate, endDate);
	}

	public List<Recovery> getAllPaidAmountPerInsuranceAndPeriod(Insurance insurance, Date startDate, Date endDate) {
		return billingDAO.getAllPaidAmountPerInsuranceAndPeriod(insurance, startDate, endDate);
	}

	/**
	 * @see BillingService#getInsurancePolicyByCardNo(String)
	 */
	@Override
	public InsurancePolicy getInsurancePolicyByCardNo(String insuranceCardNo) {
		return billingDAO.getInsurancePolicyByCardNo(insuranceCardNo);
	}

	@Override
	public List<PatientBill> billCohortBuilder(Insurance insurance, Date startDate, Date endDate, Integer patientId, String serviceName, String billStatus, String billCollector) {
		return billingDAO.billCohortBuilder(insurance, startDate, endDate, patientId, serviceName, billStatus, billCollector);
	}

	@Override
	public BillableService getBillableServiceByConcept(FacilityServicePrice price, Insurance insurance) {
		return billingDAO.getBillableServiceByConcept(price, insurance);
	}

	/**
	 * @see BillingService#getThirdParty(Integer)
	 */
	@Override
	public ThirdParty getThirdParty(Integer thirdPartyId) throws DAOException {
		return billingDAO.getThirdParty(thirdPartyId);
	}

	/**
	 * @see BillingService#getAllThirdParties()
	 */
	@Override
	public List<ThirdParty> getAllThirdParties() {
		return billingDAO.getAllThirdParties();
	}

	/**
	 * @see BillingService#saveThirdParty(ThirdParty)
	 */
	@Override
	public void saveThirdParty(ThirdParty thirdParty) throws DAOException {
		billingDAO.saveThirdParty(thirdParty);
	}

	/**
	 * @see BillingService#getAllRecoveries()
	 */
	@Override
	public List<Recovery> getAllRecoveries() throws DAOException {
		return billingDAO.getAllRecoveries();
	}

	/**
	 * @see BillingService#getRecovery(Integer)
	 */
	@Override
	public Recovery getRecovery(Integer recoveryId) throws DAOException {
		return billingDAO.getRecovery(recoveryId);
	}

	/**
	 * @see BillingService#getBeneficiaryByPolicyNumber(String)
	 */
	@Override
	public Beneficiary getBeneficiaryByPolicyNumber(String policyIdNumber) throws DAOException {
		return billingDAO.getBeneficiaryByPolicyNumber(policyIdNumber);
	}

	/**
	 * @see BillingService#getBillsByBeneficiary(Beneficiary)
	 */
	@Override
	public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary) {
		return billingDAO.getBillsByBeneficiary(beneficiary);
	}

	/**
	 * @see BillingService#getInsurancePolicyByBeneficiary(Beneficiary)
	 */
	@Override
	public InsurancePolicy getInsurancePolicyByBeneficiary(Beneficiary beneficiary) {
		return billingDAO.getInsurancePolicyByBeneficiary(beneficiary);
	}

	/**
	 * @see BillingService#getBillableService(Integer)
	 */
	@Override
	public BillableService getBillableService(Integer id) {
		return billingDAO.getBillableService(id);
	}

	/**
	 * @see BillingService#getServiceCategory(Integer)
	 */
	@Override
	public ServiceCategory getServiceCategory(Integer id) {
		return billingDAO.getServiceCategory(id);
	}

	/**
	 * @see BillingService#getBillableServiceByCategory(ServiceCategory)
	 */
	@Override
	public List<BillableService> getBillableServiceByCategory(ServiceCategory sc) {
		return billingDAO.getBillableServiceByCategory(sc);
	}

	/**
	 * @see BillingService#getFacilityServiceByConcept(org.openmrs.Concept)
	 */
	@Override
	public FacilityServicePrice getFacilityServiceByConcept(Concept concept) {

		return billingDAO.getFacilityServiceByConcept(concept);
	}

	/**
	 * @see BillingService#getBillableServicesByFacilityService(FacilityServicePrice)
	 */
	@Override
	public List<BillableService> getBillableServicesByFacilityService(FacilityServicePrice fsp) {
		return billingDAO.getBillableServicesByFacilityService(fsp);
	}

	/**
	 * @see BillingService#getBillableServicesByInsurance(Insurance)
	 */
	@Override
	public List<BillableService> getBillableServicesByInsurance(Insurance insurance) {
		return billingDAO.getBillableServicesByInsurance(insurance);
	}

	/**
	 * @see BillingService#getPolicyIdByPatient(Integer)
	 */
	@Override
	public List<String[]> getPolicyIdByPatient(Integer patientId) {
		return billingDAO.getPolicyIdByPatient(patientId);
	}

	@Override
	public List<BillPayment> getAllBillPayments(){
		return billingDAO.getAllBillPayments();
	}

	@Override
	public List<BillPayment> getBillPaymentsByDateAndCollector(Date createdDate,Date endDate, User collector) {
		return billingDAO.getBillPaymentsByDateAndCollector(createdDate,endDate,collector);
	}

	@Override
	public List<BillPayment> paymentsCohortBuilder(Insurance insurance, Date startDate, Date endDate, Integer patientId, String serviceName, String billStatus, String billCollector) {
		return billingDAO.paymentsCohortBuilder(insurance, startDate, endDate, patientId, serviceName, billStatus, billCollector);
	}

	@Override
	public ServiceCategory getServiceCategoryByName(String name, Insurance insurance) {
		return billingDAO.getServiceCategoryByName(name, insurance);
	}

	@Override
	public List<Date> getRevenueDatesBetweenDates(Date startDate, Date endDate){
		return billingDAO.getRevenueDatesBetweenDates(startDate,endDate);
	}


	@Override
	public Object[] getBills(Date startDate,Date endDate,User collector) {
		return billingDAO.getBills(startDate,endDate,collector);
	}

	@Override
	public Map<String,Double> getRevenueByService(Date receivedDate, String[] serviceCategory, String collector, Insurance insurance) {
		return billingDAO.getRevenueByService(receivedDate, serviceCategory, collector, insurance);
	}

	@Override
	public List<PatientBill> getPatientBillsByCollector(Date receivedDate, User collector) {
		return billingDAO.getPatientBillsByCollector(receivedDate,collector);
    }

	@Override
	public PatientBill getBills(Patient patient, Date startDate, Date endDate) {
		return billingDAO.getBills(patient, startDate, endDate);
	}

	@Override
	public InsuranceRate getInsuranceRateByInsurance(Insurance insurance) {
		return billingDAO.getInsuranceRateByInsurance(insurance);
	}

	@Override
	public List<Beneficiary> getBeneficiaryByCardNumber(String cardNo) {
		return billingDAO.getBeneficiaryByCardNumber(cardNo);
	}

	@Override
	public List<InsurancePolicy> getInsurancePoliciesBetweenTwodates(Date startDate, Date endDate) {
		return billingDAO.getInsurancePoliciesBetweenTwodates(startDate,endDate);
	}

	@Override
	public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary, Date startDate, Date endDate) {
		return billingDAO.getBillsByBeneficiary(beneficiary, startDate, endDate);
	}

	@Override
	public void loadBillables(Insurance insurance) {
		 billingDAO.loadBillables(insurance);
	}

	@Override
	public List<Object[]> getBaseBillableServices(Insurance i) {
		return billingDAO.getBaseBillableServices(i);
	}

	@Override
	public List<Object[]> getPharmacyBaseBillableServices(Insurance i) {
		return billingDAO.getPharmacyBaseBillableServices(i);
	}

	@Override
	public List<PatientBill> getPendingBill() {
		return billingDAO.getPendingBill();
	}

	@Override
	public Set<PatientBill> getRefundedBills(Date startDate, Date endDate, User collector) {
		return billingDAO.getRefundedBills(startDate,endDate,collector);
	}
}
