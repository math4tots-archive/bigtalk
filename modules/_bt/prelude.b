import _globals as globals

globals.range = def* range(start, end=nil) {
  if (end is nil) {
    end = start
    start = 0
  }

  i = start
  while (i < end) {
    yield i
    i = i + 1
  }
}
