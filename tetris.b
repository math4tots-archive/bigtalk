"""tetris game
"""
import _bt.basic_test
import gui.simple as sg
import random

NUMBER_OF_ROWS = 24
NUMBER_OF_COLUMNS = 10

"""Board with coordinates where (0, 0) is the upper left corner.
Coordinates are (row, column)
"""
class Board {
  def __init(nrows, ncols) {
    this.nrows = nrows
    this.ncols = ncols
    this._rows = ([0] * nrows).map(x -> [0] * ncols)
  }

  def __setitem(row, col, value) {
    this._rows[row][col] = value
  }

  def __getitem(row, col) {
    return this._rows[row][col]
  }

  def __str() {
    return ''.join(this._rows.map(row -> ''.join(row) + '\n'))
  }

  def __repr() {
    return 'Board(' + str(this.nrows) + ', ' + str(this.ncols) + ')'
  }

  def place(piece) {
    for point in piece.coordinates() {
      [row, col] = point
      this[row, col] = 1
    }
  }

  def contains_point(point) {
    [r, c] = point
    return (
      0 <= r and r < this.nrows and
      0 <= c and c < this.ncols)
  }

  def _row_is_full(row) {
    return this._rows[row].all()
  }

  def _clear_row(row) {
    for r in reversed(range(row)) {
      this._rows[r + 1] = this._rows[r]
    }
    this._rows[0] = [0] * this.ncols
  }

  def clear_completed_rows() {
    number_of_completed_rows = 0
    for r in reversed(range(this.nrows)) {
      while (this._row_is_full(r)) {
        number_of_completed_rows = number_of_completed_rows + 1
        this._clear_row(r)
      }
    }
    return number_of_completed_rows
  }
}

class Piece {
  def __init(data, row = 0, col = 0, do_check = true) {
    this.data = data
    this.row = row
    this.col = col
    if (do_check) {
      assert.equal(4, len(this.data))
      data.each(row -> assert.equal(4, len(row)))
    }
  }

  def __eq(other) {
    return (type(other) == Piece and
      this.data == other.data and
      this.row == other.row and
      this.col == other.col)
  }

  def within(board) {
    return List(this.coordinates()).all(board.contains_point)
  }

  def conflicts_with_board(board) {
    return this.coordinates().any(point -> % {
      [r, c] = point
      return not board.contains_point(point) or board[r, c]
    })
  }

  def move_to(r, c) {
    return Piece(this.data, r, c, false)
  }

  def move(dr, dc) {
    return this.move_to(this.row + dr, this.col + dc)
  }

  def rotate() {
    "Rotate 90 degrees clockwise"
    new_data = ([0] * 4).map(_ -> [0] * 4)
    for row in range(4) {
      for col in range(4) {
        new_data[col][3 - row] = this.data[row][col]
      }
    }
    return Piece(new_data, this.row, this.col, false)
  }

  def* coordinates() {
    for row in range(4) {
      for col in range(4) {
        if (this.data[row][col]) {
          yield [this.row + row, this.col + col]
        }
      }
    }
  }
}

pieces = [
  Piece([
    [0, 1, 0, 0],
    [0, 1, 0, 0],
    [0, 1, 0, 0],
    [0, 1, 0, 0],
  ]),
  Piece([
    [0, 1, 0, 0],
    [0, 1, 0, 0],
    [0, 1, 1, 0],
    [0, 0, 0, 0],
  ]),
  Piece([
    [0, 0, 1, 0],
    [0, 0, 1, 0],
    [0, 1, 1, 0],
    [0, 0, 0, 0],
  ]),
  Piece([
    [0, 0, 1, 0],
    [0, 1, 1, 0],
    [0, 0, 1, 0],
    [0, 0, 0, 0],
  ]),
  Piece([
    [0, 0, 0, 0],
    [0, 1, 1, 0],
    [0, 1, 1, 0],
    [0, 0, 0, 0],
  ]),
  Piece([
    [0, 0, 0, 0],
    [0, 1, 1, 0],
    [0, 0, 1, 1],
    [0, 0, 0, 0],
  ]),
  Piece([
    [0, 0, 0, 0],
    [0, 1, 1, 0],
    [1, 1, 0, 0],
    [0, 0, 0, 0],
  ]),
]


