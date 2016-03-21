package com.ramenstudio.sandglass.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.ramenstudio.sandglass.game.controller.GameController;
import com.ramenstudio.sandglass.game.view.GameCanvas;
import com.ramenstudio.sandglass.util.AbstractMode;

/**
 * This is the controller for any game level. It is the root for each level. It
 * implements an update-draw loop and handles application-based events.
 * 
 * @author Jiacong Xu
 */
public class GameMode extends AbstractMode implements Screen {
  // The game play controller that handles the basic logic of the game
  private GameController gameplayController = new GameController();
  
  // The game canvas.
  private GameCanvas canvas = new GameCanvas();

  // A debug renderer
  Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
  
  /**
   * Initializes an instance of the game with all the controllers, model and
   * view canvas.
   */
  public GameMode() {
  }

  @Override
  public void show() {
  }

  @Override
  public void render(float delta) {
    // Implements an update-draw loop
    gameplayController.update(delta);
    
    // Now we render all objects that we can render
    canvas.clear();
    canvas.begin(gameplayController.world2ScreenMatrix());
    gameplayController.draw(canvas);
    canvas.end();

    debugRenderer.render(gameplayController.world, gameplayController.world2ScreenMatrix());
  }

  @Override
  public void resize(int width, int height) {
    // We should not need to resize, ever.
  }

  @Override
  public void pause() {
    // Called when the game is paused (loss of focus)
  }

  @Override
  public void resume() {
    // Called when the game regains focus.
  }

  @Override
  public void hide() {
    // When the game disposes this mode.
  }

  @Override
  public void dispose() {
    // When we should release all resources for this screen.
  }


@Override
public String[] getTexturePaths() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public String[] getSoundPaths() {
	// TODO Auto-generated method stub
	return null;
}
  
}
