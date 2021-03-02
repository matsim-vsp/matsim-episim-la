/*-
 * #%L
 * MATSim Episim
 * %%
 * Copyright (C) 2020 matsim-org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.matsim.run.modules;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.VaccinationConfigGroup;
import org.matsim.episim.model.AgeDependentInfectionModelWithSeasonality;
import org.matsim.episim.model.AgeDependentProgressionModel;
import org.matsim.episim.model.ContactModel;
import org.matsim.episim.model.FaceMask;
import org.matsim.episim.model.InfectionModel;
import org.matsim.episim.model.ProgressionModel;
import org.matsim.episim.model.SymmetricContactModel;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.Restriction;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Scenario based on the publicly available LA scenario (https://github.com/matsim-scenarios/matsim-los-angeles).
 */
public class OpenLosAngelesScenarioIK extends AbstractModule {

	/**
	 * Activity names of the default params from {@link #addDefaultParams(EpisimConfigGroup)}.
	 */
	public static final String[] DEFAULT_ACTIVITIES = {
			"work", "university", "school", "escort", "schoolescort", "schoolpureescort", "schoolridesharing", "non-schoolescort", 
			"maintenance", "HHmaintenance", "personalmaintenance", "eatout", "eatoutbreakfast", "eatoutlunch", "eatoutdinner",
			"visiting", "discretionary", "specialevent", "atwork", "atworkbusiness", "atworklunch", "atworkother", "shop", "business"
	};
	
	public static final String[] ACTIVITIES_MASKES = { 
			"maintenance", "HHmaintenance", "personalmaintenance", "discretionary", "shop"
	};

	/**
	 * Adds default parameters for LA scenario.
	 */
	public static void addDefaultParams(EpisimConfigGroup config) {
		config.getOrAddContainerParams("pt", "tr");
		// regular out-of-home acts:
		config.getOrAddContainerParams("home").setContactIntensity(1.0);
		config.getOrAddContainerParams("work").setContactIntensity(1.47);
		config.getOrAddContainerParams("university").setContactIntensity(5.5);
		config.getOrAddContainerParams("escort").setContactIntensity(1.0);
		
		config.getOrAddContainerParams("school").setContactIntensity(11.0); // many people, small space, no air exchange
//		config.getOrAddContainerParams("schoolescort"); // no need to be set because the activity type starts with "school", for which the CI is already set.
//		config.getOrAddContainerParams("schoolpureescort");
//		config.getOrAddContainerParams("schoolridesharing");
		
		config.getOrAddContainerParams("shop").setContactIntensity(0.88);
		config.getOrAddContainerParams("maintenance").setContactIntensity(0.88);
		config.getOrAddContainerParams("HHmaintenance").setContactIntensity(0.88);
		config.getOrAddContainerParams("personalmaintenance").setContactIntensity(0.88);
		
		config.getOrAddContainerParams("eatout").setContactIntensity(9.24).setSeasonal(true);
//		config.getOrAddContainerParams("eatoutbreakfast");
//		config.getOrAddContainerParams("eatoutlunch");
//		config.getOrAddContainerParams("eatoutdinner");
		
		config.getOrAddContainerParams("visiting").setContactIntensity(9.24);
		config.getOrAddContainerParams("discretionary").setContactIntensity(9.24);
		config.getOrAddContainerParams("specialevent").setContactIntensity(9.24);
		
		config.getOrAddContainerParams("atwork").setContactIntensity(1.47);
//		config.getOrAddContainerParams("atworkbusiness");
//		config.getOrAddContainerParams("atworklunch");
//		config.getOrAddContainerParams("atworkother");
		
		config.getOrAddContainerParams("business").setContactIntensity(1.47); // 
		config.getOrAddContainerParams("non-schoolescort").setContactIntensity(1.0);
		
		config.getOrAddContainerParams("quarantine_home").setContactIntensity(1.0);
		
	}

	@Provides
	@Singleton
	public Config config() {
		
		int sample = 10; // currently possible: 1, 10, 25
//		String svnLocation = "https://svn.vsp.tu-berlin.de/repos/public-svn/";
		String svnLocation = "../public-svn/";

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");  
		LocalDateTime now = LocalDateTime.now();
		String dateTimeString = dtf.format(now);
		
		   
		config.controler().setOutputDirectory("output/output_" + sample + "pct_" + dateTimeString);
		config.global().setCoordinateSystem("EPSG:3310");	
		
		if (sample == 1) {
			config.plans().setInputFile(svnLocation + "matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/input/los-angeles-v1.0-population-1pct_2020-03-07_reduced-for-episim.xml.gz");
			episimConfig.setInputEventsFile(svnLocation + "matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/output/los-angeles-v1.1-1pct/los-angeles-v1.1-1pct.output_events-reduced-for-episim.xml.gz");
			episimConfig.setSampleSize(0.01);
			episimConfig.setCalibrationParameter(2);		
		}
		else if (sample == 10) {
			config.plans().setInputFile(svnLocation + "matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/output/los-angeles-v1.1-10pct/los-angeles-v1.0-population-10pct_2020-03-07_teleported_reduced-for-episim.xml.gz");
			episimConfig.setInputEventsFile(svnLocation + "matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/output/los-angeles-v1.1-10pct/la-v1.1-10pct_teleported1.output_events-reduced-for-episim.xml.gz");
			episimConfig.setSampleSize(0.1);
			episimConfig.setCalibrationParameter(1.0E-4);		
		}
		else if (sample == 25) {
			config.plans().setInputFile(svnLocation + "matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/output/los-angeles-v1.1-25pct/los-angeles-v1.0-population-25pct_2020-03-07_teleported_reduced-for-episim.xml.gz");
			episimConfig.setInputEventsFile(svnLocation + "matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/output/los-angeles-v1.1-25pct/la-v1.1-25pct_teleported1.output_events-reduced-for-episim.xml.gz");
			episimConfig.setSampleSize(0.25);
			episimConfig.setCalibrationParameter(9E-5);		
		}
		else throw new RuntimeException("Sample size does not exist!");
		
		
		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.snz);
		episimConfig.setStartDate("2020-02-15");
		episimConfig.setHospitalFactor(5.);
		
