package kau.easystudio.ui;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jay on 2016. 11. 9..
 */

public class FragmentList {
    int videoNum;
    private static final int FROM = 0;
    private static final int TO = 1;
    private ArrayList<Integer> nonZeroFrom;
    private ArrayList<Integer> zeroFrom;
    private ArrayList<Integer> nonZeroTO;
    private ArrayList<Integer> zeroTO;
    private ArrayList<Integer> resultFrom;
    private ArrayList<Integer> resultTO;

    public FragmentList(int videoNum){
        this.videoNum = videoNum;
        nonZeroFrom = new ArrayList<Integer>();
        nonZeroTO = new ArrayList<Integer>();
        zeroFrom =new ArrayList<Integer>();
        zeroTO = new ArrayList<Integer>();
        resultTO = new ArrayList<Integer>();
        resultFrom = new ArrayList<Integer>();
    }

    public void addToResultList(int from, int to){
        resultFrom.add(from);
        resultTO.add(to);
    }

    public void add(int from, int to, boolean isNonZero){
        if(isNonZero){
            nonZeroFrom.add(from);
            nonZeroTO.add(to);
        }
        else {
            zeroFrom.add(from);
            zeroTO.add(to);
        }
    }
    public ArrayList<Integer> getResultFrom (){
        return resultFrom;
    }
    public ArrayList<Integer> getResultTO (){
        return resultTO;
    }

    public ArrayList<Integer> getList(int fromORto,boolean isNonZero){
        if((fromORto==FROM)&&(isNonZero == true)){
            return nonZeroFrom;
        }
        else if((fromORto==FROM)&&(isNonZero == false)){
            return zeroFrom;
        }
        else if((fromORto==TO)&&(isNonZero == true)){
            return nonZeroTO;
        }
        else {
            return zeroTO;
        }
    }
}
