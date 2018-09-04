import simple.gui as sg

def main() {
  window = sg.Window()
  "window.size = [400, 400]"
  window.on('draw', g -> % {
    print('g.dimension = ' + str(g.dimension))
    g.clip(300, 100, g.width - 300, 100, g -> % {
      g.color = sg.Color(1, 1, 1)
      g.fill_rectangle(0, 0, g.width, g.height)

      font = sg.Monospace[50]
      font_metrics = g.font_metrics(font)
      font_height = font_metrics.height

      g.color = sg.Color(0, 0, 0)
      g.font = font
      g.draw_string('Hello world!!!', 0, font_height)
    })
  })
  window.show()
}

main()
