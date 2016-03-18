package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.mapping.Array;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.BillStatus;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.Invoice;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;



/**
 * Helper class to support the Patient Bill domain
 * 
 * The parent class in this domain is PatientBill, and the child classes are
 * BillPayment, and PatientServiceBill.
 * 
 * 
 * @author dthomas
 * 
 */
public class PatientBillUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {
		return Context.getService(BillingService.class);
	}

	/**
	 * This should return all Bills corresponding to a given Beneficiary
	 * 
	 * @param beneficiary
	 * @return
	 */
	public static List<PatientBill> getBillsByBeneficiary(
			Beneficiary beneficiary) {

		return getService().getBillsByBeneficiary(beneficiary);
	}

	/**
	 * 
	 * This should return all paid Bills on,before,before a given date or
	 * between two dates (isPaid == true), otherwise it returns unpaid
	 * (isPaid==false)
	 * 
	 * @param isPaid
	 * @param date
	 * @return
	 */
	public static List<PatientBill> getPatientBillsInDates(Date startDate,
			Date endDate) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
			if (startDate != null && endDate == null)
				if (pb.getCreatedDate().compareTo(startDate) >= 0)
					bills.add(pb);
			if (startDate == null && endDate != null)
				if (pb.getCreatedDate().compareTo(endDate) <= 0)
					bills.add(pb);
			if (startDate != null && endDate != null)
				if (pb.getCreatedDate().compareTo(startDate) >= 0
						&& pb.getCreatedDate().compareTo(endDate) <= 0)
					bills.add(pb);
		}
		return bills;
	}

	/**
	 * This should change the PatientBill to printed status (printed == true)
	 * 
	 * @param bill
	 */
	public static void printBill(PatientBill bill) {

		bill.setPrinted(true);
		getService().savePatientBill(bill);
	}


	/**
	 * Creates a PatientBill object and saves it in the DB
	 * 
	 * @param bill
	 *            the PatientBill to be saved
	 * @return bill the PatientBill that has been saved
	 */
	public static PatientBill savePatientBill(PatientBill bill) {

		if (bill != null) {
			getService().savePatientBill(bill);
			return bill;
		}

		return null;
	}


	/**
	 * Creates a BillPayment object and saves it in the DB through PatientBill
	 * which is its parent
	 * 
	 * @param payment
	 *            the BillPayment to be saved
	 * @return payment the BillPayment that has been saved
	 */
	public static BillPayment createBillPayment(BillPayment payment) {

		PatientBill bill = new PatientBill();

		if (payment != null) {
			payment.setVoided(false);
			payment.setCreatedDate(new Date());
			payment.setCreator(Context.getAuthenticatedUser());
			bill = payment.getPatientBill();
			bill.addBillPayment(payment);
			getService().savePatientBill(bill);
			return payment;
		}

		return null;
	}

	/**
	 * Gets the PatientBill by billId
	 * 
	 * @param billId
	 *            the one to be matched
	 * @return the PatientBill that matches the provided billId
	 */
	public static PatientBill getPatientBillById(Integer billId) {

		return getService().getPatientBill(billId);
	}

	/**
	 * Gets Bills by a given Period
	 * 
	 * @param startDate
	 *            the starting period
	 * @param endDate
	 *            the ending period
	 * @return bills as a list of all matched bills
	 */
	public static List<PatientBill> getBillsByPeriod(Date startDate,
			Date endDate) {

		List<PatientBill> bills = null;

		if (startDate != null && endDate != null) {

			bills = new ArrayList<PatientBill>();

			for (PatientBill bill : getService().getAllPatientBills()) {
				if (bill.getCreatedDate().compareTo(startDate) >= 0
						&& bill.getCreatedDate().compareTo(endDate) <= 0) {
					bills.add(bill);
				}
			}
		}

		return bills;
	}
	
	public static PatientBill getPatientBill(Patient patient,Date startDate,
			Date endDate) {

		PatientBill pb = null;

		if (startDate != null && endDate != null) {
			for (PatientBill bill : getPatientBillsInDates(startDate, endDate)) {
				if (bill.getBeneficiary().getPatient()==patient && bill.getCreatedDate().compareTo(startDate) >= 0	&& bill.getCreatedDate().compareTo(endDate) <= 0) {
					pb=bill;
				}
			}
		}

		return pb;
	}
	
	public static void markBillAsPaid(PatientBill bill) {

		PatientBill pb = getService().getPatientBill(bill.getPatientBillId());
		double amountNotPaid = 0d;
		double amountPaid = pb.getAmountPaid().doubleValue();
		double insuranceRate = pb.getBeneficiary().getInsurancePolicy()
				.getInsurance().getCurrentRate().getRate();
		double patientRate = (100f - insuranceRate) / 100f;
		double amountDueByPatient = (pb.getAmount().doubleValue() * patientRate);

		if (pb.getBeneficiary().getInsurancePolicy().getThirdParty() == null)
			
			amountNotPaid = amountDueByPatient - amountPaid;
		else {

			double thirdPartRate = pb.getBeneficiary().getInsurancePolicy()
					.getThirdParty().getRate().doubleValue();

			double amountPaidByThirdPart = pb.getAmount().doubleValue()
					* (thirdPartRate / 100);

			amountNotPaid = amountDueByPatient
					- (amountPaidByThirdPart + amountPaid);
			amountDueByPatient -= amountPaidByThirdPart;

		}

		double rest = amountPaid-amountDueByPatient;
		System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmrest "+rest);
		/** Marking the BILL as FULLY PAID */
		if (amountPaid >= amountDueByPatient || rest <=1) {
			pb.setIsPaid(true);
			pb.setStatus(BillStatus.FULLY_PAID.getDescription());
		}

		/** Marking the BILL as NOT PAID at all */
		if ( amountNotPaid == amountDueByPatient){
			pb.setStatus(BillStatus.UNPAID.getDescription());
		}
		/** Marking the BILL as PARTLY PAID */
		if (amountNotPaid > 1d && amountNotPaid < amountDueByPatient)
			pb.setStatus(BillStatus.PARTLY_PAID.getDescription());
		
		//System.out.println("llllllllllllllllllllllllllllllllllllRest "+amountNotPaid+"statussssssssss "+pb.getStatus());
		getService().savePatientBill(pb);
	}
	
	public static PatientInvoice getPatientInvoice(PatientBill pb,Insurance insurance ){
		
		Set<PatientServiceBill> billItems =pb.getBillItems(); 
		
		LinkedHashMap< String, Invoice> invoiceMap = new LinkedHashMap<String,Invoice>();
		Double currentRate = pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate().doubleValue();	
		LinkedHashMap< String, Double> categGroupedMap = new LinkedHashMap<String,Double>();
		
		LinkedHashMap<String,List<String>> map =getRecoveryCategiesMap();
		Double gdTotal =0.0;	
	for (String categGrouped : map.keySet()) {
		List<String > serviceCategories =map.get(categGrouped);
		
		Double total =0.0;	
		List<Consommation> consommations =new ArrayList<Consommation>();
		Invoice invoice = new Invoice();
		for (String sviceCatgory : serviceCategories) {			
			
			
			Double subTotal =0.0;
			
			for (PatientServiceBill item : billItems) {		
				String category =item.getService().getFacilityServicePrice().getCategory();
				
				if (category.startsWith(sviceCatgory)) {					
					Consommation consomm = new Consommation();
					// Double  quantity = (Double)item.getQuantity();
					
					String libelle = item.getService().getFacilityServicePrice().getName();	
					consomm.setRecordDate(item.getServiceDate());
					consomm.setLibelle(libelle);
					consomm.setUnitCost(item.getUnitPrice().doubleValue());
					consomm.setQuantity(item.getQuantity());
					consomm.setCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue());
					consomm.setInsuranceCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue()*currentRate/100);
					consomm.setPatientCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue()*(100-currentRate)/100);						
					consommations.add(consomm);					
					//Double unitPrice=item.getUnitPrice().doubleValue();
					//Double cost =item.getQuantity()*item.getUnitPrice().doubleValue()*currentRate/100;	
					Double cost =item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue();
					subTotal+=cost;	
					
				}	
			}
			
			invoice.setCreatedDate(pb.getCreatedDate());
			invoice.setConsommationList(consommations);			
			total+=subTotal;            
			}
		invoice.setSubTotal(ReportsUtil.roundTwoDecimals(total));
		//if(invoice.getSubTotal()!=0)

		//get all service categories
		if(!categGrouped.equals("AUTRES"))
		invoiceMap.put(categGrouped, invoice);
		
		//filter ambulance amounts from formalite
		else{
			List<Consommation> autresConso = invoice.getConsommationList();
			List<Consommation> ambulanceConsom = new ArrayList<Consommation>();
			List<Consommation> formaliteConsom=new ArrayList<Consommation>();
			Invoice ambulanceInvoice  = new Invoice();
			Invoice formaliteInvoice = new Invoice();
			Double subTotAmbul = 0.0,subTotalFormalites=0.0;
			for (Consommation c : autresConso) {
				if(c.getLibelle().startsWith("Ambul")){
					ambulanceConsom.add(c);
					subTotAmbul+=c.getCost();
				}
				else{
					formaliteConsom.add(c);
					subTotalFormalites+=c.getCost();
				}
			}
			ambulanceInvoice.setConsommationList(ambulanceConsom);
			ambulanceInvoice.setCreatedDate(pb.getCreatedDate());
			ambulanceInvoice.setSubTotal(subTotAmbul);
			
			formaliteInvoice.setConsommationList(formaliteConsom);
			formaliteInvoice.setCreatedDate(pb.getCreatedDate());
			formaliteInvoice.setSubTotal(subTotalFormalites);
			
			invoiceMap.put("AMBULANCE", ambulanceInvoice);
			invoiceMap.put("AUTRES", formaliteInvoice);
		}
	
		
		
		gdTotal+=total;		
		
		//categGroupedMap.put(categGrouped, ReportsUtil.roundTwoDecimals(total));		
	
	}
	//create patient invoice  to  be displayed on the interface

	PatientInvoice patientInvoice = new PatientInvoice();
	
	patientInvoice.setPatientBill(pb);
	patientInvoice.setInvoiceMap(invoiceMap);
	patientInvoice.setTotalAmount( ReportsUtil.roundTwoDecimals(gdTotal));
	patientInvoice.setPatientCost(ReportsUtil.roundTwoDecimals(gdTotal*(100-currentRate)/100));
	patientInvoice.setInsuranceCost( ReportsUtil.roundTwoDecimals(gdTotal*currentRate/100));
	
	return  patientInvoice;
		
	}
	
 public static LinkedHashMap<String,List<String>> getRecoveryCategiesMap(){
	LinkedHashMap<String,List<String>> map = new LinkedHashMap<String, List<String>>();
	List<String> consult = Arrays.asList("CONSULTATION");
	List<String> labo = Arrays.asList("LABORATOIRE");
	List<String> imagery = Arrays.asList("ECHOGRAPHIE", "RADIOLOGIE");
	List<String> medicActs = Arrays.asList("STOMATOLOGIE", "CHIRURGIE","SOINS INTENSIFS","GYNECO - OBSTETRIQUE","ORL","DERMATOLOGIE", "SOINS INFIRMIERS","MATERNITE","OPHTALMOLOGIE","KINESITHERAPIE","MEDECINE INTERNE","NEUROLOGIE");
	List<String> medic = Arrays.asList("MEDICAMENTS");
	List<String> consommables = Arrays.asList("CONSOMMABLES");
	List<String> ambul = Arrays.asList("AMBULANCE");
	List<String> autres = Arrays.asList("FORMALITES ADMINISTRATIVES","OXYGENOTHERAPIE");
	List<String> hosp = Arrays.asList("HOSPITALISATION");

	map.put("CONSULTATION", consult);
	map.put("LABORATOIRE", labo);
	map.put("IMAGERIE", imagery);
	map.put("ACTS", medicActs);
	map.put("MEDICAMENTS", medic);
	map.put("CONSOMMABLES", consommables);
	map.put("AMBULANCE", ambul);
	map.put("AUTRES", autres);
	map.put("HOSPITALISATION", hosp);
	return map;
}

 public static Set<PatientBill> getRefundedBill(Date startDate, Date endDate, User collector){
	return  getService().getRefundedBills(startDate, endDate, collector);
}
}
