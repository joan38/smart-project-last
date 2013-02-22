package fr.umlv.lastproject.smart.layers;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import fr.umlv.lastproject.smart.R;
import fr.umlv.lastproject.smart.layers.Geometry.GeometryType;

/**
 * This class represent the geometry layer and draw it if it is contained in the
 * screen
 * 
 * @author Fad's, thibault brun
 * 
 */
public class GeometryLayer extends Overlay implements Layer {

	private static final double VALUE_1E6 = 1E6;
	private GeometryType type;
	private final List<Geometry> geometries;
	private Projection projection;
	private Paint paint;
	private Symbology symbology;
	private boolean editable = false;
	private String name;
	private final List<GeometryLayerSingleTapListener> singleTapListeners = new ArrayList<GeometryLayerSingleTapListener>();
	private final List<GeometryLayerDoubleTapListener> doubleTapListeners = new ArrayList<GeometryLayerDoubleTapListener>();
	private final Context context;

	private static final float cX = 24;
	private static final float cY = 24;
	private static final float radius = 12;
	private static final float lineStopX = 48;
	private static final float lineStopY = 48;
	private static final float rectLeft = 6;
	private static final float rectTop = 6;
	private static final float rectRight = 42;
	private static final float rectBottom = 42;
	private static final float strokeWidth = 5;

	/**
	 * 
	 * @param ctx
	 *            : context for the GeometryLayer
	 * 
	 */
	public GeometryLayer(Context ctx) {
		this(ctx, new ArrayList<Geometry>());

	}

	public GeometryLayer(final Context ctx, List<Geometry> geometries) {
		this(ctx, geometries, GeometryType.POLYGON, new PolygonSymbology(),
				"default");

	}

	public GeometryLayer(final Context ctx, List<Geometry> geometries,
			GeometryType type, Symbology symbologie, String name) {
		super(ctx);
		this.context = ctx;
		this.geometries = geometries;
		this.type = type;
		this.symbology = symbologie;
		this.name = name;
	}

	public List<Geometry> getGeometries() {
		return geometries;
	}

	public void editSymbology() {

	}

	/**
	 * Funcion which add geometry to the geometries list
	 * 
	 * @param geometry
	 */
	public void addGeometry(Geometry geometry) {
		this.geometries.add(geometry);
	}

	/**
	 * Function which add all geometries to the geometries list
	 * 
	 * @param geometry
	 */
	public void addGeometries(List<Geometry> geometries) {
		this.geometries.addAll(geometries);
	}

	/**
	 * Function which set a type to the geometry
	 * 
	 * @param type
	 */
	public void setType(GeometryType type) {
		this.type = type;
	}

