/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.BillStatus;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingPatientBillPaymentFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null) {
			handleSavePatientBillPayment(request);
		}

		try {
			PatientBill pb = null;
			List<PatientBill> patientBills = null;

			if (request.getParameter("patientBillId") == null)
				return new ModelAndView(new RedirectView(
						"patientSearchBill.form"));

			pb = Context.getService(BillingService.class).getPatientBill(
					Integer.parseInt(request.getParameter("patientBillId")));
			
			patientBills = PatientBillUtil.getBillsByBeneficiary(pb
					.getBeneficiary());
			
			/** Updating status for unmarked ones */
			for(PatientBill bill: patientBills)
				if(bill.getStatus() == null)
					PatientBillUtil.markBillAsPaid(bill);

			mav.addObject("patientBill", pb);
			mav.addObject("patientBills", patientBills);
			mav.addObject("beneficiary", pb.getBeneficiary());

			InsurancePolicy ip = pb.getBeneficiary().getInsurancePolicy();
			mav.addObject("insurancePolicy", ip);

			// check the validity of the insurancePolicy for today
			Date today = new Date();
			mav.addObject(
					"valid",
					((ip.getCoverageStartDate().getTime() <= today.getTime()) && (today
							.getTime() <= ip.getExpirationDate().getTime())));
			mav.addObject("todayDate", today);
			mav.addObject("authUser", Context.getAuthenticatedUser());

		} catch (Exception e) {
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			return new ModelAndView(new RedirectView("patientSearchBill.form"));
		}

		return mav;
	}

	/**
	 * @param request
	 * @return
	 */
	private BillPayment handleSavePatientBillPayment(HttpServletRequest request) {

		BillPayment billPayment = null;
		// Float rate = null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			PatientBill pb = PatientBillUtil.getPatientBillById(Integer
					.parseInt(request.getParameter("patientBillId")));

			// BigDecimal amountPaidByThirdPart = new BigDecimal(0);

			if (null != request.getParameter("receivedCash")) {
				BillPayment bp = new BillPayment();
				/**
				 * We need to add both Patient Due amount and amount paid by
				 * third part
				 */

				// if (pb.getBeneficiary().getInsurancePolicy().getThirdParty()
				// != null) {
				// rate = pb.getBeneficiary().getInsurancePolicy()
				// .getThirdParty().getRate();
				// if (rate != null)// to avoid NullPointerException when this
				// // is
				// // null...
				// amountPaidByThirdPart = pb.getAmount()
				// .multiply(BigDecimal.valueOf(rate))
				// .divide(new BigDecimal(100));
				//
				// BigDecimal patientAmount = BigDecimal.valueOf(Double
				// .parseDouble(request.getParameter("receivedCash")));
				//
				// bp.setAmountPaid(patientAmount.add(amountPaidByThirdPart));
				// } else
				// We don't need to add anything as the patient will be
				// paying...

//				if(Integer.parseInt(request.getParameter("receivedCash"))>0)
				bp.setAmountPaid(BigDecimal.valueOf(Double.parseDouble(request
						.getParameter("receivedCash"))));
					
				
//				List<BillPayment> pay = ReportsUtil.getDailyPayments(bp.getDateReceived());
//				for (BillPayment p : pay) {
//					log.info("rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr "+p.getAmountPaid());
//				}

				bp.setCollector(Context.getUserService()
						.getUser(
								Integer.parseInt(request
										.getParameter("billCollector"))));
				bp.setDateReceived(Context.getDateFormat().parse(
						request.getParameter("dateBillReceived")));
				
//				log.info("dddddddddddddddddddddddddate "+Context.getDateFormat().parse(
//						request.getParameter("dateBillReceived")));
				
				bp.setPatientBill(pb);

				bp.setCreatedDate(new Date());
				bp.setCreator(Context.getAuthenticatedUser());

				billPayment = PatientBillUtil.createBillPayment(bp);

				/** Marking a Bill as PAID */
				PatientBillUtil.markBillAsPaid(pb);

				request.getSession().setAttribute(
						WebConstants.OPENMRS_MSG_ATTR,
						"The Bill Payment has been saved successfully !");

				return billPayment;

			} else {
				request.getSession()
						.setAttribute(
								WebConstants.OPENMRS_MSG_ATTR,
								"The Bill Payment cannot be saved when the 'Received Amount' is BLANK or is < 0 !");
				return null;
			}

		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The Bill Payment has not been saved !");
			log.error("" + e.getMessage());
			e.printStackTrace();

			return null;
		}

	}

}
