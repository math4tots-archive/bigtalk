"""tetris game
"""
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
  def __init(data) {
    assert.equal(4, data.size())
    assert(data.map(row -> assert.equal(4, row.size())))
    this.data = data
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
]

board = Board(HEIGHT, WIDTH)
print(board)

gui = simple.Gui(g -> % {
  print('width = ' + str(g.width))
  print('height = ' + str(g.height))
  g.fillRect(0, 0, g.width, g.height, [0, 0, 1])
  g.fillRect(0, 0, g.width / 2, g.height / 2, [1, 0, 0])
})
gui.title = 'Tetris'
gui.size = [600, 1200]
gui.start()
