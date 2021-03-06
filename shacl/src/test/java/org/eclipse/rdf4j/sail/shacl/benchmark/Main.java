/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author Håvard Ottestad
 */
public class Main {

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include("MinCount")
//			.include(MinCountBenchmark.class.getSimpleName()+".noShacl")

//			.include(MinCountBenchmarkPrefilled.class.getSimpleName())
//			.include(MinCountPrefilledVsEmptyBenchmark.class.getSimpleName()+"..shaclPrefilled$")
//			.include(MinCountPrefilledVsEmptyBenchmark.class.getSimpleName()+"..shaclEmpty$")

			.warmupIterations(10)
			.measurementIterations(10)
			.forks(1)
			//.addProfiler("stack", "lines=20;period=1;top=20")
			.build();

		new Runner(opt).run();
	}

}