		// Here we set the disease import.
		// First, set the day until which we have a disease import.
		// -> set to Integer.MAX_VALUE in order to not have a limitation
		episimConfig.setInitialInfections(Integer.MAX_VALUE);
		// Second, set the daily infected agents rates.
		// -> these numbers are given in infected agents per day
		// -> these numbers are daily numbers that are valid from provided start day
		Map<LocalDate, Integer> infectionsPerDay = new HashMap<>();
//		infectionsPerDay.put(LocalDate.parse("2020-02-15"), 1);
//		infectionsPerDay.put(LocalDate.parse("2020-03-01"), 0);
		episimConfig.setInfections_pers_per_day(infectionsPerDay);
		
		addDefaultParams(episimConfig);
		
		// Here we set the restrictions. A possible starting point could be the google mobility reports: https://www.google.com/covid19/mobility/
		episimConfig.setPolicy(FixedPolicy.class, FixedPolicy.config()
				// fraction of out-of-home activities that still occur
				.restrict("2020-03-15", 0.9, DEFAULT_ACTIVITIES)
				.restrict("2020-03-17", 0.8, DEFAULT_ACTIVITIES)
				.restrict("2020-03-20", 0.7, DEFAULT_ACTIVITIES)
				.restrict("2020-03-22", 0.6, DEFAULT_ACTIVITIES)
				.restrict("2020-04-05", 0.55, DEFAULT_ACTIVITIES)
				.restrict("2020-04-25", 0.6, DEFAULT_ACTIVITIES)
				.restrict("2020-05-01", 0.7, DEFAULT_ACTIVITIES)

				// fraction of people that wear a mask
				.restrict("2020-06-18", Restriction.ofMask(FaceMask.CLOTH, 0.9), "pt") // 90% of public transport passengers wear a cloth mask 
				.restrict("2020-06-18", Restriction.ofMask(FaceMask.CLOTH, 0.25), ACTIVITIES_MASKES)
				.restrict("2020-07-01", Restriction.ofMask(FaceMask.CLOTH, 0.8), ACTIVITIES_MASKES)

				// adjust the contact intensity over time
//				.restrict("2020-06-01", Restriction.ofCiCorrection(0.9), DEFAULT_ACTIVITIES)
				.build()
		);
		
		//vaccination example
		//todo adapt this for LA
		{
			VaccinationConfigGroup vaccinationConfig = ConfigUtils.addOrGetModule(config, VaccinationConfigGroup.class);
			//probability of infection is reduced to 10% 28 days after the vaccination. Until day 28 the effectiveness is interpolated linearly.
			vaccinationConfig.setEffectiveness(0.9);
			vaccinationConfig.setDaysBeforeFullEffect(28);

			//in this exmaple vaccinations start on 2020-12-27
			vaccinationConfig.setVaccinationCapacity_pers_per_day(Map.of(
					episimConfig.getStartDate(), 0,
					LocalDate.parse("2020-12-27"), 2000
					));
		}
		
		// account for seasonal effects
		Map<LocalDate, Double> date2fraction = new HashMap<>();
		date2fraction.put(LocalDate.parse("2020-02-01"), 0.);
		date2fraction.put(LocalDate.parse("2020-04-01"), 0.8);
		date2fraction.put(LocalDate.parse("2020-09-01"), 0.8);
		date2fraction.put(LocalDate.parse("2020-10-01"), 0.6);
		date2fraction.put(LocalDate.parse("2020-11-01"), 0.1);
		episimConfig.setLeisureOutdoorFraction(date2fraction);


		return config;
	}
	
	@Override
	protected void configure() {
		bind(ContactModel.class).to(SymmetricContactModel.class).in(Singleton.class);
		bind(ProgressionModel.class).to(AgeDependentProgressionModel.class).in(Singleton.class);
		bind(InfectionModel.class).to(AgeDependentInfectionModelWithSeasonality.class).in(Singleton.class);
	}

}
