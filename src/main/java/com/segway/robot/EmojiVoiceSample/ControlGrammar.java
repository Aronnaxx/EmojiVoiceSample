package com.segway.robot.EmojiVoiceSample;

import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;


public class ControlGrammar {
    public static GrammarConstraint createMoveSlotGrammar() {
        GrammarConstraint moveSlotGrammar = new GrammarConstraint();
        moveSlotGrammar.setName("movement orders");

        Slot moveSlot = new Slot("movement");
        moveSlot.setOptional(false);
        moveSlot.addWord("look");
        moveSlot.addWord("turn");
        moveSlot.addWord("Tell me a story about");
        moveSlotGrammar.addSlot(moveSlot);

        Slot orientationSlot = new Slot("orientation");
        orientationSlot.setOptional(false);
        orientationSlot.addWord("right");
        orientationSlot.addWord("left");
        orientationSlot.addWord("up");
        orientationSlot.addWord("down");
        orientationSlot.addWord("full");
        orientationSlot.addWord("around");
        orientationSlot.addWord("space");
        moveSlotGrammar.addSlot(orientationSlot);

        return moveSlotGrammar;
    }
}

