"""tetris game
"""
import _bt.basic_test
import gui.simple

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
}

class Piece {
  def __init(data, row = nil, col = nil, do_check = true) {
    this.data = data
    this.row = row or 0
    this.col = col or 0
    if (do_check) {
      assert.equal(4, this.data.size())
      data.each(row -> assert.equal(4, row.size()))
    }
  }

  def move(dr, dc) {
    return Piece(this.data, this.row + dr, this.col + dc, false)
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
    [0, 0, 0, 0],
    [0, 0, 1, 0],
    [0, 1, 1, 1],
    [0, 0, 0, 0],
  ]),
]

print(List(pieces[0].coordinates()))

def main() {
  board = Board(HEIGHT, WIDTH)
  live_piece = pieces[0]

  gui = simple.Gui(g -> % {
    background_color = [0, 0, 0]
    fill_color = [0.5, 0.5, 0]
    live_color = [0, 0.5, 0.5]

    g.fillRect(0, 0, g.width, g.height, background_color)

    def fill(row, col, color) {
      g.fillRect(
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

    for point in live_piece.coordinates() {
      [row, col] = point
      fill(row, col, live_color)
    }
  })
  gui.title = 'Tetris'
  gui.size = [600, 1200]
  gui.start()
}

main()
