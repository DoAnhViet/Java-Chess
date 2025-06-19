package com.goldthumb.chess;

import java.util.HashSet;
import java.util.Set;

public class ChessModel {
    private Set<ChessPiece> piecesBox = new HashSet<ChessPiece>();
    private Player playerInTurn = Player.WHITE;
    private ChessPiece enPassantVulnerablePawn = null;
    
    public void reset() {
        piecesBox.clear();
        

        piecesBox.add(new ChessPiece(0, 0, Player.WHITE, Rank.ROOK, ChessConstants.wRook));
        piecesBox.add(new ChessPiece(7, 0, Player.WHITE, Rank.ROOK, ChessConstants.wRook));
        piecesBox.add(new ChessPiece(0, 7, Player.BLACK, Rank.ROOK, ChessConstants.bRook));
        piecesBox.add(new ChessPiece(7, 7, Player.BLACK, Rank.ROOK, ChessConstants.bRook));

                piecesBox.add(new ChessPiece(1, 0, Player.WHITE, Rank.KNIGHT, ChessConstants.wKnight));
        piecesBox.add(new ChessPiece(6, 0, Player.WHITE, Rank.KNIGHT, ChessConstants.wKnight));
        piecesBox.add(new ChessPiece(1, 7, Player.BLACK, Rank.KNIGHT, ChessConstants.bKnight));
        piecesBox.add(new ChessPiece(6, 7, Player.BLACK, Rank.KNIGHT, ChessConstants.bKnight));

                piecesBox.add(new ChessPiece(2, 0, Player.WHITE, Rank.BISHOP, ChessConstants.wBishop));
        piecesBox.add(new ChessPiece(5, 0, Player.WHITE, Rank.BISHOP, ChessConstants.wBishop));
        piecesBox.add(new ChessPiece(2, 7, Player.BLACK, Rank.BISHOP, ChessConstants.bBishop));
        piecesBox.add(new ChessPiece(5, 7, Player.BLACK, Rank.BISHOP, ChessConstants.bBishop));

                piecesBox.add(new ChessPiece(3, 0, Player.WHITE, Rank.QUEEN, ChessConstants.wQueen));
        piecesBox.add(new ChessPiece(4, 0, Player.WHITE, Rank.KING, ChessConstants.wKing));
        piecesBox.add(new ChessPiece(3, 7, Player.BLACK, Rank.QUEEN, ChessConstants.bQueen));
        piecesBox.add(new ChessPiece(4, 7, Player.BLACK, Rank.KING, ChessConstants.bKing));

                for (int i = 0; i < 8; i++) {
            piecesBox.add(new ChessPiece(i, 1, Player.WHITE, Rank.PAWN, ChessConstants.wPawn));
            piecesBox.add(new ChessPiece(i, 6, Player.BLACK, Rank.PAWN, ChessConstants.bPawn));
        }
        
        playerInTurn = Player.WHITE;
        enPassantVulnerablePawn = null;
    }
    
    public void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece movingPiece = pieceAt(fromCol, fromRow);
        if (movingPiece == null || movingPiece.getPlayer() != playerInTurn) return;
        if (!isValidMove(fromCol, fromRow, toCol, toRow)) return;
        if (!isMoveValidToEscapeCheck(fromCol, fromRow, toCol, toRow)) return;

        // Xử lý bắt tốt qua đường
        if (movingPiece.getRank() == Rank.PAWN && 
            Math.abs(fromCol - toCol) == 1 && 
            pieceAt(toCol, toRow) == null) {
            piecesBox.remove(pieceAt(toCol, fromRow));
        }

        // Xử lý nhập thành
        if (movingPiece.getRank() == Rank.KING && Math.abs(fromCol - toCol) == 2) {
            performCastling(fromCol, fromRow, toCol, toRow);
            switchPlayer();
            return;
        }

        // Xóa quân bị ăn TRƯỚC
        ChessPiece capturedPiece = pieceAt(toCol, toRow);
        if (capturedPiece != null) {
            piecesBox.remove(capturedPiece);
        }

        // Di chuyển quân
        piecesBox.remove(movingPiece);
        ChessPiece newPiece = movingPiece.withPosition(toCol, toRow);

        // Phong cấp (chỉ cho Tốt)
        if (movingPiece.getRank() == Rank.PAWN && (toRow == 0 || toRow == 7)) {
            newPiece = new ChessPiece(toCol, toRow, 
                                    movingPiece.getPlayer(), 
                                    Rank.QUEEN, 
                                    movingPiece.getPlayer() == Player.WHITE ? 
                                        ChessConstants.wQueen : ChessConstants.bQueen);
        }

        piecesBox.add(newPiece);

        // Đánh dấu tốt có thể bắt qua đường
        if (movingPiece.getRank() == Rank.PAWN && Math.abs(fromRow - toRow) == 2) {
            enPassantVulnerablePawn = newPiece;
        } else {
            enPassantVulnerablePawn = null;
        }
        
