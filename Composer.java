package cbsp;
import javax.sound.midi.*;
import java.lang.*;
import java.util.*;

public class Composer {

    public static void main(String[] args) {
        //controlling variables
        int instrument = 1;
        int chordDuration = 1000; //500 or 1000

        //int[] scale = {60, 62, 64, 65, 67, 69, 71}; //C major
        //int[] inputComposition = {64,62,60,62,64,64,64,62,62,62,64,67,67,64,62,60,62,64,64,64,64,62,62,64,62,60}; //Marry had a little lamb
        int[] scale = {62, 64, 66, 67, 69, 71, 73}; //D major
        int[] inputComposition = {62, 64, 66, 69, 66, 64, 62, 66, 69, 71, 74, 73, 69, 66, 64, 62, 64, 66, 69, 66, 64, 62, 64, 62,
                                    66, 69, 71, 69, 66, 64, 62, 74, 76, 78, 78, 81, 76, 74, 76}; //concerning hobbits

        int numOfDistinctMeasures = 8; //even number 4
        int numOfMeasureVariations = 1;

        int numOfPhrases = 2; //1 or twice as large as numOfDistinctMeasures ***LEAVE 2 FOR TERNARY FORM OF COMPOSITION***
        int phraseRepetition = 1;

        int compositionRepetition = 2;



        //create measures (building unit of phrases)
        ArrayList<ArrayList<Note>> measures = new ArrayList<>();
        for(int i = 0; i < numOfDistinctMeasures; i ++){
            ArrayList<Note> measure = createMeasure(inputComposition, 2);
            measures.add(measure);

            for(int j = 0; j < numOfMeasureVariations; j ++){
                int [] measureNoteArray = getPitchArray(measure);

                ArrayList<Note> measureVariation = createMeasure(measureNoteArray, 1);
                measures.add(measureVariation);
            }
        }

        //create phrases from measures in BINARY MUSIC FORM (AB/AABB/...)
        ArrayList<ArrayList<Note>> phrases = new ArrayList<>();
        for(int p = 0; p < numOfPhrases; p ++) {
            ArrayList<Note> phrase = new ArrayList<>();
            for (int i = 0; i < (measures.size()/numOfPhrases); i++) {
                for (int j = 0; j < phraseRepetition; j++) {
                    phrase.addAll(measures.get(i+(p*2)));
                }
            }
            phrases.add(phrase);
        }

        //create melody composition from phrases in SIMPLE TERNARY FORM (ABA)
        ArrayList<Note> composition = new ArrayList<>();
        for(int i = 0; i < compositionRepetition; i ++) {
            composition.addAll(phrases.get(0));
            composition.addAll(phrases.get(1));
            composition.addAll(phrases.get(0));
        }

        //play the compositions with added chords
        MIDIsynthesizer ms = new MIDIsynthesizer();
        Note[][] majorChords = findMajorChords(scale, chordDuration);
        int measureSum = 0;
        int prevChord = 0;

        System.out.println(composition.size());
        for(int i = 0; i < composition.size(); i ++){
            Note n = composition.get(i);
            ms.playNote(0, instrument, n);

            measureSum += n.duration;
            if(measureSum == 1000){ //end of measure - add a chord
                //find a correct chord
                int currChord = -1;
                for (int j = 0; j < majorChords.length; j ++) {
                    for (int k = 0; k < majorChords[j].length; k++) {
                        if(majorChords[j][k].pitch == n.pitch) {
                            currChord = j;
                            if (currChord == 2 && prevChord == 2) {
                                currChord = 0;
                            }
                            break;
                        }
                    }
                    if(currChord != -1){
                        break;
                    }
                }
                //if we didnt find the chord for the note
                if(currChord == -1){
                    currChord = 0;
                }

                ms.playChord(2, instrument, majorChords[currChord]);

                measureSum = 0;
            }
        }


    }

    public static Note[][] findMajorChords(int[] scale, int duration){
        Note[][] majorChords = new Note[3][3];
        //I chord
        majorChords[0][0] = new Note(scale[0], duration, 50, 0);
        majorChords[0][1] = new Note(scale[2], duration, 50, 0);
        majorChords[0][2] = new Note(scale[4], duration, 50, 0);

        //IV chord
        majorChords[1][0] = new Note(scale[3], duration, 50, 0);;
        majorChords[1][1] = new Note(scale[5], duration, 50, 0);;
        majorChords[1][2] = new Note(scale[0], duration, 50, 0);;

        //V chord
        majorChords[2][0] = new Note(scale[4], duration, 50, 0);;
        majorChords[2][1] = new Note(scale[6], duration, 50, 0);;
        majorChords[2][2] = new Note(scale[1], duration, 50, 0);;

        return majorChords;
    }

    public static int [] getPitchArray(ArrayList<Note> list){
        int [] array = new int[list.size()];
        for(int i = 0; i < list.size(); i ++){
            array[i] = list.get(i).pitch;
        }
        return array;
    }

