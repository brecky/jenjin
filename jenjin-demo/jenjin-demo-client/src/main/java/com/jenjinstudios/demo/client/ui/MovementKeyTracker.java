package com.jenjinstudios.demo.client.ui;

import com.jenjinstudios.world.math.Angle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * @author Caleb Brinkman
 */
public class MovementKeyTracker
{
	private boolean upKey, downKey, leftKey, righKey;

	public Angle getMoveAngle(Angle angle) {
		if (upKeyNotDown())
		{
			angle = getUpKeyAngle(angle);
		} else if (downKeyNotUp())
		{
			angle = getDownKeyAngle(angle);
		} else if (leftKeyNotRight())
		{
			angle = getLeftKeyAngle();
		} else if (rightKeyNotLeft())
		{
			angle = getRightKeyAngle();
		}
		return angle;
	}

	private Angle getRightKeyAngle() {
		Angle angle;
		angle = new Angle(Angle.FRONT, Angle.FRONT);
		return angle;
	}

	private Angle getLeftKeyAngle() {
		Angle angle;
		angle = new Angle(Angle.BACK, Angle.FRONT);
		return angle;
	}

	private boolean rightKeyNotLeft() {return righKey && !leftKey;}

	private boolean leftKeyNotRight() {return leftKey && !righKey;}

	private boolean downKeyNotUp() {return downKey && !upKey;}

	private boolean upKeyNotDown() {return upKey && !downKey;}

	private Angle getDownKeyAngle(Angle angle) {
		if (leftKeyNotRight())
		{
			angle = new Angle(Angle.BACK_LEFT, Angle.FRONT);
		} else if (rightKeyNotLeft())
		{
			angle = new Angle(Angle.FRONT_LEFT, Angle.FRONT);
		} else if (!righKey)
		{
			angle = new Angle(Angle.LEFT, Angle.FRONT);
		}
		return angle;
	}

	private Angle getUpKeyAngle(Angle angle) {
		if (leftKeyNotRight())
		{
			angle = new Angle(Angle.BACK_RIGHT, Angle.FRONT);
		} else if (rightKeyNotLeft())
		{
			angle = new Angle(Angle.FRONT_RIGHT, Angle.FRONT);
		} else if (!righKey)
		{
			angle = new Angle(Angle.RIGHT, Angle.FRONT);
		}
		return angle;
	}

	public void setKeyFlags(KeyEvent keyEvent) {
		KeyCode keyCode = keyEvent.getCode();
		if (keyCode.equals(KeyCode.UP) || keyCode.equals(KeyCode.W))
		{
			setUpKeyFlag(keyEvent);
		}
		if (keyCode.equals(KeyCode.DOWN) || keyCode.equals(KeyCode.S))
		{
			setDownKeyFlag(keyEvent);
		}
		if (keyCode.equals(KeyCode.LEFT) || keyCode.equals(KeyCode.A))
		{
			setLeftKeyDown(keyEvent);
		}
		if (keyCode.equals(KeyCode.RIGHT) || keyCode.equals(KeyCode.D))
		{
			setRightKeyDown(keyEvent);
		}
	}

	private void setRightKeyDown(KeyEvent keyEvent) {
		if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED && !righKey)
		{
			righKey = true;
		} else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED && righKey)
		{
			righKey = false;
		}
	}

	private void setLeftKeyDown(KeyEvent keyEvent) {
		if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED && !leftKey)
		{
			leftKey = true;
		} else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED && leftKey)
		{
			leftKey = false;
		}
	}

	private void setDownKeyFlag(KeyEvent keyEvent) {
		if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED && !downKey)
		{
			downKey = true;
		} else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED && downKey)
		{
			downKey = false;
		}
	}

	private void setUpKeyFlag(KeyEvent keyEvent) {
		if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED && !upKey)
		{
			upKey = true;
		} else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED && upKey)
		{
			upKey = false;
		}
	}
}
