package com.ramenstudio.sandglass.game.controller;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.ramenstudio.sandglass.game.controller.UIController.UIState;
import com.ramenstudio.sandglass.game.model.GameModel;
import com.ramenstudio.sandglass.game.model.GameObject;
import com.ramenstudio.sandglass.game.model.GameState;
import com.ramenstudio.sandglass.game.model.Gate;
import com.ramenstudio.sandglass.game.model.Monster;
import com.ramenstudio.sandglass.game.model.Monster.MonsterLevel;
import com.ramenstudio.sandglass.game.model.Player;
import com.ramenstudio.sandglass.game.model.Resource;
import com.ramenstudio.sandglass.game.model.ShipPiece;
import com.ramenstudio.sandglass.game.util.LevelLoader;
import com.ramenstudio.sandglass.game.util.LevelLoader.LayerKey;
import com.ramenstudio.sandglass.game.view.GameCanvas;
import com.ramenstudio.sandglass.util.controller.SoundController;
import com.ramenstudio.sandglass.util.view.ui.KeyboardUIListener;

/**
 * This takes care of the game initialization, maintains update and drawing
 * order, and keeps the physics for the game.
 * 
 * @author Jiacong Xu
 */
public class GameController extends AbstractController implements ContactListener {

	/** If this flag is true, we need a new game controller. */
	public boolean needsReset = false;

	/** If this flag is true, we need to go back to the menu. */
	public boolean needsMenu = false;

	// The physics world object
	public World world = new World(new Vector2(0, -9.8f), true);

	// The amount of time for a physics engine step.
	private static final float WORLD_STEP = 1/60.0f;

	// The maximum allowed time for each step in the simulation.
	private static final float WORLD_MAX_STEP = 1/4.0f;

	// Number of velocity iterations for the constrain solvers
	private static final int WORLD_VELOC = 6;

	// Number of position iterations for the constrain solvers
	private static final int WORLD_POSIT = 2;

	// An accumulator for last processed physics step time.
	private float accumulator;

	// The place to store all top-level game related data.
	private GameModel gameModel = new GameModel();

	//Is the player collided with the gate?
	private boolean touchingGate = false;

	// The player controller for the game
	private PlayerController playerController;

	/** The Camera Controller for this game */
	private CameraController cameraController;

	// The UI controller for the game - special case. Do not draw UI inside game
	// controller.
	public UIController uiController;

	// Arrays of Monstercontroller
	private Array<MonsterController> monsterController = new Array<MonsterController>();

	//Level Loader
	public LevelLoader loader = new LevelLoader();

	private Map<LevelLoader.LayerKey, Array<GameObject>> mapObjects;
	
	// bounds of the level
	private Rectangle bound;
	
	// callback when level has changed
	public boolean isChangingLevel;

	//fade time for UI messages
	private static int MESSAGE_FADETIME = 3;
	//current alpha value of message
	private float alpha = 1.0f;

	//For handling High Scores + Time
	private static Preferences prefs;
	private String prefFile = "sandglass_scores";
	private int level;
	private int currentTime = 0;
	private float timeCount = 0;
	private int highScore;


