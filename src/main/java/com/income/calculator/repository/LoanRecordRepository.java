package com.income.calculator.repository;

import static javax.transaction.Transactional.TxType.SUPPORTS;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import com.income.calculator.model.LoanRecord;

@Transactional(SUPPORTS)
public class LoanRecordRepository {

	@PersistenceContext(unitName = "incomeCalculatorPU")
	private EntityManager entityManager;

	public List<LoanRecord> findLoanRecods(@NotNull Long userId) {
		return entityManager.createQuery(
				"SELECT loanRecord " + 
				"FROM LoanRecord loanRecord " + 
				"WHERE loanRecord.user.userId = :userId", LoanRecord.class)
			.setParameter("userId", userId)
			.getResultList();

	}
	
	public Double monthlyDeduction(@NotNull Long userId) {
		return entityManager.createQuery(
				"SELECT SUM(loanRecord.equatedMonthlyInstallment) " + 
				"FROM LoanRecord loanRecord " + 
				"WHERE loanRecord.user.userId = :userId", Double.class)
			.setParameter("userId", userId)
			.getSingleResult();

	}
}
