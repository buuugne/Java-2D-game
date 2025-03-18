//Ugnė Pacevičiūtė, 2 grupė
import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;

public class Main extends PApplet {
    PImage tilesetImg;
    PImage characterImg;
    PImage[] tileImages;
    PImage[][] playerFrames;
    PImage[] coinFrames;
    ArrayList<Coin> coins = new ArrayList<>();
    int coinCount = 0;
    int totalCoins = 0;

    // Level system
    int currentLevel = 1;
    int maxLevels = 3;
    boolean levelComplete = false;
    int levelTransitionTimer = 0;
    final int TRANSITION_DURATION = 120; // 2 seconds at 60fps

    // Tileset configuration
    int tilesetCols = 23;
    int tilesetRows = 8;
    int tileW = 16;
    int tileH = 16;

    float playerX = 200;
    float playerY = 200;
    boolean editor = false;
    int frameIndex = 0;
    int direction = 2; // 0 - up, 1 - left, 2 - down, 3 - right
    int animationTimer = 0;
    int animationDelay = 5;
    boolean isWalking = false;

    // Editor variables
    int selectedTile = 0;
    boolean placingTiles = true;
    boolean placingCoins = false;
    boolean showGrid = true;
    int editorMode = 0; // 0 = Tile Mode, 1 = Coin Mode, 2 = Start Position
    int editorPanelWidth = 250;
    int tileScale = 2; // Scale factor for tiles in the palette

    // Camera variables
    float cameraX = 0;
    float cameraY = 0;

    // Scale factor for rendering
    float renderScale = 3.0f;

    // Maps for each levels
    int[][][] levelMaps = new int[maxLevels][][];

    // For scrolling
    float tileScrollOffset = 0; // Scroll position for the tile selection panel
    final int SCROLL_SPEED = 40; // How much to scroll per step


    // Starting positions for each level
    int[][] startPositions = {
            {3, 2},   // Level 1 - Upper left safe area
            {5, 8},   // Level 2 - Upper left away from water
            {2, 2}    // Level 3 - Upper left safe area
    };
    // Character dimensions and hitbox
    float charWidth;
    float charHeight;
    float hitboxWidth;
    float hitboxHeight;
    float hitboxOffsetX;
    float hitboxOffsetY;

    public void settings() {
        size(800, 600);
    }

