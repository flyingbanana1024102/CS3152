/*
 * Monster.java
 * 
 * This is the first draft of model class representing monster. 
 * Monsters behave differently according to its MonsterType, 
 * whose movement will be controlled by MonsterController.java
 *
 * Author: Saerom Choi
 * Based on monster.java by Walker M. White
 */

package com.ramenstudio.sandglass.game.model;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.ramenstudio.sandglass.game.controller.MonsterController.AngleEnum;
import com.ramenstudio.sandglass.game.view.GameCanvas;
import com.ramenstudio.sandglass.util.Drawable;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * A model class representing a monster.
 */
public class Monster extends GameObject implements Drawable{
    
    public enum Move {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        NONE;
    }

	public static enum MType {
		/* For over world */
		OverMonster,
		/*For under world*/
		UnderMonster
	}

	// CONSTANTS FOR monster HANDLING
	/** How far forward this monster can move in a single turn */
	private static final float MOVE_SPEED = 6.5f;
		
	// Instance Attributes
	/** A unique identifier; used to decouple classes. */
	private int id;
	/** Monster type*/
	public MType mType;
	/** Initial Position*/
	public Vector2 initial;
	/** Goal */
	private Vector2 goal;
	/** Boolean to track if we are dead yet */
	private boolean isAlive;
	/** Speed coefficient*/
    public float speed_coeff;
    
    public AngleEnum angle;
    
    public Array<Vector2> vertices;

	/**
	 * Create monster # id at the given position.
	 *
	 * @param id The unique monster id
	 * @param x The initial x-coordinate of the monster
	 * @param y The initial y-coordinate of the monster
	 */
    
   
	public Monster(Vector2 initialPos, MType mType, int id, int level,
			float spcf, Array<Vector2> vertices) {
		super();
		this.vertices = vertices;
		angle = AngleEnum.NORTH;
		speed_coeff = spcf;
		initial = initialPos;
        if (mType == Monster.MType.OverMonster){
        	System.out.println("this is over monster");
            setTexture(new Texture(Gdx.app.getFiles().internal("overmonster.png")));
            fixtureDefs = new FixtureDef[3];
            setSize(new Vector2(0.8f, 1.2f));
            getBodyDef().position.set(initialPos);
            getBodyDef().type = BodyDef.BodyType.StaticBody;
            
            FixtureDef fixtureDef = new FixtureDef();
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.4f, 0.35f);
            fixtureDef.density = 100.0f;
            fixtureDef.shape = shape;
            fixtureDefs[0] = fixtureDef;
            fixtureDef.friction = 0.0f;
            fixtureDef.isSensor = true;
            
            
            CircleShape c = new CircleShape();
            c.setRadius(0.3f);
            c.setPosition(new Vector2(0, -0.35f));
            FixtureDef fixtureDef2 = new FixtureDef();
            fixtureDef2.shape = c;
            fixtureDef2.isSensor = true;
            fixtureDefs[1] = fixtureDef2;
            

            CircleShape c2 = new CircleShape();
            c2.setRadius(0.3f);
            c2.setPosition(new Vector2(0, 0.35f));
            FixtureDef fixtureDef3 = new FixtureDef();
            fixtureDef3.shape = c2;
            fixtureDef3.isSensor = true;
            fixtureDefs[2] = fixtureDef3;
        }
        else{
                setTexture(new Texture(Gdx.app.getFiles().internal("undermonster1.png")));
                fixtureDefs = new FixtureDef[1];
                setSize(new Vector2(0.8f, 0.8f));
                getBodyDef().position.set(initialPos);
                getBodyDef().type = BodyDef.BodyType.DynamicBody;
                getBodyDef().gravityScale=0;
                
                FixtureDef underFixtureDef = new FixtureDef();
                PolygonShape underShape = new PolygonShape();
                underShape.setAsBox(0.4f, 0.4f);
                underFixtureDef.density = 100.0f;
                underFixtureDef.shape = underShape;
                fixtureDefs[0] = underFixtureDef;
                underFixtureDef.friction = 10;
        }
		this.mType = mType;
		isAlive = true;
		System.out.println("monster is created");
	}
	
	/** 
	 * Returns the unique monster id number 
	 * 
	 * @return the unique monster id number 
	 */
	public int getId() {
		return id;
	}
	

	/* Returns the monster ypte
	 *
	 * @return the monster type
	 */
	public MType getType() {
		return mType;
	}
	
	/**
	 * Returns the goal of this monster
	 *
	 * @return the goal
	 */
	
	public Vector2 getGoal(){
		return goal;
	}
	
	/**
	 * Sets the goal of this monster
	 * 
	 * @param the goal of this monster
	 */
	
	public void setGoal(Vector2 goal){
		this.goal = goal;
	}
	

	
	/**
	 * Returns whether or not the monster is alive.
	 *
	 * A monster is dead once it has fallen past MAX_FALL_AMOUNT. A dead monster cannot be 
	 * targeted, involved in collisions, or drawn.  For all intents and purposes, it 
	 * does not exist.
	 *
	 * @return whether or not the monster is alive
	 */	 
	public boolean isAlive() {
		return isAlive;
	}
	
	public boolean isAtGoal(){
	    return this.getBody().getPosition().epsilonEquals(goal, 0.0f);
	}
	
	/**
	 * Sets whether or not the monster is alive.
	 *
	 * This method should only be used if we need to kill the monster immediately.
	 * The preferred method to get rid of a monster is destroy().
	 *
	 * @param value whether or not the monster is alive.
	 */
	public void setAlive(boolean value) {
		isAlive = value;
	}
	
	/**
	 * Push the monster so that it starts to fall.
	 * 
	 * This method will not destroy the monster immediately.  It will tumble and fall 
	 * offscreen before dy	ing. To instantly kill a monster, use setAlive().
	 */
	public void destroy() {
		// TODO: Implement
	}
	
	/**
	 * Plays the appropriate sound for this monster's current 
	 */
	public void play() {
		
	}

	/**
	 * Updates this monster position according to the control code.
	 *
	 * @param controlCode The movement controlCode (from InputController).
	 */
	public void update(Move move) {
		setRotation(AngleEnum.convertToAngle(angle));
	    //System.out.println(body.getPosition().toString());
		Vector2 velocity = body.getLinearVelocity();
		switch (move){
        case DOWN:
            velocity.x = 0;
            velocity.y = -speed_coeff*MOVE_SPEED;
            break;
        case UP:
            velocity.x = 0;
            velocity.y = speed_coeff*MOVE_SPEED;
            break;
        case LEFT:
            velocity.x = -speed_coeff*MOVE_SPEED;
            velocity.y = 0;
            break;
        case RIGHT:
            velocity.x = speed_coeff*MOVE_SPEED;
            velocity.y = 0;
            break;
        case NONE:
            break;
        default:
            break;
		}
		getBody().setLinearVelocity(velocity);
	}
	

	@Override
	public void draw(GameCanvas canvas){
		canvas.draw(getTextureRegion(), getBody().getPosition().add(getSize().cpy().scl(-0.5f)), 
		        getSize(), new Vector2(getSize()).scl(.5f), (float)(getRotation() * 180/Math.PI));
	}
}
