/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.benchmark;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.Utils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author Håvard Ottestad
 */
@State(Scope.Benchmark)
@Warmup(iterations = 10)
@BenchmarkMode({Mode.AverageTime})
@Fork(value = 1, jvmArgs = {"-Xms4G", "-Xmx4G", "-Xmn2G", "-XX:+UseSerialGC", "-XX:+UnlockCommercialFeatures", "-XX:StartFlightRecording=delay=5s,duration=60s,filename=recording.jfr,settings=profile", "-XX:FlightRecorderOptions=samplethreads=true,stackdepth=1024", "-XX:+UnlockDiagnosticVMOptions", "-XX:+DebugNonSafepoints"})
@Measurement(iterations = 50)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DatatypeBenchmarkEmpty {


	private static final int NUMBER_OF_TRANSACTIONS = 10;
	private static final int STATEMENTS_PER_TRANSACTION = 100;

	private List<List<Statement>> allStatements;

	@Setup(Level.Iteration)
	public void setUp() {

		allStatements = new ArrayList<>(NUMBER_OF_TRANSACTIONS);


		SimpleValueFactory vf = SimpleValueFactory.getInstance();

		for (int j = 0; j < NUMBER_OF_TRANSACTIONS; j++) {
			List<Statement> statements = new ArrayList<>(STATEMENTS_PER_TRANSACTION);
			allStatements.add(statements);
			for (int i = 0; i < STATEMENTS_PER_TRANSACTION; i++) {
				statements.add(
					vf.createStatement(vf.createIRI("http://example.com/" + i + "_" + j), RDF.TYPE, RDFS.RESOURCE)
				);
				statements.add(
					vf.createStatement(vf.createIRI("http://example.com/" + i + "_" + j), FOAF.AGE, vf.createLiteral(i))
				);
			}
		}
		System.gc();

	}

	@TearDown(Level.Iteration)
	public void tearDown() {
		allStatements.clear();
	}


	@Benchmark
	public void shacl() {

		SailRepository repository = new SailRepository(new ShaclSail(new MemoryStore(), Utils.getSailRepository("shaclDatatype.ttl")));

		repository.initialize();

		try (SailRepositoryConnection connection = repository.getConnection()) {
			connection.begin(IsolationLevels.SNAPSHOT);
			connection.commit();
		}

		try (SailRepositoryConnection connection = repository.getConnection()) {
			for (List<Statement> statements : allStatements) {
				connection.begin(IsolationLevels.SNAPSHOT);
				connection.add(statements);
				connection.commit();
			}
		}

	}


	@Benchmark
	public void noShacl() {

		SailRepository repository = new SailRepository(new MemoryStore());

		repository.initialize();

		try (SailRepositoryConnection connection = repository.getConnection()) {
			connection.begin(IsolationLevels.SNAPSHOT);
			connection.commit();
		}
		try (SailRepositoryConnection connection = repository.getConnection()) {
			for (List<Statement> statements : allStatements) {
				connection.begin(IsolationLevels.SNAPSHOT);
				connection.add(statements);
				connection.commit();
			}
		}

	}


	@Benchmark
	public void sparqlInsteadOfShacl() {

		SailRepository repository = new SailRepository(new MemoryStore());

		repository.initialize();

		try (SailRepositoryConnection connection = repository.getConnection()) {
			connection.begin(IsolationLevels.SNAPSHOT);
			connection.commit();
		}
		try (SailRepositoryConnection connection = repository.getConnection()) {
			for (List<Statement> statements : allStatements) {
				connection.begin(IsolationLevels.SNAPSHOT);
				connection.add(statements);
				try (Stream<BindingSet> stream = Iterations.stream(connection.prepareTupleQuery("select * where {?a a <" + RDFS.RESOURCE + ">; <" + FOAF.AGE + "> ?age. FILTER(datatype(?age) != <http://www.w3.org/2001/XMLSchema#int>)}").evaluate())) {
					stream.forEach(System.out::println);
				}
				connection.commit();
			}
		}

	}


}