    public void setup() {
        background(125, 175, 225);
        tilesetImg = loadImage("tiles.png");
        characterImg = loadImage("shura.png");

        // Initialize maps for each level
        initializeLevelMaps();

        // Initialize coin frames
        initCoinFrames();

        // Error handling for images
        if (tilesetImg == null) {
            println("Error: Tileset image not found!");
            exit();
        }

        if (characterImg == null) {
            println("Error: Character image not found!");
            exit();
        }


        // Calculate actual tile dimensions from tileset
        tileW = tilesetImg.width / tilesetCols;
        tileH = tilesetImg.height / tilesetRows;


        // Extract individual tiles from tileset
        tileImages = new PImage[tilesetCols * tilesetRows];

        for (int y = 0; y < tilesetRows; y++) {
            for (int x = 0; x < tilesetCols; x++) {
                int index = x + y * tilesetCols;
                if (index < tileImages.length) {
                    tileImages[index] = tilesetImg.get(x * tileW, y * tileH, tileW, tileH);
                }
            }
        }

        // Load character animation frames
        playerFrames = new PImage[4][9]; // 4 directions, 9 frames per direction

        charWidth = characterImg.width / 9;
        charHeight = characterImg.height / 4;

        // Set up character hitbox
        hitboxWidth = tileW * 0.8f;
        hitboxHeight = tileH * 0.8f;
        hitboxOffsetX = (tileW * 1.5f - hitboxWidth) / 2;
        hitboxOffsetY = (tileH * 1.5f - hitboxHeight) / 2;

        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < 9; frame++) {
                playerFrames[dir][frame] = characterImg.get(frame * (int)charWidth, dir * (int)charHeight, (int)charWidth, (int)charHeight);
            }
        }

        // Load first level
        loadLevel(currentLevel);
    }

    private void initializeLevelMaps() {
        // Level 1
        levelMaps[0] = new int[][] {
                {0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5},
                {23, 73, 73, 73, 73, 73, 73, 24, 25, 26, 73, 73, 73, 73, 73, 73, 73, 73, 73, 28},
                {46, 73, 73, 73, 73, 73, 73, 47, 48, 49, 73, 73, 73, 73, 73, 73, 73, 73, 73, 51},
                {69, 73, 73, 73, 73, 73, 73, 70, 71, 72, 73, 73, 103, 104, 104, 104, 104, 104, 105, 74},
                {69, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 121, 122, 7, 7, 7, 147, 126, 128, 74},
                {69, 73, 121, 7, 7, 7, 7, 7, 7, 7, 7, 7, 145, 104, 104, 104, 104, 127, 128, 74},
                {69, 73, 73, 73, 73, 73, 73, 103, 104, 104, 104, 104, 56, 100, 127, 127, 127, 127, 128, 74},
                {69, 73, 73, 73, 73, 73, 73, 126, 127, 127, 127, 121, 122, 123, 127, 127, 127, 127, 128, 74},
                {69, 73, 73, 73, 73, 73, 73, 78, 78, 78, 78, 78, 145, 146, 147, 127, 127, 127, 128, 74},
                {69, 73, 73, 73, 73, 73, 73, 126, 127, 127, 127, 127, 168, 127, 127, 127, 127, 127, 128, 74},
                {69, 73, 73, 73, 73, 73, 73, 126, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 128, 74},
                {69, 73, 73, 73, 73, 73, 73, 149, 150, 150, 150, 150, 150, 150, 150, 150, 150, 150, 151, 74},
                {92, 93, 94, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 97},
                {115, 116, 117, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 119}
        };

        // Level 2
        levelMaps[1] = new int[][] {
                {39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 41, 41, 41, 41, 41, 41, 41, 41, 41, 42},
                {62, 63, 63, 63, 63, 63, 63, 63, 63, 63, 64, 64, 64, 64, 64, 64, 64, 64, 64, 65},
                {158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158},
                {158, 158, 158, 158, 152, 153, 158, 158, 158, 158, 158, 158, 158, 158, 152, 153, 158, 158, 158, 158},
                {158, 158, 158, 158, 175, 176, 158, 158, 158, 158, 158, 158, 158, 158, 175, 176, 158, 158, 158, 158},
                {158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158},
                {158, 158, 158, 158, 158, 158, 158, 158, 12, 13, 14, 15, 158, 158, 158, 158, 158, 158, 158, 158},
                {158, 158, 158, 158, 158, 158, 158, 158, 35, 36, 37, 38, 158, 158, 158, 158, 158, 158, 158, 158},
                {158, 158, 158, 158, 158, 158, 158, 158, 58, 59, 60, 61, 158, 158, 158, 158, 158, 158, 158, 158},
                {158, 158, 158, 158, 158, 158, 158, 158, 158, 82, 83, 158, 158, 158, 158, 158, 158, 158, 158, 158},
                {158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158},
                {158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158, 158},
                {32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 34},
                {115, 116, 117, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 119}
        };

        // Level 3
        levelMaps[2] = new int[][] {
                {0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5},
                {23, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 89, 90, 90, 90, 90, 90, 90, 91, 28},
                {46, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 112, 113, 113, 113, 113, 113, 113, 114, 51},
                {69, 73, 73, 73, 44, 44, 44, 104, 104, 104, 104, 112, 113, 113, 113, 113, 113, 113, 114, 74},
                {20, 21, 21, 21, 21, 21, 22, 127, 127, 127, 127, 112, 113, 113, 113, 113, 113, 113, 114, 74},
                {43, 44, 44, 44, 44, 44, 45, 1, 2, 3, 4, 112, 113, 113, 113, 113, 113, 113, 114, 74},
                {43, 44, 44, 44, 44, 44, 45, 73, 73, 73, 73, 135, 136, 136, 136, 136, 136, 136, 137, 74},
                {43, 44, 44, 44, 44, 44, 45, 73, 7, 7, 73, 51, 127, 44, 44, 73, 73, 73, 73, 74},
                {43, 44, 44, 44, 44, 44, 45, 73, 73, 73, 73, 74, 127, 20, 21, 21, 21, 21, 21, 21},
                {43, 44, 44, 44, 44, 44, 45, 93, 94, 95, 95, 97, 127, 43, 44, 44, 44, 44, 44, 44},
                {43, 44, 44, 44, 44, 44, 45, 127, 127, 127, 127, 127, 127, 43, 44, 44, 44, 44, 44, 44},
                {66, 67, 67, 67, 67, 67, 68, 150, 150, 150, 150, 150, 150, 43, 44, 44, 44, 44, 44, 44},
                {92, 93, 94, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 43, 44, 44, 44, 44, 44, 44},
                {115, 116, 117, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 43, 44, 44, 44, 44, 44, 44}
        };
    }

    private void loadLevel(int level) {
        // Reset game state
        coins.clear();
        coinCount = 0;
        levelComplete = false;
        levelTransitionTimer = 0;

        // Adjust level index (1-based to 0-based)
        int levelIndex = level - 1;

        // Set player starting position for this level
        playerX = startPositions[levelIndex][0] * tileW * renderScale;
        playerY = startPositions[levelIndex][1] * tileH * renderScale;

        // Place coins according to the level
        placeCoinsByLevel(levelIndex);

        // Reset animation state
        frameIndex = 0;
        direction = 2; // Down
        isWalking = false;
    }

    private void placeCoinsByLevel(int levelIndex) {
        // Different coin patterns for each level
        switch (levelIndex) {
            case 0:
                // Level 1 - Original coin pattern
                placeCoinPattern1();
                break;
            case 1:
                // Level 2 - New coin pattern with more challenges
                placeCoinPattern2();
                break;
            case 2:
                // Level 3 - Most difficult coin pattern
                placeCoinPattern3();
                break;
        }

        // Count total coins in the level
        totalCoins = coins.size();
    }

    private void placeCoinPattern1() {
        // Original coin pattern for level 1
        int[][] coinPositions = {
                {8, 4}, {10, 4}, // Upper path coins
                {12, 3}, {14, 3}, {16, 3}, // Upper bridge coins
                {7, 5}, {9, 5}, // Middle path coins
                {12, 5}, {13, 5}, {14, 5}, // Under the bridge
                {6, 7}, {4, 7}, // Left side path
                {7, 9}, {8, 9}, // Bottom area safe spots
                {12, 11}, {14, 11}, {16, 11} // Bottom right path
        };

        addCoinsAtPositions(coinPositions, 0);
    }

    private void placeCoinPattern2() {
        // Level 2 - More spaced out coins that lead player through the map
        int[][] coinPositions = {
                {5, 2}, {7, 2}, // Top water bridge
                {3, 4}, {5, 5}, // Middle of water
                {9, 6}, {11, 6}, {13, 6}, {15, 6}, // The path across middle
                {3, 8}, {5, 8}, {7, 8}, // Bottom left path
                {10, 9}, {12, 9}, {14, 9}, // Bottom right approach
                {16, 10}, {16, 11} // Near the bottom right water
        };

        addCoinsAtPositions(coinPositions, 1);
    }

    private void placeCoinPattern3() {
        // Level 3 - Island and water challenge
        int[][] coinPositions = {
                {7, 2}, {9, 2}, {11, 2}, {13, 2}, // Top row
                {4, 4}, {6, 4}, {8, 4}, // Left approach to water
                {8, 7}, {10, 7}, // Center island path
                {13, 5}, {13, 7}, {13, 9}, // Right side of water
                {6, 10}, {8, 10}, {10, 10}, // Bottom of water
                {16, 6}, {16, 8}, {16, 10} // Far right path
        };

        addCoinsAtPositions(coinPositions, 2);
    }

    private void addCoinsAtPositions(int[][] positions, int levelIndex) {
        for (int[] pos : positions) {
            float x = pos[0] * tileW * renderScale;
            float y = pos[1] * tileH * renderScale;

            // Check if position is valid and not on water
            int tileX = pos[0];
            int tileY = pos[1];

            if (tileX >= 0 && tileX < levelMaps[levelIndex][0].length &&
                    tileY >= 0 && tileY < levelMaps[levelIndex].length) {

                int tileType = levelMaps[levelIndex][tileY][tileX];

                // Don't place coins on water
                if (!isWaterTile(tileType)) {
                    coins.add(new Coin(x, y));
                }
            }
        }
    }

    private void initCoinFrames() {
        // Create frames for the coin animation
        coinFrames = new PImage[15];

        // Try to load the sprite sheet
        PImage coinSheet = loadImage("bitcoin.png");

        if (coinSheet != null) {
            // The coin sheet has 15 frames, extract each frame
            int frameWidth = coinSheet.width / 15;
            for (int i = 0; i < 15; i++) {
                coinFrames[i] = coinSheet.get(i * frameWidth, 0, frameWidth, coinSheet.height);
            }
        } else {
            println("Error: Coin sheet not found! Creating placeholder frames.");

            // Create placeholder frames with different colors
            for (int i = 0; i < 15; i++) {
                coinFrames[i] = createImage(tileW, tileH, ARGB);
                coinFrames[i].loadPixels();

                // Calculate color based on frame (yellow to orange gradient)
                int r = 255;
                int g = 255 - (i * 12); // Adjusted to spread over 15 frames
                int b = 0;

                for (int j = 0; j < coinFrames[i].pixels.length; j++) {
                    coinFrames[i].pixels[j] = color(r, g, b);
                }
                coinFrames[i].updatePixels();
            }
        }
    }

    public void draw() {
        background(80, 140, 190); // Slightly darker blue for water/sky background

        if (editor) {
            drawEditor();
        } else {
            updateCamera();

            if (levelComplete) {
                handleLevelTransition();
            } else {
                drawGame();
                checkLevelCompletion();
            }
        }
    }

    private void handleLevelTransition() {
        // Display transition screen
        pushMatrix();

        // Fill screen with semi-transparent black
        fill(0, 0, 0, 150);
        rect(0, 0, width, height);

        // Show level completion message
        fill(255, 255, 255);
        textAlign(CENTER, CENTER);
        textSize(32);
        text("Level " + currentLevel + " Complete!", width/2, height/2 - 50);

        if (currentLevel < maxLevels) {
            textSize(24);
            text("Collecting all coins: " + totalCoins + "/" + totalCoins, width/2, height/2);
            text("Loading next level...", width/2, height/2 + 50);
        } else {
            textSize(30);
            text("Congratulations!", width/2, height/2);
            text("You've completed all levels!", width/2, height/2 + 50);
            textSize(16);
            text("Press 'R' to restart the game", width/2, height/2 + 100);
        }

        popMatrix();

        // Increment timer
        levelTransitionTimer++;

        // Move to next level after transition time
        if (levelTransitionTimer >= TRANSITION_DURATION) {
            if (currentLevel < maxLevels) {
                currentLevel++;
                loadLevel(currentLevel);
            }
        }
    }

    private void checkLevelCompletion() {
        // Count remaining coins
        int remainingCoins = 0;
        for (Coin coin : coins) {
            if (!coin.collected) {
                remainingCoins++;
            }
        }

        // Level is complete when all coins are collected
        if (remainingCoins == 0 && totalCoins > 0) {
            levelComplete = true;
        }
    }

    private void updateCamera() {
        // Center camera on player
        cameraX = playerX - width/2 + (tileW * renderScale) / 2;
        cameraY = playerY - height/2 + (tileH * renderScale) / 2;

        // Calculate map boundaries
        float mapWidth = levelMaps[currentLevel - 1][0].length * tileW * renderScale;
        float mapHeight = levelMaps[currentLevel - 1].length * tileH * renderScale;

        // Constrain camera to map boundaries
        cameraX = constrain(cameraX, 0, max(0, mapWidth - width));
        cameraY = constrain(cameraY, 0, max(0, mapHeight - height));
    }

    private void drawEditor() {
        background(60);
        float mapAreaWidth = width - editorPanelWidth;

        // Draw the current level map
        pushMatrix();
        translate(10, 10);
        scale(1.5f);

        int[][] currentMap = levelMaps[currentLevel - 1];

        for (int row = 0; row < currentMap.length; row++) {
            for (int col = 0; col < currentMap[0].length; col++) {
                int tileIndex = currentMap[row][col];

                if (tileIndex >= 0 && tileIndex < tileImages.length && tileImages[tileIndex] != null) {
                    image(tileImages[tileIndex], col * tileW, row * tileH, tileW, tileH);
                }

                if (showGrid) {
                    stroke(200, 200, 200, 100);
                    noFill();
                    rect(col * tileW, row * tileH, tileW, tileH);
                }
            }
        }
        popMatrix();

        // Draw right panel with tile palette
        fill(40);
        noStroke();
        rect(mapAreaWidth, 0, editorPanelWidth, height);

        // Draw title
        fill(255);
        textSize(24);
        textAlign(CENTER);
        text("Tile Palette", mapAreaWidth + editorPanelWidth / 2, 30);
        textAlign(LEFT);

        drawEditorButtons(mapAreaWidth);

        // Scrollable Tile Palette
        int tilesPerRow = 5;
        int tileDisplaySize = tileW * tileScale;
        int startY = 100 - (int) tileScrollOffset;

        for (int i = 0; i < tileImages.length; i++) {
            int row = i / tilesPerRow;
            int col = i % tilesPerRow;

            int x = (int) (mapAreaWidth + 20 + col * (tileDisplaySize + 5));
            int y = startY + row * (tileDisplaySize + 5);

            if (y + tileDisplaySize < 100 || y > height) continue;

            if (i == selectedTile) {
                fill(255, 200, 0);
                stroke(255, 220, 0);
                strokeWeight(3);
                rect(x - 3, y - 3, tileDisplaySize + 6, tileDisplaySize + 6);
                noStroke();
            }

            if (tileImages[i] != null) {
                image(tileImages[i], x, y, tileDisplaySize, tileDisplaySize);
            }

            noFill();
            stroke(150);
            strokeWeight(1);
            rect(x, y, tileDisplaySize, tileDisplaySize);

            fill(255);
            textSize(12);
            text(i, x + 4, y + 14);
        }

        // Draw editor controls help on the left side
        fill(0, 0, 0, 200); // Semi-transparent background
        rect(10, height - 100, 150, 60); // Move to the left side

        fill(255);
        textSize(14);
        int helpY = height - 80;
        text("G - Toggle Grid", 20, helpY);
        text("S - Save Map", 20, helpY + 20);
    }


    private void drawEditorButtons(float xPos) {
        int buttonY = 50;
        int buttonWidth = 70;
        int buttonHeight = 30;

        // Tile Mode button
        if (editorMode == 0) fill(0, 200, 0);
        else fill(100);
        rect(xPos + 20, buttonY, buttonWidth, buttonHeight);
        fill(255);
        textSize(14);
        text("Tiles", xPos + 35, buttonY + 20);
    }

    public void handleEditorKeys() {
        println("Key pressed in editor: " + key);

        if (key == 'g' || key == 'G') {
            showGrid = !showGrid;
        } else if (key == '1') {
            currentLevel = 1;
        } else if (key == '2') {
            currentLevel = 2;
        } else if (key == '3') {
            currentLevel = 3;
        } else if (key == 's' || key == 'S') {
            if (key == 'S') {
                saveMap(); // Only call saveMap if uppercase S
            } else {
                float maxScroll = max(0, ((tileImages.length / 5) + 1) * (tileW * tileScale + 5) - height + 150);
                tileScrollOffset = min(maxScroll, tileScrollOffset + SCROLL_SPEED);
                println("Scrolling down: " + tileScrollOffset);
            }
        } else if (key == 'w' || key == 'W') {
            tileScrollOffset = max(0, tileScrollOffset - SCROLL_SPEED);
            println("Scrolling up: " + tileScrollOffset);
        }
        if (key == 'g' || key == 'G') {
            showGrid = !showGrid;
            println("Grid Toggled: " + (showGrid ? "ON" : "OFF"));
        } else if (key == 's' || key == 'S') {
            println("Saving Map...");
            saveMap();
        }
    }



    public void handleEditorMousePressed() {
        float mapAreaWidth = width - editorPanelWidth;

        // Check if click is in the tile palette area
        if (mouseX > mapAreaWidth) {
            int tilesPerRow = 5;
            int tileDisplaySize = tileW * tileScale;
            int startY = 100 - (int) tileScrollOffset; // Apply scroll offset

            // Check tile selection
            for (int i = 0; i < tileImages.length; i++) {
                int row = i / tilesPerRow;
                int col = i % tilesPerRow;

                int x = (int)(mapAreaWidth + 20 + col * (tileDisplaySize + 5));
                int y = startY + row * (tileDisplaySize + 5);

                if (mouseX >= x && mouseX < x + tileDisplaySize &&
                        mouseY >= y && mouseY < y + tileDisplaySize) {
                    selectedTile = i;
                    println("Selected tile: " + selectedTile);
                    break;
                }
            }

            // Check editor buttons (not affected by scroll)
            int buttonY = 50;
            int buttonWidth = 70;
            int buttonHeight = 30;
            int spacing = 10;

            if (mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                if (mouseX >= mapAreaWidth + 20 && mouseX <= mapAreaWidth + 20 + buttonWidth) {
                    editorMode = 0; // Tile mode
                    println("Switched to Tile mode");
                }
            }
        }
    }


    // Add this to save the map
    private void saveMap() {
        // Print the current map to console for copy-pasting
        println("Saving map for level " + currentLevel);
        println("{");

        int[][] currentMap = levelMaps[currentLevel - 1];
        for (int row = 0; row < currentMap.length; row++) {
            print("    {");
            for (int col = 0; col < currentMap[0].length; col++) {
                print(currentMap[row][col]);
                if (col < currentMap[0].length - 1) {
                    print(", ");
                }
            }
            println("},");
        }
        println("};");

        // Also save coin positions
        println("Coin positions:");
        print("int[][] coinPositions = {");
        for (Coin coin : coins) {
            int tileX = (int)(coin.origX / (tileW * renderScale));
            int tileY = (int)(coin.origY / (tileH * renderScale));
            print("{" + tileX + ", " + tileY + "}, ");
        }
        println("};");

        // Save start position
        println("Start position: {" + startPositions[currentLevel - 1][0] + ", " +
                startPositions[currentLevel - 1][1] + "}");
    }

    private void drawGame() {
        pushMatrix();
        translate(-cameraX, -cameraY);

        // Draw current level map
        drawMap();

        // Update and draw coins
        for (int i = coins.size() - 1; i >= 0; i--) {
            Coin coin = coins.get(i);
            coin.update();
            coin.draw();
        }

        // Draw animated player character
        if (playerFrames[direction][frameIndex] != null) {
            image(playerFrames[direction][frameIndex],
                    playerX,
                    playerY,
                    tileW * 1.5f * renderScale,
                    tileH * 1.5f * renderScale);

            // Only update animation if character is walking
            if (isWalking) {
                animationTimer++;
                if (animationTimer >= animationDelay) {
                    frameIndex = (frameIndex + 1) % 9;
                    animationTimer = 0;
                }
            } else {
                // Reset to standing pose (frame 0) when not walking
                frameIndex = 0;
            }
        } else {
            println("Warning: Character frame is null!");
            // Fallback to using a tile if character is missing
            if (tileImages.length > 0 && tileImages[0] != null) {
                image(tileImages[0], playerX, playerY, tileW * renderScale, tileH * renderScale);
            }
        }

        popMatrix();

        // Display game info and controls
        displayGameInfo();
    }

    private void displayGameInfo() {
        // Draw semi-transparent background for UI with better opacity
        noFill();
        noStroke();
        rect(10, 10, 200, 80, 5); // Added rounded corners (5px radius)

        // Display coin count with improved visibility and spacing
        fill(255, 215, 0); // Gold color for coin text
        textSize(24);
        textAlign(LEFT);
        text("Coins: " + coinCount + "/" + totalCoins, 30, 40);

        // Add more space between lines
        fill(255); // White for level display
        textSize(24);
        text("Level: " + currentLevel + "/" + maxLevels, 30, 75);

        // Controls info at bottom (keep as is)
        noFill();
        rect(10, height - 40, width - 20, 30);
        noFill();
        textSize(14);
        text("WASD to move, E to toggle editor, R to reset level", 20, height - 20);
    }

    private void drawMap() {
        // Get current map data
        int[][] currentMap = levelMaps[currentLevel - 1];

        // Calculate visible area with scaling factor
        int startCol = max(0, floor(cameraX / (tileW * renderScale)));
        int endCol = min(currentMap[0].length - 1, ceil((cameraX + width) / (tileW * renderScale)));
        int startRow = max(0, floor(cameraY / (tileH * renderScale)));
        int endRow = min(currentMap.length - 1, ceil((cameraY + height) / (tileH * renderScale)));

        // Only draw visible tiles
        for (int i = startRow; i <= endRow; i++) {
            for (int j = startCol; j <= endCol; j++) {
                int tileIndex = currentMap[i][j];

                // Only draw valid tile indices
                if (tileIndex >= 0 && tileIndex < tileImages.length && tileImages[tileIndex] != null) {
                    float x = j * tileW * renderScale;
                    float y = i * tileH * renderScale;
                    image(tileImages[tileIndex], x, y, tileW * renderScale, tileH * renderScale);
                }
            }
        }
    }

    public void keyPressed() {
        if (key == 'e') {
            editor = !editor;
            return;
        } else if (key == 'r') {
            if (levelComplete && currentLevel == maxLevels) {
                currentLevel = 1;
                loadLevel(currentLevel);
            } else {
                loadLevel(currentLevel);
            }
            return;
        }

        // Handle editor keys when in editor mode
        if (editor) {
            handleEditorKeys();
            return;
        }

        // Don't handle movement during level transition
        if (levelComplete) return;

        float prevX = playerX;
        float prevY = playerY;
        int moveAmount = (int)(tileW * renderScale / 6);

        if (key == 'w') {
            playerY -= moveAmount;
            direction = 0;
            isWalking = true;
        } else if (key == 's') {
            playerY += moveAmount;
            direction = 2;
            isWalking = true;
        } else if (key == 'a') {
            playerX -= moveAmount;
            direction = 1;
            isWalking = true;
        } else if (key == 'd') {
            playerX += moveAmount;
            direction = 3;
            isWalking = true;
        } else {
            return; // Don't animate for other keys
        }

        // Collision Check
        float hitboxLeft = playerX + hitboxOffsetX * renderScale;
        float hitboxRight = hitboxLeft + hitboxWidth * renderScale;
        float hitboxTop = playerY + hitboxOffsetY * renderScale;
        float hitboxBottom = hitboxTop + hitboxHeight * renderScale;

        int[][] currentMap = levelMaps[currentLevel - 1];
        float mapWidth = currentMap[0].length * tileW * renderScale;
        float mapHeight = currentMap.length * tileH * renderScale;

        if (hitboxLeft < 0 || hitboxRight > mapWidth || hitboxTop < 0 || hitboxBottom > mapHeight) {
            playerX = prevX;
            playerY = prevY;
            return;
        }

        boolean collision = checkCollision(hitboxLeft, hitboxTop, hitboxRight, hitboxBottom);

        if (collision) {
            playerX = prevX;
            playerY = prevY;
        }
    }

    public void keyReleased() {
        // Stop walking animation when key is released
        if (key == 'w' || key == 's' || key == 'a' || key == 'd') {
            isWalking = false;
        }
    }

    // konstantos netinkamoms plytelems
    private static final int[] WATER_TILES = {
            126, 127, 128,  // Water edges/corners
            149, 150, 151,  // Water bottom edges
            103, 104, 105,  // Water top edges
            48, // ne water tile bet irgi negalima
            115, 116, 117, 118, 119, //apacia
            12, 13, 14, 15, 35, 36, 37, 38, 58, 59, 60, 61, 82, 83, // medis
            152, 153, 175, 176, // enemy
            39, 40, 41, 42 //virsus
    };

    // methodas patikrinti plyteles
    private boolean isWaterTile(int tileIndex) {
        for (int waterTile : WATER_TILES) {
            if (tileIndex == waterTile) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCollision(float left, float top, float right, float bottom) {
        // Get current map data
        int[][] currentMap = levelMaps[currentLevel - 1];

        // Check if any corner of the hitbox is in a collision tile
        float[][] corners = {
                {left, top},      // Top-left
                {right, top},     // Top-right
                {left, bottom},   // Bottom-left
                {right, bottom}   // Bottom-right
        };

        for (float[] corner : corners) {
            int tileX = (int) (corner[0] / (tileW * renderScale));
            int tileY = (int) (corner[1] / (tileH * renderScale));

            // Check map boundaries
            if (tileX >= 0 && tileX < currentMap[0].length && tileY >= 0 && tileY < currentMap.length) {
                int tileType = currentMap[tileY][tileX];

                // Check if this tile is water
                if (isWaterTile(tileType)) {
                    return true; // Water collision detected
                }
            } else {
                // Off map boundaries
                return true;
            }
        }
        return false;
    }

    class Coin {
        float x, y;
        boolean collected = false;
        int animFrame = 0;
        int animTimer = 0;

        // Variables for coin movement
        float origX, origY;  // Original position
        float moveRadius = 20;  // How far the coin moves
        float moveSpeed = 0.03f;  // Speed of movement
        float moveAngle = 0;  // Current angle in the movement pattern

        public Coin(float x, float y) {
            this.x = x;
            this.y = y;
            this.origX = x;
            this.origY = y;
            // Randomize starting angle for variety
            this.moveAngle = random(TWO_PI);
        }

        public void update() {
            if (collected) return;

            // Animation logic - cycle through frames
            animTimer++;
            if (animTimer > 5) { // Change animation frame every 5 game frames
                animFrame = (animFrame + 1) % coinFrames.length;
                animTimer = 0;
            }

            // Movement pattern - circular or floating motion
            moveAngle += moveSpeed;
            if (moveAngle > TWO_PI) moveAngle -= TWO_PI;

            // Update position using circular motion
            x = origX + sin(moveAngle) * moveRadius;
            y = origY + cos(moveAngle) * (moveRadius / 2); // Elliptical motion looks better

            // Check if player collects this coin
            float playerCenterX = playerX + (tileW * renderScale * 0.75f);
            float playerCenterY = playerY + (tileH * renderScale * 0.75f);

            float coinCenterX = x + (tileW * renderScale * 0.5f);
            float coinCenterY = y + (tileH * renderScale * 0.5f);

            float distance = dist(playerCenterX, playerCenterY, coinCenterX, coinCenterY);

            // If player is close enough, collect the coin
            if (distance < tileW * renderScale) {
                collected = true;
                coinCount++;
                // You could play a sound effect here
            }
        }

        public void draw() {
            if (collected) return;

            // Use the current animation frame from our coinFrames array
            if (coinFrames != null && coinFrames[animFrame] != null) {
                image(coinFrames[animFrame], x, y, tileW * renderScale, tileH * renderScale);

                // Optional: Add a subtle glow effect around the coin
                if (frameCount % 30 < 15) { // Pulsing effect
                    noFill();
                    stroke(255, 215, 0, 100); // Semi-transparent gold
                    strokeWeight(2);
                    ellipse(x + (tileW * renderScale / 2), y + (tileH * renderScale / 2),
                            tileW * renderScale * 1.5f, tileH * renderScale * 1.5f);
                    strokeWeight(1);
                }
            }
        }

    }

    public void mousePressed() {
        if (editor) {
            float mapAreaWidth = width - editorPanelWidth;

            // Check if click is in the tile palette area
            if (mouseX > mapAreaWidth) {
                handleEditorMousePressed();
            } else {
                // Click in map area - place selected tile/coin/start pos
                int mapX = (int) ((mouseX - 10) / (tileW * 1.5f));
                int mapY = (int) ((mouseY - 10) / (tileH * 1.5f));

                // Debug output
                println("Placing at map position: " + mapX + ", " + mapY);

                int[][] currentMap = levelMaps[currentLevel - 1];
                // Check if coordinates are within map bounds
                if (mapX >= 0 && mapX < currentMap[0].length && mapY >= 0 && mapY < currentMap.length) {
                    // Place tile
                    currentMap[mapY][mapX] = selectedTile;
                    println("Placed tile " + selectedTile + " at " + mapX + ", " + mapY);
                }
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main("Main");
    }
}