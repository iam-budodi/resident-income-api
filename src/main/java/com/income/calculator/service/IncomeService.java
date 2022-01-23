package com.income.calculator.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.income.calculator.commonMath.MathEngine;
import com.income.calculator.model.Income;
import com.income.calculator.repository.IncomeRepository;

public class IncomeService {

	@Inject
	private IncomeRepository incomeRepository;

	@Inject
	private MathEngine mathEngine;

	public Response findIncomes() {
		List<Income> taxableIncomes = incomeRepository.findAllIncomes();

		if (taxableIncomes.isEmpty())
			return Response.status(Response.Status.NO_CONTENT).build();

		return Response.ok(taxableIncomes).build();
	}

	public Response findIncomeById(@NotNull Long incomeId) {
		Income taxableIncome = findIncome(incomeId);
		if (taxableIncome == null)
			return Response.status(Response.Status.NOT_FOUND).build();

		return Response.ok(taxableIncome).build();
	}

	public Response createIncome(@NotNull Income income, @Context() UriInfo uriInfo) {
		try {
			income = incomeRepository.createIncome(income);
		} catch (Exception rollbackException) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		String incomeId = String.valueOf(income.getId());

		URI createdUri = uriInfo.getAbsolutePathBuilder().path(incomeId).build();
		return Response.created(createdUri).build();
	}

	public Response deduceEarnedIncome(@NotNull double earnedIncome, @NotNull boolean heslb) {
		Income retrievedIncome;
		double heslbDeduction = 0;

		Map<String, Double> resultsMap = new HashMap<>();
		//		Map<String, Map<String, Double>> formartedMap = new HashMap<>();

		double socialSecurityFund = mathEngine.computeTenPercent(earnedIncome);
		double taxableAmount = earnedIncome - socialSecurityFund;

		try {
			retrievedIncome = incomeRepository.findEarnedIncome(Math.round(taxableAmount));

		} catch (NoResultException nre) {
			retrievedIncome = null;
		}

		if (retrievedIncome == null) {
			return Response.status(Response.Status.NOT_FOUND).build();

		}

		double paye = mathEngine.computePaye(retrievedIncome, taxableAmount);

		// TODO: check how you can handle hlsb smoothly
		if (heslb) {
			heslbDeduction += mathEngine.computeFifteenPercent(earnedIncome);
		}

		double incomeRemain = earnedIncome - (socialSecurityFund + paye + heslbDeduction);

		resultsMap.put("nssf", socialSecurityFund);
		resultsMap.put("taxableAmount", taxableAmount);
		resultsMap.put("paye", paye);
		resultsMap.put("heslbDeduction", heslbDeduction);
		resultsMap.put("takeHome", incomeRemain);
		resultsMap.put("individualClassLimit", Double.valueOf(retrievedIncome.getIncomeLimit()));

		//		formartedMap.put("results", resultsMap);
		System.out.println("\n\n#### INCOME FNL " + resultsMap.toString());

		return Response.ok(resultsMap).build();
	}

	public Response deleteIncome(@NotNull Long incomeId) {
		Income taxableIncome = findIncome(incomeId);
		if (taxableIncome == null)
			return Response.status(Response.Status.NOT_FOUND).build();

		incomeRepository.deleteIncome(incomeId);

		return Response.status(Response.Status.NO_CONTENT).build();
	}

	public Response updateIncome(@NotNull Long incomeId, @NotNull Income income) {
		if (!incomeId.equals(income.getId())) {
			return Response.status(Response.Status.CONFLICT).build();
		}

		if (findIncome(incomeId) == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		try {
			incomeRepository.updateIncome(income);
		} catch (OptimisticLockException ex) {
			return Response.status(Response.Status.CONFLICT).entity(ex.getEntity()).build();
		}

		return Response.status(Response.Status.NO_CONTENT).build();
	}

	// ===========================================
	// Put the method in a common package
	// ==========================================

	public Income findIncome(Long incomeId) {
		return incomeRepository.findIncomeById(incomeId);

	}
}
