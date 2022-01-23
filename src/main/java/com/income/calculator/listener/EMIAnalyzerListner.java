package com.income.calculator.listener;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import com.income.calculator.model.LoanRecord;

public class EMIAnalyzerListner {
	private static final int YEAR_MONTHS = 12;
	
	@PostUpdate
	@PostPersist
	@PostLoad
	public void analysisEMICalculations(LoanRecord loanRecord) {
		if (loanRecord.getLoanInterestRate() == 0) {
			loanRecord.setTotalLoanWithInterest(Double.valueOf(loanRecord.getLoanPrincipal()));
		}
		
		loanRecord.setTotalLoanWithInterest(totalOutstanding(loanRecord));
		loanRecord.setTotalInterest(totalInterest(loanRecord));
		loanRecord.setYearlyInterest(annualInterest(loanRecord));
	}
	
	private double annualInterest(LoanRecord loanRecord) {
		return totalInterest(loanRecord) / loanRecord.getLoanTenor();
	}
	
	private double totalInterest(LoanRecord loanRecord) {
		return totalOutstanding(loanRecord) - loanRecord.getLoanPrincipal();
	}
	
	private double totalOutstanding(LoanRecord loanRecord) {
		return loanRecord.getEquatedMonthlyInstallment() * loanRecord.getLoanTenor() * YEAR_MONTHS;
	}

}
