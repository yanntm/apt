/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.util.ArrayList;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * An abstract region of a LTS. This assigns to each event a backward and forward number.
 * @author Uli Schlachter
 */
public class Region {
	private final RegionUtility utility;
	private final List<BigInteger> backwardWeights;
	private final List<BigInteger> forwardWeights;
	private BigInteger initialMarking;
	private final Map<State, BigInteger> stateMarkingCache = new HashMap<>();

	/**
	 * Create a new region.
	 * @param utility The RegionUtility instance that supports this region.
	 * @param backwardWeights List of weights for the backward weight of each event.
	 * @param forwardWeights List of weights for the forward weights of each event.
	 * @param initialMarking Initial marking, or null if one should be calculated.
	 */
	public Region(RegionUtility utility, List<BigInteger> backwardWeights, List<BigInteger> forwardWeights,
			BigInteger initialMarking) {
		this.utility = utility;
		this.backwardWeights = Collections.unmodifiableList(new ArrayList<>(backwardWeights));
		this.forwardWeights = Collections.unmodifiableList(new ArrayList<>(forwardWeights));
		this.initialMarking = initialMarking;

		int numberEvents = utility.getNumberOfEvents();
		if (backwardWeights.size() != numberEvents)
			throw new IllegalArgumentException("There must be as many backward weights as events");
		if (forwardWeights.size() != numberEvents)
			throw new IllegalArgumentException("There must be as many forward weights as events");
		for (BigInteger i : backwardWeights)
			if (i.compareTo(BigInteger.ZERO) < 0)
				throw new IllegalArgumentException("Backward weight i=" + i + " must not be negative");
		for (BigInteger i : forwardWeights)
			if (i.compareTo(BigInteger.ZERO) < 0)
				throw new IllegalArgumentException("Forward weight i=" + i + " must not be negative");
		if (initialMarking != null && initialMarking.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException("Initial marking " + initialMarking + " must not be negative");
	}

	/**
	 * Create a new region.
	 * @param utility The RegionUtility instance that supports this region.
	 * @param backwardWeights List of weights for the backward weight of each event.
	 * @param forwardWeights List of weights for the forward weights of each event.
	 */
	public Region(RegionUtility utility, List<BigInteger> backwardWeights, List<BigInteger> forwardWeights) {
		this(utility, backwardWeights, forwardWeights, (BigInteger) null);
	}

	/**
	 * Return the region utility which this region uses.
	 * @return The region utility on which this region is defined.
	 */
	public RegionUtility getRegionUtility() {
		return utility;
	}

	/**
	 * Return the transitions system on which this region is defined.
	 * @return The transition system on which this region is defined.
	 */
	public TransitionSystem getTransitionSystem() {
		return utility.getTransitionSystem();
	}

	/**
	 * Return the backward weight for the given event index.
	 * @param index The event index to query.
	 * @return the backwards weight of the given event.
	 */
	public BigInteger getBackwardWeight(int index) {
		return backwardWeights.get(index);
	}

	/**
	 * Return the backward weight for the given event.
	 * @param event The event to query.
	 * @return the backwards weight of the given event.
	 */
	public BigInteger getBackwardWeight(String event) {
		return backwardWeights.get(utility.getEventIndex(event));
	}

	/**
	 * Return the forward weight for the given event index.
	 * @param index The event index to query.
	 * @return the forwards weight of the given event.
	 */
	public BigInteger getForwardWeight(int index) {
		return forwardWeights.get(index);
	}

	/**
	 * Return the forward weight for the given event.
	 * @param event The event to query.
	 * @return the forwards weight of the given event.
	 */
	public BigInteger getForwardWeight(String event) {
		return forwardWeights.get(utility.getEventIndex(event));
	}

	/**
	 * Return the total weight for the given event index.
	 * @param index The event index to query.
	 * @return the total weight of the given event.
	 */
	public BigInteger getWeight(int index) {
		return getForwardWeight(index).subtract(getBackwardWeight(index));
	}

	/**
	 * Return the total weight for the given event.
	 * @param event The event to query.
	 * @return the total weight of the given event.
	 */
	public BigInteger getWeight(String event) {
		return getWeight(utility.getEventIndex(event));
	}

	/**
	 * Evaluate the given Parikh vector with respect to this region.
	 * @param vector The vector to evaluate.
	 * @return The resulting number that this region assigns to the arguments
	 */
	public BigInteger evaluateParikhVector(List<BigInteger> vector) {
		assert vector.size() == utility.getEventList().size();

		BigInteger result = BigInteger.ZERO;
		for (int i = 0; i < vector.size(); i++)
			result = result.add(vector.get(i).multiply(getWeight(i)));

		return result;
	}

	/**
	 * Return the initial marking of this region.
	 * @return The initial marking of this region.
	 */
	public BigInteger getInitialMarking() {
		if (this.initialMarking == null) {
			this.initialMarking = getNormalRegionMarking();
			assert this.initialMarking.compareTo(BigInteger.ZERO) >= 0;
		}
		return this.initialMarking;
	}

	/**
	 * Get the marking that a normal region based on this abstract would assign to the initial state.
	 * @return The resulting number.
	 */
	public BigInteger getNormalRegionMarking() {
		BigInteger marking = BigInteger.ZERO;
		for (State state : getTransitionSystem().getNodes()) {
			try {
				BigInteger value = evaluateParikhVector(utility.getReachingParikhVector(state));
				marking = marking.max(value.negate());
			} catch (UnreachableException e) {
				continue;
			}
		}
		return marking;
	}

	/**
	 * Get the marking that a normal region based on this abstract would assign to the given state.
	 * @param state The state to evaluate. Must be reachable from the initial state.
	 * @return The resulting number.
	 * @throws UnreachableException if the given state is unreachable from the initial state
	 */
	public BigInteger getMarkingForState(State state) throws UnreachableException {
		BigInteger i = stateMarkingCache.get(state);
		if (i == null) {
			i = getInitialMarking().add(evaluateParikhVector(utility.getReachingParikhVector(state)));
			stateMarkingCache.put(state, i);
		}
		return i;
	}

	/**
	 * Add a multiple of another region to this region and return the result.
	 * @param otherRegion The region to add to this one.
	 * @param factor A factor with which the other region is multiplied before addition.
	 * @return The resulting region.
	 */
	public Region addRegionWithFactor(Region otherRegion, int factor) {
		return this.addRegionWithFactor(otherRegion, BigInteger.valueOf(factor));
	}

	/**
	 * Add a multiple of another region to this region and return the result.
	 * @param otherRegion The region to add to this one.
	 * @param factor A factor with which the other region is multiplied before addition.
	 * @return The resulting region.
	 */
	public Region addRegionWithFactor(Region otherRegion, BigInteger factor) {
		assert otherRegion.utility == utility;

		if (factor.equals(BigInteger.ZERO))
			return this;

		List<BigInteger> backwardList = new ArrayList<>(utility.getNumberOfEvents());
		List<BigInteger> forwardList = new ArrayList<>(utility.getNumberOfEvents());

		for (int i = 0; i < utility.getNumberOfEvents(); i++) {
			BigInteger backward = this.backwardWeights.get(i);
			BigInteger forward = this.forwardWeights.get(i);
			if (factor.compareTo(BigInteger.ZERO) > 0) {
				backward = backward.add(factor.multiply(otherRegion.getBackwardWeight(i)));
				forward = forward.add(factor.multiply(otherRegion.getForwardWeight(i)));
			} else {
				forward = forward.add(factor.negate().multiply(otherRegion.getBackwardWeight(i)));
				backward = backward.add(factor.negate().multiply(otherRegion.getForwardWeight(i)));
			}
			backwardList.add(backward);
			forwardList.add(forward);
		}

		return new Region(utility, backwardList, forwardList);
	}

	/**
	 * Add a multiple of another region to this region and return the result.
	 * @param otherRegion The region to add to this one.
	 * @return The resulting region.
	 */
	public Region addRegion(Region otherRegion) {
		return addRegionWithFactor(otherRegion, 1);
	}

	/**
	 * Turn this region into a pure region by enforcing that for every event, at least one of the forward or
	 * backward weight must be zero.
	 * @return The resulting region.
	 */
	public Region makePure() {
		List<BigInteger> vector = new ArrayList<>(utility.getNumberOfEvents());

		for (int i = 0; i < utility.getNumberOfEvents(); i++) {
			vector.add(getWeight(i));
		}

		return Builder.createPure(utility, vector).withNormalRegionInitialMarking();
	}

	/**
	 * Create a new region that is a copy of this region, but with the specified initial marking.
	 * @param initial The initial marking for the new region.
	 * @return The new region.
	 */
	public Region withInitialMarking(BigInteger initial) {
		return new Region(utility, backwardWeights, forwardWeights, initial);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{ init=");
		result.append(getInitialMarking());
		for (String event : utility.getEventList()) {
			result.append(", ");
			result.append(getBackwardWeight(event));
			result.append(":");
			result.append(event);
			result.append(":");
			result.append(getForwardWeight(event));
		}
		result.append(" }");
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Region))
			return false;
		if (obj == this)
			return true;
		Region reg = (Region) obj;
		return reg.utility.equals(utility) &&
			reg.forwardWeights.equals(forwardWeights) &&
			reg.backwardWeights.equals(backwardWeights) &&
			reg.getInitialMarking().equals(getInitialMarking());
	}

