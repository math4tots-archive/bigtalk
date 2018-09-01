"""tetris game
"""
import _bt.basic_test
import gui.simple
import random

HEIGHT = 24
WIDTH = 10

"""Board with coordinates where (0, 0) is the upper left corner.
Coordinates are (row, column)
"""
class Board {
  def __init(height, width) {
    this._height = height
    this._width = width
    this._rows = ([0] * height).map(x -> [0] * width)
  }

  def __get_height() {
    return this._height
  }

  def __get_width() {
    return this._width
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
    return 'Board(' + str(this._height) + ', ' + str(this._width) + ')'
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
      0 <= r and r < this.height and
      0 <= c and c < this.width)
  }

  def _row_is_full(row) {
    return this._rows[row].all()
  }

  def _clear_row(row) {
    for r in reversed(range(row)) {
      this._rows[r + 1] = this._rows[r]
    }
    this._rows[0] = [0] * this.width
  }

  def clear_completed_rows() {
    for r in reversed(range(this.height)) {
      while (this._row_is_full(r)) {
        this._clear_row(r)
      }
    }
  }
}

class Piece {
  def __init(data, row = 0, col = 0, do_check = true) {
    this.data = data
    this.row = row
    this.col = col
    if (do_check) {
      assert.equal(4, this.data.size())
      data.each(row -> assert.equal(4, row.size()))
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
    [0, 1, 1, 0],
    [0, 0, 1, 0],
    [0, 0, 0, 0],
  ]),
]


def main() {
  rand = random.Random()

  def spawn_piece() {
    return rand.pick(pieces).move_to(0, board.width // 2 - 2)
  }

  board = Board(HEIGHT, WIDTH)
  background_color = [0, 0, 0]
  fill_color = [0.5, 0.5, 0]
  live_color = [0, 0.5, 0.5]
  live_piece = [spawn_piece()]
  gui = simple.Gui(g -> % {
    g.fill_rectangle(0, 0, g.width, g.height, background_color)

    def fill(row, col, color) {
      g.fill_rectangle(
        col * cell_width,
        row * cell_height,
        cell_width,
        cell_height,
        color)
    }

    cell_height = g.height / board.height
    cell_width = g.width / board.width
    for row in range(board.height) {
      for col in range(board.width) {
        if (board[row, col]) {
          fill(row, col, fill_color)
        }
      }
    }

    for [row, col] in live_piece[0].coordinates() {
      fill(row, col, live_color)
    }
  })
  gui.title = 'Tetris'
  gui.size = [600, 1200]
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
        for i in range(board.height) {
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
      board.clear_completed_rows()
      live_piece[0] = spawn_piece()
    }
  }

  def tick() {
    print('tick called!')
    move_piece_down()
    gui.repaint()
    wait_for(2, tick)
  }

  wait_for(2, tick)
  gui.start()
}

main()
