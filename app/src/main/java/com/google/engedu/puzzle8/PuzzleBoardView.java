package com.google.engedu.puzzle8;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    private Comparator<PuzzleBoard> puzzleBoardComparator = new Comparator<PuzzleBoard>() {
        @Override
        public int compare(PuzzleBoard lhs, PuzzleBoard rhs) {
            return lhs.priority() - rhs.priority();
        }
    };

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap, View parent) {
        int width = getWidth();
        puzzleBoard = new PuzzleBoard(imageBitmap, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    //The implementation of PuzzleBoardView.shuffle is then a matter of repeatedly
    // updating PuzzleBoardView.puzzleBoard to a randomly selected value from
    // PuzzleBoardView.puzzleBoard.neighbours(). Don't forget to call invalidate()
    // at the end of the shuffle method in order to update the UI.
    public void shuffle() {
        if (animation == null && puzzleBoard != null) {
            // Do something.
            ArrayList<PuzzleBoard> newBoard = puzzleBoard.neighbours();
            int random = new Random().nextInt(newBoard.size());
            puzzleBoard = newBoard.get(random);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        PriorityQueue<PuzzleBoard> queue = new PriorityQueue<>(1, puzzleBoardComparator);
        /*PuzzleBoard current = new PuzzleBoard(puzzleBoard);
        queue.add(current);*/
        queue.add(puzzleBoard);
        while (!queue.isEmpty()) {
            PuzzleBoard removedBoard = queue.poll();
            if (!removedBoard.resolved()) {
                for (int i = 0; i < removedBoard.neighbours().size(); i++) {
                    queue.add(removedBoard.neighbours().get(i));
                }
            } else {
                ArrayList<PuzzleBoard> puzzleBoardSolutions = new ArrayList<>();
                while (removedBoard.getPreviousBoard() != null) {
                    puzzleBoardSolutions.add(removedBoard);
                    removedBoard = removedBoard.getPreviousBoard();
                }
                Collections.reverse(puzzleBoardSolutions);
                animation = puzzleBoardSolutions;
                invalidate();
                break;
            }
        }
    }
}