	@Override
	public int hashCode() {
		return 31 * (forwardWeights.hashCode() + 31 * backwardWeights.hashCode()) + getInitialMarking().hashCode();
	}

	/**
	 * Helper class for creating Region instances.
	 */
	static public class Builder {
		private final RegionUtility utility;
		private final List<BigInteger> backwardList;
		private final List<BigInteger> forwardList;

		/**
		 * Create a builder for the region with the given weights.
		 * @param utility The region utility that should be used.
		 * @param backward The backward weights.
		 * @param forward The forward weights.
		 */
		public Builder(RegionUtility utility, List<BigInteger> backward, List<BigInteger> forward) {
			if (backward.size() != utility.getNumberOfEvents())
				throw new IllegalArgumentException("The backward list must contain one entry per event");
			if (forward.size() != utility.getNumberOfEvents())
				throw new IllegalArgumentException("The forward list must contain one entry per event");
			this.utility = utility;
			this.backwardList = new ArrayList<>(backward);
			this.forwardList = new ArrayList<>(forward);
		}

		/**
		 * Create a builder for the region which, for now, assigns weight 0 to everything.
		 * @param utility The region utility that should be used.
		 */
		public Builder(RegionUtility utility) {
			this(utility, Collections.nCopies(utility.getNumberOfEvents(), BigInteger.ZERO),
					Collections.nCopies(utility.getNumberOfEvents(), BigInteger.ZERO));
		}

