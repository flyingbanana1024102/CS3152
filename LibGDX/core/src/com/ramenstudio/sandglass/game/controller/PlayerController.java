package com.ramenstudio.sandglass.game.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.ramenstudio.sandglass.game.model.AbstractTile;
import com.ramenstudio.sandglass.game.model.GameObject;
import com.ramenstudio.sandglass.game.model.GoalTile;
import com.ramenstudio.sandglass.game.model.Player;
import com.ramenstudio.sandglass.game.model.TurnTile;
import com.ramenstudio.sandglass.game.view.*;
import com.ramenstudio.sandglass.util.controller.SoundController;
import com.ramenstudio.sandglass.game.controller.AngleEnum;

/**
 * Handles player input and manages the player object.
 * 
 * @author Jiacong Xu
 * @author Sung Won An
 * @author Jonathan Park
 */
public class PlayerController extends AbstractController {

	/** The input controller for player. */
	private InputController inputController = InputController.getInstance();

	/** The player we are managing */
	private Player player;

	/** This is the offset from the center of the body to the foot. */
	private float footOffset = -0.5f;

	/** This is the distance from where we are raycasting */
	private float rayDist = 0.3f;

	/** Maximum move speed in horizontal movement */
	private float moveSpeed = 4f;

	/** Vertical jump velocity when jump is begun. */
	private float jumpVelocity = 5.25f;
	/** Saving an instance of the delegate */
	private World world;

	// Variables concerned with turning at corners.
	
	//number of flips we have left in this level

	/** The active corner we are tracking whether we should turn or not. */
	private TurnTile activeCorner;

	/** Whether this player is in the underworld. */
	private boolean isUnder = false;

	/** RayCastHandler that detects tiles in this one frame. Should always be set
	 * to null after every update loop. */
	private RayCastHandler oneFrameRayHandler;

	/** OverlapHandler that detects tiles in this one frame. Should always be set to null
	 * after every update loop. */
	private OverlapHandler oneFrameOverlapHandler;

	/** The direction the player's head is facing. */
	private AngleEnum heading = AngleEnum.NORTH;
	
	private int tick;
	
	// Variables for animation
	
	/** Number of rows in the player image filmstrip */
	private static final int FILMSTRIP_ROWS = 1;
	/** Number of columns in the player image filmstrip */
	private static final int FILMSTRIP_COLS = 71;
	/** Number of elements in the player image filmstrip */
	private static final int FILMSTRIP_SIZE = 71;
	
	/** The frame number for neutral stance. */
    private static final int NEUTRAL_START_EAST = 15;
    /** The frame number for neutral stance. */
    private static final int NEUTRAL_START_WEST = 0;
    /** The frame number for beginning a jump. */
    private static final int JUMP_START_EAST = 24;
    /** The frame number for ending a jump. */
    private static final int JUMP_END_EAST = 29;
    /** The frame number for beginning a jump. */
    private static final int JUMP_START_WEST = 9;
    /** The frame number for ending a jump. */
    private static final int JUMP_END_WEST = 14;
    /** The frame number for beginning a walk. */
    private static final int WALK_START_EAST = 16;
    /** The frame number for ending a walk. */
    private static final int WALK_END_EAST = 23;
    /** The frame number for beginning a walk. */
    private static final int WALK_START_WEST = 1;
    /** The frame number for ending a walk. */
    private static final int WALK_END_WEST = 8;
    /** The frame number for starting a flip. */
    private static final int FLIP_START = 30;
    /** The frame number for middle of a flip. */
    private static final int FLIP_MID = 36;
    /** The frame number for ending a flip. */
    private static final int FLIP_END = 38;
    /** The frame number for getting hit. */
    private static final int HIT_START_WEST = 39;
    /** The frame number for ending hit. */
    private static final int HIT_END_WEST = 43;
    /** The frame number for getting hit. */
    private static final int HIT_START_EAST = 44;
    /** The frame number for ending hit. */
    private static final int HIT_END_EAST = 48;
    /** The frame offset for animation while pressing down. */
    private static final int DOWN_WEST_OFFSET = 50;
    private static final int DOWN_EAST_OFFSET = 44;
    /** The frame number for getting hit by a non-deduct flip monster. */
    private static final int OTHER_HIT_START_WEST = 67;
    private static final int OTHER_HIT_END_WEST = 68;
    private static final int OTHER_HIT_START_EAST = 69;
    private static final int OTHER_HIT_END_EAST = 70;
	
