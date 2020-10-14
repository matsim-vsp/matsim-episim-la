package org.matsim.episim;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BenchmarkIteration {

	private EpisimRunner runner;
	private InfectionEventHandler handler;
	private ReplayHandler replay;
	private EpisimReporting reporting;
	private int iteration = 1;

	public static void main(String[] args) throws RunnerException {

		Options opt = new OptionsBuilder()
				.include(BenchmarkIteration.class.getSimpleName())
				.warmupIterations(12).warmupTime(TimeValue.seconds(1))
				.measurementIterations(30).measurementTime(TimeValue.seconds(1))
				.forks(1)
				.build();

		new Runner(opt).run();
	}

	@Benchmark
	public void iteration() {

		runner.doStep(replay, handler, reporting, iteration);
		iteration++;

	}
}
