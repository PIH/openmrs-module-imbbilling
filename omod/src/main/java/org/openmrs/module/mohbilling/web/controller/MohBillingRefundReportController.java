package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingRefundReportController extends 	ParameterizableViewController {

	@SuppressWarnings("unchecked")
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date startDate = null;

		String insuranceStr = null, startDateStr = null, endDateStr = null, serviceId = null, cashCollector = null, startHourStr = null, startMinute = null, endHourStr = null, endMinuteStr = null;
	
				/*startHourStr = request.getParameter("startHour");
				startMinute = request.getParameter("startMinute");
				endHourStr = request.getParameter("endHour");
				endMinuteStr = request.getParameter("endMinute");*/
		
		String startTimePar = request.getParameter("startTime");
		String endTimePar = request.getParameter("endTime");


				String startTimeStr = startTimePar + ":00";
				String endTimeStr = endTimePar + ":59";
				Date startDate = null, endDate = null;
				if (request.getParameter("startDate") != null
						&& !request.getParameter("startDate").equals("")) {
					startDateStr = request.getParameter("startDate");
					startDate = sdf.parse(startDateStr.split("/")[2] + "-"
							+ startDateStr.split("/")[1] + "-"
							+ startDateStr.split("/")[0] + " " + startTimeStr);
				}

				if (request.getParameter("endDate") != null
						&& !request.getParameter("endDate").equals("")) {
					endDateStr = request.getParameter("endDate");
					endDate = sdf.parse(endDateStr.split("/")[2] + "-"
							+ endDateStr.split("/")[1] + "-"
							+ endDateStr.split("/")[0] + " " + endTimeStr);
				}

				User collector = null;

				if (request.getParameter("cashCollector") != null
						&& !request.getParameter("cashCollector").equals("")) {
					cashCollector = request.getParameter("cashCollector");
					collector = Context.getUserService().getUser(Integer.parseInt(cashCollector));
				}
				
				if (startDate != null && endDate != null) {
			     
					Set<PatientBill> refundedBills = PatientBillUtil.getRefundedBill(startDate, endDate, collector);
					request.getSession().setAttribute("refundedBills" , refundedBills);
					
					mav.addObject("collector", collector);
					mav.addObject("refundedBills", refundedBills);	
		 
					//Math.abs(x)==>to get an absolute value
					mav.addObject("totalRefundedAmount", Math.abs(ReportsUtil.roundTwoDecimals(ReportsUtil.getTotalRefundedAmount(refundedBills))));

				}

				

		
				if (request.getParameter("printed")!=null) {
					HttpSession session = request.getSession(true);

					Set<PatientBill> billsWithRefunds = (Set<PatientBill>) session.getAttribute("refundedBills");
					
					FileExporter fexp = new FileExporter();
					String fileName = "Refund Report.pdf";

					//System.out.println("ttttttttttttttttttttttttttttttttt "+billsWithRefunds.size());
					fexp.pdfPrintRefundReport(request, response, billsWithRefunds, fileName, fileName);	
				}
	
		mav.setViewName(getViewName());

		return mav;
	}
}
