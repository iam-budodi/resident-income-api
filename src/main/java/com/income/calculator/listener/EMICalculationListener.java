package com.income.calculator.listener;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.income.calculator.model.LoanRecord;

public class EMICalculationListener {
	
	private static final int YEAR_MONTHS = 12;
	private static final double RATE_PER_YEAR = 0.0008333333; // deduced from 1 / (12 * 100)

	@PreUpdate
	@PrePersist
	public void calculateEMI(LoanRecord loanRecord) {
		if (loanRecord.getLoanInterestRate() == 0) {
			double installment = loanRecord.getLoanPrincipal() / yearTenor(loanRecord);
			loanRecord.setEquatedMonthlyInstallment(installment);
			return;
		}

		double emi = EMINumerator(loanRecord) / EMIDenominator(loanRecord);
		loanRecord.setEquatedMonthlyInstallment(emi);
	}

	private double EMINumerator(LoanRecord loanRecord) {
		return loanRecord.getLoanPrincipal() * interestRate(loanRecord) * ratePowerOfYearTenor(loanRecord);
	}

	private double EMIDenominator(LoanRecord loanRecord) {
		return ratePowerOfYearTenor(loanRecord) - 1;
	}

	private double ratePowerOfYearTenor(LoanRecord loanRecord) {
		double onePlusRate = 1 + interestRate(loanRecord);
		double tenor = yearTenor(loanRecord);
		return Math.pow(onePlusRate, tenor);
	}

	private double interestRate(LoanRecord loanRecord) {
		return loanRecord.getLoanInterestRate() * RATE_PER_YEAR;
	}

	private double yearTenor(LoanRecord loanRecord) {
		return loanRecord.getLoanTenor() * YEAR_MONTHS;
	}

}
