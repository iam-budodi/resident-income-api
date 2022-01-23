package com.income.calculator.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.income.calculator.model.IncomeCategory;
import com.income.calculator.model.Income;


@RunWith(Arquillian.class)
public class IncomeRepositoryTest {
	
	private static Long untaxableClassId;
	private static Long lowClassId;
	
	@Inject
	private IncomeRepository incomeRepository;
	
	@Deployment
	public static Archive<?> createDeploymentPackage() {
		return ShrinkWrap.create(JavaArchive.class)
				.addClass(Income.class)
				.addClass(IncomeCategory.class)
				.addClass(IncomeRepository.class)
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml");
	}
	
	@Test
	@InSequence(1)
	public void shouldBeDeployed() {
		assertNotNull(incomeRepository);
	}

	@Test
	@InSequence(2)
	public void shouldGetNoIncome() {
		assertEquals(0, incomeRepository.findAllIncomes().size());
	}

	@Test
	@InSequence(3)
	public void shouldCreateUntaxableClassIncome() {
		Income income = new Income(IncomeCategory.Untaxable_Class, 0L, 270000L, 0L, 0L, "description");
		income = incomeRepository.createIncome(income);
		
		assertNotNull(income);
		assertNotNull(income.getId());
		untaxableClassId = income.getId();
	}
	
	@Test
	@InSequence(4)
	public void shouldCreateLowClassIncome() {
		Income income = new Income(IncomeCategory.Low_Class,270000L, 520000L, 0L, 8L, "description");
		income = incomeRepository.createIncome(income);
		
		assertNotNull(income);
		assertNotNull(income.getId());
		lowClassId = income.getId();
	}

	@Test
	@InSequence(5)
	public void shouldGetAllIncomes() {
		assertEquals(2, incomeRepository.findAllIncomes().size());
	}

	@Test
	@InSequence(6)
	public void shouldCheckCreatedIncome() {
		Income income = incomeRepository.findIncomeById(untaxableClassId);

		assertNotNull(income);
		assertTrue(income.getCategory().toString().contains("_"));
	}
	
	@Test
	@InSequence(7)
	public void shouldGetUntaxableClassGivenRandomAmountEarnedBelowLimit() {
		Income income = incomeRepository.findEarnedIncome(250000L);
		
		assertNotNull(income);
		assertEquals(IncomeCategory.Untaxable_Class, income.getCategory());
	}
	
	@Test
	@InSequence(8)
	public void shouldGetUntaxableClassGivenExactAmountEarnedLimit() {
		Income income = incomeRepository.findEarnedIncome(270000L);
		
		assertNotNull(income);
		assertEquals(IncomeCategory.Untaxable_Class, income.getCategory());
	}
	
	@Test
	@InSequence(9)
	public void shouldNotGetUntaxableClassGivenAmountEarnedBeyondLimit() {
		Income income = incomeRepository.findEarnedIncome(270001L);
		
		assertNotNull(income);
		assertNotEquals(IncomeCategory.Untaxable_Class, income.getCategory());
		assertEquals(IncomeCategory.Low_Class, income.getCategory());
	}
	
	@Test
	@InSequence(10)
	public void shouldGetLowClassGivenExactAmountEarnedLimit() {
		Income income = incomeRepository.findEarnedIncome(520000L);
		
		assertNotNull(income);
		assertEquals(IncomeCategory.Low_Class, income.getCategory());
	}

	@Test
	@InSequence(11)
	public void shouldUpdateIncome() {
		Income income = incomeRepository.findIncomeById(untaxableClassId);
		income.setCategory(IncomeCategory.Middle_Class);
		Income updatedincome = incomeRepository.updateIncome(income);

		assertFalse(updatedincome.equals(income));
		assertEquals(income.getId(), updatedincome.getId());

	}	
	
	@Test(expected = Exception.class)
	@InSequence(12)
	public void shouldFailToFindIncomeOnUnspecifiedIncomeRange() {
		incomeRepository.findEarnedIncome(0L);
	}

	@Test
	@InSequence(13)
	public void shouldDeleteTheCreatedIncome() {
		incomeRepository.deleteIncome(lowClassId);
	}
	
	
	@Test
	@InSequence(14)
	public void shouldNotGetDeletedIncome() {		
		assertNull(incomeRepository.findIncomeById(lowClassId));
	}
	
	
	@Test(expected = Exception.class)
	@InSequence(15)
	public void shouldFailCreateNullIncome() {
		incomeRepository.createIncome(null);
	}

	@Test(expected = Exception.class)
	@InSequence(16)
	public void shouldFailCreateIncomeWithNullCategory() {
		incomeRepository.createIncome(new Income(null, 0L, 270000L, 0L, 0L, "description"));
	}

	@Test(expected = Exception.class)
	@InSequence(17)
	public void shouldFailCreateIncomeWithNullAmount() {
		incomeRepository.createIncome(new Income(IncomeCategory.Untaxable_Class, null, 270000L, 0L, 0L, "description"));
	}

	@Test(expected = Exception.class)
	@InSequence(18)
	public void shouldFailCreateIncomeWithNullLimit() {
		incomeRepository.createIncome(new Income(IncomeCategory.Untaxable_Class, 0L, null, 0L, 0L, "description"));
	}

	@Test(expected = Exception.class)
	@InSequence(19)
	public void shouldFailCreateIncomeWithNullTax() {
		incomeRepository.createIncome(new Income(IncomeCategory.Untaxable_Class, 0L, 270000L, null, 0L, "description"));
	}

	@Test(expected = Exception.class)
	@InSequence(20)
	public void shouldFailCreateIncomeWithNullTaxOnExcess() {
		incomeRepository.createIncome(new Income(IncomeCategory.Untaxable_Class, 0L, 270000L, 0L, null, "description"));
	}
	
	
	@Test(expected = Exception.class)
	@InSequence(21)
	public void shouldFailCreateIncomeWithLowIncome() {
		incomeRepository.createIncome(new Income(IncomeCategory.Untaxable_Class, -1L, 270000L, 0L, 0L, "description"));
	}

	@Test(expected = Exception.class)
	@InSequence(17)
	public void shouldFailCreateIncomeWithLowIncomeLimit() {
		incomeRepository.createIncome(new Income(IncomeCategory.Untaxable_Class, 0L, 250000L, 0L, 0L, "description"));
	}

	@Test(expected = Exception.class)
	@InSequence(22)
	public void shouldFailInvokingGetIncomeByIdWithNull() {
		incomeRepository.findIncomeById(null);
	}

	@Test
	@InSequence(23)
	public void shouldFailInvokingGetIncomeByIdWithUnknownId() {
		assertNull(incomeRepository.findIncomeById(2607L));
	}

	@Test(expected = Exception.class)
	@InSequence(24)
	public void shouldFailInvokingDeleteIncomeWithNullId() {
		incomeRepository.deleteIncome(null);
	}

	@Test(expected = Exception.class)
	@InSequence(25)
	public void shouldNotDeleteIncomeWithUnknownId() {
		incomeRepository.deleteIncome(2607L);
	}

}