    private boolean pressingDown;
    private boolean pressedDown;
    private boolean otherHit;
    
    /** The enum for animation states. */
    public enum State {
    	NEUTRAL, JUMP, WALK, FLIP, HIT
    }
    
    /** The current animation state. */
    private State state = State.NEUTRAL;
    /** The next animation state. */
    private State next = State.NEUTRAL;
    
    /** The direction the player is facing, relative to the camera. */
    private AngleEnum direction = AngleEnum.EAST;
    /** The direction the player used to face, relative to the camera. */
    private AngleEnum prevDirection = AngleEnum.EAST;

    private boolean isReset = false;
    
    /** Frame cooldown (frames are too quick) */
    private static final int WALK_FRAME_COOLDOWN = 4;
    private static final int JUMP_FRAME_COOLDOWN = 10;
    private static final int FLIP_FRAME_COOLDOWN = 5;
    private static final int HIT_FRAME_COOLDOWN = 7;
    
//    /**Cooldown for preventing input while camera rotates*/
//    public static final int FREEZE_COOLDOWN = 60;
//    
//    /**Counter for flip/rotate cooldown*/
//    public int freeze_counter = FREEZE_COOLDOWN;
    
    /** Freeze flag */
    private boolean frozen = false;
    
    /** Frame counter */
    private int counter = 0;

    private float rotateAngle;

    /** Boolean for if the player must flip, after hitting a monster or something */
	private boolean mustFlip;
	
	/** How long the player has before they are vulnerable again. */
	private static final int HIT_COOLDOWN = 100;
	
	/** The hit counter. */
	private int hitCounter = 0;
	
	/** The rotate counter. */
	private int rotateCounter = 0;
	
	/** The position vector to be used halfway through flipping. */
	private Vector2 position;
    
	/**
	 * Default constructor for player object.
	 */
	public PlayerController(Player player) {
		this.player = player;
		Texture playerTexture = new Texture(Gdx.files.internal("Final_Spritesheet.png"));
		player.setPlayerSprite(new FilmStrip(playerTexture,FILMSTRIP_ROWS,FILMSTRIP_COLS,FILMSTRIP_SIZE));
		player.setFrame(NEUTRAL_START_EAST);
	}

	@Override
	public void objectSetup(World world) {
		this.world = world;
		activatePhysics(world, player);
		player.getBody().setFixedRotation(true);
	}

