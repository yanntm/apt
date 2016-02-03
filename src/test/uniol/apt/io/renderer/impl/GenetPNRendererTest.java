/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 * Copyright (C) 2016 Uli Schlachter
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package uniol.apt.io.renderer.impl;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import uniol.apt.adt.pn.PetriNet;

import static uniol.apt.TestNetCollection.*;
import static uniol.apt.adt.matcher.Matchers.*;

/** @author Uli Schlachter */
public class GenetPNRendererTest {
	private String render(PetriNet pn) throws Exception {
		return new GenetPNRenderer().render(RendererTestUtils.getSortedNet(pn));
	}

	private void test(PetriNet pn, String expectedString) throws Exception {
		assertThat(render(pn), is(expectedString));
	}

	@Test
	public void testTokenGeneratorNet() throws Exception {
		test(getTokenGeneratorNet(), ".inputs t1\n.graph\nt1 p1\n.marking {}\n.end");
	}

	@Test
	public void testDeadlockNet() throws Exception {
		test(getDeadlockNet(), ".inputs t1 t2\n.graph\np1 t1\np1 t2\n.marking {p1}\n.end");
	}

	@Test
	public void testNonPersistentNet() throws Exception {
		test(getNonPersistentNet(), ".inputs a b c\n.graph\n"
				+ "p1 b\np1 a\na p2\np2 c\nb p2\nc p1\n.marking {p1}\n.end");
	}

	@Test
	public void checkPersistentBiCFNet() throws Exception {
		test(getPersistentBiCFNet(), ".inputs a b c d\n.graph\n"
				+ "p1 a\na p2\np2 c\nb p4\np3 a\np3 b\nc p1\nc p3\np4 d\nd p5\nd p3\np5 b\n"
				+ ".marking {p1 p3=2 p5}\n.end");
	}

	@Test
	public void testConcurrentDiamondNet() throws Exception {
		test(getConcurrentDiamondNet(), ".inputs t1 t2\n.graph\np1 t1\np2 t2\n.marking {p1 p2}\n.end");
	}

	@Test
	public void testConflictingDiamondNet() throws Exception {
		test(getConflictingDiamondNet(), ".inputs t1 t2\n.graph\np1 t1\np2 t2\np3 t1\np3 t2\nt1 p3\nt2 p3\n"
				+ ".marking {p1 p2 p3}\n.end");
	}

	@Test
	public void testACBCCLoopNet() throws Exception {
		test(getACBCCLoopNet(), ".inputs a b c\n.graph\n"
				+ "p0 c\na p3(3)\np1 a(3)\nb p0(3)\np2 b(3)\nc p1\nc p2\np3 c\n"
				+ ".marking {p0 p1=3}\n.end");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
