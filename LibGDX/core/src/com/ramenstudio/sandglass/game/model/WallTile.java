package com.ramenstudio.sandglass.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

/**
 * A wall tile that has a specific collision fixture set up.
 * 
 * @author Jiacong Xu
 */
public class WallTile extends AbstractTile {
  // Thickness of the border.
  private static final float THICKNESS = 0.1f;
  private static final float TILE_WIDTH = 1.0f;
  
  public enum WallType {
    TOPLEFT, TOP, TOPRIGHT, LEFT, RIGHT, BOTLEFT, BOT, BOTRIGHT
  }
  
  /**
   * Default constructor. Creates a tile with collision corresponding to the 
   * given wall type.
   * 
   * @param type specifies fixture geometry.
   */
  public WallTile(WallType type) {
    isFlippable = true;
    isGround = true;
    
    getBodyDef().type = BodyDef.BodyType.StaticBody;
    
    PolygonShape shape = new PolygonShape();
    Vector2 c = new Vector2();
    
    
    switch (type) {
    case TOPLEFT:
      fixtureDefs = new FixtureDef[2];

      fixtureDefs[0] = new FixtureDef();
      c.x = (TILE_WIDTH - THICKNESS) / 4;
      shape.setAsBox((TILE_WIDTH + THICKNESS) / 4, THICKNESS / 2, c, 0);
      fixtureDefs[0].shape = shape;
      
      fixtureDefs[1] = new FixtureDef();
      c.x = 0;
      c.y = -(TILE_WIDTH - THICKNESS) / 4;
      shape = new PolygonShape();
      shape.setAsBox(THICKNESS / 2, (TILE_WIDTH + THICKNESS) / 4, c, 0);
      fixtureDefs[1].shape = shape;
      break;
    case BOTLEFT:
      fixtureDefs = new FixtureDef[2];

      fixtureDefs[0] = new FixtureDef();
      c.x = (TILE_WIDTH - THICKNESS) / 4;
      shape.setAsBox((TILE_WIDTH + THICKNESS) / 4, THICKNESS / 2, c, 0);
      fixtureDefs[0].shape = shape;
      
      fixtureDefs[1] = new FixtureDef();
      c.x = 0;
      c.y = (TILE_WIDTH - THICKNESS) / 4;
      shape = new PolygonShape();
      shape.setAsBox(THICKNESS / 2, (TILE_WIDTH + THICKNESS) / 4, c, 0);
      fixtureDefs[1].shape = shape;
      break;
    case BOTRIGHT:
      fixtureDefs = new FixtureDef[2];

      fixtureDefs[0] = new FixtureDef();
      c.x = -(TILE_WIDTH - THICKNESS) / 4;
      shape.setAsBox((TILE_WIDTH + THICKNESS) / 4, THICKNESS / 2, c, 0);
      fixtureDefs[0].shape = shape;
      
      fixtureDefs[1] = new FixtureDef();
      c.x = 0;
      c.y = (TILE_WIDTH - THICKNESS) / 4;
      shape = new PolygonShape();
      shape.setAsBox(THICKNESS / 2, (TILE_WIDTH + THICKNESS) / 4, c, 0);
      fixtureDefs[1].shape = shape;
      break;
    case TOPRIGHT:
      fixtureDefs = new FixtureDef[2];

      fixtureDefs[0] = new FixtureDef();
      c.x = -(TILE_WIDTH - THICKNESS) / 4;
      shape.setAsBox((TILE_WIDTH + THICKNESS) / 4, THICKNESS / 2, c, 0);
      fixtureDefs[0].shape = shape;
      
      fixtureDefs[1] = new FixtureDef();
      c.x = 0;
      c.y = -(TILE_WIDTH - THICKNESS) / 4;
      shape = new PolygonShape();
      shape.setAsBox(THICKNESS / 2, (TILE_WIDTH + THICKNESS) / 4, c, 0);
      fixtureDefs[1].shape = shape;
      break;
    case TOP:
      fixtureDefs = new FixtureDef[1];
      
      fixtureDefs[0] = new FixtureDef();
      shape.setAsBox(TILE_WIDTH / 2, THICKNESS / 2);
      fixtureDefs[0].shape = shape;
      break;
    case BOT:
      fixtureDefs = new FixtureDef[1];
      
      fixtureDefs[0] = new FixtureDef();
      shape.setAsBox(TILE_WIDTH / 2, THICKNESS / 2);
      fixtureDefs[0].shape = shape;
      break;
    case LEFT:
      fixtureDefs = new FixtureDef[1];
      
      fixtureDefs[0] = new FixtureDef();
      shape.setAsBox(THICKNESS / 2, TILE_WIDTH / 2);
      fixtureDefs[0].shape = shape;
      break;
    case RIGHT:
      fixtureDefs = new FixtureDef[1];
      
      fixtureDefs[0] = new FixtureDef();
      shape.setAsBox(THICKNESS / 2, TILE_WIDTH / 2);
      fixtureDefs[0].shape = shape;
      break;
    }

    if (fixtureDefs.length > 0 && fixtureDefs[0] != null) {
      fixtureDefs[0].friction = 0;
    }
    
    if (fixtureDefs.length > 1 && fixtureDefs[1] != null) {
      fixtureDefs[1].friction = 0;
    }
  }
}
