/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

package uniol.apt.adt.exception;

import uniol.apt.adt.IGraph;

/**
 * The NoSuchNodeException is thrown if the node, which is tried to achieve, does not exists.
 * @author Manuel Gieseking
 */
public class NoSuchNodeException extends DatastructureException {

	public static final long serialVersionUID = 0xdeadbeef00000002l;
	private String nodeId = "";

	/**
	 * Constructor creates a new NoSuchNodeException with given message.
	 * @param g The graph where the node does not exist.
	 * @param nodeId Name of the non existing node.
	 */
	public NoSuchNodeException(IGraph<?, ?, ?> g, String nodeId) {
		super("Node '" + nodeId + "' does not exist in graph '" + g.getName() + "'");
		this.nodeId = nodeId;
	}

	/**
	 * Constructor creates a new NoSuchNodeException with a given cause.
	 * @param g The graph where the node does not exist.
	 * @param nodeId Name of the non existing node.
	 * @param cause The cause for this exception as Throwable.
	 */
	public NoSuchNodeException(IGraph<?, ?, ?> g, String nodeId, Throwable cause) {
		super("Node '" + nodeId + "' does not exist in graph '" + g.getName() + "'", cause);
		this.nodeId = nodeId;
	}

	/**
	 * Returns the id of the node which causes this exception.
	 * @return the id of the node causing the exception.
	 */
	public String getNodeId() {
		return nodeId;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
