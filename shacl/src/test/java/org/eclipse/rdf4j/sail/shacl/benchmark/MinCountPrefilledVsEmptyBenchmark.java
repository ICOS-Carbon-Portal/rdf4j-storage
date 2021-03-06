/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.benchmark;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
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

/**
 * @author Håvard Ottestad
 */
@State(Scope.Benchmark)
@Warmup(iterations = 10)
@BenchmarkMode({Mode.AverageTime})
@Fork(value = 1, jvmArgs = {"-Xms4G", "-Xmx4G", "-Xmn2G"})
@Measurement(iterations = 10)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MinCountPrefilledVsEmptyBenchmark {


	private List<List<Statement>> allStatements;
	private SailRepository shaclRepo;


	@Setup(Level.Invocation)
	public void setUp() {
		allStatements = new ArrayList<>(10);


		SimpleValueFactory vf = SimpleValueFactory.getInstance();

		for (int j = 0; j < 10; j++) {
			List<Statement> statements = new ArrayList<>(101);
			allStatements.add(statements);
			for (int i = 0; i < 100; i++) {
				statements.add(
					vf.createStatement(vf.createIRI("http://example.com/" + i + "_" + j), RDF.TYPE, RDFS.RESOURCE)
				);
				statements.add(
					vf.createStatement(vf.createIRI("http://example.com/" + i + "_" + j), RDFS.LABEL, vf.createLiteral("label" + i))
				);
			}
		}

		List<Statement> allStatements2 = new ArrayList<>(10);

		for (int i = 0; i < 1; i++) {
			allStatements2.add(
				vf.createStatement(vf.createIRI("http://example.com/preinserted/" + i), RDF.TYPE, RDFS.RESOURCE)
			);
			allStatements2.add(
				vf.createStatement(vf.createIRI("http://example.com/preinserted/" + i), RDFS.LABEL, vf.createLiteral("label" + i))
			);
		}


		ShaclSail shaclRepo = new ShaclSail(new MemoryStore(), Utils.getSailRepository("shacl.ttl"));
		this.shaclRepo = new SailRepository(shaclRepo);
		this.shaclRepo.initialize();

		shaclRepo.disableValidation();
		try (SailRepositoryConnection connection = this.shaclRepo.getConnection()) {
			connection.add(allStatements2);
		}
		shaclRepo.enableValidation();
		System.gc();

	}

	@TearDown(Level.Iteration)
	public void tearDown() {
		allStatements.clear();
	}


	@Benchmark
	public void shaclPrefilled() {


		try (SailRepositoryConnection connection = shaclRepo.getConnection()) {
			connection.begin();
			connection.commit();
		}

		try (SailRepositoryConnection connection = shaclRepo.getConnection()) {
			for (List<Statement> statements : allStatements) {
				connection.begin();
				connection.add(statements);
				connection.commit();
			}
		}

	}


	@Benchmark
	public void shaclEmpty() {

		ShaclSail shaclRepo = new ShaclSail(new MemoryStore(), Utils.getSailRepository("shacl.ttl"));
		SailRepository repository = new SailRepository(shaclRepo);
		repository.initialize();

		try (SailRepositoryConnection connection = repository.getConnection()) {
			connection.begin();
			connection.commit();
		}

		try (SailRepositoryConnection connection = repository.getConnection()) {
			for (List<Statement> statements : allStatements) {
				connection.begin();
				connection.add(statements);
				connection.commit();
			}
		}

	}

	@Benchmark
	public void shaclEmptyJustInitialize() {

		ShaclSail shaclRepo = new ShaclSail(new MemoryStore(), Utils.getSailRepository("shacl.ttl"));
		SailRepository repository = new SailRepository(shaclRepo);
		repository.initialize();


	}


	@Benchmark
	public void shaclEmptyJustInitializeAndEmptyTransaction() {

		ShaclSail shaclRepo = new ShaclSail(new MemoryStore(), Utils.getSailRepository("shacl.ttl"));
		SailRepository repository = new SailRepository(shaclRepo);
		repository.initialize();

		try (SailRepositoryConnection connection = repository.getConnection()) {
			connection.begin();
			connection.commit();
		}


	}


}
