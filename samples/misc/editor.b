"""Basic text editor
"""
import _bt.basic_test
import simple.gui as sg

class TextBuffer {
  def __init(s = '') {
    this._lines = s.split('\n')
  }

  def __len() {
    return len(this._lines)
  }

  def __getitem(i) {
    return this._lines[i]
  }
}

class TextView {
  def __init(buffer) {
    this._buffer = buffer
    this.line = 0
    this.font = sg.Monospace[30]
    this.cursor = [0, 0]
    this.background_color = sg.Color(1, 1, 1)
    this.text_color = sg.Color(0, 0, 0)
    this.cursor_color = sg.Color(0, 0.2, 1)
  }

  def draw(ctx) {
    """
    ctx: simple.gui.DrawingContext
    """
    ctx.color = this.background_color
    ctx.fill_rectangle(0, 0, ctx.width, ctx.height)

    buffer = this._buffer
    cursor = [cursor_line, cursor_column] = this.cursor
    line_limit = len(buffer)
    font_metrics = ctx.font_metrics(this.font)
    font_height = font_metrics.height
    next_line_index = this.line
    last_y = 0
    clip_height = ctx.height

    ctx.font = this.font
    ctx.color = this.text_color

    while (last_y <= clip_height and next_line_index < line_limit) {
      line = buffer[next_line_index]
      if (next_line_index == cursor_line) {
        prefix = line.substring(0, cursor_column)
        x = font_metrics.width(prefix)

        ctx.color = this.cursor_color
        ctx.fill_rectangle(x, last_y + font_height / 2, 4, font_height / 2)
        ctx.color = this.text_color
      }
      ctx.draw_string(line, 0, last_y + font_height)
      last_y = last_y + font_height
      next_line_index = next_line_index + 1
    }
  }

  def is_valid_cursor_position(pos) {
    [r, c] = pos
    b = this._buffer
    return r >= 0 and r < len(b) and c >= 0 and c <= len(b[r])
  }

  def move_cursor(direction) {
    [dr, dc] = direction
    [r, c] = this.cursor
    if (this.is_valid_cursor_position([r + dr, c + dc])) {
      this.cursor = [r + dr, c + dc]
    }
  }
}

buffer = TextBuffer('hello\nworld\n')
view = TextView(buffer)

window = sg.Window()
window.size = [400, 400]
window.on('draw', view.draw)
window.on('key', event -> % {
  switch(event.key,
    'Right', () -> % {
      view.move_cursor([0, 1])
    },
    'Left', () -> % {
      view.move_cursor([0, -1])
    },
    'Down', () -> % {
      view.move_cursor([1, 0])
    },
    'Up', () -> % {
      view.move_cursor([-1, 0])
    },
    () -> % {
      print('event.key = ' + event.key + ', char = ' + str(event.char))
    })
  window.repaint()
})
window.on('type', event -> % {
  print('event.char = ' + str(event.char))
})
window.show()
