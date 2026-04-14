// package com.synkork.backend.modules.collaboration.task.board;

// import java.util.List;
// import java.util.UUID;
// import java.util.stream.Collectors;

// import javax.smartcardio.Card;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.synkork.backend.modules.collaboration.task.card.CardEntity;
// import com.synkork.backend.modules.collaboration.task.card.CardRepository;
// import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;
// import com.synkork.backend.modules.collaboration.task.column.ColumnRepository;
// import com.synkork.backend.modules.collaboration.task.dto.BoardDTO;
// import com.synkork.backend.modules.collaboration.task.dto.CardDTO;
// import com.synkork.backend.modules.collaboration.task.dto.ColumnDTO;

// import jakarta.transaction.Transactional;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class BoardService {

//     private final BoardRepository boardRepository;
//     private final CardRepository cardRepository;
//     private final ColumnRepository columnRepository;

//     public List<BoardEntity> findAll() {
//         return boardRepository.findAll();
//     }

//     // 1. Lấy danh sách tất cả Board (chỉ lấy thông tin cơ bản)
//     public List<BoardDTO> getAllBoards() {
//         return boardRepository.findAll().stream().map(board -> {
//             BoardDTO dto = new BoardDTO();
//             dto.setId(board.getId());
//             dto.setName(board.getName());
//             return dto;
//         }).collect(Collectors.toList());
//     }

//     // 2. Lấy chi tiết 1 Board
//     public BoardDTO getBoardById(UUID id) {
//         BoardEntity board = boardRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Không tìm thấy Board với ID: " + id));

//         return mapToDTO(board);
//     }

//     // 3. Tạo mới Board
//     @Transactional
//     public BoardDTO createBoard(BoardEntity board) {
//         // Lưu ý: board.getSpace() phải tồn tại trước đó
//         BoardEntity savedBoard = boardRepository.save(board);
//         BoardDTO dto = new BoardDTO();
//         dto.setName(savedBoard.getName());
//         return dto;
//     }

//     // 4. Cập nhật Board
//     @Transactional
//     public BoardDTO updateBoard(UUID id, String newName) {
//         BoardEntity board = boardRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Không tìm thấy Board"));
//         board.setName(newName);
//         BoardEntity updatedBoard = boardRepository.save(board);

//         BoardDTO dto = new BoardDTO();
//         dto.setId(updatedBoard.getId());
//         dto.setName(updatedBoard.getName());
//         return dto;
//     }

//     // 5. Xóa Board (Sẽ xóa luôn Column/Card do có CascadeType.ALL)
//     @Transactional
//     public void deleteBoard(UUID id) {
//         if (!boardRepository.existsById(id)) {
//             throw new RuntimeException("Board không tồn tại");
//         }
//         boardRepository.deleteById(id);
//     }

//     // Hàm hỗ trợ map DTO
//     private BoardDTO mapToDTO(BoardEntity board) {
//         BoardDTO dto = new BoardDTO();
//         dto.setId(board.getId());
//         dto.setName(board.getName());

//         // Đừng để trống chỗ này nữa nè Vy!
//         if (board.getColumns() != null) {
//             dto.setColumns(board.getColumns().stream().map(col -> {
//                 ColumnDTO colDTO = new ColumnDTO();
//                 colDTO.setId(col.getId());
//                 colDTO.setName(col.getName());
//                 // ... map thêm cards tương tự như hàm getBoardDetails ở dưới của Vy
//                 return colDTO;
//             }).collect(Collectors.toList()));
//         }
//         return dto;
//     }

//     public BoardDTO getBoardDetails(UUID boardId) {
//         BoardEntity board = boardRepository.findById(boardId)
//                 .orElseThrow(() -> new RuntimeException("Không tìm thấy Board với id: " + boardId));

//         BoardDTO boardDTO = new BoardDTO();
//         boardDTO.setId(board.getId());
//         boardDTO.setName(board.getName());

//         List<ColumnDTO> columnDTOs = board.getColumns().stream().map(col -> {
//             ColumnDTO colDTO = new ColumnDTO();
//             colDTO.setId(col.getId());
//             colDTO.setName(col.getName());
//             colDTO.setPosition(col.getPosition());

