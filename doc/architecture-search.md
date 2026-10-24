# Architecture: search

`sonouchi.core.search`. The overview is in
[architecture.md](architecture.md); how search appears on screen is in
[ux-journey.md](ux-journey.md), "The frame".

Search is the owner's main way of finding things, so it uses a
prepared index and an established tokenizer. The index is a value, so
it can live in the state, be compared, and be tested on both
platforms. The JavaScript search libraries (MiniSearch, FlexSearch,
Orama) all keep a mutable index, which could not.

## The index

```clojure
{:terms (sorted-map "runout" #{id1} "spindle" #{id1 id2 id3} …)
 :docs  {id1 #{"measure" "spindle" "runout"} …}}
```

- `:terms` maps each term to the ids of the tasks whose titles contain
  it. It is sorted, so the terms that start with a word are one range
  of it (`subseq`).
- `:docs` maps each task to its terms, so that a task can be taken out
  of the index when its title changes.

The map is the namespace's own: other code reads it only through the
functions below.

- `(search/index tokenize entities)` builds the index of every
  `:task/title` in an entities map.
- `(search/reindex idx tokenize entities ids)` indexes the tasks in
  `ids` again from `entities`. A task without a title leaves the index.
  Updating for one act changes only the tasks the act touched.
- `(search/matches idx tokenize text)` returns the set of ids of the
  tasks that match every word of `text`, or every task when `text` has
  no words.

## The tokenizer

`tokenize` is a function from a string to the words in it, passed in
by the caller. The core lower-cases the words to make terms. The shell
passes one built on the browser's `Intl.Segmenter`, which follows
Unicode word boundaries (UAX #29), the standard that ICU and Lucene's
StandardTokenizer implement; it splits Japanese into words as well.
The tests pass a simple one, so the core never calls the host.

## Matching

A task matches a word when one of its terms starts with the word, or
nearly does:

- one typing mistake is allowed for each five letters of the word, so
  words shorter than five letters must match exactly
- a mistake is a letter added, left out, changed, or swapped with its
  neighbour (the optimal string alignment distance, measured against
  the start of the term)
- the first letter must be right. Typos there are rare, and it limits
  the scan to the terms with that letter

The five letters per mistake are one setting, `letters-per-edit`.

## Tests and speed

The tests check matching by prefix, by several words in any order and
case, and with each kind of mistake. Two property tests check, for
random sets of titles, that updating the index for the tasks that
changed gives exactly the index built afresh, and that every task is
found by its own title.

On the JVM, with 3,000 made-up titles: building the index takes 13 ms,
updating it for one task 0.01 ms, and a two-word search with mistakes
2.4 ms. The browser is slower; it is measured on a release build.