		/**
		 * Create a builder and initialize the weights from the given region.
		 * @param region The region to copy.
		 */
		public Builder(Region region) {
			this(region.utility, region.backwardWeights, region.forwardWeights);
		}

		/**
		 * Add a loop with the given weight around the given event. This means that the backward weight and the
		 * forward weight are both increased by the given weight.
		 * @param event The event on which a loop should be added.
		 * @param weight The weight that should be added.
		 * @return This builder instance.
		 */
		public Builder addLoopAround(String event, BigInteger weight) {
			return addLoopAround(utility.getEventIndex(event), weight);
		}

		/**
		 * Add a loop with the given weight around the given event. This means that the backward weight and the
		 * forward weight are both increased by the given weight.
		 * @param event The event on which a loop should be added.
		 * @param weight The weight that should be added.
		 * @return This builder instance.
		 */
		public Builder addLoopAround(int index, BigInteger weight) {
			backwardList.set(index, backwardList.get(index).add(weight));
			forwardList.set(index, forwardList.get(index).add(weight));
			return this;
		}

		/**
		 * Add the weights of a region with some factor applied to our current state. Adding a region with a
		 * factor of one means that its backward and forward weights get added to our backward and forward
		 * weights. Adding a region with a factor of minus one means that its backward weights get added to our
		 * forward weight, and vice versa.
		 * @param region The region to add.
		 * @param factor The factor that should be used.
		 * @return This builder instance.
		 */
		public Builder addRegionWithFactor(Region region, BigInteger factor) {
			if (factor.equals(BigInteger.ZERO))
				return this;

			List<BigInteger> theirBackwardWeights = region.backwardWeights;
			List<BigInteger> theirForwardWeights = region.forwardWeights;

			// If the factor is negative, swap the weights and negate the factor
			if (factor.compareTo(BigInteger.ZERO) < 0) {
				factor = factor.negate();
				List<BigInteger> tmp = theirBackwardWeights;
				theirBackwardWeights = theirForwardWeights;
				theirForwardWeights = tmp;
			}

			// Do the addition
			for (int i = 0; i < utility.getNumberOfEvents(); i++) {
				BigInteger weight = backwardList.get(i);
				backwardList.set(i, weight.add(factor.multiply(theirBackwardWeights.get(i))));

				weight = forwardList.get(i);
				forwardList.set(i, weight.add(factor.multiply(theirForwardWeights.get(i))));
			}
			return this;
		}

