package com.example.soumilchugh.exposocial;

import java.util.ArrayList;
import java.util.Collections;

public class fearLadder {
    public ArrayList<String> situations;
    public ArrayList<Integer> scaleValues;

    public fearLadder(ArrayList<String> situations, ArrayList<Integer> scaleValues){
        Collections.reverse(situations);
        Collections.reverse(scaleValues);
        this.situations = situations;
        this.scaleValues = scaleValues;
    }
}
