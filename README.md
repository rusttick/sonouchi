
![Mascot](public/tanuki.png)

# Sonouchi

そのうち (sonouchi) means "soon", "before long", "eventually"

Every project planner knows that a perfect upfront estimate is a work of fiction.
So our [mascot](https://en.wikipedia.org/wiki/Bake-danuki) is a reminder that project estimates are foolish illusions.

Sonouchi is the browser client for [soonish](../soonish), the project estimation
server. It is written in ClojureScript, renders with
[Replicant](https://github.com/cjohansen/replicant), and is built with
[shadow-cljs](https://github.com/thheller/shadow-cljs).



## Layout

    doc/                    goals, to do, UX, architecture, research
    src/sonouchi/core/      the pure core, in .cljc: views, actions
    src/sonouchi/app.cljs   the shell: the one atom, rendering
    test/                   tests, in .cljc, run on the JVM and Node
    public/index.html       the page; builds write public/js/
    public/*.ico, *.png     the icons, made from tanuki.png
    tanuki.png              the original tanuki drawing
    guard/                  the project's own rules and their checker
    bin/                    check, fmt, the guard-rail list, git hook
    .claude/                agent hooks and settings
    .mcp.json               agent tools: clojure-mcp
    deps.edn                Clojure dependencies and tool aliases
    shadow-cljs.edn         the builds: app, and tests on Node
    package.json            npm: shadow-cljs only



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
    (sonouchi.core.view/root {:state/log []
                              :state/platform :mac
                              :state/now #inst "2026-10-24T00:00:00Z"
                              :state/utc-offset-minutes 540})

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
    clojure -M:test -v sonouchi.core.view-test/root-handlers-are-data

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

### Icons

The icons in `public/` are made from `tanuki.png` with ImageMagick
(`brew install imagemagick`). The white background is made transparent,
and the small sizes use the face only, which stays recognisable at 16
pixels. To make them again, from this directory:

    magick tanuki.png -alpha set -fuzz 12% -fill none \
      -draw "color 0,0 floodfill" -draw "color 762,0 floodfill" \
      -draw "color 0,845 floodfill" -draw "color 762,845 floodfill" \
      /tmp/tanuki-clear.png
    magick /tmp/tanuki-clear.png -crop 400x400+130+60 +repage \
      -define icon:auto-resize=48,32,16 public/favicon.ico
    magick /tmp/tanuki-clear.png -crop 560x560+50+20 +repage \
      -resize 192x192 public/icon-192.png
    magick /tmp/tanuki-clear.png -crop 560x560+50+20 +repage \
      -resize 160x160 -background '#F6DFBF' -gravity center \
      -extent 180x180 -alpha remove -alpha off public/apple-touch-icon.png

The crop offsets are in pixels of the 763 × 846 original. The Apple
icon needs an opaque background; `#F6DFBF` is the tanuki's belly.

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

All versions are exact in `deps.edn`, `package.json` and `package-lock.json`

Tools outside these files, as last checked:

- JDK 25.0.1
- Clojure CLI 1.12.6.1673
- Node.js 24.8.0, npm 11.6.0


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
