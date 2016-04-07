package com.ramenstudio.sandglass.game.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ramenstudio.sandglass.game.view.GameCanvas;
import com.ramenstudio.sandglass.game.view.ui.GameView;
import com.ramenstudio.sandglass.game.view.ui.OptionsView;
import com.ramenstudio.sandglass.game.view.ui.PauseView;

/**
 * The root controller for all UI-related functionalities in-game. This keeps
 * the stage and skin.
 * 
 * @author Jiacong Xu
 */
public class UIController extends AbstractController {
  private Stage stage;
  private Skin skin = new Skin(Gdx.files.internal("UI/Skin/uiskin.json"));
  
  public enum UIState {
    PLAYING, PAUSED, OPTIONS, LOST, WON
  }
  
  // For caching purposes.
  private UIState uiState;
  
  /**
   * The main game UI view.
   */
  public GameView gameView = new GameView(skin);
  
  /**
   * The paused UI view.
   */
  public PauseView pauseView = new PauseView(skin);
  
  /**
   * The options UI view.
   */
  public OptionsView optionsView = new OptionsView(skin);
  
  public UIController() {
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);
    
    // Add playing UI.
    stage.addActor(gameView);
    
    // Add paused UI.
    stage.addActor(pauseView);
    
    pauseView.optionsButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        setGameState(UIController.UIState.OPTIONS);
      }
    });
    
    // Add options UI.
    stage.addActor(optionsView);
    
    optionsView.backButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        setGameState(UIController.UIState.PAUSED);
      }
    });
    
    // Finally, let the game play.
    setGameState(UIState.PLAYING);
  }
  
  /**
   * Sets which game state we are in. Uses this to switch visibility of
   * different UIs.
   * 
   * @param state is the current play status of the game.
   */
  public void setGameState(UIState state) {
    if (state == uiState) {
      return;
    }
    
    // Hide all tables
    gameView.setVisible(false);
    pauseView.setVisible(false);
    optionsView.setVisible(false);
    
    switch (state) {
    case PLAYING:
      gameView.setVisible(true);
      break;
    case PAUSED:
      pauseView.setVisible(true);
      break;
    case OPTIONS:
      optionsView.setVisible(true);
      break;
    case WON:
      break;
    case LOST:
      break;
    }
  }
  
  @Override
  public void update(float dt) {
    stage.act(dt);
  }
  
  @Override
  public void draw(GameCanvas canvas) {
    stage.draw();
  }

}
