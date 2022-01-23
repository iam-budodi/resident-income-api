package com.income.calculator.service;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import com.income.calculator.filter.AuthenticatedUser;
import com.income.calculator.model.LoanRecord;
import com.income.calculator.model.User;
import com.income.calculator.repository.LoanRecordRepository;
import com.income.calculator.repository.UserRepository;

@Transactional
public class LoanRecordService {

	@Inject
	@AuthenticatedUser
	private User currentUser;

	@Inject
	private UserRepository userRepository;

	@Inject
	private LoanRecordRepository loanRecordRepository;

	public Response addLoanRecord(@NotNull Long userId, @NotNull LoanRecord loan) {
		User user = userRepository.findUserById(userId);
		if (user == null)
			return Response.status(Response.Status.NOT_FOUND).build();

		if (!currentUser.getUserId().equals(user.getUserId())) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		user.addLoanRecord(loan);

		try {
			userRepository.updateUser(user);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
		}

		return Response.status(Response.Status.NO_CONTENT).build();
	}

	public Response loadLoanRecords(@NotNull Long userId) {
		User user = userRepository.findUserById(userId);
		if (user == null)
			return Response.status(Response.Status.BAD_REQUEST).build();

		if (!currentUser.getUserId().equals(user.getUserId())) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		List<LoanRecord> loans = loanRecordRepository.findLoanRecods(userId);
		if (loans.isEmpty())
			return Response.status(Response.Status.NO_CONTENT).entity("No loan found").build();

		return Response.ok(loans).build();
	}

	public Response monthlyDeduction(@NotNull Long userId) {
		User user = userRepository.findUserById(userId);
		if (user == null)
			return Response.status(Response.Status.BAD_REQUEST).build();

		if (!currentUser.getUserId().equals(user.getUserId())) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		Double totalDeduction = loanRecordRepository.monthlyDeduction(userId);
		if (totalDeduction == 0)
			return Response.status(Response.Status.NO_CONTENT).build();

		return Response.ok(totalDeduction).build();
	}

	public Response deleteLoanRecord(@NotNull Long userId, @NotNull Long loanId) {
		User user = userRepository.findUserById(userId);
		if (user == null)
			return Response.status(Response.Status.BAD_REQUEST).build();

		if (!currentUser.getUserId().equals(user.getUserId())) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		LoanRecord loanRecord = user.getLoans().get(loanId.intValue());
		System.out.println("RESPONSE NAM: " + loanRecord.toString());
		user.removeLoanRecord(loanRecord);

		return Response.status(Response.Status.NO_CONTENT).build();
	}
}
