package com.income.calculator.restController;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.income.calculator.filter.Secured;
import com.income.calculator.model.Income;
import com.income.calculator.model.UserRole;
import com.income.calculator.service.IncomeService;

@Path("/incomes")
public class IncomeEndpoint {

	@Inject
	IncomeService incomeService;

//	@GET
//	@Produces(APPLICATION_JSON)
//	public Response findIncomes(@QueryParam("income") double income,
//			@DefaultValue("false") @QueryParam("heslb") boolean heslb) {
//		return incomeService.findIncomes(income, heslb);
//	}
	
	@GET
	@Produces(APPLICATION_JSON)
	public Response findIncomes() {
		return incomeService.findIncomes();
	}

	@GET
	@Path("{id: \\d+}")
	@Produces(APPLICATION_JSON)
	public Response getIncomeById(@PathParam("id") @Min(1) Long incomeId) {
		return incomeService.findIncomeById(incomeId);
	}
	
	@POST
	@Path("/compute")
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response deduceEarnedIncome(@FormParam("income") double income,
			@DefaultValue("false") @FormParam("heslb") boolean heslb) {
		return incomeService.deduceEarnedIncome(income, heslb);
	}

	@POST
	@Consumes(APPLICATION_JSON)
	@Secured(permission = UserRole.ADMIN)
	public Response createIncome(Income income, @Context() UriInfo uriInfo) {
		return incomeService.createIncome(income, uriInfo);
	}
 
	@PUT
	@Path("{id: \\d+}")
	@Consumes(APPLICATION_JSON)
	@Secured(permission = UserRole.ADMIN)
	public Response updateIncome(@PathParam("id") @Min(1) Long incomeId, Income income) {
		System.out.println("##### INCOME : " + income.toString());
		return incomeService.updateIncome(incomeId, income);
	}

	@DELETE
	@Path("{id: \\d+}")
	@Secured(permission = UserRole.ADMIN)
	public Response deleteIncome(@PathParam("id") @Min(1) Long incomeId) {
		return incomeService.deleteIncome(incomeId);
	}

}
