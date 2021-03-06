/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.planNodes;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sail.SailException;

import java.util.Arrays;
import java.util.List;

/**
 * @author Håvard Ottestad
 */
public class GroupByCount implements PlanNode {

	PlanNode parent;

	public GroupByCount(PlanNode parent) {
		this.parent = parent;
	}

	@Override
	public CloseableIteration<Tuple, SailException> iterator() {
		return new CloseableIteration<Tuple, SailException>() {

			CloseableIteration<Tuple, SailException> parentIterator = parent.iterator();

			Tuple tempNext;

			Tuple next;

			private void calculateNext() {
				if (next != null) {
					return;
				}

				if (tempNext == null && parentIterator.hasNext()) {
					tempNext = parentIterator.next();
				}

				if (tempNext == null) {
					return;
				}

				long count = 0;

				next = new Tuple();

				Value subject = tempNext.line.get(0);

				while (tempNext != null && (tempNext.line.get(0) == subject || tempNext.line.get(0).equals(subject))) {

					next.addHistory(tempNext);
					if (tempNext.line.size() > 1) {
						count++;
					}

					if (parentIterator.hasNext()) {
						tempNext = parentIterator.next();
					} else {
						tempNext = null;
					}

				}

				List<Value> line = Arrays.asList(subject, SimpleValueFactory.getInstance().createLiteral(count));

				next.line = line;

			}

			@Override
			public void close() throws SailException {
				parentIterator.close();
			}

			@Override
			public boolean hasNext() throws SailException {
				calculateNext();

				return next != null;
			}


			@Override
			public Tuple next() throws SailException {

				calculateNext();

				Tuple temp = next;
				next = null;

				return temp;
			}

			@Override
			public void remove() throws SailException {

			}
		};
	}

	@Override
	public int depth() {
		return parent.depth() + 1;
	}
}
