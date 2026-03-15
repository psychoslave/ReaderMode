# ReaderMode

An IntelliJ-platform plugin that displays source code in a **reader view**:
tokens are visually replaced by more natural words and forms — without touching
the underlying source file.

Toggled via **View → Reader Mode**.  
Tested on PhpStorm 2024.3; the plugin also registers for Java and Kotlin
(optional dependencies, loaded only when the respective language plugin is
present).

---

## Features

### Compound identifiers → middot form

Any identifier written in a common multi-word naming convention is replaced
by its lowercase, middle-dot–separated reading:

| Source token       | Reader view        |
|--------------------|--------------------|
| `camelCase`        | `camel·case`       |
| `PascalCase`       | `pascal·case`      |
| `snake_case`       | `snake·case`       |
| `SCREAMING_SNAKE`  | `screaming·snake`  |
| `kebab-case`       | `kebab·case`       |
| `HTMLParser`       | `html·parser`      |
| `parseHTML`        | `parse·html`       |

The middle dot (U+00B7 `·`) was chosen because it is a genuine word-separator
glyph (used in Catalan orthography, ancient Greek manuscripts, etc.), visually
lighter than a hyphen or underscore, and unambiguous in code contexts.

### Variable sigil → "see"

The `$` prefix on PHP variables is rendered as **see**:

| Source            | Reader view            |
|-------------------|------------------------|
| `$variable`       | `see variable`         |
| `$someInstance`   | `see some·instance`    |
| `$this`           | `see this`             |

**Rationale — deictic interjection.**  
In English, "See!" is used to point at or draw attention to something — the
exact gesture a sigil performs: it marks a name as a reference to a value.
`$variable` reads *"see variable"*: look here — variable.

**Rationale — locus noun.**  
A "see" (ecclesiastical) is the seat where autonomous authority is exercised.
By metaphor: a variable is the locus at which a value resides and from which
it acts — the place the name governs.

**Phonetic fit.**  
Like every other structural word in the vocabulary, "see" starts with **s** —
echoing the struck-S shape of `$` itself. It is a complete English word with
no apocope, three letters, and reads cleanly in context:

> `$this->methodName()` → *"**see** this **whose** method·name **do** **go**"*

### Bracket tokens → English words

Each bracket or brace character is replaced by a short English word:

| Token | Word  | Rhyme pair |
|-------|-------|------------|
| `(`   | `do`  | ↕ both end in **-o** |
| `)`   | `go`  | ↕ |
| `{`   | `tap` | ↕ both end in **-p** |
| `}`   | `hop` | ↕ |

### Subscript brackets → "at" / "ate"

Array subscript brackets are rendered as a matched word pair:

| Token | Word  | Pair |
|-------|-------|------|
| `[`   | `at`  | ↕ both built on **-at** |
| `]`   | `ate` | ↕ |

**Rationale — locative / past pair.**  
`[` opens a positional lookup: you are **at** a given index.  
`]` closes it with **ate** — simple past of *eat* — the lookup consumed
the index and yielded the value.  *Eating* a token is a genuine CS
metaphor: parsers eat input, lookups consume a key to produce a value.

**Minimal pair with `at`.**  
`ate` = `at` + `e` — the same word, vowel lengthened into the past.
The phonetic contrast is unambiguous: `at` /æt/ vs `ate` /eɪt/ — no
collision when reading code aloud.

> `$array[index]` → *"see array **at** index **ate**"*

> `$matrix[row][lat]` → *"see matrix **at** row **ate** **at** lat **ate**"*

### Expression terminator → "ay"

The `;` statement terminator is rendered as **ay**:

| Token | Word |
|-------|------|
| `;`   | `ay` |

**Rationale — affirmative interjection.**  
"Ay!" signals that something is confirmed, acknowledged, done — matching
the role of `;` as the mark that a statement is complete and execution
moves on. Each statement ends with a nod: *ay*.

**Rationale — shape.**  
The word ends in **y**, whose descending curved tail mirrors the descender
of `;` itself — the same visual grounding that led `$` to words starting
with **s** (its struck-S shape) and bracket pairs to rhyming endings.

> `$one = 1; $two = 2;` → *"see one = 1 **ay** see two = 2 **ay**"*

### Argument / list separator → "eft"

The `,` separator is rendered as **eft**:

| Token | Word  |
|-------|-------|
| `,`   | `eft` |

**Rationale.**  
*Eft* is an obsolete English adverb meaning **"again; afterwards"** — precisely
what a comma does: it announces that another item follows.  After each
argument, each list element, each clause: *eft*, here comes the next one.

The word was chosen over more obvious candidates (`and`, `then`, `next`,
`or`) because all of those already carry heavy meaning in programming
languages as operators or keywords, creating a risk of semantic collision
when reading code aloud.  *Eft*'s very rarity is a feature: it sits
outside the active vocabulary of any mainstream language, leaving it
unambiguously ours.

> `set(key, value)` → *"set **do** key **eft** value **go**"*

> `$array = [one, two, three]` → *"see array = **at** one **eft** two **eft** three **ate**"*

### Member-access operator → "whose"

The `->` operator (PHP object member access) is rendered as **whose**:

| Token | Word    |
|-------|---------|
| `->`  | `whose` |

**Rationale — semantic ownership.**  
`->` denotes that a property or method belongs to an object. *Whose* is the
English possessive interrogative — the exact word we use to ask about
ownership. `$instance->property` reads *"see instance whose property"*, which maps cleanly
onto the mental model: we are accessing something that the object owns.

**Rationale — prosodic flow.**  
The word slots naturally into spoken code:

