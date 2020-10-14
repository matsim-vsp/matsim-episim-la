package org.matsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.magnos.trie.Trie;
import org.magnos.trie.TrieMatch;
import org.magnos.trie.Tries;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.run.modules.OpenLosAngelesScenario;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BenchmarkPrefixMatch {

	private List<EpisimConfigGroup.InfectionParams> params;
	private Map<String, EpisimConfigGroup.InfectionParams> paramsMap;
	private Map<String, EpisimConfigGroup.InfectionParams> activityMap;
	private Trie<String, EpisimConfigGroup.InfectionParams> trie;
	private PatriciaTrie<EpisimConfigGroup.InfectionParams> pTrie;

	private List<String> activities;

	public static void main(String[] args) throws RunnerException {

		Options opt = new OptionsBuilder()
				.include(BenchmarkPrefixMatch.class.getSimpleName())
				.warmupTime(TimeValue.seconds(10)).warmupIterations(3)
				.measurementTime(TimeValue.seconds(20)).measurementIterations(5)
				.forks(1)
				.build();

		new Runner(opt).run();
	}

	@Setup
	public void setup() {

		EpisimConfigGroup config = new EpisimConfigGroup();
		trie = Tries.forStrings();
		pTrie = new PatriciaTrie<>();
		params = new ArrayList<>();
		paramsMap = new HashMap<>();
		activityMap = new IdentityHashMap<>();
		activities = new ArrayList<>();

		for (String act : OpenLosAngelesScenario.DEFAULT_ACTIVITIES) {
			EpisimConfigGroup.InfectionParams param = config.getOrAddContainerParams(act);
			params.add(param);
			paramsMap.put(act, param);
			trie.put(act, param);
			pTrie.put(act, param);
		}

		SplittableRandom rnd = new SplittableRandom(1);

		for (int i = 0; i < 10_000; i++) {
			int idx = rnd.nextInt(OpenLosAngelesScenario.DEFAULT_ACTIVITIES.length);
			String act = OpenLosAngelesScenario.DEFAULT_ACTIVITIES[idx] + "_" + i;
			activities.add(act);
			activityMap.put(act, config.getOrAddContainerParams(OpenLosAngelesScenario.DEFAULT_ACTIVITIES[idx]));
		}
	}

	@Benchmark
	public void startsWith(Blackhole bh) {

		for (String act : activities) {
			for (EpisimConfigGroup.InfectionParams param : params) {
				if (param.includesActivity(act)) {
					bh.consume(false);
					break;
				}
			}
		}
	}

	@Benchmark
	public void trie(Blackhole bh) {
		for (String act : activities) {
			bh.consume(trie.get(act, TrieMatch.STARTS_WITH));
		}
	}

	@Benchmark
	public void pTrie(Blackhole bh) {
		for (String act : activities) {
			EpisimConfigGroup.InfectionParams param = pTrie.selectValue(act);
			bh.consume(param.includesActivity(act));
		}
	}

	@Benchmark
	public void substring(Blackhole bh) {
		for (String act : activities) {
			String prefix = act.substring(0, act.indexOf("_"));
			EpisimConfigGroup.InfectionParams param = paramsMap.get(prefix);
			if (param == null) throw new NoSuchElementException("Error");
			bh.consume(param);
		}
	}

	@Benchmark
	public void preprocessed(Blackhole bh) {
		for (String act : activities) {
			bh.consume(activityMap.get(act));
		}
	}
}