def main() {
  rand = random.Random()

  def spawn_piece() {
    return rand.pick(pieces).move_to(0, board.ncols // 2 - 2)
  }

  board = Board(NUMBER_OF_ROWS, NUMBER_OF_COLUMNS)
  background_color = sg.Color(0.2, 0.1, 0.1)
  board_color = sg.Color(0, 0, 0)
  fill_color = sg.Color(0.5, 0.5, 0)
  live_color = sg.Color(0, 0.5, 0.5)
  score_color = sg.Color(1, 1, 1)
  live_piece = [spawn_piece()]
  score = [0]
  gui = sg.Gui(g -> % {
    g.fill_rectangle(0, 0, g.width, g.height, background_color)
    g.fill_rectangle(0, 0, g.width / 2, g.height, board_color)

    g.draw_string(
      g.width * 2 / 3,
      g.height / 3,
      str(score),
      sg.MONOSPACED,
      50,
      score_color)

    def fill(row, col, color) {
      g.fill_rectangle(
        col * cell_width,
        row * cell_height,
        cell_width,
        cell_height,
        color)
    }

    def outline(row, col, color) {
      g.draw_rectangle(
        col * cell_width,
        row * cell_height,
        cell_width,
        cell_height,
        color)
    }

    cell_height = g.height / board.nrows
    cell_width = g.width / board.ncols / 2
    for row in range(board.nrows) {
      for col in range(board.ncols) {
        if (board[row, col]) {
          fill(row, col, fill_color)
        }
      }
    }
    for row in range(board.nrows) {
      for col in range(board.ncols) {
        if (board[row, col]) {
          outline(row, col, background_color)
        }
      }
    }

    for [row, col] in live_piece[0].coordinates() {
      fill(row, col, live_color)
    }
    for [row, col] in live_piece[0].coordinates() {
      outline(row, col, background_color)
    }
  })
  gui.title = 'Tetris'
  gui.size = [1000, 1200]
  gui.resizable = false
  gui.on('key', event -> % {
    switch(event.key,
      'A', nil,
      'Left', () -> {
        move_piece(0, -1)
      },
      'D', nil,
      'Right', () -> {
        move_piece(0, 1)
      },
      'S', nil,
      'Down', () -> {
        move_piece_down()
      },
      'W', nil,
      'Up', () -> {
        rotate_piece()
      },
      'Space', () -> % {
        for i in range(board.nrows) {
          move_piece(1, 0)
        }
        move_piece_down()
      },
      () -> {
        print('Unrecognized key ' + event.key)
      })
    gui.repaint()
  })

  def rotate_piece() {
    new_piece = live_piece[0].rotate()
    if (new_piece.conflicts_with_board(board)) {
      if (not new_piece.move(0, -1).conflicts_with_board(board)) {
        new_piece = new_piece.move(0, -1)
      } else if (not new_piece.move(0, 1).conflicts_with_board(board)) {
        new_piece = new_piece.move(0, 1)
      } else if (not new_piece.move(0, -2).conflicts_with_board(board)) {
        new_piece = new_piece.move(0, -2)
      } else if (not new_piece.move(0, 2).conflicts_with_board(board)) {
        new_piece = new_piece.move(0, 2)
      } else {
        new_piece = live_piece[0]
      }
    }
    live_piece[0] = new_piece
  }

  def move_piece(dr, dc) {
    new_piece = live_piece[0].move(dr, dc)
    if (not new_piece.conflicts_with_board(board)) {
      live_piece[0] = new_piece
    }
  }

  def move_piece_down() {
    old_piece = live_piece[0]
    move_piece(1, 0)
    if (old_piece == live_piece[0]) {
      board.place(old_piece)
      score[0] = score[0] + board.clear_completed_rows() ** 2
      live_piece[0] = spawn_piece()
    }
  }

  def tick() {
    move_piece_down()
    gui.repaint()
    wait_for(2, tick)
  }

  wait_for(2, tick)
  gui.start()
}

main()