> `$variable->methodName()` → *"**see** variable **whose** method·name **do** **go**"*

Possession and invocation read as a natural phrase rather than a symbol
interrupt.

#### Word-choice rationale for bracket tokens

The four bracket words were chosen to satisfy several constraints at once:

**Phonetic symmetry within each pair.**  
`do` / `go` share the `-o` ending; `tap` / `hop` share the `-p` ending.
Open and close brackets are thus audibly paired, mirroring the visual
pairing they form on the page.

**Short and percussive.**  
Each word is a single syllable. Deeply nested code like `fob(gob(hob()))` reads
`fob do gob do hob do go go go` — it scans and even has a rhythmic quality when
spoken aloud. Repetitions roll off the tongue: *tap tap tap … hop hop hop.*

**Interpretable as action words.**  
`do` and `go` are everyday imperatives. `tap` evokes an interjection or
onomatopoeia (a light knock on a surface); `hop` evokes a small jump or skip.
This gives each bracket a mild semantic flavour consistent with what brackets
actually *do* in code:
- `(…)` — you *do* something, then *go* on;
- `{…}` — you *tap* into a block, then *hop* out.

**Prosody when vocalised.**  
A function call like `__construct(SomeClass $instance)` reads aloud as:
> *"underscore underscore construct **do** some·class **see** instance **go**"*

A class body opening `SomeClass {` reads as:
> *"some·class **tap**"*

### Scope-resolution operator → "whence"

The `::` operator (namespace / class scope resolution) is rendered as **whence**:

| Token | Word     |
|-------|----------|
| `::`  | `whence` |

**Rationale — provenance adverb.**  
*Whence* is the English adverb meaning **"from where"** or **"from which
place"**. That is exactly what `::` expresses: each segment in a
`Namespace::SubNamespace::Class` chain names the place *from which* the next
name is drawn. Reading left to right, every `::` is a step back up the
breadcrumb — *whence* comes this name?

> `Psr\Log::Logger::create()` → *"psr·log **whence** logger **whence** create **do** **go**"*

**Contrast with `whose`.**  
*Whose* (used for `->`) asks about ownership of an instance member; *whence*
asks about static origin in the type hierarchy. The two operators serve
different navigation models, and the two words reflect that distinction.

### Prefix-negation operator → "non-"

The `!` prefix operator is rendered as **non-**:

| Token | Word    |
|-------|---------|
| `!`   | `non-`  |

**Rationale — living English prefix.**  
*Non-* is the standard English prefix for logical, objective negation — the
exact semantic role of `!` in code.  Unlike *not* (a reserved word in Python,
Ruby, PHP, and Perl), *non-* carries no collision risk in any mainstream
language.  It is a highly productive, fully standard prefix in both British and
American English (Wiktionary lists over 9 000 derived forms), used with nouns,
adjectives, and adverbs alike, with or without a hyphen depending on register.

**The hyphen is deliberate.**  
It makes the prefix relationship visually explicit and ensures that chained
negations — the common boolean-cast idiom `!!$value` — read as a clean chain
rather than two disconnected words:

> `!$valid` → *"non-see valid"*

> `!!$value` → *"non-non-see value"*

**Spacing rule.**  
`non-` suppresses its own trailing space (the hyphen is the connector) and
also suppresses the *leading* space of the immediately following token, so
chains never render as `non- non- value`.

### Spacing

Bracket, operator, and terminator folds are padded automatically so that no
token ever visually glues to its neighbour:

- a **leading space** is inserted when the preceding source character is not
  whitespace and is not itself a connecting-prefix token (such as `!`);
- a **trailing space** is inserted when the following source character is
  neither whitespace nor another structural token, and the replacement word
  does not itself end with `-`.

Result for `__construct(SomeClass $instance)`:

```
__construct do some·class see instance go
```

---

## How it works

The plugin uses IntelliJ's **code-folding** API.  A fold region replaces a
source range with a placeholder string; expanding the fold restores the
original text.  No file is ever written to.

One fold is created per token:

```
Source:   makeSomething   (            arguments          )
Folds:    [make·something] [ do ]  [arguments] [ go]
Reads:     make·something   do     arguments    go
```

The folding builder (`ReaderModeFoldingBuilder`) is a single-pass
`PsiRecursiveElementVisitor` over all leaf PSI elements.  It is registered as
a `lang.foldingBuilder` extension for each supported language via optional
`<depends>` entries in `plugin.xml`, so it degrades gracefully when a language
plugin is absent.

State (enabled / disabled) is persisted across IDE restarts by
`ReaderModeService`, an application-level `PersistentStateComponent`.

The **View → Reader Mode** action immediately collapses or expands all
reader-mode folds in every open editor, so the switch feels instant.

---

## Project structure

```
src/main/kotlin/com/example/readermode/
  BracketRenderer.kt        structural token → word map, padding helpers
  MiddotConverter.kt        identifier word-splitting (regex-based)
  ReaderModeFoldingBuilder.kt  PSI visitor, fold descriptor builder
  ReaderModeService.kt      persistent enabled/disabled toggle
  ToggleReaderModeAction.kt View-menu action

src/main/resources/META-INF/
  plugin.xml                core descriptor
  readermode-java.xml       optional: registers for language="JAVA"
  readermode-kotlin.xml     optional: registers for language="kotlin"
  readermode-php.xml        optional: registers for language="PHP"
```

---

## Building & running

Requirements: JDK 17, Gradle 9 (wrapper included), `mise` for JDK management.

```bash
# Run a sandboxed PhpStorm instance with the plugin installed
./gradlew runIde

# Run unit tests
./gradlew test
```
