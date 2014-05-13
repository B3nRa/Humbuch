package de.dhbw.humbuch.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static de.dhbw.humbuch.test.TestUtils.*;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.dhbw.humbuch.guice.GuiceJUnitRunner;
import de.dhbw.humbuch.guice.TestModule;
import de.dhbw.humbuch.guice.GuiceJUnitRunner.GuiceModules;
import de.dhbw.humbuch.model.DAO;
import de.dhbw.humbuch.model.entity.Category;
import de.dhbw.humbuch.model.entity.SchoolYear;
import de.dhbw.humbuch.model.entity.SettingsEntry;
import de.dhbw.humbuch.model.entity.TeachingMaterial;
import de.dhbw.humbuch.model.entity.TestPersistenceInitialiser;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ TestModule.class })
public class SettingsViewModelTest extends BaseTest {

	private SettingsViewModel vm;
	private Properties properties;
	private DAO<Category> daoCategory;
	private DAO<SchoolYear> daoSchoolYear;
	private DAO<SettingsEntry> daoSettingsEntry;
	private DAO<TeachingMaterial> daoTeachingMaterial;

	@Inject
	public void setInjected(TestPersistenceInitialiser persistenceInitialiser,
			Provider<EntityManager> emProvider,
			SettingsViewModel settingsViewModel, Properties properties,
			DAO<Category> daoCategory, DAO<SchoolYear> daoSchoolYear,
			DAO<SettingsEntry> daoSettingsEntry,
			DAO<TeachingMaterial> daoTeachingMaterial) {
		this.properties = properties;
		this.daoCategory = daoCategory;
		this.daoSchoolYear = daoSchoolYear;
		this.daoSettingsEntry = daoSettingsEntry;
		this.daoTeachingMaterial = daoTeachingMaterial;
		super.setInjected(persistenceInitialiser, emProvider);

		this.vm = settingsViewModel;
	}

	@Before
	public void refreshViewModel() {
		properties.currentUser.set(randomUser());
		vm.refresh();
	}

	@Test
	public void testStateInitialisation() {
		assertNotNull(vm.schoolYears.get());
		assertNotNull(vm.categories.get());
		assertNotNull(vm.settingsEntries.get());
		assertNotNull(vm.userName.get());
		assertNotNull(vm.userEmail.get());
	}

	@Test
	public void testStateCategoriesInsertOne() {
		daoCategory.insert(category());
		daoCategory.insert(category());
		vm.refresh();
		assertEquals(2, vm.categories.get().size());
	}

	@Test
	public void testStateCategoriesInsertThreeDeleteOne() {
		daoCategory.insert(category());
		daoCategory.insert(category());
		Category category = daoCategory.insert(category());
		vm.refresh();
		assertEquals(3, vm.categories.get().size());

		daoCategory.delete(category);
		vm.refresh();
		assertEquals(2, vm.categories.get().size());
	}

	@Test
	public void testStateSchoolYearsInsertTwo() {
		daoSchoolYear.insert(schoolYearSecondTermEnded());
		daoSchoolYear.insert(schoolYearFirstTermStarted());
		vm.refresh();
		assertEquals(2, vm.schoolYears.get().size());
	}

	@Test
	public void testStateSchoolYearsInsertThreeDeleteOne() {
		daoSchoolYear.insert(schoolYearSecondTermEnded());
		daoSchoolYear.insert(schoolYearFirstTermStarted());
		SchoolYear schoolYear = daoSchoolYear
				.insert(schoolYearSecondTermStarted());
		vm.refresh();
		assertEquals(3, vm.schoolYears.get().size());

		daoSchoolYear.delete(schoolYear);
		vm.refresh();
		assertEquals(2, vm.schoolYears.get().size());
	}

	@Test
	public void testStateSettingsEntriesInsertTwo() {
		daoSettingsEntry.insert(settingsEntry());
		daoSettingsEntry.insert(settingsEntry());
		vm.refresh();
		assertEquals(2, vm.settingsEntries.get().size());
	}

	@Test
	public void testStateSettingsEntriesInsertThreeDeleteOne() {
		daoSettingsEntry.insert(settingsEntry());
		daoSettingsEntry.insert(settingsEntry());
		SettingsEntry settingsEntry = daoSettingsEntry.insert(settingsEntry());
		vm.refresh();
		assertEquals(3, vm.settingsEntries.get().size());

		daoSettingsEntry.delete(settingsEntry);
		vm.refresh();
		assertEquals(2, vm.settingsEntries.get().size());
	}

	@Test
	public void testDoDeleteCategoryNotAllowed() {
		TeachingMaterial teachingMaterial = daoTeachingMaterial
				.insert(teachingMaterialInBothTermsOfGrade(6));
		vm.refresh();
		assertEquals(1, vm.categories.get().size());
		vm.doDeleteCategory(teachingMaterial.getCategory());
		assertEquals(1, vm.categories.get().size());
	}

