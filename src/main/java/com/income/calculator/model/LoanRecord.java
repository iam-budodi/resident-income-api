package com.income.calculator.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.income.calculator.listener.EMIAnalyzerListner;
import com.income.calculator.listener.EMICalculationListener;

@Entity
@Table(name = "loan_record")
@EntityListeners({ EMICalculationListener.class, EMIAnalyzerListner.class })
public class LoanRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "loan_id")
	private Long loanId;

	@Column(length = 60)
	@Size(min = 1, max = 60)
	private String institution;

	@NotNull
	@Column(name = "loan_type")
	private String loanType;

	@NotNull
	@Min(1)
	@Column(name = "loan_principal")
	private Long loanPrincipal;

	@NotNull
	@Min(0)
	@Column(name = "interest_rate")
	private Long loanInterestRate;

	@NotNull
	@Min(1)
	@Column(name = "tenor_in_years")
	private Long loanTenor;

	@NotNull
	@Min(1)
	@Column(name = "monthly_installment")
	private Double equatedMonthlyInstallment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Transient
	private Double totalLoanWithInterest;

	@Transient
	private Double totalInterest;

	@Transient
	private Double yearlyInterest;

	public LoanRecord() {

	}

	public LoanRecord(String institution, String loanType, Long loanPrincipal, Long loanInterestRate, Long loanTenor) {
		super();
		this.institution = institution;
		this.loanType = loanType;
		this.loanPrincipal = loanPrincipal;
		this.loanInterestRate = loanInterestRate;
		this.loanTenor = loanTenor;
	}

	public Long getLoanId() {
		return loanId;
	}

	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getLoanType() {
		return loanType;
	}

	public void setLoanType(String loanType) {
		this.loanType = loanType;
	}

	public Long getLoanPrincipal() {
		return loanPrincipal;
	}

	public void setLoanPrincipal(Long loanPrincipal) {
		this.loanPrincipal = loanPrincipal;
	}

	public Long getLoanInterestRate() {
		return loanInterestRate;
	}

	public void setLoanInterestRate(Long loanInterestRate) {
		this.loanInterestRate = loanInterestRate;
	}

	public Long getLoanTenor() {
		return loanTenor;
	}

	public void setLoanTenor(Long loanTenor) {
		this.loanTenor = loanTenor;
	}

	public Double getEquatedMonthlyInstallment() {
		return equatedMonthlyInstallment;
	}

	public void setEquatedMonthlyInstallment(Double equatedMonthlyInstallment) {
		this.equatedMonthlyInstallment = equatedMonthlyInstallment;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Double getTotalLoanWithInterest() {
		return totalLoanWithInterest;
	}

	public void setTotalLoanWithInterest(Double totalLoanWithInterest) {
		this.totalLoanWithInterest = totalLoanWithInterest;
	}

	public Double getTotalInterest() {
		return totalInterest;
	}

	public void setTotalInterest(Double totalInterest) {
		this.totalInterest = totalInterest;
	}

	public Double getYearlyInterest() {
		return yearlyInterest;
	}

	public void setYearlyInterest(Double yearlyInterest) {
		this.yearlyInterest = yearlyInterest;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof LoanRecord))
			return false;
		return loanId != null && loanId.equals(((LoanRecord) obj).getLoanId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "LoanRecord [loanId=" + loanId + ", institution=" + institution + ", loanType=" + loanType
				+ ", loanPrincipal=" + loanPrincipal + ", loanInterestRate=" + loanInterestRate + ", loanTenor="
				+ loanTenor + "]";
	}

}
