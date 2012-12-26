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
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				lightValues[x][y] = 0;
		LinkedList<LightingPoint> sources = new LinkedList<LightingPoint>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height - 1; y++) {
				if (tiles[x][y].type.lightBlocking != 0) {
					break;
				}
				sources.add(new LightingPoint(x, y, Direction.SOURCE, Constants.LIGHT_VALUE_SUN));
			}
		}
		spreadLightingDijkstra(sources, true);
	}
	
	public int getLightValue(int x, int y) {
		return lightValues[x][y];
	}
	
	public void removedTile(int x, int y) {
		spreadLightingDijkstra(getSunSources(x), true);
		spreadLightingDijkstra(
				new LightingPoint(x, y, Direction.UNKNOWN, lightValues[x][y]).getNeighbors(true,
						width, height), true);
	}
	
	public void addedTile(int x, int y) {
		lightFlow[x][y] = Direction.UNKNOWN;
		// resetLighting(x, y);
		
		List<LightingPoint> wells = new LightingPoint(x, y, Direction.WELL, 0).getExactNeighbors(
				width, height, 0);
		lightValues[x][y] = 0;
		for (LightingPoint well : wells) {
			well.lightValue = 0;
			if (tiles[well.x][well.y].type.lightBlocking != Constants.LIGHT_VALUE_OPAQUE) {
				spreadLightingDijkstra(well, false);
			}
		}
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
	
	/*
	 * This function works but is totally useless
	 */
	public void resetLighting(int x, int y) {
		int left = Math.max(x - Constants.LIGHT_VALUE_SUN, 0);
		int right = Math.min(x + Constants.LIGHT_VALUE_SUN, width - 1);
		int top = Math.max(y - Constants.LIGHT_VALUE_SUN, 0);
		int bottom = Math.min(y + Constants.LIGHT_VALUE_SUN, height - 1);
		List<LightingPoint> sources = new LinkedList<LightingPoint>();
		
		for (int i = left; i <= right; i++) {
			for (int j = top; j <= bottom; j++) {
				if (lightFlow[i][j] == Direction.SOURCE)
					sources.add(new LightingPoint(i, j, Direction.SOURCE, lightValues[i][j]));
				lightValues[i][j] = 0;
			}
		}
		spreadLightingDijkstra(sources, true);
	}
	
	public class LightingPoint { // implements Comparable<LightingPoint> {
	
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
			return other.x == this.x && other.y == this.y && other.lightValue == this.lightValue
					&& other.flow == this.flow;
		}
		
		public List<LightingPoint> getNeighbors(boolean additive, int width, int height) {
			List<LightingPoint> neighbors = new LinkedList<LightingPoint>();
			if (tiles[x][y].type.lightBlocking == Constants.LIGHT_VALUE_OPAQUE) {
				return neighbors;
			}
			int newValue = lightValue - 1 - tiles[x][y].type.lightBlocking;
			if (!additive)
				newValue = -newValue;
			
			neighbors = getExactNeighbors(width, height, newValue);
			// if (additive) {
			
			// } else {
			// if (x - 1 >= 0) {
			// neighbors.add(new LightingPoint(x - 1, y));
			// }
			// if (x + 1 < width) {
			// neighbors.add(new LightingPoint(x + 1, y));
			// }
			// if (y - 1 >= 0) {
			// neighbors.add(new LightingPoint(x, y - 1));
			// }
			// if (y + 1 < height) {
			// neighbors.add(new LightingPoint(x, y + 1));
			// }
			// }
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
			return x * 13 + y * 17 + lightValue * 23 + flow.ordinal() * 29;
			
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
			if (arg0.x < arg1.x)
				return 1;
			else if (arg0.x > arg1.x) {
				return -1;
			}
			
			if (arg0.y < arg1.y)
				return 1;
			else if (arg0.y > arg1.y) {
				return -1;
			}
			
			return 0;
		}
	}
	
	private void spreadLightingDijkstra(LightingPoint source, boolean additive) {
		LinkedList<LightingPoint> sources = new LinkedList<LightingPoint>();
		sources.add(source);
		spreadLightingDijkstra(sources, additive);
	}
	
	private void spreadLightingDijkstra(List<LightingPoint> sources, boolean additive) {
		HashSet<LightingPoint> out = new HashSet<LightingPoint>();
		PriorityQueue<LightingPoint> in;
		if (additive)
			in = new PriorityQueue<LightingPoint>(sources.size(), new LightValueComparator());
		else
			in = new PriorityQueue<LightingPoint>(sources.size(), new LightValueComparator());
		List<LightingPoint> turningPoints = new LinkedList<LightingPoint>();
		
		in.addAll(sources);
		while (!in.isEmpty()) {
			LightingPoint current = in.poll();
			if (additive) {
				lightFlow[current.x][current.y] = current.flow;
			}
			if (additive
					&& (current.lightValue <= lightValues[current.x][current.y] || current.lightValue < 0)) {
				continue;
			}
			if (!additive && current.lightValue <= -Constants.LIGHT_VALUE_SUN) {
				continue;
			}
			
			List<LightingPoint> neighbors = current.getNeighbors(additive, width, height);
			for (LightingPoint next : neighbors) {
				// for (LightingPoint o : out) {
				// if (next.equals(o)) {
				// continue nexter;
				// }
				// }
				if (out.contains(next)) {
					continue;
				}
				
				if (!additive) {
					
					if (lightFlow[next.x][next.y] != next.flow
							|| lightFlow[next.x][next.y] == Direction.SOURCE) {
						
						// if ((lightValues[current.x][current.y] < lightValues[next.x][next.y] &&
						// lightValues[current.x][current.y] != 0)
						// || lightFlow[current.x][current.y] == Direction.SOURCE) {
						turningPoints.add(new LightingPoint(next.x, next.y, next.flow,
								lightValues[next.x][next.y]));
						out.add(next);
						continue;
						// while (!in.isEmpty()) {
						// LightingPoint leftover = in.poll();
						// lightValues[leftover.x][leftover.y] = 0;
						// }
						// lightValues[current.x][current.y] = current.lightValue;
						// spreadLightingDijkstra(next, true);
						// return;
					}
				}
				in.add(next);
			}
			if (additive) {
				lightValues[current.x][current.y] = current.lightValue;
			} else {
				if (lightFlow[current.x][current.y] != Direction.SOURCE)
					lightValues[current.x][current.y] = 0;
			}
			out.add(current);
		}
		if (!additive) {
			if (turningPoints.size() == 0) {
				return;
			}
			System.out.println(turningPoints.size() + " turning points.");
			spreadLightingDijkstra(turningPoints, true);
		}
		System.out.println("Updated " + out.size() + "/" + width * height + " tiles");
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
}
