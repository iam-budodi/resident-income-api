package com.income.calculator.repository;

import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.SUPPORTS;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import com.income.calculator.model.Income;

@Transactional(SUPPORTS)
public class IncomeRepository {

	@PersistenceContext(unitName = "incomeCalculatorPU")
	private EntityManager entityManager;

	public List<Income> findAllIncomes() {
		return entityManager.createQuery("SELECT income FROM Income income", Income.class).getResultList();
	}

	public Income findEarnedIncome(@NotNull Long earnedIncome) {
		String statementString = "SELECT income FROM Income income "
				+ "WHERE :earnedIncome BETWEEN income.income AND income.incomeLimit "
				+ "AND :earnedIncome != income.income";

		TypedQuery<Income> query = entityManager.createQuery(statementString, Income.class);
		query.setParameter("earnedIncome", earnedIncome);

		return query.getSingleResult();
	}

	public Income findIncomeById(@NotNull Long incomeId) {
		return entityManager.find(Income.class, incomeId);
	}

	@Transactional(REQUIRED)
	public Income createIncome(@NotNull Income income) {
		entityManager.persist(income);
		return income;
	}

	@Transactional(REQUIRED)
	public Income updateIncome(@NotNull Income income) {
		return entityManager.merge(income);
		// return income;
	}

	@Transactional(REQUIRED)
	public void deleteIncome(@NotNull Long incomeId) {
		Income incomeReference = entityManager.getReference(Income.class, incomeId);
		entityManager.remove(incomeReference);

	}

}
