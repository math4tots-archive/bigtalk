"""Dead simple API for making a GUI

You get:

  * a single canvas to use as your window and do all your drawing,
  * chance to add some event listeners,

and that's it.

"""
import gui.swing
import gui.swing.color


class Gui {
  def __init(draw_callback) {
    this._frame = swing.Frame()
    this._canvas = swing.Canvas(g -> draw_callback(Graphics(g, this)))
    this._frame.getContentPane().add(this._canvas)
  }

  def __set_title(title) {
    """Not all implementations of simple gui will support __set_title
    """
    this._frame.setTitle(title)
  }

  def __set_size(dimensions) {
    """Not all implementations of simple gui will support __set_size
    """
    [width, height] = dimensions
    this._canvas.setPreferredSize(width, height)
    this._frame.pack()
  }

  def start() {
    this._frame.setVisible(true)
  }

  def repaint() {
    this._frame.repaint()
  }
}


class Graphics {
  def __init(graphics, gui) {
    this.graphics = graphics
    this.gui = gui
  }

  def __get_width() {
    return this.gui._canvas.getSize()[0]
  }

  def __get_height() {
    return this.gui._canvas.getSize()[1]
  }

  def fillRect(x, y, width, height, rgb) {
    [r, g, b] = rgb
    color_ = color.of(r, g, b, 1.0)
    this.graphics.setColor(color_)
    this.graphics.fillRect(x, y, width, height)
  }
}