	public GameController(int gameLevel) {
		//Load/set high score
		prefs = Gdx.app.getPreferences(prefFile);
		// Provide default high score of 0
		if (!prefs.contains("highScore" + gameLevel)) {
			prefs.putInteger("highScore" + gameLevel, 0);
		}
		level = gameLevel;
		highScore = getHighScore();

		uiController = new UIController(gameLevel);
		getGameModel().setGameLevel(gameLevel);
		mapObjects = loader.loadLevel(gameLevel);
		SoundController.getInstance().playBGMForLevel(gameLevel);
		Player player = (Player) mapObjects.get(LayerKey.PLAYER).get(0);
		player.setFlips(loader.maxFlip);
		Vector2 cameraCenter = loader.center;
		bound = loader.bound;
		//	System.out.println(cameraCenter.toString());
		playerController = new PlayerController(player);
		cameraController = new CameraController(cameraCenter,loader.zoom);
		cameraController.setTarget(player);

		Array<GameObject> mArray = (Array<GameObject>) 
				mapObjects.get(LevelLoader.LayerKey.MONSTER);
		Gate gate = (Gate) ((Array<GameObject>) 
				mapObjects.get(LevelLoader.LayerKey.GATE)).get(0);
		Array<GameObject> ship = (Array<GameObject>) 
				mapObjects.get(LevelLoader.LayerKey.SHIP);
		List<ShipPiece> shipList = gameModel.getShipPieces();
		for (GameObject s: ship){
			shipList.add((ShipPiece) s);
		}
		Array<GameObject> resource = (Array<GameObject>)
				mapObjects.get(LevelLoader.LayerKey.RESOURCE);
		List<Resource> resourceList = gameModel.getResources();
		for (GameObject r: resource){
			resourceList.add((Resource) r);
		}

		gameModel.setNumberOfPieces(ship.size);
		gameModel.setGate(gate);

		for (GameObject m: mArray){
			monsterController.add(new MonsterController((Monster) m));
		}

		// Set up the world!
		objectSetup(world);

		// Set up UI callbacks
		uiController.pauseView.resumeButton.addListener(resumeButtonCallback);
		uiController.pauseView.restartButton.addListener(restartButtonCallback);
		uiController.pauseView.mainMenuButton.addListener(mainMenuButtonCallback);
		uiController.levelCompleteView.restartButton.addListener(restartButtonCallback);
		uiController.levelCompleteView.mainMenuButton.addListener(mainMenuButtonCallback);
		uiController.levelFailedView.restartButton.addListener(restartButtonCallback);
		uiController.levelFailedView.mainMenuButton.addListener(mainMenuButtonCallback);
		uiController.levelCompleteView.nextLevelButton.addListener(nextLevelButtonCallback);

		uiController.gameView.setMessage("Level "+ level);
	}

	/**
	 * UI Callbacks
	 */

	/**
	 * Called when resume button from the paused screen is clicked.
	 */
	private KeyboardUIListener resumeButtonCallback = new KeyboardUIListener() {
		@Override
		public void interacted() {
			resumeGame();
		}
	};

	/**
	 * Called when restart button from the paused screen is clicked.
	 */
	private KeyboardUIListener restartButtonCallback = new KeyboardUIListener() {
		@Override
		public void interacted() {
			reset();
			uiController.setGameState(UIController.UIState.PLAYING);
		}
	};

	/**
	 * Called when main menu button from the paused screen is clicked.
	 */
	private KeyboardUIListener mainMenuButtonCallback = new KeyboardUIListener() {
		@Override
		public void interacted() {
			needsMenu = true;
		}
	};

	/**
	 * Called when player finished a level and wants to go to next level.
	 */
	private KeyboardUIListener nextLevelButtonCallback = new KeyboardUIListener() {
		@Override
		public void interacted() {
			if (gameModel.setGameLevel(gameModel.getGameLevel() + 1)) {
				reset();
				isChangingLevel = true;
			} else {
				System.out.println("You already cleared the final level!");
			}
		}
	};

	@Override
	public void objectSetup(World world) {
		Array<GameObject> mapTiles = mapObjects.get(LevelLoader.LayerKey.GROUND);

		for (GameObject o : mapTiles) {
			activatePhysics(world, o);
		}

		playerController.objectSetup(world);
		//    cameraController.setTarget(playerController.getPlayer());
		cameraController.objectSetup(world);

		for (MonsterController m: monsterController){
			m.objectSetup(world);
			m.monster.target = playerController.getPlayer();
		}

		for (ShipPiece c : getGameModel().getShipPieces()) {
			activatePhysics(world, c);
		}

		for (Resource r: gameModel.getResources()){
			activatePhysics(world, r);
		}

		activatePhysics(world, gameModel.getGate());

		world.setContactListener(this);
	}

	@Override
	public void update(float dt) {

		uiController.update(dt);

		if (getGameModel().getGameState() != GameState.TUTORIAL) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
				if (getGameModel().getGameState() == GameState.PLAYING) {
					pauseGame();
				} else if (getGameModel().getGameState() == GameState.PAUSED) {
					resumeGame();
				}
			}
			
