package com.goldthumb.chess;

import java.util.Objects;

enum Player {
    WHITE,
    BLACK;

    public Player opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}

enum Rank {
    KING,
    QUEEN,
    BISHOP,
    ROOK,
    KNIGHT,
    PAWN;
}

public class ChessPiece {
    private  int col;
    private  int row;
    private final Player player;
    private Rank rank;
    private  String imgName;
    private boolean hasMoved;  
    private boolean enPassantVulnerable;  
    
    public ChessPiece(int col, int row, Player player, Rank rank, String imgName) {
        this.col = col;
        this.row = row;
        this.player = player;
        this.rank = rank;
        this.imgName = imgName;
        this.hasMoved = false;
        this.enPassantVulnerable = false;
    }

        public ChessPiece(ChessPiece other) {
        this.col = other.col;
        this.row = other.row;
        this.player = other.player;
        this.rank = other.rank;
        this.imgName = other.imgName;
        this.hasMoved = other.hasMoved;
        this.enPassantVulnerable = other.enPassantVulnerable;
    }

        public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public Player getPlayer() {
        return player;
    }

    public Rank getRank() {
        return rank;
    }

    public String getImgName() {
        return imgName;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public boolean isEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    // Setters
    public ChessPiece withPosition(int newCol, int newRow) {
        ChessPiece copy = new ChessPiece(this);
        copy.col = newCol;
        copy.row = newRow;
        copy.hasMoved = true;
        return copy;
    }

    public ChessPiece withPromotion(Rank newRank, String newImgName) {
        ChessPiece copy = new ChessPiece(this);
        copy.rank = newRank;
        copy.imgName = newImgName;
        return copy;
    }

    public ChessPiece withEnPassantVulnerable(boolean vulnerable) {
        ChessPiece copy = new ChessPiece(this);
        copy.enPassantVulnerable = vulnerable;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return col == that.col && 
               row == that.row && 
               player == that.player && 
               rank == that.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row, player, rank);
    }
}