	@Override
	public void update(float dt) {
		inputController.update(dt);
		player.isGrounded = isGrounded();
		// Reset variables
		rotateAngle = 0f;
//		freeze_counter++;

		Vector2 pos = player.getPosition();
		Vector2 vel = player.getBody().getLinearVelocity();
		Vector2 size = player.getSize();

		if (player.isTouchMF()){
			mustFlip = true;
		}
		boolean jump = false;
		float y = AngleEnum.isVertical(heading) ? vel.y: vel.x;
		
		// Handle movement
		if (!frozen) {
			float x = moveSpeed * inputController.getHorizontal();
			if (x != 0) {
				prevDirection = direction;
				direction = (x > 0)? AngleEnum.EAST : AngleEnum.WEST;
			}

			if (inputController.didPressJump() && isGrounded()) {
				y = jumpVelocity;
				jump = true;
			}
			if (heading == AngleEnum.NORTH) {
				vel.x = x;
				vel.y = y; 
			} else if (heading == AngleEnum.EAST) {
				vel.x = y;
				vel.y = -x;
			} else if (heading == AngleEnum.SOUTH) {
				vel.x = -x;
				vel.y = jump? -y : y;
			} else {
				vel.x = jump? -y : y;
				vel.y = x;
			}
			if (x != 0 && isGrounded()) {
				next = State.WALK;
			} else if (x == 0 && isGrounded()) {
				next = State.NEUTRAL;
			} else {
				next = State.JUMP;
			}
			player.getBody().setLinearVelocity(vel);
		} else {
			if (state == State.FLIP) {
				next = State.FLIP;
			}
			player.getBody().setLinearVelocity(0,0);
		}
		
		// Epsilon velocity to ensure collisions
		if (player.getBody().getLinearVelocity().epsilonEquals(0, 0, 0.01f)) {
			player.getBody().setLinearVelocity(0.01f, 0.01f);
		}
		
		// Handle rotating
		checkCorner();
		pressedDown = pressingDown;
		pressingDown = inputController.getVertical() == -1;
		if (activeCorner != null) {
			if (!jump && isUnder && activeCorner.validTurn(vel, heading) &&
					pressingDown && isGrounded()) {
				SoundController.getInstance().playRotation();
				Vector2 cornerPos = activeCorner.getPosition();
				float diff = (AngleEnum.isVertical(heading))?
						pos.x - cornerPos.x : pos.y - cornerPos.y;
				if (heading == AngleEnum.SOUTH || heading == AngleEnum.EAST) {
					diff *= -1;
				}

				float blockSize = activeCorner.getSize().x / 2;
				Vector2 blockPos = activeCorner.getPosition();
				float newX;
				float newY;

				if (diff > 0) {
					rotateAngle = -90;
					world.setGravity(world.getGravity().rotate(90));
					if (heading == AngleEnum.NORTH) {
						newX = blockPos.x + blockSize - size.y/2 - 0.015f;
						newY = blockPos.y - blockSize - size.x/2;
					} else if (heading == AngleEnum.EAST) {
						newX = blockPos.x - blockSize - size.x/2;
						newY = blockPos.y - blockSize + size.y/2 + 0.015f;
					} else if (heading == AngleEnum.SOUTH) {
						newX = blockPos.x - blockSize + size.y/2 + 0.015f;
						newY = blockPos.y + blockSize + size.x/2;
					} else {
						newX = blockPos.x + blockSize + size.x/2;
						newY = blockPos.y + blockSize - size.y/2 - 0.015f;
					}
					heading = AngleEnum.flipCounterClockWise(heading);
				} else {
					rotateAngle = 90;;
					world.setGravity(world.getGravity().rotate(-90));

					if (heading == AngleEnum.NORTH) {
						newX = blockPos.x - blockSize + size.y/2 + 0.015f;
						newY = blockPos.y - blockSize - size.x/2;
					} else if (heading == AngleEnum.EAST) {
						newX = blockPos.x - blockSize - size.x/2;
						newY = blockPos.y + blockSize - size.y/2 - 0.015f;
					} else if (heading == AngleEnum.SOUTH) {
						newX = blockPos.x + blockSize - size.y/2 - 0.015f;
						newY = blockPos.y + blockSize + size.x/2;
					} else {
						newX = blockPos.x + blockSize + size.x/2;
						newY = blockPos.y - blockSize + size.y/2 + 0.015f;
					}
					heading = AngleEnum.flipClockWise(heading);
				}
//				newX = Math.round(newX*1000.0f)/1000.0f;
//				newY = Math.round(newY*1000.0f)/1000.0f;
				player.setPosition(new Vector2(newX, newY));
				player.getBody().setLinearVelocity(0,0);
				player.setRotation(AngleEnum.convertToAngle(heading));
				activeCorner = null;
			}
			
		}
		
		// Handle flipping
		else if ((inputController.didPressFlip() || mustFlip) && canFlip() 
				&& !jump && !frozen && !player.isFlashing) {
			mustFlip = false;
			AbstractTile under = oneFrameRayHandler.tileUnderneath;
			if (under.isFlippable()) {
				SoundController.getInstance().playRotation();
				next = State.FLIP;
				rotateAngle = 180;
				float tilePos;
				float offset = 0.05f + size.y/2;
				if (AngleEnum.isVertical(heading)) {
					tilePos = under.getPosition().y;
					pos.y = heading == AngleEnum.SOUTH ? 
							tilePos + offset : tilePos - offset;
				} else {
					tilePos = under.getPosition().x;
					pos.x = heading == AngleEnum.WEST ? 
							tilePos + offset : tilePos - offset;
				}
				heading = AngleEnum.flipEnum(heading);
//				player.setPosition(pos);
				position = pos;
//				player.setRotation(AngleEnum.convertToAngle(heading));
				world.setGravity(world.getGravity().rotate(180));
				isUnder ^= true;
				if (player.isTouchMF()){
					player.setTouchMF(false);
				}
				else{
					player.subtractFlip(1);					
				}
			}
		}
		player.heading = heading;
		
		// Handle goal collision
		collidedWithGoal();
		
		// Handle reset button
		if (inputController.didPressReset()) {
			isReset = true;
		}
		oneFrameRayHandler = null;
		oneFrameOverlapHandler = null;
		
//		if (player.isImpulse){
//			player.getBody().applyLinearImpulse(player.getImpulse(), player.getBody().getPosition(), true);
////			System.out.println("GIVING IMPULSE NOW. I SHOULD ONLY APPEAR ONCE");
////			player.getBody().applyForceToCenter(player.getImpulse().cpy().scl(80.0f), true);
//			player.isImpulse = false;
//		}
		
		// Handle Collisions
		if (player.isFlashing && hitCounter >= HIT_COOLDOWN) {
			hitCounter = 0;
			player.isFlashing = false;
			player.setDeductFlip(false);
			tick = 0;
		} else if (player.isFlashing) {
			hitCounter++;
			if (player.isDeductFlip()) {
				next = State.HIT;
				otherHit = false;
				tick++;
				if (tick < player.DEDUCT_COOL_TIME){
					player.subtractFlip(2);
				}
			} else {
				next = State.HIT;
				otherHit = true;
			}
		}
		
//		if (player.isDeductFlip()){
//			next = State.HIT;
//			tick++;
//			if (tick < player.DEDUCT_COOL_TIME){
//				player.subtractFlip(2);
//			} else{
//				tick = 0;
//				player.setDeductFlip(false);
//			}
//		}
		
		// Handle animation
		handleAnimation();
	}

