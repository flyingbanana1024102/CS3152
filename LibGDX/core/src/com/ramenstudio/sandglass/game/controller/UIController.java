package com.ramenstudio.sandglass.game.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ramenstudio.sandglass.game.view.GameCanvas;

/**
 * The root controller for all UI-related functionalities in-game. This keeps
 * the stage object.
 * 
 * @author Jiacong Xu
 */
public class UIController extends AbstractController {

  private Stage stage = new Stage();
  private Skin skin = new Skin();
  
  public UIController() {
    Gdx.input.setInputProcessor(stage);
    
    skin = new Skin();

    // Generate a 1x1 white texture and store it in the skin named "white".
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();
    skin.add("white", new Texture(pixmap));

    // Store the default libgdx font under the name "default".
    skin.add("default", new BitmapFont());

    // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
    TextButtonStyle textButtonStyle = new TextButtonStyle();
    textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
    textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
    textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
    textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
    textButtonStyle.font = skin.getFont("default");
    skin.add("default", textButtonStyle);

    // Create a table that fills the screen. Everything else will go inside this table.
    Table table = new Table();
    table.setFillParent(true);
    stage.addActor(table);
    
    // Add an image actor. Have to set the size, else it would be the size of the drawable (which is the 1x1 texture).
    table.add(new Image(skin.newDrawable("white", Color.GRAY))).size(64);

    // Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
    final TextButton button = new TextButton("Click me!", skin);
    table.add(button);

    // Add a listener to the button. ChangeListener is fired when the button's checked state changes, eg when clicked,
    // Button#setChecked() is called, via a key press, etc. If the event.cancel() is called, the checked state will be reverted.
    // ClickListener could have been used, but would only fire when clicked. Also, canceling a ClickListener event won't
    // revert the checked state.
    button.addListener(new ChangeListener() {
      public void changed (ChangeEvent event, Actor actor) {
        System.out.println("Clicked! Is checked: " + button.isChecked());
        button.setText("UI button test success!");
      }
    });

  }
  
  @Override
  public void update(float dt) {
    stage.act(dt);
  }

  @Override
  public void draw(GameCanvas canvas) {
    stage.draw();
  }

  @Override
  public void objectSetup(PhysicsDelegate handler) {
    // TODO Auto-generated method stub

  }

}
