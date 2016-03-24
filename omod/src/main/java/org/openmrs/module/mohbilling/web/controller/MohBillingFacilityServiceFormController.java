/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Yves GAKUBA
 */
@Controller
public class MohBillingFacilityServiceFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping("/module/mohbilling/facilityService.form")
	protected String handleRequestInternal(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		
		model.addAttribute("categories", InsuranceUtil.getAllServiceCategories());
		
		if (request.getParameter("save") != null) {
			boolean saved = handleSaveFacilityService(request);
			if (saved)
				return "redirect:facilityService.list";
		}
		
		if (request.getParameter("retire") != null) {
			boolean retired = handleRetireFacilityService(request);
			if (retired)
				return "redirect:facilityService.list";
		}
		
		if (request.getParameter("facilityServiceId") != null) {
			
			try {
				model.addAttribute("facilityService", (Context.getService(BillingService.class))
				        .getFacilityServicePrice(Integer.valueOf(request.getParameter("facilityServiceId"))));
				
			}
			catch (Exception e) {
				log.error(">>>MOH>>BILLING>> The Facility Service with '" + request.getParameter("facilityServiceId")
				        + "' cannot be found !");
				e.printStackTrace();
				
				return "redirect:facilityService.list";
			}
		}
		if (Context.getLocationService().getDefaultLocation() == null)
			model.addAttribute("locations", Context.getLocationService().getAllLocations());
		else {
			List<Location> locations = new ArrayList<Location>();
			locations.add(Context.getLocationService().getDefaultLocation());
			model.addAttribute("locations", locations);
		}
		
		return "/module/mohbilling/mohBillingFacilityServiceForm";
		
	}
	
	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private boolean handleSaveFacilityService(HttpServletRequest request) {
		
		FacilityServicePrice fs = null;
		FacilityServicePrice oldfs = null;
		
		try {
			// check if the facilityService is NEW or if you are trying to
			// UPDATE an
			// existing facilityService
			if (request.getParameter("facilityServiceId") != null) {
				fs = Context.getService(BillingService.class).getFacilityServicePrice(
				    Integer.valueOf(request.getParameter("facilityServiceId")));
				oldfs = fs;
				
				FacilityServicePrice fspCopy = new FacilityServicePrice();
				
				// keep previews fs info before setting new price
				fspCopy.setName(fs.getName());
				fspCopy.setDescription(fs.getDescription());
				fspCopy.setCategory(fs.getCategory());
				fspCopy.setFullPrice(oldfs.getFullPrice());
				fspCopy.setStartDate(fs.getStartDate());
				fspCopy.setEndDate(new Date());
				fspCopy.setLocation(fs.getLocation());
				fspCopy.setCreatedDate(fs.getCreatedDate());
				fspCopy.setRetired(true);
				fspCopy.setRetiredBy(Context.getAuthenticatedUser());
				fspCopy.setCreator(Context.getAuthenticatedUser());
				fspCopy.setRetiredDate(new Date());
				fspCopy.setRetireReason("price expired");
				FacilityServicePriceUtil.createFacilityService(fspCopy);
				//				FacilityServicePriceUtil.retireFacilityServicePrice(copyOfexistingFsp, new Date(),"Price Expired");
				
				//retire its bs
				Set<BillableService> billableServices = oldfs.getBillableServices();
				for (BillableService bs : billableServices) {
					bs.setRetired(true);
					bs.setRetiredBy(Context.getAuthenticatedUser());
					bs.setRetiredDate(new Date());
				}
				
			} else
				fs = new FacilityServicePrice();
		}
		catch (Exception e) {
			log.error(">>>MOH>>BILLING>> The Facility Service with '" + request.getParameter("facilityServiceId")
			        + "' cannot be found !");
			e.printStackTrace();
			
			return false;
		}
		
		try {
			// facilityService
			fs.setName(request.getParameter("facilityServiceName"));
			fs.setShortName(request.getParameter("facilityServiceShortName"));
			fs.setDescription(request.getParameter("facilityServiceDescription"));
			fs.setCategory(request.getParameter("facilityServiceCategory"));
			fs.setStartDate(Context.getDateFormat().parse(request.getParameter("facilityServiceStartDate")));
			
			// Letting the Full Price to be 0 when not entered.
			if (request.getParameter("facilityServiceFullPrice") != null
			        && !request.getParameter("facilityServiceFullPrice").equals(""))
				fs.setFullPrice(BigDecimal.valueOf(Double.valueOf(request.getParameter("facilityServiceFullPrice"))));
			else
				fs.setFullPrice(new BigDecimal(0));
			
			fs.setLocation(Context.getLocationService().getLocation(
			    Integer.valueOf(request.getParameter("facilityServiceLocation"))));
			fs.setConcept(Context.getConceptService().getConcept(
			    Integer.valueOf(request.getParameter("facilityServiceRelatedConcept"))));
			
			// check if the facilityService is NEW or if you are trying to
			// UPDATE an
			// existing facilityService
			if (null == fs.getFacilityServicePriceId()) {
				fs.setCreatedDate(new Date());
				fs.setCreator(Context.getAuthenticatedUser());
				fs.setRetired(false);
				
				FacilityServicePriceUtil.createFacilityService(fs);
			} else {
				
				FacilityServicePriceUtil.editFacilityService(fs);
				
				// update all related billable service
				FacilityServicePriceUtil.cascadeUpdateFacilityService(fs);
				
			}
			
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
			    "The Facility Service has been saved successfully !");
		}
		catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "The Facility Service has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private boolean handleRetireFacilityService(HttpServletRequest request) {
		
		FacilityServicePrice fs = null;
		
		try {
			fs = Context.getService(BillingService.class).getFacilityServicePrice(
			    Integer.valueOf(request.getParameter("facilityServiceId")));
		}
		catch (Exception e) {
			log.error(">>>MOH>>BILLING>> The Facility Service with '" + request.getParameter("facilityServiceId")
			        + "' cannot be found !");
			e.printStackTrace();
			
			return false;
		}
		
		try {
			// facilityService
			
			fs.setRetiredDate(new Date());
			fs.setRetiredBy(Context.getAuthenticatedUser());
			fs.setRetired(true);
			
			//			getService().saveFacilityServicePrice(fscopy);		
			
			FacilityServicePriceUtil.retireFacilityServicePrice(fs, new Date(),
			    request.getParameter("facilityServiceRetireReason"));
			
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
			    "The Facility Service has been retired successfully !");
		}
		catch (Exception e) {
			request.getSession()
			        .setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "The Facility Service has not been retired !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
}
