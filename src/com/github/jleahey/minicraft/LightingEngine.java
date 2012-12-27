package com.github.jleahey.minicraft;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class LightingEngine implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public enum Direction {
		RIGHT, UP, LEFT, DOWN, SOURCE, WELL, UNKNOWN
	};
	
	public Direction[][] lightFlow;
	
	private int[][] lightValues;
	private int width, height;
	private Tile[][] tiles;
	
	public LightingEngine(int width, int height, Tile[][] tiles) {
		this.width = width;
		this.height = height;
		this.tiles = tiles;
		lightValues = new int[width][height];
		lightFlow = new Direction[width][height];
		init();
	}
	
	private void init() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				lightValues[x][y] = 0;
				lightFlow[x][y] = Direction.UNKNOWN;
			}
		}
		LinkedList<LightingPoint> sources = new LinkedList<LightingPoint>();
		for (int x = 0; x < width; x++) {
			sources.addAll(getSunSources(x));
		}
		spreadLightingDijkstra(sources);
	}
	
	public int getLightValue(int x, int y) {
		return lightValues[x][y];
	}
	
	public void removedTile(int x, int y) {
		spreadLightingDijkstra(getSunSources(x));
		spreadLightingDijkstra(new LightingPoint(x, y, Direction.UNKNOWN, lightValues[x][y])
				.getNeighbors(true, width, height));
	}
	
	public void addedTile(int x, int y) {
		// redo the column for sun
		boolean sun = true;
		for (int i = 0; i < height; i++) {
			if (tiles[x][i].type.lightBlocking != 0) {
				sun = false;
			}
			if (sun) {
				lightFlow[x][i] = Direction.SOURCE;
			} else {
				lightFlow[x][i] = Direction.UNKNOWN;
			}
		}
		lightFlow[x][y] = Direction.UNKNOWN;
		resetLighting(x, y);
	}
	
	public List<LightingPoint> getSunSources(int column) {
		LinkedList<LightingPoint> sources = new LinkedList<LightingPoint>();
		for (int y = 0; y < height - 1; y++) {
			if (tiles[column][y].type.lightBlocking != 0) {
				break;
			}
			sources.add(new LightingPoint(column, y, Direction.SOURCE, Constants.LIGHT_VALUE_SUN));
		}
		return sources;
	}
	
	public void resetLighting(int x, int y) {
		int left = Math.max(x - Constants.LIGHT_VALUE_SUN, 0);
		int right = Math.min(x + Constants.LIGHT_VALUE_SUN, width - 1);
		int top = Math.max(y - Constants.LIGHT_VALUE_SUN, 0);
		int bottom = Math.min(y + Constants.LIGHT_VALUE_SUN, height - 1);
		List<LightingPoint> sources = new LinkedList<LightingPoint>();
		
		// safely circle around the target zeroed zone
		boolean bufferLeft = (left > 0);
		boolean bufferRight = (right < width - 1);
		boolean bufferTop = (top > 0);
		boolean bufferBottom = (bottom < height - 1);
		if (bufferTop) {
			if (bufferLeft) {
				sources.add(getLightingPoint(left - 1, top - 1));
				zeroLightValue(left - 1, top - 1);
			}
			if (bufferRight) {
				sources.add(getLightingPoint(right + 1, top - 1));
				zeroLightValue(right + 1, top - 1);
			}
			for (int i = left; i <= right; i++) {
				sources.add(getLightingPoint(i, top - 1));
				zeroLightValue(i, top - 1);
			}
		}
		if (bufferBottom) {
			if (bufferLeft) {
				sources.add(getLightingPoint(left - 1, bottom + 1));
				zeroLightValue(left - 1, bottom + 1);
			}
			if (bufferRight) {
				sources.add(getLightingPoint(right + 1, bottom + 1));
				zeroLightValue(right + 1, bottom + 1);
			}
			for (int i = left; i <= right; i++) {
				sources.add(getLightingPoint(i, bottom + 1));
				zeroLightValue(i, bottom + 1);
			}
		}
		if (bufferLeft) {
			for (int i = top; i <= bottom; i++) {
				sources.add(getLightingPoint(left - 1, i));
				zeroLightValue(left - 1, i);
			}
		}
		if (bufferRight) {
			for (int i = top; i <= bottom; i++) {
				sources.add(getLightingPoint(right + 1, i));
				zeroLightValue(right + 1, i);
			}
		}
		for (int i = left; i <= right; i++) {
			for (int j = top; j <= bottom; j++) {
				if (lightFlow[i][j] == Direction.SOURCE) {
					sources.add(getLightingPoint(i, j));
				}
				lightValues[i][j] = 0;
			}
		}
		spreadLightingDijkstra(sources);
	}
	
	private void zeroLightValue(int x, int y) {
		lightValues[x][y] = 0;
	}
	
	private LightingPoint getLightingPoint(int x, int y) {
		return new LightingPoint(x, y, lightFlow[x][y], lightValues[x][y]);
	}
	
	public class LightingPoint {
		
		public int x, y, lightValue;
		public Direction flow;
		
		public LightingPoint(int x, int y, Direction flow, int lightValue) {
			this.x = x;
			this.y = y;
			this.flow = flow;
			this.lightValue = lightValue;
		}
		
		@Override
		public boolean equals(Object o) {
			LightingPoint other = (LightingPoint) o;
			return other.x == this.x && other.y == this.y;
		}
		
		public List<LightingPoint> getNeighbors(boolean additive, int width, int height) {
			List<LightingPoint> neighbors = new LinkedList<LightingPoint>();
			if (tiles[x][y].type.lightBlocking == Constants.LIGHT_VALUE_OPAQUE) {
				return neighbors;
			}
			int newValue = lightValue - 1 - tiles[x][y].type.lightBlocking;
			neighbors = getExactNeighbors(width, height, newValue);
			return neighbors;
		}
		
		public List<LightingPoint> getExactNeighbors(int width, int height, int lightingValue) {
			LinkedList<LightingPoint> neighbors = new LinkedList<LightingPoint>();
			if (x - 1 >= 0) {
				neighbors.add(new LightingPoint(x - 1, y, Direction.RIGHT, lightingValue));
			}
			if (x + 1 < width) {
				neighbors.add(new LightingPoint(x + 1, y, Direction.LEFT, lightingValue));
			}
			if (y - 1 >= 0) {
				neighbors.add(new LightingPoint(x, y - 1, Direction.DOWN, lightingValue));
			}
			if (y + 1 < height) {
				neighbors.add(new LightingPoint(x, y + 1, Direction.UP, lightingValue));
			}
			return neighbors;
		}
		
		@Override
		public int hashCode() {
			return x * 13 + y * 17;
			
		}
	}
	
	public class LightValueComparator implements Comparator<LightingPoint> {
		@Override
		public int compare(LightingPoint arg0, LightingPoint arg1) {
			if (arg0.lightValue < arg1.lightValue) {
				return 1;
			} else if (arg0.lightValue > arg1.lightValue) {
				return -1;
			}
			return 0;
		}
	}
	
	public static class LightPositionComparator implements Comparator<LightingPoint> {
		
		static final LightPositionComparator single = new LightPositionComparator();
		
		public static LightPositionComparator get() {
			return single;
		}
		
		@Override
		public int compare(LightingPoint arg0, LightingPoint arg1) {
			if (arg0.x < arg1.x) {
				return 1;
			} else if (arg0.x > arg1.x) {
				return -1;
			}
			
			if (arg0.y < arg1.y) {
				return 1;
			} else if (arg0.y > arg1.y) {
				return -1;
			}
			
			return 0;
		}
	}
	
	private void spreadLightingDijkstra(LightingPoint source) {
		LinkedList<LightingPoint> sources = new LinkedList<LightingPoint>();
		sources.add(source);
		spreadLightingDijkstra(sources);
	}
	
	private List<LightingPoint> spreadDarknessDijkstra(List<LightingPoint> wells) {
		LinkedList<LightingPoint> turningPoints = new LinkedList<LightingPoint>();
		PriorityQueue<LightingPoint> in = new PriorityQueue<LightingPoint>(wells.size(),
				new LightValueComparator());
		HashSet<LightingPoint> out = new HashSet<LightingPoint>();
		
		in.addAll(wells);
		out.addAll(wells);
		
		while (!in.isEmpty()) {
			LightingPoint current = in.poll();
			int x = current.x, y = current.y;
			if (current.flow != lightFlow[x][y] && lightFlow[x][y] != Direction.UNKNOWN) {
				current.lightValue = lightValues[x][y];
				turningPoints.add(current);
				out.add(current);
				continue;
			}
			for (LightingPoint next : current.getNeighbors(false, x, y)) {
				if (out.contains(next)) {
					continue;
				}
				in.add(next);
			}
			lightValues[x][y] = 0;
			out.add(current);
		}
		
		return turningPoints;
	}
	
	private void spreadLightingDijkstra(List<LightingPoint> sources) {
		HashSet<LightingPoint> out = new HashSet<LightingPoint>();
		PriorityQueue<LightingPoint> in = new PriorityQueue<LightingPoint>(sources.size(),
				new LightValueComparator());
		
		// consider that the input sources are done (this is not a good assumption if different
		// light sources have different values......)
		out.addAll(sources);
		
		in.addAll(sources);
		while (!in.isEmpty()) {
			LightingPoint current = in.poll();
			out.add(current);
			
			if (current.lightValue <= lightValues[current.x][current.y] || current.lightValue < 0) {
				continue;
			}
			lightValues[current.x][current.y] = current.lightValue;
			lightFlow[current.x][current.y] = current.flow;
			if (lightFlow[current.x][current.y] == Direction.SOURCE
					&& current.flow != Direction.SOURCE) {
				System.out.println("There's a bug in the source map!");
			}
			
			List<LightingPoint> neighbors = current.getNeighbors(true, width, height);
			for (LightingPoint next : neighbors) {
				if (out.contains(next)) {
					continue;
				}
				in.add(next);
			}
		}
	}
	
	public Direction opposite(Direction direction) {
		switch (direction) {
		case RIGHT:
			return Direction.LEFT;
		case LEFT:
			return Direction.RIGHT;
		case UP:
			return Direction.DOWN;
		case DOWN:
			return Direction.UP;
		default:
			return Direction.UNKNOWN;
		}
	}
	
	public Int2 followDirection(int x, int y, Direction direction) {
		switch (direction) {
		case RIGHT:
			return new Int2(x + 1, y);
		case LEFT:
			return new Int2(x - 1, y);
		case UP:
			return new Int2(x, y - 1);
		case DOWN:
			return new Int2(x, y + 1);
		default:
			return null;
		}
	}
}
