package com.income.calculator.restController;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.income.calculator.filter.Secured;
import com.income.calculator.model.LoanRecord;
import com.income.calculator.model.UserRole;
import com.income.calculator.service.LoanRecordService;

@Path("/loans")
@RequestScoped
@Secured(permission = {UserRole.USER, UserRole.ADMIN})
public class LoanRecordEndpoint {

	@Inject
	private LoanRecordService loanRecordService;
	
	@GET
	@Path("/{id: \\d+}")
	@Produces(APPLICATION_JSON)
	public Response getAllLoans(@PathParam("id") Long userId) {
		return loanRecordService.loadLoanRecords(userId);
	}
	
	@GET
	@Path("/installment/{id: \\d+}")
	@Produces(TEXT_PLAIN)
	public Response monthlyDeduction(@PathParam("id") Long userId) {
		return loanRecordService.monthlyDeduction(userId);
	}

	@PUT
	@Path("/{id: \\d+}")
	@Consumes(APPLICATION_JSON)
	public Response addLoanRecord(@PathParam("id") Long userId, LoanRecord loan) {
		return loanRecordService.addLoanRecord(userId, loan);
	}
	
	@DELETE
	@Path("/{id: \\d+}/{loan-id: \\d+}")
	@Consumes(APPLICATION_JSON)
	public Response deleteLoanRecord(@PathParam("id") Long userId, @PathParam("loan-id") Long loanId) {
		return loanRecordService.deleteLoanRecord(userId, loanId);
	}
}
