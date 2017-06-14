package com.cooksys.assessment.interfaces;

import com.cooksys.assessment.model.Message;

public interface IBroadcasterListener {
	public void recieveMessage(Message message);
}
