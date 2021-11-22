package game.view;

import javafx.scene.image.Image;

/**
this class serves a few different purposes:

1: loading all the images in a static initializer ensures that
all the slow file IO happens before the game starts,
and random game events do not trigger lag spikes for the user.

2: keeping all the images in one place (as opposed to these fields being declared in the
classes which use them) ensures that no image is ever read from disk more than once.

3: if an image stops being used, I will see it underlined here as an unused field,
and I can choose to move or delete the associated file.

4: the resolutions for all images are checked on load,
so if I change an image size but forget to update my code accordingly,
I'll know about it because the application will crash on launch.

5: same goes for if I rename an image and forget to rename the reference to it.

6: crashing on launch also means that I don't need to wait until
some other random part of code tries to use the image for something,
and I don't need to play through the whole game to know if it works or not.

@author Michael Johnston (tky886)
*/
public class Images {

	public static final Image
		EXPLOSION                    = getImage("explosion",                    576,  64),
		EXTRA_LIFE_STAT              = getImage("extra_life_stat",               16,  16),
		EXTRA_LIFE_UPGRADE           = getImage("extra_life_upgrade",            64,  64),
		FIRE_POWER_DOWNGRADE         = getImage("fire_power_downgrade",          64,  64),
		FIRE_POWER_STAT              = getImage("fire_power_stat",               16,  16),
		FIRE_POWER_UPGRADE           = getImage("fire_power_upgrade",            64,  64),
		FIRE_RATE_DOWNGRADE          = getImage("fire_rate_downgrade",           64,  64),
		FIRE_RATE_STAT               = getImage("fire_rate_stat",                16,  16),
		FIRE_RATE_UPGRADE            = getImage("fire_rate_upgrade",             64,  64),
		GHOST_UPGRADE                = getImage("ghost_upgrade",                 64,  64),
		SHIP                         = getImage("ship",                          64,  64),
		SHIP_GHOST                   = getImage("ship_ghost",                    64,  64),
		SPEED_DOWNGRADE              = getImage("speed_downgrade",               64,  64),
		SPEED_STAT                   = getImage("speed_stat",                    16,  16),
		SPEED_UPGRADE                = getImage("speed_upgrade",                 64,  64),
		TIME_WARP_UPGRADE            = getImage("time_warp_upgrade",             64,  64),
		WIDE_SPREAD_UPGRADE          = getImage("wide_spread_upgrade",           64,  64),

		GUI_CONTROLS                 = getImage("gui/controls",                 256,  64),
		GUI_GAME_OVER                = getImage("gui/game_over",                256, 192),
		GUI_KEYBOARD_CONTROL         = getImage("gui/keyboard_control",          64,  64),
		GUI_KEYBOARD_CONTROL_HOVERED = getImage("gui/keyboard_control_hovered",  64,  64),
		GUI_KEYBOARD_CONTROL_PRESSED = getImage("gui/keyboard_control_pressed",  64,  64),
		GUI_MOUSE_CONTROL            = getImage("gui/mouse_control",             64,  64),
		GUI_MOUSE_CONTROL_HOVERED    = getImage("gui/mouse_control_hovered",     64,  64),
		GUI_MOUSE_CONTROL_PRESSED    = getImage("gui/mouse_control_pressed",     64,  64),
		GUI_PLAY                     = getImage("gui/play",                     320,  64),
		GUI_PLAY_AGAIN               = getImage("gui/play_again",               320,  64),
		GUI_PLAY_AGAIN_HOVERED       = getImage("gui/play_again_hovered",       320,  64),
		GUI_PLAY_AGAIN_PRESSED       = getImage("gui/play_again_pressed",       320,  64),
		GUI_PLAY_HOVERED             = getImage("gui/play_hovered",             320,  64),
		GUI_PLAY_PRESSED             = getImage("gui/play_pressed",             320,  64);

	private static Image getImage(String path, int expectedWidth, int expectedHeight) {
		path = "/assets/" + path + ".png";
		Image image;
		try {
			image = new Image(path);
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalStateException("Image probably not found: " + path, exception);
		}
		if (image.getWidth() != expectedWidth || image.getHeight() != expectedHeight) {
			throw new IllegalStateException("Wrong resolution for " + path + ": expected " + expectedWidth + 'x' + expectedHeight + ", got " + image.getWidth() + 'x' + image.getHeight());
		}
		return image;
	}

	//does nothing, but will trigger classloading and
	//therefore initialization of all the above images.
	//loading images is slow, so this should be called before the game starts.
	public static void clinit() {}
}