package com.segway.robot.EmojiVoiceSample;

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

        Slot firstSlot = new Slot("movement");
        firstSlot.addWord("move");
        firstSlot.addWord("go");
        firstSlot.addWord("turn");
        firstSlot.addWord("rotate");

        Slot secondSlot = new Slot("direction");
        secondSlot.addWord("forward");
        secondSlot.addWord("backward");
        secondSlot.addWord("left");
        secondSlot.addWord("right");

        moveSlotGrammar.addSlot(firstSlot);
        moveSlotGrammar.addSlot(secondSlot);


        return moveSlotGrammar;
    }
}