	private void handleAnimation() {
		boolean facingEast = direction == AngleEnum.EAST;
		boolean facedEast = prevDirection == AngleEnum.EAST;
		int frame = player.getFrame();
		if (frozen && next != State.FLIP) {
			if (facingEast) player.setFrame(NEUTRAL_START_EAST);
			else player.setFrame(NEUTRAL_START_WEST);
			return;
		}
		switch (next) {
		case NEUTRAL:
			frame = (facingEast) ? NEUTRAL_START_EAST : NEUTRAL_START_WEST;
			if (pressingDown) {
				frame = (facingEast) ? frame + DOWN_EAST_OFFSET : frame + DOWN_WEST_OFFSET;
				frame--;
			}
			player.setFrame(frame);
			break;
		case JUMP:
			if (state == State.JUMP) {
				int offset = (facedEast)? -JUMP_START_EAST : -JUMP_START_WEST;
				offset += frame + 1;
				offset = Math.min(offset, JUMP_END_EAST - JUMP_START_EAST);
				counter++;
				if (counter > JUMP_FRAME_COOLDOWN) {
					counter = 0;
					if (facingEast) player.setFrame(JUMP_START_EAST + offset);
					else player.setFrame(JUMP_START_WEST + offset);
				} else {
					if (offset != JUMP_END_EAST - JUMP_START_EAST) offset--;
					if (facingEast) player.setFrame(JUMP_START_EAST + offset);
					else player.setFrame(JUMP_START_WEST + offset);
				}
			} else {
				if (facingEast) player.setFrame(JUMP_START_EAST);
				else player.setFrame(JUMP_START_WEST);
			}
			break;
		case WALK:
			if (state == State.WALK) {
				int offset = (facedEast)? -WALK_START_EAST : -WALK_START_WEST;
				if (pressedDown) {
					offset = (facedEast)? offset - DOWN_EAST_OFFSET : offset - DOWN_WEST_OFFSET;
				}
				offset += frame + 1;
				offset = offset%(WALK_END_EAST - WALK_START_EAST);
				if (pressingDown) {
					offset = (facingEast) ? offset + DOWN_EAST_OFFSET : offset + DOWN_WEST_OFFSET;
				}
				counter++;
				if (counter > WALK_FRAME_COOLDOWN) {
					counter = 0;
					if (facingEast) player.setFrame(WALK_START_EAST + offset);
					else player.setFrame(WALK_START_WEST + offset);
				} else {
					if (offset == 0) {
						offset = WALK_END_EAST - WALK_START_EAST;
						if (pressingDown) {
							offset = (facingEast) ? offset + DOWN_EAST_OFFSET :
								offset + DOWN_WEST_OFFSET;
						}
					}
					if (facingEast) player.setFrame(WALK_START_EAST + --offset);
					else player.setFrame(WALK_START_WEST + --offset);
				}
			} else {
				frame = (facingEast) ? WALK_START_EAST : WALK_START_WEST;
				if (pressingDown) {
					frame = (facingEast) ? frame + DOWN_EAST_OFFSET : 
						frame + DOWN_WEST_OFFSET;
				}
				player.setFrame(frame);
			}
			break;
		case FLIP:
			if (state == State.FLIP){
				counter++;
				if (counter > FLIP_FRAME_COOLDOWN) {
					counter = 0;
					int newFrame = frame + 1;
					newFrame = Math.min(frame + 1, FLIP_END);
					if (newFrame > FLIP_MID) {
						player.setPosition(position);
						player.setRotation(AngleEnum.convertToAngle(heading));
					}
					player.setFrame(newFrame);
				}
			} else {
				player.flipping = true;
				player.setFrame(FLIP_START);
			}
			break;
		case HIT:
			if (otherHit) {
				if (state == State.HIT) {
					int offset = (facedEast)? -OTHER_HIT_START_EAST : -OTHER_HIT_START_WEST;
					offset += frame + 1;
					offset = offset%(OTHER_HIT_END_EAST - OTHER_HIT_START_EAST);
					counter++;
					if (counter > HIT_FRAME_COOLDOWN/2) {
						counter = 0;
						if (facingEast) player.setFrame(OTHER_HIT_START_EAST + offset);
						else player.setFrame(OTHER_HIT_START_WEST + offset);
					} else {
						if (offset == 0) offset = OTHER_HIT_END_EAST - OTHER_HIT_START_EAST;
						if (facingEast) player.setFrame(OTHER_HIT_START_EAST + --offset);
						else player.setFrame(OTHER_HIT_START_WEST + --offset);
					}
				} else {
					if (facingEast) player.setFrame(OTHER_HIT_START_EAST);
					else player.setFrame(OTHER_HIT_START_WEST);
				}
			} else {
				if (state == State.HIT) {
					int offset = (facedEast)? -HIT_START_EAST : -HIT_START_WEST;
					offset += frame + 1;
					offset = offset%(HIT_END_EAST - HIT_START_EAST);
					counter++;
					if (counter > HIT_FRAME_COOLDOWN) {
						counter = 0;
						if (facingEast) player.setFrame(HIT_START_EAST + offset);
						else player.setFrame(HIT_START_WEST + offset);
					} else {
						if (offset == 0) offset = HIT_END_EAST - HIT_START_EAST;
						if (facingEast) player.setFrame(HIT_START_EAST + --offset);
						else player.setFrame(HIT_START_WEST + --offset);
					}
				} else {
					if (facingEast) player.setFrame(HIT_START_EAST);
					else player.setFrame(HIT_START_WEST);
				}
			}
			break;
		}
		state = next;
	}


