package com.ramenstudio.sandglass.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ramenstudio.sandglass.game.view.GameCanvas;
import com.ramenstudio.sandglass.util.Drawable;

/**A class describing ship pieces
 * @author Nathaniel Hunter*/
public class ShipPiece extends GameObject implements Drawable {
	//the texture of this piece
	private Texture texture = new Texture(Gdx.files.internal("FinalShip.png"));
	
	private boolean isCollected;
	
	public ShipPiece(Vector2 pos){
		super();
		size.x = 1f;
	    size.y = 1f;
	    getBodyDef().type = BodyDef.BodyType.StaticBody;
	    getBodyDef().position.set(pos);
	    fixtureDefs = new FixtureDef[1];
	    CircleShape shape = new CircleShape();
	    fixtureDefs[0] = new FixtureDef();
	    fixtureDefs[0].isSensor = true;
	    shape.setRadius(0.35f);
	    fixtureDefs[0].shape = shape;
	}
	
	public Texture getTexture(){
		return texture;
	}
	
	public void setTexture(Texture t){
		texture = t;
	}
	
	public boolean getIsCollected() {
		return isCollected;
	}
	
	public void setCollected() {
		isCollected = true;
	}
	
	@Override
	public void draw(GameCanvas canvas){
		if (!isCollected) {
			canvas.draw(texture, getPosition().sub(0.5f,0.5f), getSize());
		}
	}
}
