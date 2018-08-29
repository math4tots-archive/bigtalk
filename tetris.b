"""tetris game
"""

HEIGHT = 24
WIDTH = 10

"""Board with coordinates where (0, 0) is the upper left corner.
Coordinates are (row, column)
"""
class Board {
  def __init(height, width) {
    this.height = height
    this.width = width
    this.buffer = ([0] * height).map(x -> [0] * width)
  }

  def __setitem(row, col, value) {
    this.buffer[row][col] = value
  }

  def __getitem(row, col) {
    return this.buffer[row][col]
  }

  def __str() {
    return ''.join(this.buffer.map(row -> ''.join(row) + '\n'))
  }

  def __repr() {
    return 'Board(' + str(this.height) + ', ' + str(this.weight) + ')'
  }
}

board = Board(HEIGHT, WIDTH)
board[0, 2] = 4
print(board)