	//    /**
	//     * @return the matrix transformation from world to screen. Used in drawing.
	//     */
	//    public Matrix4 world2ScreenMatrix() {
	//        return cameraController.world2ScreenMatrix();
	//    }

	@Override
	public void draw(GameCanvas canvas) {
		player.draw(canvas);
	}


	
	/**
	 * @return whether player is touching the ground.
	 */
	public boolean isGrounded() {
		Vector2 g = world.getGravity().nor();
		Vector2 footPos = player.getPosition().add(g.cpy().scl(-footOffset));
		Vector2 endPos = footPos.cpy().add(g.cpy().scl(rayDist));

		RayCastHandler handler = new RayCastHandler();
		world.rayCast(handler, footPos, endPos);
		oneFrameRayHandler = handler;
		return handler.isGrounded;
	}

	/**
	 * Returns whether or not the game should be reset, for various reasons
	 * including but not limited to hitting a GoalTile or pressing the
	 * reset button.
	 * 
	 * @return true iff the level should be reset.
	 */
	public boolean isReset() {
		return isReset;
	}
	
	/**
	 * Sets the isReset field to be true, used for when the Player collides
	 * with an OverMonster.
	 */
	public void setResetTrue() {
		isReset = true;
	}

	/**
	 * @return the angle to rotate the camera by
	 */
	public float getRotateAngle() {
		return rotateAngle;
	}