//             List<CardDTO> cardDTOs = col.getCards().stream().map(card -> {
//                 CardDTO cardDTO = new CardDTO();
//                 cardDTO.setId(card.getId());
//                 cardDTO.setTitle(card.getTitle());
//                 cardDTO.setDescription(card.getDescription());
//                 cardDTO.setPosition(card.getPosition());
//                 return cardDTO;
//             }).collect(Collectors.toList());

//             colDTO.setCards(cardDTOs);
//             return colDTO;
//         }).collect(Collectors.toList());

//         boardDTO.setColumns(columnDTOs);
//         return boardDTO;
//     }

//     @Transactional
//     public void moveCard(UUID cardId, UUID targetColumnId, int newPosition) {
//         CardEntity card = cardRepository.findById(cardId)
//                 .orElseThrow(() -> new RuntimeException("Card không tồn tại"));

//         ColumnEntity oldColumn = card.getColumn();
//         ColumnEntity targetColumn = columnRepository.findById(targetColumnId)
//                 .orElseThrow(() -> new RuntimeException("Cột đích không tồn tại"));

//         if (oldColumn.getId().equals(targetColumnId)) {
//             // CASE 1: Di chuyển trong cùng 1 cột
//             handleSameColumnMove(oldColumn, card, newPosition);
//         } else {
//             // CASE 2: Di chuyển sang cột khác
//             handleCrossColumnMove(oldColumn, targetColumn, card, newPosition);
//         }

//         card.setColumn(targetColumn);
//         card.setPosition(newPosition);
//         cardRepository.save(card);
//     }

//     // CASE 1: Di chuyển trong cùng một cột
//     private void handleSameColumnMove(ColumnEntity column, CardEntity card, int newPosition) {
//         int oldPosition = card.getPosition();
//         List<CardEntity> cards = cardRepository.findByColumn_IdOrderByPositionAsc(column.getId());

//         if (oldPosition < newPosition) {
//             // Kéo xuống: Các card ở giữa sẽ bị giảm vị trí đi 1
//             for (CardEntity c : cards) {
//                 if (c.getPosition() > oldPosition && c.getPosition() <= newPosition) {
//                     c.setPosition(c.getPosition() - 1);
//                 }
//             }
//         } else if (oldPosition > newPosition) {
//             // Kéo lên: Các card ở giữa sẽ bị tăng vị trí lên 1
//             for (CardEntity c : cards) {
//                 if (c.getPosition() >= newPosition && c.getPosition() < oldPosition) {
//                     c.setPosition(c.getPosition() + 1);
//                 }
//             }
//         }
//         cardRepository.saveAll(cards);
//     }

//     // CASE 2: Di chuyển sang cột khác
//     private void handleCrossColumnMove(ColumnEntity oldCol, ColumnEntity newCol, CardEntity card, int newPosition) {
//         // 1. Cập nhật các card ở cột cũ (lấp chỗ trống)
//         List<CardEntity> oldCards = cardRepository.findByColumn_IdOrderByPositionAsc(oldCol.getId());
//         for (CardEntity c : oldCards) {
//             if (c.getPosition() > card.getPosition()) {
//                 c.setPosition(c.getPosition() - 1);
//             }
//         }
//         cardRepository.saveAll(oldCards);

//         // 2. Cập nhật các card ở cột mới (nhường chỗ cho card mới tới)
//         List<CardEntity> newCards = cardRepository.findByColumn_IdOrderByPositionAsc(newCol.getId());
//         for (CardEntity c : newCards) {
//             if (c.getPosition() >= newPosition) {
//                 c.setPosition(c.getPosition() + 1);
//             }
//         }
//         cardRepository.saveAll(newCards);
//     }

//     // @Transactional
//     // public BoardDTO getBoardsBySpaceId(UUID spaceId) {
//     //     BoardEntity boards = boardRepository.findBySpaceId(spaceId);

//     //     return boards.stream().map(board -> {
//     //         BoardDTO dto = new BoardDTO();
//     //         dto.setId(board.getId());
//     //         dto.setName(board.getName());
//     //         return dto;
//     //     }).toList();
//     // }
// }
