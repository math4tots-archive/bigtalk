import _globals as globals

class Range(RandomAccessContainer) {
  def __init(start, end) {
    this.start = start
    this.end = end
  }

  def* __iter() {
    start = this.start
    end = this.end

    i = start
    while (i < end) {
      yield i
      i = i + 1
    }
  }

  def __getitem(i) {
    if (i < 0 or i >= len(this)) {
      fail("Index " + str(i) + " out of bounds")
    }
    return this.start + i
  }

  def __len() {
    return this.end - this.start
  }
}

class ReversedRandomAccessContainer {
  def __init(container) {
    this.container = container
  }

  def* __iter() {
    container = this.container
    i = len(container) - 1
    while (i >= 0) {
      yield container[i]
      i = i - 1
    }
  }

  def __getitem(i) {
    size = len(this.container)
    return this.container[size - 1 - i]
  }

  def __len() {
    return len(this.container)
  }
}

globals.reversed = def reversed(xs) {
  if (type(xs) < RandomAccessContainer) {
    return ReversedRandomAccessContainer(xs)
  }
  list = List(xs)
  reversed_list = []
  i = len(list) - 1
  while (i >= 0) {
    reversed_list.push(list[i])
    i = i - 1
  }
  return reversed_list
}

globals.range = def range(start, end=nil) {
  if (end is nil) {
    end = start
    start = 0
  }
  return Range(start, end)
}

globals.switch = def switch(value, *parts) {
  i = 0
  while (i + 1 < len(parts)) {
    if (parts[i] == value) {
      callback = parts[i + 1]
      while (i + 1 < len(parts) and parts[i + 1] is nil) {
        i = i + 2
      }
      if (i + 1 >= len(parts)) {
        fail("Missing switch implementation")
      }
      return parts[i + 1]()
    }
    i = i + 2
  }
  if (i < len(parts)) {
    return parts[i]()
  }
}
