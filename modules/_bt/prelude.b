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

globals.switch = def switch(value, *parts) {
  i = 0
  while (i + 1 < parts.size()) {
    if (parts[i] == value) {
      callback = parts[i + 1]
      while (i + 1 < parts.size() and parts[i + 1] is nil) {
        i = i + 2
      }
      if (i + 1 >= parts.size()) {
        fail("Missing switch implementation")
      }
      parts[i + 1]()
      return
    }
    i = i + 2
  }
  if (i < parts.size()) {
    parts[i]()
  }
}