    public static ArrayList<Note> createMeasure(int [] input, int order){

        //find unique notes that appear in input
        Set<Integer> setUniqueNumbers = new LinkedHashSet<Integer>();
        for(int x : input) {
            setUniqueNumbers.add(x);
        }
        int[] keys = new int[setUniqueNumbers.size()];
        int pos = 0;
        for(Integer x : setUniqueNumbers) {
            keys[pos] = x;
            pos++;
        }
        Arrays.sort(keys);

        //create markov matrix based od input and order
        double[][] markovMatrix = markovChain(input, keys, order);
        //divide the measure to a 4/4 scale
        ArrayList<Note> measure = divide44Measure(500);

        //The output as an index value
        int output = 0;

        //generate starting seed
        Random r = new Random();
        int seed = r.nextInt(keys.length);
        for(int i = 0; i < markovMatrix.length; i ++) {
            double sum = 0;
            for (int j = 0; j < markovMatrix[i].length; j++) {
                sum += markovMatrix[i][j];
            }
            if(sum > 0){
                seed = i;
                break;
            }
        }

        //generate prev output for order 2
        int prevOutput = -1;
        int[][] lookupTable = createLookupTable2(keys);
        if(order == 2) {
            for (int i = 0; i < lookupTable.length; i++) {
                for (int j = 0; j < lookupTable[i].length; j++) {
                    if (lookupTable[i][j] == seed) {
                        prevOutput = j;
                        break;
                    }
                }
                if(prevOutput != -1){
                    break;
                }
            }
        }

        //Generate notes and add them to a phrase
        for(int i=0;i<measure.size();i++){
            //Retrieve a random number between 0.0 and 1.0
            double choice = Math.random();
            //The current sum of weightings left to right
            double currentSum = 0.0;
            //Check matrix left to right
            for(output = 0;output<markovMatrix[0].length;output++){
                currentSum += markovMatrix[seed][output];
                if(choice <= currentSum){
                    break; //break when we have chosen the right number
                }
            }

            //if we are stuck in a state with no exits, we stay in that state
            if(currentSum == 0){
                output = seed;
            }

            //ms.playNote(0, 1, new Note(keys[output], 500, 50, 0));
            measure.get(i).pitch = keys[output];

            //Change the seed for next note interation
            if(order == 2) {
                //lookup
                int index = lookupTable[prevOutput][output];
                prevOutput = output;

                seed = index;
            }
            else{
                seed = output;
            }
        }
        return measure;
    }

    public static ArrayList<Note> divide44Measure(int duration){
        ArrayList<Note> list = new ArrayList<Note>();
        if(duration >= 250){
            //define chance weight
            double chance;
            if(duration == 500){
                chance = 0.75; //0.75
            }else{ //if(duration == 250)
                chance = 0.25;
            }

            //left side
            ArrayList<Note> list1 = new ArrayList<Note>();
            double choice1 = Math.random();
            if(choice1 <= chance) {
                list1.addAll(divide44Measure(duration/2));
            }else{
                list1.add(new Note(0, duration, 50, 0));
            }

            //right side
            ArrayList<Note> list2 = new ArrayList<Note>();
            double choice2 = Math.random();
            if(choice2 <= chance) {
                list2.addAll(divide44Measure(duration/2));
            }else{
                list2.add(new Note(0, duration, 50, 0));
            }

            //add the lists together
            list.addAll(list1);
            list.addAll(list2);
        }
        else{
            //left side
            list.add(new Note(0, duration, 50, 0));

            //right side
            list.add(new Note(0, duration, 50, 0));
        }
        return list;
    }

    public static double [][] markovChain(int [] inputF, int [] keys, int order){
        double [][] markovMatrix = new double [(int)Math.pow(keys.length, order)][keys.length];
        int [] input = inputF.clone();

        //convert input array values to index values of keys
        for(int i = 0; i < input.length; i ++){
            for(int j = 0; j < keys.length; j ++){
                if(input[i] == keys[j]){
                    input[i] = j;
                }
            }
        }

        if(order == 2){
            //create lookup table for order 2
            int [][] lookUpTable2 = createLookupTable2(keys);
            //read 2 input elements and its next element and count occurences
            for(int i = 0; i < input.length-order; i ++){
                int index = lookUpTable2[input[i]][input[i+1]];
                markovMatrix[index][input[i+order]] ++;
            }
            //get probabilities
            for(int i = 0; i < markovMatrix.length; i ++){
                double sum = 0;
                for(int j = 0; j < markovMatrix[i].length; j ++){
                    sum += markovMatrix[i][j];
                }
                for(int j = 0; j < markovMatrix[i].length; j ++){
                    if(sum > 0) {
                        markovMatrix[i][j] = markovMatrix[i][j] / sum;
                    }
                }
            }
        }
        else{ //order 1
            //read one input element and its next element and count occurences
            for(int i = 0; i < input.length-order; i ++){
                markovMatrix[input[i]][input[i+1]] ++;
            }
            //get probabilities
            for(int i = 0; i < markovMatrix.length; i ++){
                double sum = 0;
                for(int j = 0; j < markovMatrix[i].length; j ++){
                    sum += markovMatrix[i][j];
                }
                for(int j = 0; j < markovMatrix[i].length; j ++){
                    if(sum > 0) {
                        markovMatrix[i][j] = markovMatrix[i][j] / sum;
                    }
                }
            }
        }

        return markovMatrix;
    }

    public static int[][] createLookupTable2(int [] keys){
        int [][] lookUpTable2 = new int [keys.length][keys.length];
        int cnt = 0;
        for(int i = 0; i < keys.length; i ++){
            for(int j = 0; j < keys.length; j ++) {
                lookUpTable2[i][j] = cnt;
                cnt ++;
            }
        }
        return lookUpTable2;
    }

}