	public Player getPlayer() {
		return player;
	}
	
	public void setFrozen(boolean bool) {
		frozen = bool;
	}

	/**
	 * Returns true if the player can flip and false if not
	 * 
	 * @return whether player can flip
	 */
	private boolean canFlip() {
		if (player.getFlips() <= 0){
			return false;
		}
		if (oneFrameRayHandler == null) {
			Vector2 g = world.getGravity().nor();
			Vector2 footPos = player.getPosition().add(g.cpy().scl(-footOffset));
			Vector2 endPos = footPos.cpy().add(g.cpy().scl(rayDist));

			RayCastHandler handler = new RayCastHandler();
			world.rayCast(handler, footPos, endPos);
			oneFrameRayHandler = handler;
			return handler.isGrounded && handler.isFlip;
		}
		else {
			return oneFrameRayHandler.isGrounded && oneFrameRayHandler.isFlip;
		}
	}

	/**
	 * If we are not currently at a turning corner, we try to find one. Otherwise
	 * we decide whether we want to flip or not.
	 */
	private void checkCorner() {
		activeCorner = null;
		RayCastHandler handler;
		if (oneFrameRayHandler == null) {
			//            handler = new OverlapHandler();
			//            Vector2 upper = getUpperRight();
			//            Vector2 lower = getLowerLeft();
			//
			//            delegate.QueryAABB(handler, lower.x, lower.y, upper.x, upper.y);

			Vector2 g = world.getGravity().nor();
			Vector2 footPos = player.getPosition().add(g.cpy().scl(-footOffset));
			Vector2 endPos = footPos.cpy().add(g.cpy().scl(rayDist));

			handler = new RayCastHandler();
			world.rayCast(handler, footPos, endPos);
			oneFrameRayHandler = handler;

			// We only set active corner if we WALKED into the corner. We can land on
			// the corner too.
		} else {
			handler = oneFrameRayHandler;
		}

		TurnTile corner = handler.cornerTile;
		if (corner != null) {
			float coord;
			float pos;
			float vel;
			if (AngleEnum.isVertical(heading)) {
				coord = corner.getPosition().x;
				pos = player.getPosition().x;
				vel = player.getBody().getLinearVelocity().x;
			} else {
				coord = corner.getPosition().y;
				pos = player.getPosition().y;
				vel = player.getBody().getLinearVelocity().y;
			}
			if (coord < pos && vel < 0) {
				activeCorner = corner;
			} else if (coord > pos && vel > 0) {
				activeCorner = corner;
			} else {
				activeCorner = null;
			}
		}
	}