	/**
	 * 
	 * @return the type
	 */
	public GeometryType getType() {
		return type;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Function which set a symbology to the geometry
	 * 
	 * @param symbology
	 */
	public void setSymbology(Symbology symbology) {
		this.symbology = symbology;
	}

	/**
	 * Function which return the symbology for the geometry
	 * 
	 * @return the symbology of geometry
	 */
	public Symbology getSymbology() {
		return this.symbology;
	}

	/**
	 * Function which test if the geometry is contained in the boundingBox
	 * 
	 * @param clipBound
	 *            : boundingBox of the screen
	 * @param geometryBoundingBox
	 *            : boundingBox of the geometry
	 * @return true the geometry is contened in the screen else false
	 */
	public boolean isInBoundingBox(Rect clipBound, Rect geometryBoundingBox) {
		if (clipBound.contains(geometryBoundingBox)
				|| geometryBoundingBox.contains(clipBound)) {
			return true;
		}
		return false;
	}

	/**
	 * Function which draw the geometry if it is contained in the boundingBox
	 */
	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean b) {
		projection = mapView.getProjection();
		paint = new Paint();
		paint.setColor(getSymbology().getColor());

		for (Geometry geometry : geometries) {

			switch (type) {
			case POINT:
				// Récupération de la géometrie et de la symbologie
				PointGeometry pointGeometry = (PointGeometry) geometry;
				PointSymbology pointSymbology = (PointSymbology) symbology;
				int radius = pointSymbology.getRadius();

				// Si le point est contenu dans la boundinBox
				// Transforme les coordonnées (lat/long) en pixels

				Point point = projection.toPixels(
						pointGeometry.getCoordinates(), null);
				// Dessin du point
				// Si le point est contenu dans la boundinBox
				if (canvas.getClipBounds().contains(point.x, point.y)) {
					canvas.drawCircle(point.x, point.y, radius, paint);

				}
				break;

			case LINE:
				// Récupération de la géometrie et de sa symbologie
				LineGeometry lineGeometry = (LineGeometry) geometry;
				LineSymbology lineSymbology = (LineSymbology) symbology;
				paint.setStrokeWidth(lineSymbology.getThickness());

				// Récupéartion de la liste de points de la géometrie
				List<PointGeometry> linePoints = lineGeometry.getPoints();

				for (int j = 0; j < linePoints.size() - 1; j++) {

					PointGeometry pointA = linePoints.get(j);
					PointGeometry pointB = linePoints.get(j + 1);

					Point pointT = projection.toPixels(pointA.getCoordinates(),
							null);
					// Projection des coordonnées en pixel

					Point pixelA = projection.toPixels(pointA.getCoordinates(),
							null);
					Point pixelB = projection.toPixels(pointB.getCoordinates(),
							null);

					Point pointD = projection.toPixels(pointB.getCoordinates(),
							null);
					// Dessine la geometrie si elle est contenue dans la
					// boundingBox
					if (isInBoundingBox(
							canvas.getClipBounds(),
							new Rect(Math.max(pixelA.x, pixelB.x), Math.max(
									pixelA.y, pixelB.y), Math.min(pixelA.x,

							pixelB.x), Math.min(pixelA.y, pixelB.y)))) {

						canvas.drawLine(pointT.x, pointT.y, pointD.x, pointD.y,
								paint);

						canvas.drawLine(pixelA.x, pixelA.y, pixelB.x, pixelB.y,
								paint);
					}
				}

				break;

			case POLYGON:

				// Récupération de la géometrie et de sa symbologie
				PolygonGeometry polygonGeometry = (PolygonGeometry) geometry;

				PolygonSymbology polygonSymbology = (PolygonSymbology) symbology;
				paint.setStrokeWidth(polygonSymbology.getThickness());

				// Récupéartion de la liste de points de la géometrie
				List<PointGeometry> polygonPoints = polygonGeometry.getPoints();

				for (int j = 0; j < polygonPoints.size(); j++) {

					PointGeometry pointA = polygonPoints.get(j
							% polygonPoints.size());
					PointGeometry pointB = polygonPoints.get((j + 1)
							% polygonPoints.size());

					// Projection des coordonnées en pixel
					Point pixelA = projection.toPixels(pointA.getCoordinates(),
							null);
					Point pixelB = projection.toPixels(pointB.getCoordinates(),
							null);

					// Dessine la geometrie si elle est contenue dans la
					// boundingBox
					if (isInBoundingBox(
							canvas.getClipBounds(),
							new Rect(Math.max(pixelA.x, pixelB.x), Math.max(
									pixelA.y, pixelB.y), Math.min(pixelA.x,
									pixelB.x), Math.min(pixelA.y, pixelB.y)))) {

						canvas.drawLine(pixelA.x, pixelA.y, pixelB.x, pixelB.y,
								paint);

					}
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return type.toString();
	}

	/**
	 * 
	 * @param editable
	 *            is the layer editable ?
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e, MapView mapView) {

		if (editable) {
			float x = e.getX();

			float y = e.getY();
			if (e.getPointerCount() > 1) {
				float x0 = e.getX(0);
				float x1 = e.getX(1);
				float y0 = e.getY(0);
				float y1 = e.getY(1);
				if (Math.abs(x1 - x0) < 5 && Math.abs(y1 - y0) < 5) {

					IGeoPoint p = mapView.getProjection().fromPixels(x0, y0);
					float latitude = (float) (p.getLatitudeE6() / VALUE_1E6);
					float longitude = (float) (p.getLongitudeE6() / VALUE_1E6);

					for (int i = 0; i < singleTapListeners.size(); i++) {
						singleTapListeners.get(i).actionPerformed(
								new PointGeometry(latitude, longitude));
					}
					IGeoPoint p1 = mapView.getProjection().fromPixels(x1, y1);
					float latitude1 = (float) (p.getLatitudeE6() / VALUE_1E6);
					float longitude1 = (float) (p.getLongitudeE6() / VALUE_1E6);

					for (int i = 0; i < singleTapListeners.size(); i++) {
						singleTapListeners.get(i).actionPerformed(
								new PointGeometry(latitude, longitude));
					}
					return super.onDoubleTap(e, mapView);
				}

			}

			IGeoPoint p = mapView.getProjection().fromPixels(x, y);
			float latitude = (float) (p.getLatitudeE6() / VALUE_1E6);
			float longitude = (float) (p.getLongitudeE6() / VALUE_1E6);

			for (int i = 0; i < singleTapListeners.size(); i++) {
				doubleTapListeners.get(i).actionPerformed(
						new PointGeometry(latitude, longitude));
			}
		}

		return super.onDoubleTap(e, mapView);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e, MapView mapView) {

		if (editable) {
			float x = e.getX();
			float y = e.getY();

			IGeoPoint p = mapView.getProjection().fromPixels(x, y);
			float latitude = (float) (p.getLatitudeE6() / VALUE_1E6);
			float longitude = (float) (p.getLongitudeE6() / VALUE_1E6);

			for (int i = 0; i < singleTapListeners.size(); i++) {
				singleTapListeners.get(i).actionPerformed(
						new PointGeometry(latitude, longitude));
			}
		}
		return super.onSingleTapUp(e, mapView);
	}

	/**
	 * 
	 * @param listener
	 *            the listener which will listen
	 */
	public void addGeometryLayerSingleTapListener(
			GeometryLayerSingleTapListener listener) {
		singleTapListeners.add(listener);
	}

	/**
	 * 
	 * @param listener
	 *            the listener wich will listen
	 */
	public void removeGeometryLayerSingleTapListener(
			GeometryLayerSingleTapListener listener) {
		singleTapListeners.remove(listener);
	}

	/**
	 * 
	 * @param listener
	 *            the listener wich will listen
	 */
	public void addGeometryLayerDoubleTapListener(
			GeometryLayerDoubleTapListener listener) {
		doubleTapListeners.add(listener);
	}

	/**
	 * 
	 * @param listener
	 *            the listener which will listen
	 */
	public void removeGeometryLayerDoubleTapListener(
			GeometryLayerDoubleTapListener listener) {
		doubleTapListeners.remove(listener);
	}

	@Override
	public Bitmap getOverview() {

		final Bitmap bit = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.geometry);

		Bitmap bitmap = bit.copy(Config.ARGB_8888, true);
		final Canvas canvas = new Canvas(bitmap);

		final Paint paint = new Paint();

		paint.setColor(symbology.getColor());
		switch (type) {
		case POINT:
			canvas.drawCircle(cX, cY, radius, paint);
			break;
		case LINE:
			paint.setStrokeWidth(strokeWidth);
			canvas.drawLine(0, 0, lineStopX, lineStopY, paint);
			break;
		case POLYGON:
			canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);
			break;
		default:
			break;
		}
		return bitmap;

	}

	@Override
	public Overlay getOverlay() {
		return this;
	}

}
