package com.income.calculator.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "t_income")
public class Income {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long Id;

	@Enumerated(EnumType.ORDINAL)
	@NotNull
	private IncomeCategory category;
	
	@Column()
	@NotNull
	@Min(0)
	private Long income;
	
	@Column(name = "income_limit")
	@NotNull
	@Min(270000)
	private Long incomeLimit;
	
	@Column(name = "tax_on_income")
	@NotNull
	@Min(0)
	private Long taxOnIncome;
	
	@Column(name = "tax_on_excess_income")
	@NotNull
	@Min(0)
	private Long taxOnExcessIncome;
	
	@Column(length = 1000)
	@Size(min = 1, max = 1000)
	private String description;

	public Income() {

	}

	public Income(IncomeCategory category, Long income, Long incomeLimit, 
			Long taxOnIncome, Long taxOnExcessIncome, String description) {

		this.category = category;
		this.income = income;
		this.incomeLimit = incomeLimit;
		this.taxOnIncome = taxOnIncome;
		this.taxOnExcessIncome = taxOnExcessIncome;
		this.description = description;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		this.Id = id;
	}

	public IncomeCategory getCategory() {
		return category;
	}

	public void setCategory(IncomeCategory category) {
		this.category = category;
	}

	public Long getIncome() {
		return income;
	}

	public void setIncome(Long income) {
		this.income = income;
	}

	public Long getIncomeLimit() {
		return incomeLimit;
	}

	public void setIncomeLimit(Long incomeLimit) {
		this.incomeLimit = incomeLimit;
	}

	public Long getTaxOnIncome() {
		return taxOnIncome;
	}

	public void setTaxOnIncome(Long taxOnIncome) {
		this.taxOnIncome = taxOnIncome;
	}

	public Long getTaxOnExcessIncome() {
		return taxOnExcessIncome;
	}

	public void setTaxOnExcessIncome(Long taxOnExcessIncome) {
		this.taxOnExcessIncome = taxOnExcessIncome;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Income [ Id=" + Id + 
				", category=" + category + 
				", income=" + income + 
				", incomeLimit=" + incomeLimit + 
				", taxOnIncome=" + taxOnIncome + 
				", taxOnExcessIncome=" + taxOnExcessIncome + 
				", description=" + description + 
				"]";
	}
}
