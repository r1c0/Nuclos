//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.

package org.nuclos.client.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.nuclos.common.collection.Pair;

public class Bubble extends Window implements AncestorListener, WindowListener {
	public static enum Position {
		UPPER(0.2, 0.1, 0.15) {
	        @Override
	        public Shape getShape(int width, int height, int arcSize) {
		        return new BubbleShape(this, width, height, arcSize);
	        }

			@Override
            public void relocate(Bubble bubble, Point parentLocation, Dimension parentSize, Dimension size) {
				bubble.setBounds(
					parentLocation.x + 10,
		    		parentLocation.y - size.height,
		    		size.width,
		    		size.height);
            }

			@Override
            public void translateForLabel(Graphics g, int width, int height, int arcSize) {
				g.translate(
					arcSize / 2, 
					arcSize / 2);
			}
        },
		NW(0.2, 0.1, 0.15) {
	        @Override
	        public Shape getShape(int width, int height, int arcSize) {
		        return new BubbleShape(this, width, height, arcSize);
	        }

			@Override
            public void relocate(Bubble bubble, Point parentLocation, Dimension parentSize, Dimension size) {
				bubble.setBounds(
					parentLocation.x + parentSize.width - 10,
		    		parentLocation.y + 10 - size.height,
		    		size.width,
		    		size.height);
            }

			@Override
            public void translateForLabel(Graphics g, int width, int height, int arcSize) {
				g.translate(
					arcSize / 2, 
					arcSize / 2);
			}
        },
        NO_ARROW_CENTER(0.0, 0.0, 0.0) {
	        @Override
	        public Shape getShape(int width, int height, int arcSize) {
	        	return new BubbleShapeWithoutArrow(this, width, height, arcSize);
	        }

			@Override
            public void relocate(Bubble bubble, Point parentLocation, Dimension parentSize, Dimension size) {
				bubble.setBounds(
					parentLocation.x + parentSize.width / 2 - size.width / 2,
					parentLocation.y + parentSize.height / 2 - size.height / 2,
					size.width,
					size.height);
            }

			@Override
            public void translateForLabel(Graphics g, int width, int height, int arcSize) {
				g.translate(
					arcSize / 2,
					arcSize / 2);
            }
        },
		SE(0.2, 0.75, 0.15) {
	        @Override
	        public Shape getShape(int width, int height, int arcSize) {
	        	return new BubbleShapeSE(this, width, height, arcSize);
	        }

			@Override
            public void relocate(Bubble bubble, Point parentLocation, Dimension parentSize, Dimension size) {
				bubble.setBounds(
					parentLocation.x - size.width + 10,
					parentLocation.y + parentSize.height - 10,
					size.width,
					size.height);
            }

			@Override
            public void translateForLabel(Graphics g, int width, int height, int arcSize) {
				g.translate(
					arcSize / 2,
					(int) (height * arrowRelLength + maxArcSize / 4));
            }
        };
    	public final double arrowRelLength;
    	public final double arrowRelPos;
    	public final double arrowRelWidth;
    	
    	private Position(double arrowRelLength, double arrowRelPos, double arrowRelWidth) {
    		this.arrowRelLength = arrowRelLength;
    		this.arrowRelPos = arrowRelPos;
    		this.arrowRelWidth = arrowRelWidth;
    	}
		
		public abstract Shape getShape(int width, int height, int arcSize);
		public abstract void relocate(Bubble bubble, Point parentLocation, Dimension parentSize, Dimension size);
		public abstract void translateForLabel(Graphics g, int width, int height, int arcSize);
	};

	private static final long serialVersionUID = 2444945538413906843L;

	private static int maxArcSize = 20;

	private static Color bubbleBorderColor = new Color(50, 50, 50);
	private static Color bubbleFillColor = new Color(255, 255, 160);

	private JComponent parent;
	private Window windowAncestor;
	
	private Shape bubbleShape;
	private JLabel textLabel = new JLabel();

	private boolean stayOnTop = true;
	private final Position  pos;

	public Bubble(String text) {
		this(null, text);
	}

	public Bubble(JComponent parent, String text) {
		this(parent, text, null);
	}

	public Bubble(JComponent parent, String text, Integer timeout) {
		this(parent, text, timeout, Position.NW);
	}
	
