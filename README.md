
![Mascot](tanuki.png)

# Sonouchi

そのうち (sonouchi) means "soon", "before long", "eventually"

Every project planner knows that a perfect upfront estimate is a work of fiction.
So our [mascot](https://en.wikipedia.org/wiki/Bake-danuki) is a reminder that project estimates are foolish illusions.

The browser client for [soonish](../soonish), the project estimation
server. It is written in ClojureScript, renders with
[Replicant](https://github.com/cjohansen/replicant), and is built with
[shadow-cljs](https://github.com/thheller/shadow-cljs).

## Status

The project is set up, and the design work has started. The page shows
only a title and a count of recorded facts. The plan is
[TODO.md](TODO.md): the interface is designed first, standalone in the
browser on an in-browser data set, and soonish's data and rules are
revisited afterwards. The research behind it is in
[ux-research.md](ux-research.md) and
[ux-research-visual.md](ux-research-visual.md).

The guard-rails are in place: formatting, linting, the project's own
rules, a ratchet, tests on the JVM and on Node, and Claude Code hooks.
See [CONTRIBUTING.md](CONTRIBUTING.md).

## Installation

You need these on your PATH:

- **a JDK, version 25** (the current long-term support release), for
  the Clojure tools and the shadow-cljs compiler
- **the Clojure CLI**, version 1.12.6 or later
- **Node.js and npm**, for shadow-cljs and the tests on Node. Tested
  with Node 24.8.0 and npm 11.6.0.
- **git** and **jq**. The agent hooks use jq.

On macOS, with Homebrew:

    brew install openjdk clojure/tools/clojure node jq
    sudo ln -sfn /opt/homebrew/opt/openjdk/libexec/openjdk.jdk \
      /Library/Java/JavaVirtualMachines/openjdk.jdk

The link lets macOS tools find the JDK. git and jq come with macOS 15
and later.

On Ubuntu or Raspberry Pi OS, install a JDK 25 build (for example
Eclipse Temurin), git and jq with the package manager. Follow the
"Linux instructions" at https://clojure.org/guides/install_clojure for
the Clojure CLI. The distribution's Node.js may be old; if
`node --version` shows less than 24, install a current release from
https://nodejs.org.

On any machine that will show Japanese, install a CJK font. On Ubuntu
and Raspberry Pi OS:

    sudo apt install fonts-noto-cjk

Check the tools:

    java -version          # openjdk version "25..."
    clojure --version      # Clojure CLI version 1.12.6...
    node --version         # v24...
    jq --version

Then, from this directory, once per clone:

    npm ci                                    # shadow-cljs, as locked
    clojure -P -M:test:lint:fmt:guard:nrepl   # Clojure dependencies
    clojure -P -T:mcp                         # the agents' REPL tool
    bin/check                                 # every check

`npm ci` installs exactly what `package-lock.json` says. Use it rather
than `npm install`, which can change the lockfile. The first
`bin/check` downloads the ClojureScript compiler, which takes a
minute.

## Development workflow

### Run the app

    npx shadow-cljs watch app

Open http://localhost:8080. shadow-cljs compiles on every save and
reloads the page's code without losing its state. Warnings are errors,
so a warning stops the reload and is shown in the terminal and on the
page. Stop it with Ctrl-C.

### Work in a REPL

`watch` also starts an nREPL server on port 7888. Connect your editor
to it, then evaluate this to reach the page:

    (shadow.cljs.devtools.api/repl :app)

The page must be open in a browser. `:cljs/quit` returns to the JVM.

For the pure core, a JVM REPL is quicker, and needs no browser:

    clj -M:dev

Then, for example:

    (require 'sonouchi.core.view :reload)
    (sonouchi.core.view/root {:state/facts []})

### Check before each commit

    bin/check

It runs six checks, and reports at the end which failed:

1. **format:** `clojure -M:fmt check src test guard/src`. Fix
   failures with `bin/fmt`.
2. **lint:** `clojure -M:lint --lint src test guard/src`
3. **guard:** `clojure -M:guard`. The project's own rules: bans,
   the attribute registry, unused public vars, and the ratchet.
4. **test-jvm:** `clojure -M:test`. Every test in `test/`, on the
   JVM.
5. **build:** `npx shadow-cljs compile app test`. Both builds, with
   warnings as errors.
6. **test-node:** `node target/node-test.js`. The same tests, as
   compiled ClojureScript, on Node.

Each line above also runs alone. To run one test namespace, or one
test, on the JVM:

    clojure -M:test -n sonouchi.core.view-test
    clojure -M:test -v sonouchi.core.view-test/root-counts-the-facts

To run the checks on every commit, enable the git hook once per clone:

    git config core.hooksPath bin/git-hooks

It runs `bin/check` on each commit, and lists any guard-rail files the
commit changes, for you to review.

### Build for release

    npx shadow-cljs release app

This writes an optimised `public/js/main.js`. The whole app is then
the static files in `public/`, and any web server can serve them. To
try the release build:

    python3 -m http.server -d public 8080

Judge speed on a release build, not on `watch`, which is much slower.

## Coding agents

Start Claude Code from this directory, so that it loads `CLAUDE.md`,
`.mcp.json` and the hooks in `.claude/settings.json`:

- edits to guard-rail files are blocked, by file tools, Bash or the
  REPL. The list is in `bin/protected`.
- npm commands that change the dependencies are blocked. Agents may run
  `npm ci`.
- every edited Clojure file is linted at once
- `bin/check` runs when the agent stops, if sources changed
- the agent gets a REPL through
  [clojure-mcp](https://github.com/bhauman/clojure-mcp), with its
  `:repl` profile: `clojure_eval`, `list_nrepl_ports` and
  `paren_repair`. clojure-mcp starts its own JVM nREPL with
  `clojure -M:nrepl` for the core. While you run
  `npx shadow-cljs watch app`, the agent can also reach the browser
  REPL on port 7888.

clojure-mcp is pinned in `deps.edn` (alias `:mcp`), so nothing is
installed globally. Check it with `/mcp` inside Claude Code.

Each `clojure_eval` asks for permission, because it runs arbitrary
code. To allow it without asking, add
`"mcp__clojure-mcp__clojure_eval"` to `permissions.allow` in
`.claude/settings.local.json`, which is not committed.

To let an agent edit guard-rail files for one session, start it with:

    SONOUCHI_OWNER_EDIT=1 claude

## Commands

- `npx shadow-cljs watch app`: the app at http://localhost:8080, with
  hot reload and the browser REPL on port 7888.
- `npx shadow-cljs release app`: an optimised build in `public/js/`.
- `bin/check`: every check. Run it before each commit.
- `bin/fmt`: reformat the sources.
- `clj -M:dev`: a JVM REPL with the tests on the classpath.
- `clojure -M:test`: the tests on the JVM only.
- `clojure -M:guard`: the project's own rules only.
- `clojure -M:guard --update-baseline`: accept new ratchet counts.
  Owner only.

## Pinned versions

All versions are exact. In `deps.edn`:

- Clojure 1.12.6
- Replicant 2026.07.1
- shadow-cljs 3.5.7
- clj-kondo 2026.08.04
- cljfmt 0.16.6
- test.check 1.1.3
- cognitect test-runner v0.5.1 (git sha dfb30dd)
- nREPL 1.8.0
- clojure-mcp v0.6.0 (git sha 2b0213f)

In `package.json` and `package-lock.json`:

- shadow-cljs 3.5.7, which must match the version in `deps.edn`

Tools outside these files, as last checked:

- JDK 25.0.1
- Clojure CLI 1.12.6.1673
- Node.js 24.8.0, npm 11.6.0

## Layout

    TODO.md                 the plan
    ux-research.md          the workflow, the data, design ideas I1-I10
    ux-research-visual.md   perception and interaction, rules V1-V17
    src/sonouchi/core/      the pure core, in .cljc: views, actions
    src/sonouchi/app.cljs   the shell: the one atom, rendering
    test/                   tests, in .cljc, run on the JVM and Node
    public/index.html       the page; builds write public/js/
    guard/                  the project's own rules and their checker
    bin/                    check, fmt, the guard-rail list, git hook
    .claude/                agent hooks and settings
    .mcp.json               agent tools: clojure-mcp
    deps.edn                Clojure dependencies and tool aliases
    shadow-cljs.edn         the builds: app, and tests on Node
    package.json            npm: shadow-cljs only
