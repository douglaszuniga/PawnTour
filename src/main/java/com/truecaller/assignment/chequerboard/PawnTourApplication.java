package com.truecaller.assignment.chequerboard;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PawnTourApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(PawnTourApplication.class, args);
	}

	@Override
	public void run(String... args) {
		start();
		System.exit(0);
	}

	/**
	 * This is starting point of the app, basically the app will try to find a path for the Pawn
	 * in the chequerboard so that it visits each tile | square | point only once.
	 * The implementation uses a Heuristic (Warnsdorff’s algorithm) to choose from all possible moves
	 * the "best" one.
	 *
	 * The "best" next location for the Pawn is chosen by
	 * 1. calculating the degree value for each possible location
	 * 1.1 the degree means all possible location that can and are allowed to be visited
	 * 2. then the app will choose the option with the lowest degree ( lowest number of possible locations )
	 * 3. repeat the process until the Pawn cannot move anymore
	 *
	 * The advantage of using this Heuristic is that even when it is a difficult task the process will take linear time to complete
	 *
	 * A duration tracking is added to the output, most of the time the app takes 200 ms to complete
	 */
	private void start() {
		//-- starting to get prerequisite data
		int dimension = 10;
		int unvisitedValue = 0;

		System.out.println(
				String.format("Welcome the board dimensions are: %s X %s", dimension, dimension)
		);
		System.out.println();

		List<Point> pawnMovements = getPawnMovements();

		//-- represent the maximum number of times this app will try to find a path
		//-- each attempt will restart/clean the board and get a new starting position
		int maxIterations = 100;
		//-- a flag to indicate if the iteration was successful
		boolean pathNotFound = true;

		//-- startTime will be used to calculate the total duration that it takes to find at least one pawn path
		long startTime = System.currentTimeMillis();

		while (mustKeepTrying(maxIterations, pathNotFound)) {

			int[][] chequerboard = createAndInitializeChequerboard(dimension, unvisitedValue);

			//-- instead of giving a manual initial position, we will use a random generated one
			//-- following the basic rules:
			//-- It should be inside the chequerboard ( 0 >= position < dimension )
			Point initialPosition = getInitialPosition(dimension);
			System.out.println(
					"The initial position for the pawn is: [row:" + initialPosition.getX() + ", column:"
							+ initialPosition.getY() + "]");

			if (getNextMove(chequerboard, pawnMovements , initialPosition, 1, dimension, unvisitedValue)) {
				System.out.println("Found path for pawn");
				System.out.println();
				pathNotFound = false;
			} else {
				System.out.println("Cannot find path for pawn");
				System.out.println("retrying...");
				System.out.println();
			}
		}

		System.out.println("Finished after " + (System.currentTimeMillis() - startTime) + "ms");
	}

	/**
	 * Helper function that will indicate if we must keep trying to find a path for the Pawn
	 * @param currentIteration this is the current iteration, giving that we are decreasing, its value be greater than 0
	 * @param pathNotFound basically if during the current iteration a solution was found
	 * @return true if another iteration must be performed, otherwise false
	 */
	private boolean mustKeepTrying(int currentIteration, boolean pathNotFound) {
		return pathNotFound && currentIteration > 0;
	}

	/**
	 * Calculates the initial position in the chequerboard using random values
	 * @param dimension indicates the bound for the random generated integer (this value is excluded)
	 * @return a new Point representing the starting position in the board
	 */
	private Point getInitialPosition(int dimension) {
		Random random = new Random();
		return Point.newPoint(random.nextInt(dimension), random.nextInt(dimension));
	}

	/**
	 * Finds the pawn's next move in the chequerboard using the current position as its base and recursion
	 * It uses the Warnsdorff’s algorithm (heuristic), tries to move to a position that contains the min
	 * number of degrees (graph theory)
	 * basically the Pawn will move to the location where it has the minimum number of possibles allowed moves
	 * @param chequerboard this is the "game" board
	 * @param pawnMovements represent all the pawn pawnMovements
	 * @param currentPosition this is the current location of the pawn in the chequerboard
	 * @param stepNumber represents the step or number of moves the pawn has performed so far
	 * @param dimension the dimensions of the chequerboard
	 * @param unvisitedValue value representing that the pawn has not yet visited a tile|square|location in the board
	 * @return indicates if the the next move can be done or not
	 */
	private boolean getNextMove(int[][] chequerboard, List<Point> pawnMovements, Point currentPosition,
			int stepNumber, int dimension, int unvisitedValue) {

		//-- this will helps us with two situations:
		//-- knowing that the pawn already visited a tile
		//-- during the printing it will be easy to see the path
		chequerboard[currentPosition.getX()][currentPosition.getY()] = stepNumber;

		//-- the idea is to follow the pawn movements, so we print the current state of the chequerboard
		System.out.println("Step: " + stepNumber);
		System.out.println();
		displayChequerboard(chequerboard, dimension);
		System.out.println();

		//-- this will gives us the next location in the chequerboard (from current position) that will have the lowest
		//-- degree (number of allowed positions)
		//-- this is the heuristic used to choose the next movement
		Optional<Point> positionWithLowestDegree = findPositionWithLowestDegree(chequerboard, pawnMovements,
				currentPosition, dimension, unvisitedValue);

		//-- Giving that from whatever location in the chequerboard a pawn must be able to move at least to one location
		//-- when we get to the point where there is not possible next move we can safely say we have finished
		if (positionWithLowestDegree.isEmpty()) {
			return true;
		}

		//-- we recursively call this method again but changing the data with the just found information
		return getNextMove(chequerboard, pawnMovements, positionWithLowestDegree.get(), stepNumber + 1,
				dimension, unvisitedValue);
	}

	/**
	 * Iterates and finds the next position with the lowest degree value
	 * @param chequerboard the "game" board
	 * @param pawnMovements all possible moves that the pawn can perform
	 * @param currentPosition the current location of the pawn
	 * @param boardDimension dimension of the chequerboard
	 * @param unvisitedValue value used to identify whether or not the pawn has already visited a location in the chequerboard
	 * @return The next position the pawn should move to, in the case that it is not possible to move to other location
	 * then this function will return an empty optional
	 */
	private Optional<Point> findPositionWithLowestDegree(int[][] chequerboard, List<Point> pawnMovements, Point currentPosition,
			int boardDimension, int unvisitedValue) {

		int minimumDegreeValue = Integer.MAX_VALUE;
		Point chosenNextPosition = null;

		for (Point pawnMovement : pawnMovements) {


			//-- we calculate the position in the chequerboard
			Point nextPossiblePosition = Point.newPoint(
					currentPosition.getX() + pawnMovement.getX(),
					currentPosition.getY() + pawnMovement.getY()
			);

			//-- only allowed position must be considered
			if (isMovementAllowed(chequerboard, nextPossiblePosition, boardDimension, unvisitedValue)) {
				//-- we get the degree value for the possible next movement
				int currentPawnMovementDegree = getDegree(chequerboard, pawnMovements, nextPossiblePosition,
						boardDimension, unvisitedValue);
				//-- the idea is to find position with the lowest degree
				if (currentPawnMovementDegree < minimumDegreeValue) {
					minimumDegreeValue = currentPawnMovementDegree;
					chosenNextPosition = nextPossiblePosition;
				}
			}
		}
		return Optional.ofNullable(chosenNextPosition);
	}

	/**
	 * Here we calculate the number of tiles that are allowed to be visited from the current location
	 *
	 * @param chequerboard the "game" board
	 * @param pawnMovements all possible moves that the pawn can perform
	 * @param currentPosition the location from where we will calculate the degree
	 * @param boardDimension dimension of the chequerboard
	 * @param unvisitedValue value used to identify whether or not the pawn has already visited a location in the chequerboard
	 * @return number of tiles that are allowed to be visited from the current location
	 */
	private int getDegree(int[][] chequerboard, List<Point> pawnMovements, Point currentPosition, int boardDimension,
			int unvisitedValue) {

		int degree = 0;
		for (Point pawnMovement : pawnMovements) {
			Point nextPosition = Point.newPoint(
					currentPosition.getX() + pawnMovement.getX(),
					currentPosition.getY() + pawnMovement.getY());

			if (isMovementAllowed(chequerboard, nextPosition, boardDimension, unvisitedValue)) {
				degree++;
			}
		}
		return degree;
	}

	/**
	 * Creates and Initializes the board
	 * @param dimension indicates the board dimension
	 * @param unvisitedValue indicates the value which will be used to indicate that a square has not been visited yet
	 * @return a new board of dimension x dimension
	 */
	private int[][] createAndInitializeChequerboard(int dimension, int unvisitedValue) {
		int[][] board = new int[dimension][dimension];

		for (int x = 0; x < dimension; x++) {
			for (int y = 0; y < dimension; y++) {
				board[x][y] = unvisitedValue;
			}
		}

		return board;
	}

	/**
	 * Creates a list a of the movements, following this specification:
	 * 1. three tiles moving in North, West, South and East direction
	 * 2. two tiles moving in Northeast, Northwest, Southeast, Southwest
	 * @return Gets the list of all the possible moves the pawn can perform
	 */
	private List<Point> getPawnMovements() {
		return List.of(
				Point.newPoint(0, 3), Point.newPoint(-2, 2), Point.newPoint(-3, 0), Point.newPoint(-2, -2),
				Point.newPoint(0, -3), Point.newPoint(2, -2), Point.newPoint(3, 0), Point.newPoint(2, 2)
		);
	}

	/**
	 * Helper function that will print in the console the content of the chequerboard with some formatting
	 * @param chequerboard the "game" board
	 * @param dimension dimension value of the chequerboard
	 */
	private void displayChequerboard(int[][] chequerboard, int dimension) {
		for (int row = 0; row < dimension; row++) {
			for (int column = 0; column < dimension; column++) {
				System.out.printf("%3d", chequerboard[row][column]);
			}
			System.out.println();
		}
	}

	/**
	 * Checks that the nextPosition is inside the board ( more than 0 and less than dimension) and if the pawn has not already visited
	 * @return true if the nextPosition is safe to move to, false otherwise
	 */
	private boolean isMovementAllowed(int[][] board, Point nextPosition, int dimension, int unvisitedValue) {
		return isInsideTheBoard(dimension, nextPosition) && isSquareUnvisited(board, unvisitedValue, nextPosition);
	}

	private boolean isSquareUnvisited(int[][] board, int unvisited, Point nextPoint) {
		return board[nextPoint.getX()][nextPoint.getY()] == unvisited;
	}

	private boolean isInsideTheBoard(int dimension, Point nextPoint) {
		return (nextPoint.getX() >= 0 && nextPoint.getY() >= 0) && (nextPoint.getX() < dimension
				&& nextPoint.getY() < dimension);
	}
}

