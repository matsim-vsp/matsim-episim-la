package org.matsim.episim;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.SplittableRandom;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.util.FastMath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.data.Percentage;
import org.junit.Test;

public class EpisimUtilsTest {
	private static final Logger log = LogManager.getLogger(EpisimUtilsTest.class);

	private static final Percentage OFFSET = Percentage.withPercentage(1.5);

	@Test
	public void dayOfWeek() {

		assertThat(EpisimUtils.getDayOfWeek(LocalDate.of(2020, 7, 1), 1))
				.isEqualTo(DayOfWeek.WEDNESDAY);
	}

	@Test
	public void seed() {

		SplittableRandom rnd = new SplittableRandom(1000);

		assertThat(EpisimUtils.getSeed(rnd))
				.isEqualTo(1000);

		long n = rnd.nextLong();

		assertThat(EpisimUtils.getSeed(rnd))
				.isNotEqualTo(1000);

		EpisimUtils.setSeed(rnd, 1000);
		assertThat(rnd.nextLong())
				.isEqualTo(n);

	}

	@Test
	public void chars() throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream(10000);

		DataOutputStream dout = new DataOutputStream(out);

		EpisimUtils.writeChars(dout, "home");
		EpisimUtils.writeChars(dout, "act");
		EpisimUtils.writeChars(dout, "");

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

		DataInputStream din = new DataInputStream(in);

		assertThat(EpisimUtils.readChars(din))
				.isEqualTo("home");

		assertThat(EpisimUtils.readChars(din))
				.isEqualTo("act");

		assertThat(EpisimUtils.readChars(din))
				.isEqualTo("");
	}

	@Test
	public void nextLogNormal() {

		SplittableRandom rnd = new SplittableRandom(1);

		double mu = 5;
		double sigma = 1.5;
		double ssigma = 1.5 * 1.5;

		double[] values = new double[2_000_000];
		for (int i = 0; i < values.length; i++) {
			values[i] = EpisimUtils.nextLogNormal(rnd, mu, sigma);
		}

		double median = new Median().evaluate(values);
		double mean = new Mean().evaluate(values);
		double std = new StandardDeviation().evaluate(values);

		// https://en.wikipedia.org/wiki/Log-normal_distribution

		assertThat(median).isCloseTo(FastMath.exp(mu), OFFSET);
		assertThat(mean).isCloseTo(FastMath.exp(mu + ssigma / 2.0), OFFSET);
		assertThat(std * std).isCloseTo((FastMath.exp(ssigma) - 1) * FastMath.exp(2 * mu + ssigma), OFFSET);

	}

}
