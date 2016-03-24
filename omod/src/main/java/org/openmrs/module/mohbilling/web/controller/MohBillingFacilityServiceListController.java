/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Yves GAKUBA
 */
@Controller
public class MohBillingFacilityServiceListController {
	
	@RequestMapping("/module/mohbilling/facilityService.list")
	protected String handleRequestInternal(ModelMap model,
	                                       @RequestParam(required = false, value = "facilityServiceId") String facilityServiceId,
	                                       @RequestParam(required = false, value = "addCategoryToFacility") String addCategoryToFacility,
	                                       HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		if (facilityServiceId != null)
			if (!facilityServiceId.equals(""))
				FacilityServicePriceUtil.addCategoryToFacilityService(FacilityServicePriceUtil
				        .getFacilityServicePrice(Integer.parseInt(request.getParameter("facilityServiceId"))));
		
		if (addCategoryToFacility != null)
			if (addCategoryToFacility.equals("UPDATE"))
				if (FacilityServicePriceUtil.addCategoryToAllFacilityServices(InsuranceCategory.BASE.toString()))
					
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					    "The Facility Service Categories have been added successfully !");
				else
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "DID NOT Update AT ALL !");
		
		model.addAttribute("facilityServices", FacilityServicePriceUtil.getFacilityServices(true));
		
		return "/module/mohbilling/mohBillingFacilityServiceList";
		
	}
}