	@Test
	public void testDoDeleteCategoryAllowed() {
		Category category = daoCategory.insert(category());
		vm.refresh();
		vm.doDeleteCategory(category);
		assertEquals(0, vm.categories.get().size());
	}

	@Test
	public void testDoDeleteSchoolYearNotAllowedWhenInFirstTerm() {
		SchoolYear schoolYear = daoSchoolYear
				.insert(schoolYearFirstTermStarted());
		vm.refresh();
		assertEquals(1, vm.schoolYears.get().size());
		vm.doDeleteSchoolYear(schoolYear);
		assertEquals(1, vm.schoolYears.get().size());
	}

	@Test
	public void testDoDeleteSchoolYearNotAllowedWhenAfterFirstTerm() {
		SchoolYear schoolYear = daoSchoolYear
				.insert(schoolYearFirstTermEnded());
		vm.refresh();
		assertEquals(1, vm.schoolYears.get().size());
		vm.doDeleteSchoolYear(schoolYear);
		assertEquals(1, vm.schoolYears.get().size());
	}

	@Test
	public void testDoDeleteSchoolYearNotAllowedWhenInSecondTerm() {
		SchoolYear schoolYear = daoSchoolYear
				.insert(schoolYearSecondTermStarted());
		vm.refresh();
		assertEquals(1, vm.schoolYears.get().size());
		vm.doDeleteSchoolYear(schoolYear);
		assertEquals(1, vm.schoolYears.get().size());
	}

	@Test
	public void testDoDeleteSchoolYearAllowedWhenBeforeFirstTerm() {
		SchoolYear schoolYear = daoSchoolYear
				.insert(schoolYearFirstTermNotStarted());
		vm.refresh();
		assertEquals(1, vm.schoolYears.get().size());
		vm.doDeleteSchoolYear(schoolYear);
		assertEquals(0, vm.schoolYears.get().size());
	}

	@Test
	public void testDoDeleteSchoolYearAllowedWhenAfterSecondTerm() {
		SchoolYear schoolYear = daoSchoolYear
				.insert(schoolYearSecondTermEnded());
		vm.refresh();
		assertEquals(1, vm.schoolYears.get().size());
		vm.doDeleteSchoolYear(schoolYear);
		assertEquals(0, vm.schoolYears.get().size());
	}

	@Test
	public void testDoUpdateSchoolYear() {
		SchoolYear schoolYear = daoSchoolYear
				.insert(schoolYearFirstTermStarted());
		vm.refresh();
		assertEquals(1, vm.schoolYears.get().size());
		SchoolYear referenceSchoolYear = schoolYearSecondTermEnded();
		schoolYear.setFromDate(referenceSchoolYear.getFromDate());
		schoolYear.setEndFirstTerm(referenceSchoolYear.getEndFirstTerm());
		schoolYear.setBeginSecondTerm(referenceSchoolYear.getBeginSecondTerm());
		schoolYear.setToDate(referenceSchoolYear.getToDate());
		vm.doUpdateSchoolYear(schoolYear);
		schoolYear = vm.schoolYears.get().iterator().next();
		assertEquals(referenceSchoolYear.getFromDate(),
				schoolYear.getFromDate());
		assertEquals(referenceSchoolYear.getEndFirstTerm(),
				schoolYear.getEndFirstTerm());
		assertEquals(referenceSchoolYear.getBeginSecondTerm(),
				schoolYear.getBeginSecondTerm());
		assertEquals(referenceSchoolYear.getToDate(), schoolYear.getToDate());
	}

	@Test
	public void testDoUpdateCategoryAllowed() {
		final String newName = "NEWNAME";
		Category category = daoCategory.insert(category());
		vm.refresh();
		category.setName(newName);
		vm.doUpdateCategory(category);
		assertEquals(newName, vm.categories.get().iterator().next().getName());
	}

	@Test
	public void testDoUpdateCategoryNotAllowedWithEmptyName() {
		final String newName = "";
		Category category = daoCategory.insert(category());
		vm.refresh();
		category.setName(newName);
		vm.doUpdateCategory(category);
		assertThat(vm.categories.get().iterator().next().getName(),
				not(newName));
	}

	@Test
	public void testDoUpdateSettingsEntry() {
		SettingsEntry settingsEntry = daoSettingsEntry.insert(settingsEntry());
		vm.refresh();
		assertEquals(1, vm.settingsEntries.get().size());

		SettingsEntry referenceSettingsEntry = settingsEntry();
		settingsEntry.setSettingStandardValue(referenceSettingsEntry
				.getSettingStandardValue());
		settingsEntry.setSettingValue(referenceSettingsEntry.getSettingValue());
		vm.doUpdateSettingsEntry(settingsEntry);
		assertThat(vm.settingsEntries.get().iterator().next()
				.getSettingStandardValue(),
				is(referenceSettingsEntry.getSettingStandardValue()));
		assertThat(
				vm.settingsEntries.get().iterator().next().getSettingValue(),
				is(referenceSettingsEntry.getSettingValue()));
	}

	// assertNotNull(vm.passwordChangeStatus.get());
}
