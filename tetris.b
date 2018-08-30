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

board = Board(HEIGHT, WIDTH)
print(board)

gui = simple.Gui(g -> % {
  g.fillRect(0, 0, g.width, g.height, [0, 0, 1])
  g.fillRect(0, 0, g.width / 2, g.height / 2, [1, 0, 0])
})
gui.title = 'Tetris'
gui.size = [600, 1200]
gui.start()