	/**
	 * Return true if the player has collided with a tile that's marked as a
	 * goal tile.
	 * 
	 * @return true iff the player has collided with the goal
	 */
	private void collidedWithGoal() {
		OverlapHandler handler;
		if (oneFrameOverlapHandler == null) {
			handler = new OverlapHandler();
			Vector2 upper = getUpperRight();
			Vector2 lower = getLowerLeft();
			world.QueryAABB(handler, lower.x, lower.y, upper.x, upper.y);
		}
		else {
			handler = oneFrameOverlapHandler;
		}
		GameObject goal = handler.goal;
		if (goal != null) {
			isReset = true;
		}
	}

	/**
	 * This method gets the lower left point of the hit box. In this case,
	 * lower left refers to the absolute lower left, regardless of the player's
	 * current orientation.
	 * 
	 * @return the absolute lower left point of the player's bounding box.
	 */
	private Vector2 getLowerLeft() {
		Vector2 playerPos = player.getPosition();
		Vector2 playerSize = player.getSize().scl(.5f);
		Vector2 theLowerLeft;
		if (heading == AngleEnum.NORTH || heading == AngleEnum.SOUTH) {
			theLowerLeft = new Vector2(playerPos.x - playerSize.x, playerPos.y - playerSize.y);
		}
		else {
			theLowerLeft = new Vector2(playerPos.x - playerSize.y, playerPos.y - playerSize.x);
		}
		return theLowerLeft;
	}

	/**
	 * This method gets the upper right point of the hit box. In this case,
	 * upper right refers to the absolute upper right, regarldess of the player's
	 * current orientation.
	 * @return the absolute upper right of the player's bounding box.
	 */
	private Vector2 getUpperRight() {
		Vector2 playerPos = player.getPosition();
		Vector2 playerSize = player.getSize().scl(.5f);
		Vector2 theUpperRight;
		if (heading == AngleEnum.NORTH || heading == AngleEnum.SOUTH) {
			theUpperRight = new Vector2(playerPos.x + playerSize.x, playerPos.y + playerSize.y);
		}
		else {
			theUpperRight = new Vector2(playerPos.x + playerSize.y, playerPos.y + playerSize.x);
		}
		return theUpperRight;
	}
	
	public AngleEnum getHeading(){
		return heading;
	}

	/**
	 * @return whether the player is in the underworld
	 */
	public boolean isUnder() {
		return isUnder;
	}
	
	public void setMustFlip() {
		mustFlip = true;
	}

	/**
	 * Our implementation of the reportRayFixture for
	 * detecting if the player can jump.
	 */
	private class RayCastHandler implements RayCastCallback {
		boolean isGrounded = false;
		boolean isFlip = false;
		private AbstractTile tileUnderneath;
		private TurnTile cornerTile;

		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, 
				Vector2 normal, float fraction) {
			Object obj = fixture.getBody().getUserData();

			if (obj != null && obj instanceof AbstractTile && !(obj instanceof TurnTile)) {
				AbstractTile tempGameObject = (AbstractTile)obj;
				isGrounded = tempGameObject.isGround() || isGrounded;
				isFlip = tempGameObject.isFlippable() || isFlip;
				tileUnderneath = tempGameObject;
			}
			if (obj != null && obj instanceof TurnTile) {
				TurnTile tempCorner = (TurnTile) obj;
				cornerTile = tempCorner;
			}

			return -1;
		}
	}

	/**
	 * Our implementation of the reportFixture
	 * for detecting what kind of Tile the player has hit.
	 */
	private class OverlapHandler implements QueryCallback {
		GoalTile goal;

		@Override
		public boolean reportFixture(Fixture fixture) {
			Object obj = fixture.getUserData();
			if (obj != null && obj instanceof TurnTile) {
				return true;
			}
			if (obj != null && obj instanceof GoalTile) {
				goal = (GoalTile) obj;
				return true;
			}
			return true;
		}
	}

	@Override
	public void dispose() {
		player.dispose();
	}
}
