package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import sun.font.TextLabel;

import java.util.Iterator;

import static com.mygdx.game.MainMenu.SCREEN_HEIGHT;
import static com.mygdx.game.MainMenu.SCREEN_WIDTH;

//public class DropGame extends ApplicationAdapter {
public class GameScreen implements Screen {
	final DropGame game;
	// Use badlogic for performance reasons
	private final Array<Rectangle> raindrops;
	private final Rectangle bucket;

	private Texture dropImage;
	private final Vector3 touchPos;
	private long lastDropTime;

	private Texture bucketImage;
	public TextLabel font;
	OrthographicCamera camera;

	private Sound dropSound;
	private Music rainMusic;
	private int dropCaughtCount;

	public GameScreen(final DropGame game) {
		this.game = game;
		loadAssets();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

		touchPos = new Vector3();

		bucket = new Rectangle();
		bucket.width = 64;
		bucket.height = 64;
		bucket.x = (SCREEN_WIDTH / 2) - (bucket.width / 2);
		bucket.y = 20;

		raindrops = new Array<>();
		spawnRaindrop();
		dropCaughtCount = 0;
	}

	/**
	 * Populates the class fields, with assets loaded from disk
	 */
	void loadAssets() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("drop_image.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop_sound.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
	}


	public void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.width = 64;
		raindrop.height = 64;
		raindrop.x = MathUtils.random(0, SCREEN_WIDTH - raindrop.width);
		raindrop.y = SCREEN_HEIGHT;

		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void show() {
		rainMusic.play();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();
		game.font.draw(game.batch, "Drops Collected: " + dropCaughtCount, 0, 480);
		game.batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop : raindrops) {
			game.batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		game.batch.end();

		/// User Input
		// Move bucket to mouse click
		if (Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - bucket.width / 2;
		}
		// Move bucket if arrow keys
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			game.setScreen(new MainMenu(game));
		}

		// Check bucket is in screen bounds
		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > SCREEN_WIDTH - bucket.width) bucket.x = SCREEN_WIDTH - bucket.width;

		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + raindrop.height < 0) iter.remove();
			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				dropCaughtCount += 1;
				iter.remove();
			}
		}
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
	}
}