		/**
		 * Turn this builder's weight into the weight for a normal region. This modifies the weight so that the
		 * effect of each event is still the same, but at least one of the forward or the backward weights are
		 * zero.
		 * @return This builder instance.
		 */
		public Builder makePure() {
			for (int i = 0; i < utility.getNumberOfEvents(); i++) {
				BigInteger weight = forwardList.get(i).subtract(backwardList.get(i));
				if (weight.compareTo(BigInteger.ZERO) >= 0) {
					forwardList.set(i, weight);
					backwardList.set(i, BigInteger.ZERO);
				} else {
					forwardList.set(i, BigInteger.ZERO);
					backwardList.set(i, weight.negate());
				}
			}
			return this;
		}

		/**
		 * Create a region from the current state of the builder and the given initial marking.
		 * @param initial The initial marking of the region.
		 * @return A new region corresponding to the weights that are currently in this builder.
		 */
		public Region withInitialMarking(BigInteger initial) {
			return new Region(utility, backwardList, forwardList, initial);
		}

		/**
		 * Create a region from the current state of this builder and the initial marking that a normal region
		 * would have. Please note that the normal region marking is only valid for pure region. Also, of course
		 * the region has to be cycle-consistent (going through a cycle reaches the same value again).
		 * @return a new region corresponding to the weights that are currently in this builder.
		 */
		public Region withNormalRegionInitialMarking() {
			int numEvents = utility.getNumberOfEvents();
			BigInteger initial = BigInteger.ZERO;
			for (State state : utility.getTransitionSystem().getNodes()) {
				try {
					BigInteger value = BigInteger.ZERO;
					List<BigInteger> pv = utility.getReachingParikhVector(state);
					for (int i = 0; i < numEvents; i++)
						value = value.add(pv.get(i).multiply(forwardList.get(i).subtract(backwardList.get(i))));
					initial = initial.max(value.negate());
				} catch (UnreachableException e) {
					continue;
				}
			}
			return withInitialMarking(initial);
		}

		/**
		 * Create a new region builder for a pure region with the given weights.
		 * @param utility The region utility that should be used.
		 * @param vector A vector which contains the weight for each event in the order described by utility.
		 * @return A region builder containing the given weights.
		 */
		static public Builder createPure(RegionUtility utility, List<BigInteger> vector) {
			if (vector.size() != utility.getNumberOfEvents())
				throw new IllegalArgumentException("The vector must contain one entry per event");

			Builder result = new Builder(utility);
			for (int i = 0; i < vector.size(); i++) {
				BigInteger value = vector.get(i);
				if (value.compareTo(BigInteger.ZERO) > 0)
					result.forwardList.set(i, value);
				else
					result.backwardList.set(i, value.negate());
			}
			return result;
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