	public Bubble(JComponent parent, String text, Integer timeout, Position pos) {
		super(null);
		this.pos = pos;
		textLabel.setFont(new Font("System", Font.PLAIN, 11));
		setText(text);
		setAlwaysOnTop(stayOnTop);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) { Bubble.this.dispose(); }
		});
		if(parent != null) {
			this.parent = parent;
			parent.addAncestorListener(this);
			this.windowAncestor = SwingUtilities.getWindowAncestor(parent);
			if(windowAncestor != null) {
				windowAncestor.addWindowListener(this);
			}
			relocate(parent);
		}
		if(timeout != null) {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					dispose();
					timer.cancel();
				}}, timeout * 1000);
		}
	}

	@Override
	public void dispose() {
		if (parent != null) {
			parent.removeAncestorListener(this);
		}
		if (windowAncestor != null) {
			windowAncestor.removeWindowListener(this);
		}
		super.dispose();
	}
	
	public void setText(String text) {
		textLabel.setText(text);
		Dimension textSize = textLabel.getPreferredSize();
		setSize(
			textSize.width + 2 * maxArcSize,
			textSize.height + 2 * maxArcSize + (int) (textSize.height * pos.arrowRelLength));
	}

	@Override
	public void setSize(Dimension d) {
		super.setSize(d);
		initShapeAndOpacity();
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		initShapeAndOpacity();
	}

	public void setStayOnTop(boolean stayOnTop) {
		this.stayOnTop = stayOnTop;
	}

	private void initShapeAndOpacity() {
		bubbleShape = pos.getShape(getWidth()-3, getHeight()-3, maxArcSize);
		UIUtils.setWindowOpacity(Bubble.this, 0.9f);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(bubbleFillColor);
		g2.fill(bubbleShape);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
		//g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		BasicStroke bs = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
		g2.setStroke(bs);
		
		g2.setColor(bubbleBorderColor);
		g2.draw(bubbleShape);

		Graphics gtl = g2.create();
		pos.translateForLabel(gtl, getWidth(), getHeight(), maxArcSize);
		
		gtl.translate(
			Math.min(Math.min(getWidth(), getHeight()), maxArcSize)/2, 
			Math.min(Math.min(getWidth(), getHeight()), maxArcSize)/4);

		textLabel.setSize(textLabel.getPreferredSize());
		textLabel.paint(gtl);
	}

	private void relocate(Component parent) {
		try {
			pos.relocate(this, parent.getLocationOnScreen(), parent.getSize(), getSize());
		} catch (IllegalComponentStateException ex) {
			dispose();
		}
    }

	@Override
	public void ancestorAdded(AncestorEvent event) {}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
		Bubble.this.dispose();
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
		relocate(event.getComponent());
	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {
		Bubble.this.dispose();
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
		Bubble.this.dispose();
	}
	
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		Bubble.this.dispose();
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}
	
	private static class BubbleShape extends java.awt.geom.RoundRectangle2D.Float {
		private static final long serialVersionUID = 8895815821355924870L;
		private Polygon arrow;

		public BubbleShape(Position pos, int width, int height, int arcSize) {
			this(pos, 1, 1, width, height, Math.min(Math.min(width, height), arcSize));
		}

		public BubbleShape(Position pos, int i, int j, int width, int height, int arcSize) {
			super(i, j, width, (int)(height - (height * pos.arrowRelLength)), arcSize, arcSize);

			Point p1 = new Point(0, 0);
			Point p2 = new Point(width - arcSize, height - (int)(height * pos.arrowRelLength) - arcSize);

			int[] xPoints = new int[] { 
				(int)Math.max((width * pos.arrowRelPos), maxArcSize/2), 
				p1.x, 
				(int)(Math.max((width * pos.arrowRelPos), maxArcSize/2) + (width * pos.arrowRelWidth)) };
			int[] yPoints = new int[] { 
				p2.y + arcSize, 
				p2.y + arcSize + (int)(height * pos.arrowRelLength), 
				p2.y + arcSize };
			arrow = new Polygon(xPoints, yPoints, xPoints.length);
		}

		@Override
		public boolean contains(double x, double y) {
			return super.contains(x, y) || arrow.contains(x, y);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			return super.contains(x, y, w, h) || arrow.contains(x, y, w, h);
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			return super.intersects(x, y, w, h) || arrow.intersects(x, y, w, h);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			return new BubblePathIterator(super.getPathIterator(at), arrow);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			return new BubblePathIterator(super.getPathIterator(at, flatness), arrow);
		}
		
		private static class BubblePathIterator implements PathIterator {
			private List<Pair<Integer, double[]>> l = new ArrayList<Pair<Integer, double[]>>();
			private int index;

			public BubblePathIterator(PathIterator parent, Polygon arrow) {
				while(!parent.isDone()) {
					double[] values = new double[6];
					int t = parent.currentSegment(values);
					l.add(new Pair<Integer, double[]>(t, values));
					parent.next();
				}
				l.add(3, new Pair<Integer, double[]>(PathIterator.SEG_LINETO, new double[] { arrow.xpoints[0], arrow.ypoints[0], 0.0, 0.0, 0.0, 0.0 }));
				l.add(4, new Pair<Integer, double[]>(PathIterator.SEG_LINETO, new double[] { arrow.xpoints[1], arrow.ypoints[1], 0.0, 0.0, 0.0, 0.0 }));
				l.add(5, new Pair<Integer, double[]>(PathIterator.SEG_LINETO, new double[] { arrow.xpoints[2], arrow.ypoints[2]-1, 0.0, 0.0, 0.0, 0.0 }));
				index = 0;
			}

			@Override
			public int getWindingRule() {
				return PathIterator.WIND_EVEN_ODD;
			}

			@Override
			public boolean isDone() {
				return index >= l.size();
			}

			@Override
			public void next() {
				index++;
			}

			@Override
			public int currentSegment(float[] coords) {
				Pair<Integer, double[]> pair = l.get(index);
				for(int i=0; i<6; i++)
					coords[i] = (float) pair.y[i];
				return pair.x;
			}

			@Override
			public int currentSegment(double[] coords) {
				Pair<Integer, double[]> pair = l.get(index);
				System.arraycopy(pair.y, 0, coords, 0, 6);
				return pair.x;
			}
		}
	}

	
	private static class BubbleShapeSE extends java.awt.geom.RoundRectangle2D.Float {
		private static final long serialVersionUID = 8895815821355924870L;
		private Polygon arrow;

		public BubbleShapeSE(Position pos, int width, int height, int arcSize) {
			this(pos, 1, 1, width, height, Math.min(Math.min(width, height), arcSize));
		}

		public BubbleShapeSE(Position pos, int x, int y, int width, int height, int arcSize) {
			super(x, (int) (height * pos.arrowRelLength),
				width, (int) (height - (height * pos.arrowRelLength)),
				arcSize, arcSize);

			int topY = (int) (height * pos.arrowRelLength)/* - arcSize*/;
			int[] xPoints = new int[] {
				(int) (width * pos.arrowRelPos),
				width,
				(int) ((width * pos.arrowRelPos) - (width * pos.arrowRelWidth)) };
			int[] yPoints = new int[] {
				topY,
				0,
				topY };
			arrow = new Polygon(xPoints, yPoints, xPoints.length);
		}

		@Override
		public boolean contains(double x, double y) {
			return super.contains(x, y) || arrow.contains(x, y);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			return super.contains(x, y, w, h) || arrow.contains(x, y, w, h);
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			return super.intersects(x, y, w, h) || arrow.intersects(x, y, w, h);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			return new BubblePathIteratorSE(super.getPathIterator(at), arrow);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			return new BubblePathIteratorSE(super.getPathIterator(at, flatness), arrow);
		}
		
		private static class BubblePathIteratorSE implements PathIterator {
			private List<Pair<Integer, double[]>> l = new ArrayList<Pair<Integer, double[]>>();
			private int index;

			public BubblePathIteratorSE(PathIterator parent, Polygon arrow) {
				while(!parent.isDone()) {
					double[] values = new double[6];
					int t = parent.currentSegment(values);
					l.add(new Pair<Integer, double[]>(t, values));
					parent.next();
				}
				l.add(7, new Pair<Integer, double[]>(PathIterator.SEG_LINETO, new double[] { arrow.xpoints[0], arrow.ypoints[0], 0.0, 0.0, 0.0, 0.0 }));
				l.add(8, new Pair<Integer, double[]>(PathIterator.SEG_LINETO, new double[] { arrow.xpoints[1], arrow.ypoints[1], 0.0, 0.0, 0.0, 0.0 }));
				l.add(9, new Pair<Integer, double[]>(PathIterator.SEG_LINETO, new double[] { arrow.xpoints[2], arrow.ypoints[2], 0.0, 0.0, 0.0, 0.0 }));
				index = 0;
			}

			@Override
			public int getWindingRule() {
				return PathIterator.WIND_EVEN_ODD;
			}

			@Override
			public boolean isDone() {
				return index >= l.size();
			}

			@Override
			public void next() {
				index++;
			}

			@Override
			public int currentSegment(float[] coords) {
				Pair<Integer, double[]> pair = l.get(index);
				for(int i=0; i<6; i++)
					coords[i] = (float) pair.y[i];
				return pair.x;
			}

			@Override
			public int currentSegment(double[] coords) {
				Pair<Integer, double[]> pair = l.get(index);
				System.arraycopy(pair.y, 0, coords, 0, 6);
				return pair.x;
			}
		}
	}
	
	private static class BubbleShapeWithoutArrow extends java.awt.geom.RoundRectangle2D.Float {
		private static final long serialVersionUID = 8895815821355924870L;

		public BubbleShapeWithoutArrow(Position pos, int width, int height, int arcSize) {
			this(pos, 1, 1, width, height, Math.min(Math.min(width, height), arcSize));
		}

		public BubbleShapeWithoutArrow(Position pos, int i, int j, int width, int height, int arcSize) {
			super(i, j, width, height, arcSize, arcSize);
		}

		@Override
		public boolean contains(double x, double y) {
			return super.contains(x, y);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			return super.contains(x, y, w, h);
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			return super.intersects(x, y, w, h);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			return new BubblePathIterator(super.getPathIterator(at));
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			return new BubblePathIterator(super.getPathIterator(at, flatness));
		}
		
		private static class BubblePathIterator implements PathIterator {
			private List<Pair<Integer, double[]>> l = new ArrayList<Pair<Integer, double[]>>();
			private int index;

			public BubblePathIterator(PathIterator parent) {
				while(!parent.isDone()) {
					double[] values = new double[6];
					int t = parent.currentSegment(values);
					l.add(new Pair<Integer, double[]>(t, values));
					parent.next();
				}
				index = 0;
			}

			@Override
			public int getWindingRule() {
				return PathIterator.WIND_EVEN_ODD;
			}

			@Override
			public boolean isDone() {
				return index >= l.size();
			}

			@Override
			public void next() {
				index++;
			}

			@Override
			public int currentSegment(float[] coords) {
				Pair<Integer, double[]> pair = l.get(index);
				for(int i=0; i<6; i++)
					coords[i] = (float) pair.y[i];
				return pair.x;
			}

			@Override
			public int currentSegment(double[] coords) {
				Pair<Integer, double[]> pair = l.get(index);
				System.arraycopy(pair.y, 0, coords, 0, 6);
				return pair.x;
			}
		}
	}
	
	public static void main(String[] args) {
		final String text = "<html>"
			+ "Das Hühnchenfleisch in ca. 2 cm große Würfel schneiden und zusammen<br>"
			+ "mit Weißwein, Rosmarin, Salz und Pfeffer in eine Schüssel<br>"
			+ "geben. Abdecken und mindestens 1 Stunde marinieren.  Danach das<br>"
			+ "Fleisch abtropfen lassen und auf 8 Spieße stecken, die Marinade<br>"
			+ "aufheben. Die Spieße werden nun 10 min. auf dem Grill (falls keiner<br>"
			+ "vorhanden unter dem vorgeheizten Backofengrill) gegrillt.<br>"
			+ "</html>";
		
		
		JFrame f = new JFrame();
		f.add(
			new JButton(
				new AbstractAction("Show") {
					@Override
                    public void actionPerformed(ActionEvent e) {
						new Bubble(
							(JComponent) e.getSource(),
							text,
							10,
							Bubble.Position.SE)
						.setVisible(true);
					}
				}
			),
			BorderLayout.CENTER);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(600, 400, 200, 100);
		f.setVisible(true);
	}

}