        switchPlayer();
    }
    
    private void switchPlayer() {
        playerInTurn = playerInTurn.opposite();
    }

    private void performCastling(int kingCol, int kingRow, int toCol, int toRow) {
                ChessPiece king = pieceAt(kingCol, kingRow);
        piecesBox.remove(king);
        piecesBox.add(king.withPosition(toCol, toRow));
        
                int rookCol = toCol > kingCol ? 7 : 0;
        int newRookCol = toCol > kingCol ? toCol - 1 : toCol + 1;
        ChessPiece rook = pieceAt(rookCol, kingRow);
        piecesBox.remove(rook);
        piecesBox.add(rook.withPosition(newRookCol, kingRow));
    }

    public ChessPiece pieceAt(int col, int row) {
        for (ChessPiece chessPiece : piecesBox) {
            if (chessPiece.getCol() == col && chessPiece.getRow() == row) {
                return chessPiece;
            }
        }
        return null;
    }

    public boolean isValidMove(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece piece = pieceAt(fromCol, fromRow);
        if (piece == null || (fromCol == toCol && fromRow == toRow)) {
            return false;
        }

        if (piece.getPlayer() != playerInTurn) {
            return false;
        }

        ChessPiece target = pieceAt(toCol, toRow);
        if (target != null && target.getPlayer() == piece.getPlayer()) {
            return false;
        }

        switch (piece.getRank()) {
            case PAWN:
                return isValidPawnMove(piece, fromCol, fromRow, toCol, toRow, target);
            case KNIGHT:
                return isValidKnightMove(fromCol, fromRow, toCol, toRow);
            case BISHOP:
                return isValidBishopMove(fromCol, fromRow, toCol, toRow);
            case ROOK:
                return isValidRookMove(fromCol, fromRow, toCol, toRow);
            case QUEEN:
                return isValidQueenMove(fromCol, fromRow, toCol, toRow);
            case KING:
                return isValidKingMove(fromCol, fromRow, toCol, toRow);
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(ChessPiece pawn, int fromCol, int fromRow, int toCol, int toRow, ChessPiece target) {
        int direction = pawn.getPlayer() == Player.WHITE ? 1 : -1;
        int startRow = pawn.getPlayer() == Player.WHITE ? 1 : 6;
        
                if (fromCol == toCol) {
                        if (toRow == fromRow + direction && target == null) {
                return true;
            }
                        if (fromRow == startRow && toRow == fromRow + 2 * direction && 
                target == null && pieceAt(fromCol, fromRow + direction) == null) {
                return true;
            }
        }
                else if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
                        if (target != null) {
                return true;
            }
                        if (enPassantVulnerablePawn != null && 
                toCol == enPassantVulnerablePawn.getCol() && 
                fromRow == enPassantVulnerablePawn.getRow()) {
                return true;
            }
        }
        
        return false;
    }

    private boolean isValidKnightMove(int fromCol, int fromRow, int toCol, int toRow) {
        int dx = Math.abs(toCol - fromCol);
        int dy = Math.abs(toRow - fromRow);
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }

    private boolean isValidBishopMove(int fromCol, int fromRow, int toCol, int toRow) {
        if (Math.abs(toCol - fromCol) != Math.abs(toRow - fromRow)) {
            return false;
        }

        int colStep = toCol > fromCol ? 1 : -1;
        int rowStep = toRow > fromRow ? 1 : -1;
        int steps = Math.abs(toCol - fromCol) - 1;

        for (int i = 1; i <= steps; i++) {
            if (pieceAt(fromCol + i * colStep, fromRow + i * rowStep) != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidRookMove(int fromCol, int fromRow, int toCol, int toRow) {
        if (fromCol != toCol && fromRow != toRow) {
            return false;
        }

        if (fromCol == toCol) {
            int rowStep = toRow > fromRow ? 1 : -1;
            int steps = Math.abs(toRow - fromRow) - 1;
            for (int i = 1; i <= steps; i++) {
                if (pieceAt(fromCol, fromRow + i * rowStep) != null) {
                    return false;
                }
            }
        } else {
            int colStep = toCol > fromCol ? 1 : -1;
            int steps = Math.abs(toCol - fromCol) - 1;
            for (int i = 1; i <= steps; i++) {
                if (pieceAt(fromCol + i * colStep, fromRow) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValidQueenMove(int fromCol, int fromRow, int toCol, int toRow) {
        return isValidRookMove(fromCol, fromRow, toCol, toRow) 
                || isValidBishopMove(fromCol, fromRow, toCol, toRow);
    }

    private boolean isValidKingMove(int fromCol, int fromRow, int toCol, int toRow) {
        int dx = Math.abs(toCol - fromCol);
        int dy = Math.abs(toRow - fromRow);
        if (dx <= 1 && dy <= 1) {
            return true;
        }
        
                if (fromRow == toRow && dx == 2 && dy == 0 && !pieceAt(fromCol, fromRow).hasMoved()) {
            return isValidCastling(fromCol, fromRow, toCol, toRow);
        }
        
        return false;
    }

    private boolean isValidCastling(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece king = pieceAt(fromCol, fromRow);
        if (king == null || king.getRank() != Rank.KING || king.hasMoved()) {
            return false;
        }
        
        int rookCol = toCol > fromCol ? 7 : 0;
        ChessPiece rook = pieceAt(rookCol, fromRow);
        if (rook == null || rook.getRank() != Rank.ROOK || rook.hasMoved()) {
            return false;
        }
        
        int step = toCol > fromCol ? 1 : -1;
        for (int col = fromCol + step; col != rookCol; col += step) {
            if (pieceAt(col, fromRow) != null) {
                return false;
            }
        }
        
        for (int col = fromCol; col != toCol + step; col += step) {
            if (isSquareUnderAttack(col, fromRow, king.getPlayer().opposite())) {
                return false;
            }
        }
        
        return true;
    }

    public boolean isKingInCheck(Player kingPlayer) {
        ChessPiece king = findKing(kingPlayer);
        if (king == null) return false;

        for (ChessPiece piece : piecesBox) {
            if (piece.getPlayer() != kingPlayer) {
                if (isValidMove(piece.getCol(), piece.getRow(), king.getCol(), king.getRow())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ChessPiece findKing(Player player) {
        for (ChessPiece piece : piecesBox) {
            if (piece.getRank() == Rank.KING && piece.getPlayer() == player) {
                return piece;
            }
        }
        return null;
    }

    public boolean isMoveValidToEscapeCheck(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece movingPiece = pieceAt(fromCol, fromRow);
        if (movingPiece == null) return false;
        
        ChessModel testModel = cloneModel();
        testModel.movePieceWithoutCheck(fromCol, fromRow, toCol, toRow);
        
        return !testModel.isKingInCheck(movingPiece.getPlayer());
    }

    private ChessModel cloneModel() {
        ChessModel clone = new ChessModel();
        clone.piecesBox = new HashSet<>(this.piecesBox);
        clone.playerInTurn = this.playerInTurn;
        clone.enPassantVulnerablePawn = this.enPassantVulnerablePawn;
        return clone;
    }

    private void movePieceWithoutCheck(int fromCol, int fromRow, int toCol, int toRow) {
        ChessPiece movingPiece = pieceAt(fromCol, fromRow);
        if (movingPiece == null) return;
        
        ChessPiece target = pieceAt(toCol, toRow);
        if (target != null) {
            piecesBox.remove(target);
        }
        
        piecesBox.remove(movingPiece);
        piecesBox.add(movingPiece.withPosition(toCol, toRow));
    }

    private boolean isSquareUnderAttack(int col, int row, Player byPlayer) {
        for (ChessPiece piece : piecesBox) {
            if (piece.getPlayer() == byPlayer) {
                if (isValidMove(piece.getCol(), piece.getRow(), col, row)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCheckmate(Player player) {
        if (!isKingInCheck(player)) {
            return false;
        }
        
        for (ChessPiece piece : piecesBox) {
            if (piece.getPlayer() == player) {
                for (int col = 0; col < 8; col++) {
                    for (int row = 0; row < 8; row++) {
                        if (isValidMove(piece.getCol(), piece.getRow(), col, row) 
                                && isMoveValidToEscapeCheck(piece.getCol(), piece.getRow(), col, row)) {
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }

    public boolean isStalemate(Player player) {
        if (isKingInCheck(player)) {
            return false;
        }
        
        for (ChessPiece piece : piecesBox) {
            if (piece.getPlayer() == player) {
                for (int col = 0; col < 8; col++) {
                    for (int row = 0; row < 8; row++) {
                        if (isValidMove(piece.getCol(), piece.getRow(), col, row) 
                                && isMoveValidToEscapeCheck(piece.getCol(), piece.getRow(), col, row)) {
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }

    @Override
    public String toString() {
        StringBuilder desc = new StringBuilder();
        
        for (int row = 7; row >= 0; row--) {
            desc.append(row);
            for (int col = 0; col < 8; col++) {
                ChessPiece p = pieceAt(col, row);
                if (p == null) {
                    desc.append(" .");
                } else {
                    desc.append(" ");
                    switch (p.getRank()) {
                        case KING: 
                            desc.append(p.getPlayer() == Player.WHITE ? "k" : "K");
                            break;
                        case QUEEN: 
                            desc.append(p.getPlayer() == Player.WHITE ? "q" : "Q");
                            break;
                        case BISHOP: 
                            desc.append(p.getPlayer() == Player.WHITE ? "b" : "B");
                            break;
                        case ROOK: 
                            desc.append(p.getPlayer() == Player.WHITE ? "r" : "R");
                            break;
                        case KNIGHT: 
                            desc.append(p.getPlayer() == Player.WHITE ? "n" : "N");
                            break;
                        case PAWN: 
                            desc.append(p.getPlayer() == Player.WHITE ? "p" : "P");
                            break;
                    }
                }
            }
            desc.append("\n");
        }
        desc.append("  0 1 2 3 4 5 6 7");
        
        return desc.toString();
    }
}