			if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
				if (getGameModel().getGameState() == GameState.LOST) {
					reset();
				}
			}
		}

		switch (getGameModel().getGameState()) {
			case TUTORIAL:
				if (uiController.tutorialView.isDismissed) {
					getGameModel().setGameState(GameState.PLAYING);
					uiController.setGameState(UIState.PLAYING);
				} else {
					uiController.setGameState(UIState.TUTORIAL);
				}

				return;
			case LOST:
				uiController.setGameState(UIState.LOST);
				return;
			case PAUSED:
				return;
			case PLAYING:
				break;
			case WON:
				if (currentTime < highScore || highScore == 0){
					setHighScore(currentTime);
					uiController.levelCompleteView.newHighScore = true;
				}
				uiController.levelCompleteView.setTime(currentTime, highScore);
				uiController.setGameState(UIState.WON);
				cameraController.update(dt);
				return;
		}
		
		updateTime(dt);
		if (currentTime >= MESSAGE_FADETIME && alpha != 0.0){
			alpha = uiController.gameView.fadeMessage();
		}

		uiController.gameView.setCurrentTime(currentTime);
		uiController.gameView.setFlipCount(playerController.getPlayer().getFlips());
		uiController.gameView.setShipPieceCount(gameModel.getCollectedPieces(), gameModel.getNumberOfPieces());

		cameraController.update(dt);
		// Order matters. Must call update BEFORE rotate on cameraController.
		if (playerController.getRotateAngle() != 0f) {
			cameraController.rotate(playerController.getRotateAngle());
		}
		boolean frozen = !cameraController.doneRotating;
		playerController.setFrozen(frozen);
		playerController.update(dt);

		boolean isUnder = playerController.isUnder();
		gameModel.setWorldPosition(!isUnder);

		for (MonsterController m: monsterController){
			m.monster.isFrozen = frozen;
			m.monster.setUnder(isUnder);
			m.monster.update(dt);
		}

		SoundController.getInstance().update();


		stepPhysics(dt);

		//System.out.println("the player velocity is : " + playerController.getPlayer().getBody().getLinearVelocity());

		if (!bound.contains(playerController.getPlayer().getPosition()) || 
				playerController.getPlayer().getFlips()<0){
			getGameModel().setGameState(GameState.LOST);
		}


		if (playerController.isReset()){
			reset();

		}
	}


	private void pauseGame() {
		uiController.setGameState(UIController.UIState.PAUSED);
		getGameModel().setGameState(GameState.PAUSED);
	}

	private void resumeGame() {
		uiController.setGameState(UIController.UIState.PLAYING);
		getGameModel().setGameState(GameState.PLAYING);
	}

	private void reset() {
		needsReset = true;
	}

	/**
	 * Simulates the world by dt. If dt is too long, we break the update into
	 * multiple steps.
	 */
	private void stepPhysics(float dt) {
		float frameTime = Math.min(dt, WORLD_MAX_STEP);
		accumulator += frameTime;
		while (accumulator >= WORLD_STEP) {
			world.step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);
			accumulator -= WORLD_STEP;
		}
	}

	@Override
	public void draw(GameCanvas canvas) {
		// Draw a background image
		getGameModel().draw(canvas);
		playerController.draw(canvas);
		cameraController.draw(canvas);

		for (MonsterController m: monsterController){
			m.draw(canvas);
		}
	}

	/**
	 * @return the world to screen transformation matrix kept by the camera.
	 */
	public Matrix4 world2ScreenMatrix() {
		return cameraController.world2ScreenMatrix();
	}

	/**
	 * @return the main camera used by the game
	 */
	public OrthographicCamera getViewCamera() {
		return cameraController.getViewCamera();
	}

	public PlayerController getPlayerController() {
		return playerController;
	}

	public CameraController getCameraController() {
		return cameraController;
	}

	@Override
	public void beginContact(Contact contact) {
		if(contact.getFixtureA().getBody().getUserData() == Player.class  &&
				contact.getFixtureB().getBody().getUserData() == Gate.class){
			touchingGate = true;
		}

		GameObject firstOne = (GameObject) contact.getFixtureA().getBody().getUserData();
		GameObject secondOne = (GameObject) contact.getFixtureB().getBody().getUserData();

		if ((firstOne instanceof Player && secondOne instanceof Monster) ||
				(secondOne instanceof Player && firstOne instanceof Monster)) {
			Monster theMonster;
			Player thePlayer;

			if (secondOne instanceof Monster) {
				theMonster = (Monster)secondOne;
				thePlayer = (Player)firstOne;
			} else {
				theMonster = (Monster)firstOne;
				thePlayer = (Player)secondOne;
			}

			if (!thePlayer.isFlashing) {
				SoundController.getInstance().playMonsterHit();
				if (theMonster.monsterLevel == MonsterLevel.KILL) {
					getGameModel().setGameState(GameState.LOST);
				} else if (theMonster.monsterLevel == MonsterLevel.DEDUCT_FLIPS) {
					thePlayer.setDeductFlip(true);
				} else if (theMonster.monsterLevel == MonsterLevel.MAKE_FLIP) {
					thePlayer.setTouchMF(true);
				}

				thePlayer.isFlashing = true;
			}


		}

		if (firstOne instanceof Player &&
				secondOne instanceof ShipPiece) {
			ShipPiece secondShipPiece = (ShipPiece) secondOne;
			if (!secondShipPiece.getIsCollected()) {
				SoundController.getInstance().playItemCollect();
				secondShipPiece.setCollected();
				getGameModel().collectPiece();
			}
		} else if (firstOne instanceof ShipPiece &&
				secondOne instanceof Player) {
			ShipPiece firstShipPiece = (ShipPiece) firstOne;
			if (!firstShipPiece.getIsCollected()) {
				SoundController.getInstance().playItemCollect();
				firstShipPiece.setCollected();
				getGameModel().collectPiece();
			}
		}

		if (getGameModel().allPiecesCollected()) {
			if (!getGameModel().getGate().getAllPiecesCollected()) {
				SoundController.getInstance().playDoorOpen();
			}
			getGameModel().getGate().setAllPiecesCollected(true);
		}

		if (firstOne instanceof Player &&
				secondOne instanceof Resource) {
			Resource secondResource = (Resource) secondOne;
			if (!secondResource.getIsCollected()) {
				SoundController.getInstance().playSandglassCollect();
				secondResource.setCollected();
				playerController.getPlayer().addFlip();
			}
		} else if (firstOne instanceof Resource &&
				secondOne instanceof Player) {
			Resource firstResource = (Resource) firstOne;
			if (!firstResource.getIsCollected()) {
				SoundController.getInstance().playSandglassCollect();
				firstResource.setCollected();
				playerController.getPlayer().addFlip();
			}
		}

		if ((firstOne instanceof Player && secondOne instanceof Gate) ||
				(firstOne instanceof Gate && secondOne instanceof Player)) {
			if (getGameModel().allPiecesCollected()) {
				SoundController.getInstance().playLevelComplete();
				// We won!
				getGameModel().setGameState(GameState.WON);
			}
		}
	}

	/**
	 * @return the gameModel
	 */
	public GameModel getGameModel() {
		return gameModel;
	}

	@Override
	public void endContact(Contact contact) {}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}

	public void dispose() {
		world.dispose();
		gameModel.dispose();
		for (MonsterController mc: monsterController){
			mc.dispose();
		}
		cameraController.dispose();
		playerController.dispose();
		uiController.dispose();
		
	}

	public void updateTime(float dt){
		timeCount += dt;
		if (timeCount >= 1){
			currentTime++;
			timeCount = 0;
		}
	}

	public int getHighScore(){
		return prefs.getInteger("highScore"+level);
	}

	public void setHighScore(int i){
		prefs.putInteger("highScore"+level, i);
		prefs.flush();
	}

}
