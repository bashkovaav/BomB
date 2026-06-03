package com.example.bomb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Minesweeper extends Application {

    // Настройки игры
    private static final int CELL_SIZE = 30;
    private static final int GRID_WIDTH = 8;  // Ширина в клетках
    private static final int GRID_HEIGHT = 8; // Высота в клетках
    private static final int BOMB_COUNT = 10;  // Количество бомб

    // Состояния клеток
    private static final int EMPTY = 0;
    private static final int BOMB = 9;
    private static final int COVERED = 10;
    private static final int FLAGGED = 11;
    private static final int REVEALED_BOMB = 12;

    private int[][] grid;          // Содержимое клеток (0-8 - цифры, 9 - бомба)
    private int[][] state;         // Состояние клеток (COVERED, FLAGGED, etc.)
    private boolean gameOver;
    private boolean firstClick;
    private int cellsToReveal;

    private Canvas canvas;
    private GraphicsContext gc;

    @Override
    public void start(Stage primaryStage) {
        initializeGame();

        canvas = new Canvas(GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        setupMouseHandlers();

        primaryStage.setTitle("Сапер");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        drawBoard();
    }

    private void initializeGame() {
        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        state = new int[GRID_HEIGHT][GRID_WIDTH];
        gameOver = false;
        firstClick = true;
        cellsToReveal = GRID_WIDTH * GRID_HEIGHT - BOMB_COUNT;

        // Инициализируем все клетки как пустые и закрытые
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                grid[y][x] = EMPTY;
                state[y][x] = COVERED;
            }
        }
    }

    private void setupMouseHandlers() {
        canvas.setOnMouseClicked(event -> {
            if (gameOver) return;

            int x = (int) (event.getX() / CELL_SIZE);
            int y = (int) (event.getY() / CELL_SIZE);

            if (x < 0 || x >= GRID_WIDTH || y < 0 || y >= GRID_HEIGHT) return;

            if (event.getButton() == MouseButton.PRIMARY) {
                handleLeftClick(x, y);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                handleRightClick(x, y);
            }

            drawBoard();
        });
    }

    private void handleLeftClick(int x, int y) {
        if (state[y][x] == FLAGGED) return;

        if (firstClick) {
            placeBombs(x, y);
            calculateNumbers();
            firstClick = false;
        }

        if (state[y][x] == COVERED) {
            revealCell(x, y);
        }
    }

    private void handleRightClick(int x, int y) {
        if (state[y][x] == COVERED) {
            state[y][x] = FLAGGED;
        } else if (state[y][x] == FLAGGED) {
            state[y][x] = COVERED;
        }
    }

    private void placeBombs(int safeX, int safeY) {
        int bombsPlaced = 0;
        List<Point> bombsPlacement = new ArrayList<>();

        for (int width = 0; width < GRID_WIDTH; width++) {
            for (int height = 0; height < GRID_HEIGHT; height++) {
                if (Math.abs(width - safeX) <= 1 && Math.abs(height - safeY) <= 1) {
                    continue;
                }
                bombsPlacement.add(new Point(width, height));
            }
        }
        while (bombsPlaced < BOMB_COUNT) {
            int Index = (int) (Math.random() * bombsPlacement.size());

            int x = (int) bombsPlacement.get(Index).getX();
            int y = (int) bombsPlacement.get(Index).getY();

            grid[y][x] = BOMB;
            bombsPlaced++;
        }
    }

    private void calculateNumbers() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (grid[y][x] != BOMB) {
                    grid[y][x] = countAdjacentBombs(x, y);
                }
            }
        }
    }

    private int countAdjacentBombs(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < GRID_WIDTH && ny >= 0 && ny < GRID_HEIGHT) {
                    if (grid[ny][nx] == BOMB) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void revealCell(int x, int y) {
        if (x < 0 || x >= GRID_WIDTH || y < 0 || y >= GRID_HEIGHT) return;
        if (state[y][x] != COVERED) return;

        state[y][x] = grid[y][x];

        if (grid[y][x] == BOMB) {
            gameOver = true;
            revealAllBombs();
            showGameOverMessage();
            return;
        }

        cellsToReveal--;

        if (cellsToReveal == 0) {
            gameOver = true;
            showWinMessage();
            return;
        }

        // Если клетка пустая, открываем соседей
        if (grid[y][x] == EMPTY) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    revealCell(x + dx, y + dy);
                }
            }
        }
    }

    private void revealAllBombs() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (grid[y][x] == BOMB) {
                    state[y][x] = REVEALED_BOMB;
                }
            }
        }
    }

    private void drawBoard() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Рисуем сетку и клетки
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                drawCell(x, y);
            }
        }

        // Рисуем линии сетки
        drawGridLines();
    }

    private void drawCell(int x, int y) {
        double pixelX = x * CELL_SIZE;
        double pixelY = y * CELL_SIZE;

        // Фон клетки
        if (state[y][x] == COVERED) {
            drawCoveredCell(pixelX, pixelY);
        } else if (state[y][x] == FLAGGED) {
            drawFlaggedCell(pixelX, pixelY);
        } else if (state[y][x] == REVEALED_BOMB) {
            drawBombCell(pixelX, pixelY);
        } else {
            drawRevealedCell(pixelX, pixelY, state[y][x]);
        }
    }

    private void drawCoveredCell(double x, double y) {
        // Темно-серый фон для закрытой клетки
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

        // 3D эффект - светлая тень сверху и слева
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(2);
        gc.strokeLine(x, y, x + CELL_SIZE, y);
        gc.strokeLine(x, y, x, y + CELL_SIZE);

        // Темная тень снизу и справа
        gc.setStroke(Color.GRAY);
        gc.strokeLine(x + CELL_SIZE, y, x + CELL_SIZE, y + CELL_SIZE);
        gc.strokeLine(x, y + CELL_SIZE, x + CELL_SIZE, y + CELL_SIZE);
    }

    private void drawFlaggedCell(double x, double y) {
        drawCoveredCell(x, y);

        // Красный флажок
        gc.setFill(Color.RED);
        gc.fillRect(x + 6, y + 6, CELL_SIZE - 12, 3);
        gc.fillRect(x + 8, y + 9, 3, CELL_SIZE - 15);
    }

    private void drawBombCell(double x, double y) {
        // Красный фон для взорвавшейся бомбы
        gc.setFill(Color.RED);
        gc.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

        // Черная бомба
        gc.setFill(Color.BLACK);
        gc.fillOval(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10);

        // Антенна бомбы
        gc.setFill(Color.BLACK);
        gc.fillRect(x + CELL_SIZE/2 - 1, y + 3, 2, 4);
        gc.fillOval(x + CELL_SIZE/2 - 3, y + 2, 6, 4);
    }

    private void drawRevealedCell(double x, double y, int value) {
        // Светло-серый фон для открытой клетки
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

        if (value > 0 && value < 9) {
            drawNumber(x, y, value);
        }
    }

    private void drawNumber(double x, double y, int number) {
        gc.setFont(Font.font("Arial", 16));

        // Разные цвета для разных цифр
        switch (number) {
            case 1: gc.setFill(Color.BLUE); break;
            case 2: gc.setFill(Color.GREEN); break;
            case 3: gc.setFill(Color.RED); break;
            case 4: gc.setFill(Color.DARKBLUE); break;
            case 5: gc.setFill(Color.DARKRED); break;
            case 6: gc.setFill(Color.TEAL); break;
            case 7: gc.setFill(Color.BLACK); break;
            case 8: gc.setFill(Color.GRAY); break;
        }

        String text = String.valueOf(number);
        double textWidth = gc.getFont().getSize() * text.length() * 0.6;
        double textHeight = gc.getFont().getSize();

        gc.fillText(text,
                x + CELL_SIZE/2 - textWidth/2,
                y + CELL_SIZE/2 + textHeight/3);
    }

    private void drawGridLines() {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        // Вертикальные линии
        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        }

        // Горизонтальные линии
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * CELL_SIZE, GRID_WIDTH * CELL_SIZE, y * CELL_SIZE);
        }
    }

    private void showGameOverMessage() {
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", 24));
        String text = "Игра окончена! Вы проиграли!";
        double textWidth = gc.getFont().getSize() * text.length() * 0.3;
        gc.fillText(text,
                canvas.getWidth()/2 - textWidth/2,
                canvas.getHeight()/2);
    }

    private void showWinMessage() {
        gc.setFill(Color.GREEN);
        gc.setFont(Font.font("Arial", 24));
        String text = "Поздравляем! Вы победили!";
        double textWidth = gc.getFont().getSize() * text.length() * 0.3;
        gc.fillText(text,
                canvas.getWidth()/2 - textWidth/2,
                canvas.getHeight()/2);

        // Закрываем приложение через 3 секунды
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}